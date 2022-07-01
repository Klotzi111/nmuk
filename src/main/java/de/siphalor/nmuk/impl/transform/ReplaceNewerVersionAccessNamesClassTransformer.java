package de.siphalor.nmuk.impl.transform;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.*;
import java.util.Map.Entry;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.tree.*;

import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import com.google.gson.JsonIOException;
import com.google.gson.JsonSyntaxException;

import de.klotzi111.fabricmultiversionhelper.api.mapping.MappingHelper;
import de.klotzi111.fabricmultiversionhelper.api.mapping.VersionMappingsHelper;
import de.klotzi111.fabricmultiversionhelper.api.version.MinecraftVersionHelper;
import de.klotzi111.transformerinterceptor.api.event.ClassTransformer;
import de.klotzi111.transformerinterceptor.api.event.ClassTransformerResult;
import de.klotzi111.transformerinterceptor.impl.util.ClassUtil;
import de.siphalor.nmuk.impl.NMUK;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.MappingResolver;
import net.fabricmc.loader.api.ModContainer;
import net.fabricmc.loader.api.Version;
import net.fabricmc.loader.impl.launch.FabricLauncherBase;
import net.fabricmc.mapping.tree.TinyTree;

// TODO: clean this mess up! By making all the static fields construction time fields
// TODO: export to a external library
// TODO: maybe use the tinyremapper?
public class ReplaceNewerVersionAccessNamesClassTransformer implements ClassTransformer {

	private static final String TRANSFORM_ALLOWED_PACKAGE = NMUK.class.getPackage().getName();
	private static final String TRANSFORM_DISALLOWED_PACKAGE = ReplaceNewerVersionAccessNamesClassTransformer.class.getPackage().getName();

	// key: new (compiled with) owner -> value: old (at runtime current version) owner
	private static HashMap<String, LinkedHashSet<String>> CLASS_NAME_SEARCH_MEMBER_NAME_CLASSES_MAP;

	private static final String TARGET_NAME_SPACE = FabricLauncherBase.getLauncher().getTargetNamespace();
	private static MappingResolver LOOKUP_MAPPING_RESOLVER;

	private static final String REMAP_CLASS_HIERARCHY_RESOURCE_PATH = "/remap/classHierarchy.json";

	private static final Gson gson = new Gson();

	private static final ModContainer MOD_CONTAINER = FabricLoader.getInstance().getModContainer("nmuk").get();

	@SuppressWarnings("serial")
	private static void loadClassNameSearchMemberNameClassesMap() {
		try (InputStream classHierarchyStream = ReplaceNewerVersionAccessNamesClassTransformer.class.getResourceAsStream(REMAP_CLASS_HIERARCHY_RESOURCE_PATH)) {
			if (classHierarchyStream == null) {
				throw new RuntimeException("Could not find resource: " + REMAP_CLASS_HIERARCHY_RESOURCE_PATH);
			}
			try (InputStreamReader classHierarchyReader = new InputStreamReader(classHierarchyStream)) {
				CLASS_NAME_SEARCH_MEMBER_NAME_CLASSES_MAP = gson.fromJson(classHierarchyReader, new TypeToken<HashMap<String, LinkedHashSet<String>>>() {}.getType());
			} catch (JsonIOException | JsonSyntaxException e) {
				throw new RuntimeException("Invalid json for resource: " + REMAP_CLASS_HIERARCHY_RESOURCE_PATH, e);
			}
		} catch (IOException e) {
			// ignore close failed
		}
	}

	private static void loadLookupMappingResolver() {
		Version compiledWithMinecraftSourceVersion = MinecraftVersionHelper.MINECRAFT_VERSION;
		try {
			compiledWithMinecraftSourceVersion = VersionMappingsHelper.getCompiledWithMinecraftSourceVersion(MOD_CONTAINER);
		} catch (Exception e) {
			// ignore
			// this will happen when launching with the mod in dev mode without exporting
		}
		TinyTree tinyTree = VersionMappingsHelper.getMappings(MOD_CONTAINER, compiledWithMinecraftSourceVersion);
		LOOKUP_MAPPING_RESOLVER = VersionMappingsHelper.createMappingResolver(tinyTree, MappingHelper.NAMESPACE_NAMED);
	}

	private static void remapClassNameMapKeys() {
		// to make it compatible with dev environment
		if (!TARGET_NAME_SPACE.equals(MappingHelper.NAMESPACE_INTERMEDIARY)) {
			// map entire map to TARGET_NAME_SPACE
			Map<String, LinkedHashSet<String>> addMap = new HashMap<>();
			Iterator<Entry<String, LinkedHashSet<String>>> iter = CLASS_NAME_SEARCH_MEMBER_NAME_CLASSES_MAP.entrySet().iterator();
			while (iter.hasNext()) {
				Entry<String, LinkedHashSet<String>> e = iter.next();

				String key = e.getKey();
				String mappedKey = MappingHelper.CLASS_MAPPER_FUNCTION.apply(key);

				LinkedHashSet<String> currentSet = e.getValue();
				// // map the values
				// Set<String> mappedSet = new ObjectLinkedOpenHashSet<>(currentSet.size());
				// for (String value : currentSet) {
				// String mappedValue = MappingHelper.CLASS_MAPPER_FUNCTION.apply(value);
				// mappedSet.add(mappedValue);
				// }

				if (!mappedKey.equals(key)) {
					iter.remove();
					addMap.put(mappedKey, currentSet);
				} else {
					// e.setValue(mappedSet);
				}
			}

			CLASS_NAME_SEARCH_MEMBER_NAME_CLASSES_MAP.putAll(addMap);
		}
	}

