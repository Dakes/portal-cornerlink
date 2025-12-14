package net.fabricmc.dakes.cornerlink;

import net.fabricmc.api.ModInitializer;
import net.minecraft.block.Block;
import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PortalLinking implements ModInitializer {

	public static final Logger LOGGER = LoggerFactory.getLogger("dakes_cornerlink");
	public static final TagKey<Block> LINKING_BLOCKS = TagKey.of(RegistryKeys.BLOCK, Identifier.of("dakes_cornerlink", "cornerlink"));

	@Override
	public void onInitialize() {
		// This code runs as soon as Minecraft is in a mod-load-ready state.
		// However, some things (like resources) may still be uninitialized.
		// Proceed with mild caution.

	}
}