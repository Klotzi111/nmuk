package de.siphalor.nmuk.impl.compat.controlling;

import net.fabricmc.loader.api.FabricLoader;

public class CompatibilityControlling {

	public static final String MOD_NAME = "controlling";
	public static final boolean MOD_PRESENT = FabricLoader.getInstance().isModLoaded(MOD_NAME);

}
