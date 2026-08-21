package net.dakes.cornerlink;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Shared constants for every loader. Contains no loader-specific code — the Fabric and NeoForge
 * entrypoints live in their own source sets and exist only to satisfy each loader's contract.
 */
public final class PortalLinking {

	public static final String MOD_ID = "dakes_cornerlink";

	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	/**
	 * Blocks that may be placed at a portal corner to influence linking.
	 * Populated by the block tag of the same name in this mod's data pack.
	 */
	public static final TagKey<Block> LINKING_BLOCKS =
			TagKey.create(Registries.BLOCK, Identifier.fromNamespaceAndPath(MOD_ID, "cornerlink"));

	private PortalLinking() {
	}
}
