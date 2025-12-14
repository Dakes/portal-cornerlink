package net.fabricmc.dakes.cornerlink;

import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.server.world.ChunkTicketType;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.state.property.Properties;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.BlockLocating;
import net.minecraft.world.World;
import net.minecraft.world.border.WorldBorder;
import net.minecraft.world.poi.PointOfInterest;
import net.minecraft.world.poi.PointOfInterestStorage;
import net.minecraft.world.poi.PointOfInterestTypes;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

public class PortalHelper {


    /**
     * Returns a Rectangle describing the destination nether portal.
     * @param destWorld
     * @param destPos
     * @param destIsNether
     * @param worldBorder
     * @param corners
     * @return
     */
    public static Optional<BlockLocating.Rectangle> modifiedGetPortalRect(ServerWorld destWorld, BlockPos destPos, boolean destIsNether, WorldBorder worldBorder, PortalCorners corners)
    {
        Optional<PointOfInterest> pointOfInterest = findDestPortal(destWorld, destPos, destIsNether, worldBorder, corners);
        return pointOfInterest.map(poi -> {
            BlockPos blockPos = poi.getPos();
            destWorld.getChunkManager().addTicket(ChunkTicketType.PORTAL, new ChunkPos(blockPos), 3);
            BlockState blockState = destWorld.getBlockState(blockPos);
            return BlockLocating.getLargestRectangle(blockPos, blockState.get(Properties.HORIZONTAL_AXIS), 21, Direction.Axis.Y, 21, pos -> destWorld.getBlockState((BlockPos)pos) == blockState);
        });
    }


    /**
     * Find the best point of interest that correspond to a destination portal.
     * @param world
     * @param destPos
     * @param destIsNether
     * @param worldBorder
     * @param corners
     * @return
     */
    private static Optional<PointOfInterest> findDestPortal(ServerWorld world, BlockPos destPos, boolean destIsNether, WorldBorder worldBorder, PortalCorners corners)
    {
        PointOfInterestStorage pointOfInterestStorage = world.getPointOfInterestStorage();
        int i = destIsNether ? 16 : 128;
        pointOfInterestStorage.preloadChunks(world, destPos, i);

        var collection = pointOfInterestStorage.getInSquare(poiType -> poiType.matchesKey(PointOfInterestTypes.NETHER_PORTAL), destPos, i, PointOfInterestStorage.OccupationStatus.ANY)
                .filter(poi -> worldBorder.contains(poi.getPos()))
                .filter(poi -> world.getBlockState(poi.getPos()).contains(Properties.HORIZONTAL_AXIS))
                .toList();

        PointOfInterest poi = null;

        if(!collection.isEmpty())
        {
            poi = getSortedPortalPOIs(collection, world, corners, destPos).findFirst().orElse(null);
        }

        return Optional.ofNullable(poi);
    }


    private static Stream<PointOfInterest> getSortedPortalPOIs(List<PointOfInterest> pointOfInterests, ServerWorld world, PortalCorners originVector, BlockPos destPos)
    {
        // Deduplicate POIs: multiple POIs can belong to the same portal
        // Use portal rectangle's lowerLeft as unique key
        HashMap<BlockPos, PointOfInterest> uniquePortals = new HashMap<>();
        HashMap<BlockPos, PortalCorners> cornersCache = new HashMap<>();
        HashMap<PointOfInterest, BlockPos> poiToPortalKey = new HashMap<>();

        for(var poi : pointOfInterests)
        {
            BlockState blockState = world.getBlockState(poi.getPos());
            if (!blockState.contains(Properties.HORIZONTAL_AXIS)) {
                continue;
            }

            Direction.Axis axis = blockState.get(Properties.HORIZONTAL_AXIS);
            BlockLocating.Rectangle rectangle = BlockLocating.getLargestRectangle(
                poi.getPos(),
                axis,
                21,
                Direction.Axis.Y,
                21,
                pos -> world.getBlockState((BlockPos)pos) == blockState
            );

            BlockPos portalKey = rectangle.lowerLeft;
            poiToPortalKey.put(poi, portalKey);

            // Only process each unique portal once
            if (!uniquePortals.containsKey(portalKey)) {
                uniquePortals.put(portalKey, poi);
                // Cache corner calculation for this portal
                PortalCorners corners = getCornersVector(rectangle, axis, world);
                cornersCache.put(portalKey, corners);
            }
        }

        // Score only unique portals
        HashMap<BlockPos, Float> scoreMap = new HashMap<>();
        for (var entry : uniquePortals.entrySet()) {
            BlockPos portalKey = entry.getKey();
            PortalCorners corners = cornersCache.get(portalKey);
            float score = originVector.score(corners);
            float distance = 1f - score;
            scoreMap.put(portalKey, distance);
        }

        // Sort by score, then by distance, then by Y coordinate
        var comparator = Comparator.comparingDouble((PointOfInterest poi) -> scoreMap.get(poiToPortalKey.get(poi)))
                .thenComparingDouble((PointOfInterest poi) -> poi.getPos().getSquaredDistance(destPos))
                .thenComparingInt((PointOfInterest poi) -> poi.getPos().getY());

        return uniquePortals.values().stream().sorted(comparator);
    }

    public static PortalCorners getCornersVectorAt(World world, BlockPos position)
    {
        var blockState = world.getBlockState(position);

        // Safety check: ensure the block state has the HORIZONTAL_AXIS property
        if (!blockState.contains(Properties.HORIZONTAL_AXIS)) {
            return new PortalCorners(); // Return empty corners if not a valid portal block
        }

        var axis = blockState.get(Properties.HORIZONTAL_AXIS); // X or Z only
        var rectangle = BlockLocating.getLargestRectangle(position, axis, 21, Direction.Axis.Y, 21, pos -> world.getBlockState((BlockPos)pos) == blockState);

        return getCornersVector(rectangle, axis, world);
    }

    private static PortalCorners getCornersVector(BlockLocating.Rectangle rectangle, Direction.Axis axis, World world)
    {
        BlockPos a, b, c, d;
        if( axis == Direction.Axis.X)
        {
            a = rectangle.lowerLeft.add(-1, -1, 0);
            b = rectangle.lowerLeft.add(rectangle.width, -1, 0);
            c = rectangle.lowerLeft.add(-1, rectangle.height, 0);
            d = rectangle.lowerLeft.add(rectangle.width, rectangle.height, 0);
        }
        else
        {
            a = rectangle.lowerLeft.add(0, -1, -1);
            b = rectangle.lowerLeft.add(0, -1, rectangle.width);
            c = rectangle.lowerLeft.add(0, rectangle.height, -1);
            d = rectangle.lowerLeft.add(0, rectangle.height, rectangle.width);
        }

        var corners = new PortalCorners();
        corners.lower1 = blockPosToLinkingState(world, a);
        corners.lower2 = blockPosToLinkingState(world, b);
        corners.upper1 = blockPosToLinkingState(world, c);
        corners.upper2 = blockPosToLinkingState(world, d);

        return corners;
    }

    private static BlockState blockPosToLinkingState(World world, BlockPos position)
    {
        return filterLinkingBlock(world.getBlockState(position));
    }

    private static BlockState filterLinkingBlock(BlockState state)
    {
        return state.isIn(PortalLinking.LINKING_BLOCKS) ? state : null;
    }
}
