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

@Environment(EnvType.CLIENT)
public class NMUKMixinConfig implements IMixinConfigPlugin {

	public static final String MIXIN_VERSIONED_PACKAGE = "versioned";

	public static String prependMixinPackage(String className) {
		return MIXIN_VERSIONED_PACKAGE + "." + className;
	}

	public static List<String> prependMixinPackages(List<String> classNames) {
		List<String> ret = new ArrayList<>(classNames.size());
		for (String className : classNames) {
			ret.add(prependMixinPackage(className));
		}
		return ret;
	}

	private List<String> additionalMixinClasses = new ArrayList<>();

	private void addMixins(String... mixinNames) {
		Collections.addAll(additionalMixinClasses, mixinNames);
	}

	@Override
	public void onLoad(String mixinPackage) {
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

		additionalMixinClasses = prependMixinPackages(additionalMixinClasses);

		if (CompatibilityControlling.MOD_PRESENT_CONTROLLING) {
			String prefix = CompatibilityControlling.MOD_NAME_CONTROLLING;
			addMixins(prefix + ".MixinSortOrder", prefix + ".MixinCustomList", prefix + ".MixinKeyEntry", prefix + ".MixinNewKeyBindsList", prefix + ".MixinNewKeyBindsScreen");
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
		return additionalMixinClasses == null ? null : (additionalMixinClasses.isEmpty() ? null : additionalMixinClasses);
	}

	@Override
	public void preApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {

	}

	@Override
	public void postApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {

	}

}
