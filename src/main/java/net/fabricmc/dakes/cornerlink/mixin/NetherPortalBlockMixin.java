package net.fabricmc.dakes.cornerlink.mixin;

import net.fabricmc.dakes.cornerlink.PortalHelper;
import net.fabricmc.dakes.cornerlink.PortalLinking;
import net.minecraft.block.Block;
import net.minecraft.block.NetherPortalBlock;
import net.minecraft.block.Portal;
import net.minecraft.entity.Entity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.TeleportTarget;
import net.minecraft.world.border.WorldBorder;
import org.spongepowered.asm.mixin.Debug;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Debug(export = true)
@Mixin(NetherPortalBlock.class)
public abstract class NetherPortalBlockMixin extends Block implements Portal {

    public NetherPortalBlockMixin(Settings settings) {
        super(settings);
    }

    @Inject(method = "getOrCreateExitPortalTarget", at = @At("HEAD"), cancellable = true)
    private void inject(ServerWorld world, Entity entity, BlockPos pos, BlockPos scaledPos, boolean inNether, WorldBorder worldBorder, CallbackInfoReturnable<TeleportTarget> cir)
    {
        var corners = PortalHelper.getCornersVectorAt(entity.getEntityWorld(), pos);

        if( corners.hasLinkingBlocks())
        {
            var portalRect = PortalHelper.modifiedGetPortalRect(world, scaledPos, inNether, worldBorder, corners);

            if(portalRect.isPresent())
            {
                var that = (NetherPortalBlock)(Object)this;
                TeleportTarget.PostDimensionTransition postDimensionTransition = TeleportTarget.SEND_TRAVEL_THROUGH_PORTAL_PACKET.then((entityx) -> {
                    entityx.addPortalChunkTicketAt(portalRect.get().lowerLeft);
                });

                var teleportTarget = that.getExitPortalTarget(entity, scaledPos, portalRect.get(), world, postDimensionTransition);
                cir.setReturnValue(teleportTarget);
            }
        }
    }
}
