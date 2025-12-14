package net.fabricmc.dakes.cornerlink;

import net.minecraft.block.BlockState;
import net.minecraft.registry.Registries;

public class PortalCorners {
    public BlockState lower1;
    public BlockState lower2;
    public BlockState upper1;
    public BlockState upper2;

    public PortalCorners() {}

    public PortalCorners(BlockState lower1, BlockState lower2, BlockState upper1, BlockState upper2) {
        this.lower1 = lower1;
        this.lower2 = lower2;
        this.upper1 = upper1;
        this.upper2 = upper2;
    }

    public float score(PortalCorners other)
    {
        return Math.max(scoreDirect(other), scoreDirect(other.mirrored()));
    }

    private float scoreDirect(PortalCorners other)
    {
        float score = 0;

        if( scoreCompare(lower1, other.lower1)) score += 1;
        if( scoreCompare(lower2, other.lower2)) score += 1;
        if( scoreCompare(upper1, other.upper1)) score += 1;
        if( scoreCompare(upper2, other.upper2)) score += 1;

        return score / 4f;
    }

    private boolean scoreCompare(BlockState a, BlockState b)
    {
        return a != null && b != null && a.getBlock() == b.getBlock();
    }

    public boolean hasLinkingBlocks()
    {
        return lower1 != null || lower2 != null || upper1 != null || upper2 != null;
    }

    public PortalCorners mirrored()
    {
        return new PortalCorners(lower2, lower1, upper2, upper1);
    }

    @Override
    public String toString()
    {
        var middle = String.join(", ", str(lower1), str(lower2), str(upper1), str(upper2));
        return "(" + middle + ")";
    }

    private String str(BlockState state)
    {
        if( state == null )
        {
            return "null";
        }
        else
        {
            return Registries.BLOCK.getId(state.getBlock()).toString();
        }
    }
}
