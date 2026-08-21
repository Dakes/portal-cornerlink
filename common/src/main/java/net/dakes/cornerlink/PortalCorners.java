package net.dakes.cornerlink;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.block.state.BlockState;

import org.jetbrains.annotations.Nullable;

/**
 * The four blocks diagonally adjacent to a portal frame's corners.
 *
 * <p>Each component is either a block from {@link PortalLinking#LINKING_BLOCKS} or {@code null}
 * when that corner holds anything else. "Lower"/"upper" refer to the Y extremes; "1"/"2" to the
 * two ends of the portal's horizontal axis, in increasing coordinate order.
 *
 * @param lower1 corner below the low end of the horizontal axis
 * @param lower2 corner below the high end of the horizontal axis
 * @param upper1 corner above the low end of the horizontal axis
 * @param upper2 corner above the high end of the horizontal axis
 */
public record PortalCorners(@Nullable BlockState lower1, @Nullable BlockState lower2,
                            @Nullable BlockState upper1, @Nullable BlockState upper2) {

	/** A portal with no linking blocks at any corner, i.e. one that should link the vanilla way. */
	public static final PortalCorners EMPTY = new PortalCorners(null, null, null, null);

	/** Whether at least one corner carries a linking block. */
	public boolean hasLinkingBlocks() {
		return lower1 != null || lower2 != null || upper1 != null || upper2 != null;
	}

	/** This pattern seen from the opposite side, so that a mirrored portal still matches. */
	public PortalCorners mirrored() {
		return new PortalCorners(lower2, lower1, upper2, upper1);
	}

	/**
	 * How well {@code other} matches this pattern, from {@code 0} (nothing in common) to
	 * {@code 1} (all four corners agree). Orientation does not matter: the better of the direct
	 * and mirrored comparisons wins.
	 */
	public float score(PortalCorners other) {
		return Math.max(scoreDirect(other), scoreDirect(other.mirrored()));
	}

	private float scoreDirect(PortalCorners other) {
		int matches = 0;

		if (matches(lower1, other.lower1)) matches++;
		if (matches(lower2, other.lower2)) matches++;
		if (matches(upper1, other.upper1)) matches++;
		if (matches(upper2, other.upper2)) matches++;

		return matches / 4f;
	}

	/**
	 * Corners match on block type only, so that the two halves of a link may use different
	 * facings of the same glazed terracotta. An absent corner never matches anything, including
	 * another absent corner.
	 */
	private static boolean matches(@Nullable BlockState a, @Nullable BlockState b) {
		return a != null && b != null && a.getBlock() == b.getBlock();
	}

	@Override
	public String toString() {
		return "(" + name(lower1) + ", " + name(lower2) + ", " + name(upper1) + ", " + name(upper2) + ")";
	}

	private static String name(@Nullable BlockState state) {
		return state == null ? "null" : BuiltInRegistries.BLOCK.getKey(state.getBlock()).toString();
	}
}
