package de.siphalor.nmuk.impl.mixin;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.objectweb.asm.tree.ClassNode;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;

import de.siphalor.nmuk.impl.compat.controlling.CompatibilityControlling;
import de.siphalor.nmuk.impl.version.MinecraftVersionHelper;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.loader.api.FabricLoader;

@Environment(EnvType.CLIENT)
public class NMUKMixinConfig implements IMixinConfigPlugin {

	private static final boolean IS_DEV_ENV = FabricLoader.getInstance().isDevelopmentEnvironment();
	private static boolean USE_PROD_HACK_MIXINS;

	private List<String> finalAdditionalMixinClasses = new ArrayList<>();

	public static final String MIXIN_VERSIONED_PACKAGE = "versioned";

	public static String prependMixinPackage(String className, String prefix) {
		if (prefix == null) {
			return className;
		}
		return prefix + "." + className;
	}

	public static List<String> prependMixinPackages(List<String> classNames, String prefix) {
		List<String> ret = new ArrayList<>(classNames.size());
		for (String className : classNames) {
			ret.add(prependMixinPackage(className, prefix));
		}
		return ret;
	}

	private List<String> additionalMixinClasses = new ArrayList<>();

	private void addMixins(String... mixinNames) {
		Collections.addAll(additionalMixinClasses, mixinNames);
	}

	private static final String ENV_SUFFIX_NORMAL = "_normal";
	private static final String ENV_SUFFIX_PROD_HACK = "_prodhack";

	private void addEnvMixins(boolean normal, boolean prodhack, String... mixinNames) {
		if (!normal && !prodhack) {
			return;
		}
		if (normal && !prodhack && USE_PROD_HACK_MIXINS) {
			return;
		}
		if (!normal && prodhack && !USE_PROD_HACK_MIXINS) {
			return;
		}

		for (String mixinName : mixinNames) {
			additionalMixinClasses.add(mixinName + (USE_PROD_HACK_MIXINS ? ENV_SUFFIX_PROD_HACK : ENV_SUFFIX_NORMAL));
		}
	}

	private void pushMixinsToFinal() {
		finalAdditionalMixinClasses.addAll(additionalMixinClasses);
		additionalMixinClasses.clear();
	}

	@Override
	public void onLoad(String mixinPackage) {
		// enable hack mixins for mc version < 1.16
		USE_PROD_HACK_MIXINS = !MinecraftVersionHelper.IS_AT_LEAST_V1_16;

		addEnvMixins(true, true, "MixinKeyBindingEntry");
		pushMixinsToFinal();

		// versioned mixins

		// TODO: add a json config file where for each mixinClassName a modID requirement can be made. Like in the fabric.mod.json#depends.
		// for now doing it in here

		// the order of the if statements is important. The highest version must be checked first
		if (MinecraftVersionHelper.IS_AT_LEAST_V1_18) {
			addMixins("MixinControlsListWidget_1_18", "MixinKeybindsScreen");
		} else {
			// Minecraft 1.17 and below
			addMixins("MixinControlsListWidget_1_14", "MixinControlsOptionsScreen");
		}

		if (MinecraftVersionHelper.IS_AT_LEAST_V1_17) {
			addMixins("MixinGameOptions_1_17");
		} else if (MinecraftVersionHelper.IS_AT_LEAST_V1_16) {
			addMixins("MixinGameOptions_1_16");
		} else {
			addMixins("MixinGameOptions_1_14");
		}

		if (MinecraftVersionHelper.IS_AT_LEAST_V1_16) {
			addMixins("MixinButtonWidget_1_16", "MixinKeyBindingEntry_1_16");
		} else {
			addMixins("MixinButtonWidget_1_14", "MixinClickableWidget_1_14", "MixinKeyBindingEntry_1_14", "MixinScreen_1_14");
		}

		if (!MinecraftVersionHelper.IS_AT_LEAST_V1_15) {
			// only mc versions below 1.15 (aka 1.14 versions) need this
			addMixins("MixinKeyBinding_1_14");
		}

		additionalMixinClasses = prependMixinPackages(additionalMixinClasses, MIXIN_VERSIONED_PACKAGE);
		pushMixinsToFinal();

		if (CompatibilityControlling.MOD_PRESENT_CONTROLLING) {
			addMixins("MixinSortOrder", "MixinCustomList", "MixinKeyEntry", "MixinNewKeyBindsList", "MixinNewKeyBindsScreen");

			additionalMixinClasses = prependMixinPackages(additionalMixinClasses, CompatibilityControlling.MOD_NAME_CONTROLLING);
			pushMixinsToFinal();
		}

	}

	@Override
	public String getRefMapperConfig() {
		return null;
	}

	@Override
	public boolean shouldApplyMixin(String targetClassName, String mixinClassName) {
		return true;
	}

	@Override
	public void acceptTargets(Set<String> myTargets, Set<String> otherTargets) {

	}

	@Override
	public List<String> getMixins() {
		return finalAdditionalMixinClasses == null ? null : (finalAdditionalMixinClasses.isEmpty() ? null : finalAdditionalMixinClasses);
	}

	@Override
	public void preApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {

	}

	@Override
	public void postApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {

	}

}
