package net.dakes.cornerlink.mixin;

import net.dakes.cornerlink.PortalCorners;
import net.dakes.cornerlink.PortalHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.NetherPortalBlock;
import net.minecraft.world.level.border.WorldBorder;
import net.minecraft.world.level.portal.TeleportTransition;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Redirects portal travel to the destination portal whose corners match the one entered.
 *
 * <p>Portals without linking blocks, and linked portals with no match in range, fall through to
 * vanilla's nearest-portal search.
 */
@Mixin(NetherPortalBlock.class)
public class NetherPortalBlockMixin {

	@Inject(method = "getExitPortal", at = @At("HEAD"), cancellable = true)
	private void cornerlink$useMatchingPortal(ServerLevel destLevel, Entity entity, BlockPos pos, BlockPos scaledPos,
	                                          boolean destIsNether, WorldBorder worldBorder,
	                                          CallbackInfoReturnable<TeleportTransition> cir) {
		PortalCorners corners = PortalHelper.cornersAt(entity.level(), pos);

		if (!corners.hasLinkingBlocks()) {
			return;
		}

		PortalHelper.findLinkedPortal(destLevel, scaledPos, destIsNether, worldBorder, corners).ifPresent(rectangle -> {
			TeleportTransition.PostTeleportTransition postTransition = TeleportTransition.PLAY_PORTAL_SOUND
					.then(teleported -> teleported.placePortalTicket(rectangle.minCorner));

			cir.setReturnValue(
					NetherPortalBlock.getDimensionTransitionFromExit(entity, pos, rectangle, destLevel, postTransition));
		});
	}
}