	static {
		// TODO: Future: get class super classes and interfaces from class byte[], without loading the class and without costly asm class node
		// + config file to add additionals
		loadClassNameSearchMemberNameClassesMap();

		remapClassNameMapKeys();

		// regex to remove comments in mappings.tiny file: ^\t{1,3}c.*$\n

		// load compiled MC source mappings
		loadLookupMappingResolver();
	}

	@Override
	public ClassTransformerResult transform(String className, ClassTransformerResult lastClassTransformerResult) {
		if (!lastClassTransformerResult.runTransformers || lastClassTransformerResult.classBytes == null) {
			return lastClassTransformerResult;
		}

		if (!className.startsWith(TRANSFORM_ALLOWED_PACKAGE)) {
			return lastClassTransformerResult;
		}
		if (className.startsWith(TRANSFORM_DISALLOWED_PACKAGE)) {
			return lastClassTransformerResult;
		}

		// System.out.println("++ transforming Class: " + className);

		ClassReader reader = new ClassReader(lastClassTransformerResult.classBytes);
		ClassNode node = new ClassNode();
		reader.accept(node, 0);

		boolean changesMade = transformClassNode(node);
		if (changesMade) {
			ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_FRAMES);
			node.accept(writer);

			lastClassTransformerResult.classBytes = writer.toByteArray();
		}

		// System.out.println("-- transforming end: " + changesMade);

		return lastClassTransformerResult;
	}

	private boolean transformClassNode(ClassNode classNode) {
		boolean changesMade = false;
		for (MethodNode methodNode : classNode.methods) {
			changesMade |= transformMethodNode(classNode.name, methodNode);
		}
		return changesMade;
	}

	@FunctionalInterface
	private static interface MapMemberFunction {
		String map(String namespace, String owner, String name, String descriptor);
	}

	private static enum MemberType {
		METHOD(LOOKUP_MAPPING_RESOLVER::mapMethodName),
		FIELD(LOOKUP_MAPPING_RESOLVER::mapFieldName);

		public final MapMemberFunction mapFunction;

		private MemberType(MapMemberFunction mapFunction) {
			this.mapFunction = mapFunction;
		}

	}

	private static String mapMemberName(MemberType memberType, String owner, String name, String desc) {
		String mappedOwner = ClassUtil.fromClassInternal(owner);

		// // debug
		// MemberInfo memberInfo = new MemberInfo(name, owner, desc);
		// System.out.println(memberInfo);

		Set<String> set = CLASS_NAME_SEARCH_MEMBER_NAME_CLASSES_MAP.get(mappedOwner);
		if (set == null) {
			return null;
		}
		for (String newOwner : set) {
			// TODO: map everything with current mc version mapping down to intermediary and then back up to named with other version
			// TODO: map desc down to intermediary
			// This looks very similar to what is done in MixinIntermediaryDevRemapper
			String mappedName = memberType.mapFunction.map(MappingHelper.NAMESPACE_INTERMEDIARY, newOwner, name, desc);

			if (!mappedName.equals(name)) {
				return mappedName;
			}
		}
		return null;
	}

	private boolean transformMethodNode(String className, MethodNode methodNode) {
		boolean changesMade = false;
		InsnList insns = methodNode.instructions;

		for (AbstractInsnNode insn : insns) {
			AbstractInsnNode newInsn = null;
			if (insn instanceof MethodInsnNode) {
				MethodInsnNode methodInsn = (MethodInsnNode) insn;

				String mappedName = mapMemberName(MemberType.METHOD, methodInsn.owner, methodInsn.name, methodInsn.desc);
				if (mappedName != null) {
					MethodInsnNode newMethodInsn = (MethodInsnNode) methodInsn.clone(null);
					newMethodInsn.name = mappedName;

					newInsn = newMethodInsn;
				}
			} else if (insn instanceof FieldInsnNode) {
				FieldInsnNode fieldInsn = (FieldInsnNode) insn;

				String mappedName = mapMemberName(MemberType.FIELD, fieldInsn.owner, fieldInsn.name, fieldInsn.desc);
				if (mappedName != null) {
					FieldInsnNode newFieldInsn = (FieldInsnNode) fieldInsn.clone(null);
					newFieldInsn.name = mappedName;

					newInsn = newFieldInsn;
				}
			}

			if (newInsn != null) {
				insns.set(insn, newInsn);
				changesMade = true;

				// // debug
				// MemberInfo insnInfo = new MemberInfo(insn);
				// MemberInfo newInsnInfo = new MemberInfo(newInsn);
				// System.out.println("Replaced: \"" + insnInfo + "\" with \"" + newInsnInfo + "\"");
				//
				// int lineNumber = ByteCodeHelper.getNextLineNumberToInsn(insns, newInsn, ByteCodeHelper.SearchDirection.UP_THEN_DOWN, ByteCodeHelper.DEFAULT_LINENUMBER_SEARCH_RANGE);
				// System.out.println(" -> at: " + className + ":" + lineNumber + "@" + methodNode.name);
			}
		}
		return changesMade;
	}

}
