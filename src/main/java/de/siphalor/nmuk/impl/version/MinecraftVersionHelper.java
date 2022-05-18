package de.siphalor.nmuk.impl.version;

import java.util.Optional;

import net.fabricmc.loader.api.*;

// this file is the same as in amecs-api. But we need it here too because NMUK is standalone
public class MinecraftVersionHelper {

	public static SemanticVersion V1_18;
	public static boolean IS_AT_LEAST_V1_18;
	public static SemanticVersion V1_17;
	public static boolean IS_AT_LEAST_V1_17;
	public static SemanticVersion V1_16;
	public static boolean IS_AT_LEAST_V1_16;
	public static SemanticVersion V1_15;
	public static boolean IS_AT_LEAST_V1_15;

	public static Version MINECRAFT_VERSION = null;
	public static SemanticVersion SEMANTIC_MINECRAFT_VERSION = null;

	static {
		getMinecraftVersion();

		V1_18 = parseSemanticVersion("1.18");
		IS_AT_LEAST_V1_18 = isMCVersionAtLeast(V1_18);
		V1_17 = parseSemanticVersion("1.17");
		IS_AT_LEAST_V1_17 = isMCVersionAtLeast(V1_17);
		V1_16 = parseSemanticVersion("1.16");
		IS_AT_LEAST_V1_16 = isMCVersionAtLeast(V1_16);
		V1_15 = parseSemanticVersion("1.15");
		IS_AT_LEAST_V1_15 = isMCVersionAtLeast(V1_15);
	}

	// we need to use the deprecated compareTo method because older minecraft versions do not support the new/non deprecated way
	@SuppressWarnings("deprecation")
	public static boolean isMCVersionAtLeast(SemanticVersion versionToBeAtLeast) {
		return SEMANTIC_MINECRAFT_VERSION.compareTo(versionToBeAtLeast) >= 0;
	}

	public static SemanticVersion parseSemanticVersion(String version) {
		try {
			return SemanticVersion.parse(version);
		} catch (VersionParsingException e) {
			// this should really never happen, because we carefully craft the version strings statically (at compile time)
			throw new IllegalStateException("Could not parse semantic version for minecraft version: " + version, e);
		}
	}

	private static void getMinecraftVersion() {
		Optional<ModContainer> minecraftModContainer = FabricLoader.getInstance().getModContainer("minecraft");
		if (!minecraftModContainer.isPresent()) {
			throw new IllegalStateException("Minecraft not available?!?");
		}
		MINECRAFT_VERSION = minecraftModContainer.get().getMetadata().getVersion();
		if (MINECRAFT_VERSION instanceof SemanticVersion) {
			SEMANTIC_MINECRAFT_VERSION = (SemanticVersion) MINECRAFT_VERSION;
		} else {
			throw new IllegalStateException("Minecraft version is no SemVer!");
		}
	}

}
