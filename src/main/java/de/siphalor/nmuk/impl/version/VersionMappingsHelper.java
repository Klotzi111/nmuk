package de.siphalor.nmuk.impl.version;

import java.io.*;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.function.Supplier;

import org.apache.commons.io.IOUtils;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.fabricmc.loader.api.MappingResolver;
import net.fabricmc.loader.api.Version;
import net.fabricmc.loader.api.VersionParsingException;
import net.fabricmc.loader.impl.launch.FabricLauncherBase;
import net.fabricmc.loader.impl.util.version.VersionParser;
import net.fabricmc.mapping.tree.TinyMappingFactory;
import net.fabricmc.mapping.tree.TinyTree;

public class VersionMappingsHelper {

	public static final Constructor<?> MappingResolverImpl_init;

	static {
		Constructor<?> local_MappingResolverImpl_init = null;
		try {
			Class<?> MappingResolverImpl_class = Class.forName("net.fabricmc.loader.impl.MappingResolverImpl");
			local_MappingResolverImpl_init = MappingResolverImpl_class.getDeclaredConstructor(Supplier.class, String.class);
			local_MappingResolverImpl_init.setAccessible(true);
		} catch (NoSuchMethodException | SecurityException | ClassNotFoundException e) {
			throw new RuntimeException("Failed to load constructor from class \"MappingResolverImpl\" with reflection", e);
		}
		MappingResolverImpl_init = local_MappingResolverImpl_init;
	}

	public static MappingResolver getMappingResolver(TinyTree tinyTree, String namespace) {
		try {
			Supplier<TinyTree> supplier = () -> tinyTree;
			return (MappingResolver) MappingResolverImpl_init.newInstance(supplier, namespace);
		} catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
			throw new RuntimeException("Failed to instantiate class \"MappingResolverImpl\" with reflection", e);
		}
	}

	private static final Map<String, TinyTree> MAPPINGS_CACHE = new Object2ObjectOpenHashMap<>();

	private static final String MAPPINGS_RESOURCE_PATH_FORMAT = "/mappings/mapping-%s.tiny";

	public static TinyTree getMappings(Version minecraftVersion) {
		return getMappings(minecraftVersion.toString());
	}

	public static TinyTree getMappings(String minecraftVersion) {
		String resourcePath = String.format(MAPPINGS_RESOURCE_PATH_FORMAT, minecraftVersion);
		return getMappingsFromPath(resourcePath);
	}

	public static TinyTree getMappingsFromPath(String resourcePath) {
		TinyTree mappings = MAPPINGS_CACHE.get(resourcePath);
		if (mappings == null) {
			mappings = loadMappings(resourcePath);
			MAPPINGS_CACHE.put(resourcePath, mappings);
		}
		return mappings;
	}

	private static TinyTree loadMappings(String resourcePath) {
		InputStream mappingStream = VersionMappingsHelper.class.getResourceAsStream(resourcePath);

		TinyTree mappings = null;
		if (mappingStream != null) {
			try (BufferedReader reader = new BufferedReader(new InputStreamReader(mappingStream))) {
				// long time = System.currentTimeMillis();
				mappings = TinyMappingFactory.loadWithDetection(reader);
				// Log.debug(LogCategory.MAPPINGS, "Loading extra mappings took %d ms", System.currentTimeMillis() - time);
				mappingStream.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		// if (mappings == null) {
		// Log.info(LogCategory.MAPPINGS, "Could not load extra mappings: " + resourcePath);
		// }
		return mappings;
	}

	private static final String compiledWithMinecraftSourceVersion = "compiledWithMinecraftSourceVersion";
	private static final String compiledWithMinecraftSourceVersion_RESOURCE_PATH = String.format("/mappings/%s.txt", compiledWithMinecraftSourceVersion);

	public static Version getCompiledWithMinecraftSourceVersion() {
		Version version = null;
		try (InputStream cwmsvStream = VersionMappingsHelper.class.getResourceAsStream(compiledWithMinecraftSourceVersion_RESOURCE_PATH)) {
			if (cwmsvStream == null) {
				throw new RuntimeException("Could not find resource: " + compiledWithMinecraftSourceVersion_RESOURCE_PATH);
			}

			String cwmsvString = null;
			try {
				cwmsvString = new String(IOUtils.readFully(cwmsvStream, cwmsvStream.available()), StandardCharsets.UTF_8);
			} catch (IOException e) {
				throw new RuntimeException("Failed to read resource: " + compiledWithMinecraftSourceVersion_RESOURCE_PATH, e);
			}

			try {
				version = VersionParser.parse(cwmsvString, false);
			} catch (VersionParsingException e) {
				throw new RuntimeException("Could not parse version for \"" + compiledWithMinecraftSourceVersion + "\": " + cwmsvString, e);
			}
		} catch (IOException e1) {
			// ignore close failed
		}
		return version;
	}

	// for debugging
	public static void dumpCurrentMappings(Path dumpPath) {
		Exception exception = null;
		InputStream is = FabricLauncherBase.class.getClassLoader().getResourceAsStream("mappings/mappings.tiny");
		if (is != null) {
			try {
				OutputStream os = Files.newOutputStream(dumpPath);
				IOUtils.copy(is, os);

				os.close();
				return;
			} catch (IOException e) {
				exception = e;
			} finally {
				try {
					is.close();
				} catch (IOException e) {
					// ignore close failed
				}
			}
		}

		String message = is == null ? "No mapping found!" : exception.toString();
		try {
			Files.write(dumpPath, message.getBytes(StandardCharsets.UTF_8));
		} catch (IOException e) {
		}
	}

}
