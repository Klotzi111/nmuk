package de.siphalor.nmuk.impl.compat.controlling;

import de.siphalor.nmuk.impl.mapping.MappingHelper;
import net.minecraft.client.gui.screen.option.ControlsListWidget.KeyBindingEntry;

public class KeyBindingEntryVersionHelper {

	public static final Class<?> ACTUAL_KEYBINDING_ENTRY_CLASS;

	public static final String NewKeyBindsList_CLASS_NAME = "com.blamejared.controlling.client.NewKeyBindsList";
	public static final Class<?> NewKeyBindsList_class;
	public static final String KeyEntry_CLASS_NAME = NewKeyBindsList_CLASS_NAME + "$KeyEntry";
	public static final Class<?> KeyEntry_class;
	public static final String CategoryEntry_CLASS_NAME = NewKeyBindsList_CLASS_NAME + "$CategoryEntry";
	public static final Class<?> CategoryEntry_class;

	static {
		if (CompatibilityControlling.MOD_PRESENT_CONTROLLING) {
			NewKeyBindsList_class = MappingHelper.loadClass(NewKeyBindsList_CLASS_NAME);
			KeyEntry_class = MappingHelper.loadClass(KeyEntry_CLASS_NAME);
			CategoryEntry_class = MappingHelper.loadClass(CategoryEntry_CLASS_NAME);
			ACTUAL_KEYBINDING_ENTRY_CLASS = KeyEntry_class;
		} else {
			NewKeyBindsList_class = null;
			KeyEntry_class = null;
			CategoryEntry_class = null;
			ACTUAL_KEYBINDING_ENTRY_CLASS = KeyBindingEntry.class;
		}
	}

}
