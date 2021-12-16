package de.siphalor.nmuk.impl.version;

import net.minecraft.client.gui.screen.option.ControlsOptionsScreen;
import net.minecraft.client.gui.screen.option.KeybindsScreen;

// this file is the same as in amecs-api. But we need it here too because NMUK is standalone
public class KeybindsScreenVersionHelper {

	public static final Class<?> ACTUAL_KEYBINDS_SCREEN_CLASS;

	public static final Class<?> KeybindsScreen_class;

	static {
		if (MinecraftVersionHelper.IS_AT_LEAST_V1_18) {
			KeybindsScreen_class = KeybindsScreen.class;
			ACTUAL_KEYBINDS_SCREEN_CLASS = KeybindsScreen_class;
		} else {
			KeybindsScreen_class = null;
			ACTUAL_KEYBINDS_SCREEN_CLASS = ControlsOptionsScreen.class;
		}
	}

	public static boolean isClass(Class<?> actualClass, Class<?> classShouldBe) {
		if (classShouldBe == null || actualClass == null) {
			return false;
		}
		return classShouldBe.equals(actualClass);
	}

}
