package net.dakes.cornerlink;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.TicketType;
import net.minecraft.util.BlockUtil;
import net.minecraft.world.entity.ai.village.poi.PoiManager;
import net.minecraft.world.entity.ai.village.poi.PoiRecord;
import net.minecraft.world.entity.ai.village.poi.PoiTypes;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.border.WorldBorder;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * Locates the destination portal whose corner pattern best matches the portal an entity entered.
 */
public final class PortalHelper {

	/** Largest portal vanilla will consider, along either axis. */
	private static final int MAX_PORTAL_SIZE = 21;

	/** Chunk ticket radius used to keep the destination portal loaded, matching vanilla. */
	private static final int PORTAL_TICKET_RADIUS = 3;

	private PortalHelper() {
	}

	/**
	 * A destination portal under consideration, with everything needed to rank it already computed.
	 *
	 * @param poiPos    the point-of-interest position the rectangle was resolved from
	 * @param rectangle the portal's frame interior
	 * @param score     how well this portal's corners match the origin's, see {@link PortalCorners#score}
	 */
	private record Candidate(BlockPos poiPos, BlockUtil.FoundRectangle rectangle, float score) {
	}

	/**
	 * Finds the portal near {@code destPos} that best matches {@code originCorners} and reserves
	 * its chunks. Ranking is by corner match first, then proximity to {@code destPos}, then height.
	 *
	 * @return the matching portal's frame interior, or empty if there is no portal in range
	 */
	public static Optional<BlockUtil.FoundRectangle> findLinkedPortal(ServerLevel destLevel, BlockPos destPos,
	                                                                  boolean destIsNether, WorldBorder worldBorder,
	                                                                  PortalCorners originCorners) {
		int radius = destIsNether ? 16 : 128;

		PoiManager poiManager = destLevel.getPoiManager();
		poiManager.ensureLoadedAndValid(destLevel, destPos, radius);

		Optional<Candidate> best = collectCandidates(destLevel, poiManager, destPos, radius, worldBorder, originCorners)
				.stream()
				.min(Comparator.comparingDouble((Candidate c) -> -c.score())
						.thenComparingDouble(c -> c.poiPos().distSqr(destPos))
						.thenComparingInt(c -> c.poiPos().getY()));

		best.ifPresent(candidate -> destLevel.getChunkSource()
				.addTicketWithRadius(TicketType.PORTAL, ChunkPos.containing(candidate.poiPos()), PORTAL_TICKET_RADIUS));

		return best.map(Candidate::rectangle);
	}

	/**
	 * Resolves every distinct portal within {@code radius} of {@code destPos} and scores it.
	 *
	 * <p>A single portal contributes one point of interest per portal block, so the positions
	 * covered by each resolved frame are claimed as they are found and later points of interest
	 * falling inside them are skipped. That keeps the cost of the expensive rectangle search
	 * proportional to the number of portals rather than the number of portal blocks.
	 */
	private static List<Candidate> collectCandidates(ServerLevel level, PoiManager poiManager, BlockPos destPos,
	                                                 int radius, WorldBorder worldBorder, PortalCorners originCorners) {
		List<Candidate> candidates = new ArrayList<>();
		Set<BlockPos> claimed = new HashSet<>();

		List<PoiRecord> records = poiManager
				.getInSquare(poiType -> poiType.is(PoiTypes.NETHER_PORTAL), destPos, radius, PoiManager.Occupancy.ANY)
				.toList();

		for (PoiRecord record : records) {
			BlockPos pos = record.getPos();

			if (claimed.contains(pos) || !worldBorder.isWithinBounds(pos)) {
				continue;
			}

			BlockState state = level.getBlockState(pos);
			if (!state.hasProperty(BlockStateProperties.HORIZONTAL_AXIS)) {
				continue;
			}

			Direction.Axis axis = state.getValue(BlockStateProperties.HORIZONTAL_AXIS);
			BlockUtil.FoundRectangle rectangle = rectangleAround(level, pos, axis, state);

			claimPositions(claimed, rectangle, axis);
			candidates.add(new Candidate(pos, rectangle, originCorners.score(cornersOf(level, rectangle, axis))));
		}

		return candidates;
	}

	/** The corner pattern of the portal containing {@code position}, or {@link PortalCorners#EMPTY}. */
	public static PortalCorners cornersAt(Level level, BlockPos position) {
		BlockState state = level.getBlockState(position);

		if (!state.hasProperty(BlockStateProperties.HORIZONTAL_AXIS)) {
			return PortalCorners.EMPTY;
		}

		Direction.Axis axis = state.getValue(BlockStateProperties.HORIZONTAL_AXIS);
		return cornersOf(level, rectangleAround(level, position, axis, state), axis);
	}

	private static BlockUtil.FoundRectangle rectangleAround(BlockGetter level, BlockPos position,
	                                                        Direction.Axis axis, BlockState state) {
		return BlockUtil.getLargestRectangleAround(position, axis, MAX_PORTAL_SIZE, Direction.Axis.Y, MAX_PORTAL_SIZE,
				pos -> level.getBlockState(pos) == state);
	}

	/** Records every block of {@code rectangle} so its other points of interest can be skipped. */
	private static void claimPositions(Set<BlockPos> claimed, BlockUtil.FoundRectangle rectangle, Direction.Axis axis) {
		for (int along = 0; along < rectangle.axis1Size; along++) {
			for (int up = 0; up < rectangle.axis2Size; up++) {
				claimed.add(offset(rectangle.minCorner, axis, along, up));
			}
		}
	}

	/**
	 * Reads the four blocks diagonally outside the corners of {@code rectangle}. The rectangle
	 * covers the portal's interior, so the corners sit one block beyond it on both axes.
	 */
	private static PortalCorners cornersOf(BlockGetter level, BlockUtil.FoundRectangle rectangle, Direction.Axis axis) {
		BlockPos min = rectangle.minCorner;
		int low = -1;
		int high1 = rectangle.axis1Size;
		int high2 = rectangle.axis2Size;

		return new PortalCorners(
				linkingBlockAt(level, offset(min, axis, low, low)),
				linkingBlockAt(level, offset(min, axis, high1, low)),
				linkingBlockAt(level, offset(min, axis, low, high2)),
				linkingBlockAt(level, offset(min, axis, high1, high2)));
	}

	/** Offsets {@code origin} by {@code along} on the portal's horizontal axis and {@code up} on Y. */
	private static BlockPos offset(BlockPos origin, Direction.Axis axis, int along, int up) {
		return axis == Direction.Axis.X ? origin.offset(along, up, 0) : origin.offset(0, up, along);
	}

	/** The state at {@code position} if it can influence linking, otherwise {@code null}. */
	private static @Nullable BlockState linkingBlockAt(BlockGetter level, BlockPos position) {
		BlockState state = level.getBlockState(position);
		return state.is(PortalLinking.LINKING_BLOCKS) ? state : null;
	}
}
