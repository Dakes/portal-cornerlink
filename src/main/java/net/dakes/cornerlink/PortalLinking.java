package net.dakes.cornerlink;

import net.fabricmc.api.ModInitializer;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PortalLinking implements ModInitializer {

	public static final String MOD_ID = "dakes_cornerlink";

	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	/**
	 * Blocks that may be placed at a portal corner to influence linking.
	 * Populated by the block tag of the same name in this mod's data pack.
	 */
	public static final TagKey<Block> LINKING_BLOCKS =
			TagKey.create(Registries.BLOCK, Identifier.fromNamespaceAndPath(MOD_ID, "cornerlink"));

	@Override
	public void onInitialize() {
		// Everything this mod does happens through NetherPortalBlockMixin; nothing to register.
	}
}
