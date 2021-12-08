package de.siphalor.nmuk.impl.version;

import java.util.Optional;

import org.apache.logging.log4j.Level;

import de.siphalor.nmuk.impl.NMUK;
import net.fabricmc.loader.api.*;

// this file is the same as in amecs-api. But we need it here too because NMUK is standalone
public class MinecraftVersionHelper {

	public static SemanticVersion V1_18;
	public static SemanticVersion V1_17;

	public static Version MINECRAFT_VERSION = null;
	public static SemanticVersion SEMANTIC_MINECRAFT_VERSION = null;

	static {
		getMinecraftVersion();

		V1_18 = parseSemanticVersion("1.18");
		V1_17 = parseSemanticVersion("1.17");
	}

	private static SemanticVersion parseSemanticVersion(String version) {
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
			// this line will cause errors. Because it will trigger the class load of NMUK but that loads other classes because of its static fields.
			// And these classes load too early because of the mixins
			NMUK.log(Level.WARN, "Minecraft version is no SemVer. This will cause problems!");
		}
	}

}
