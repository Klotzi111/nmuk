package de.siphalor.nmuk.impl.compat.controlling;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import org.apache.logging.log4j.Level;

import de.klotzi111.fabricmultiversionhelper.api.mapping.MappingHelper;
import de.klotzi111.fabricmultiversionhelper.api.version.MinecraftVersionHelper;
import de.siphalor.nmuk.impl.NMUK;
import de.siphalor.nmuk.impl.duck.IControlsListWidget;
import de.siphalor.nmuk.impl.duck.IKeyBindingEntry;
import net.minecraft.client.gui.screen.option.ControlsListWidget;
import net.minecraft.client.gui.screen.option.ControlsListWidget.KeyBindingEntry;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.text.Text;

public class KeyBindingEntryVersionHelper {

	public static final Class<?> ACTUAL_KEYBINDING_ENTRY_CLASS;

	public static final String NewKeyBindsList_CLASS_NAME = "com.blamejared.controlling.client.NewKeyBindsList";
	public static final Class<?> NewKeyBindsList_class;
	public static final String KeyEntry_CLASS_NAME = NewKeyBindsList_CLASS_NAME + "$KeyEntry";
	public static final Class<?> KeyEntry_class;
	public static final String CategoryEntry_CLASS_NAME = NewKeyBindsList_CLASS_NAME + "$CategoryEntry";
	public static final Class<?> CategoryEntry_class;

	static {
		if (CompatibilityControlling.MOD_PRESENT) {
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

	private static final Constructor<KeyBindingEntry> KeyBindingEntry_contructor;

	// from controlling
	private static final Constructor<?> KeyEntry_contructor;

	static {
		if (CompatibilityControlling.MOD_PRESENT) {
			KeyEntry_contructor = MappingHelper.getConstructor(KeyEntry_class, NewKeyBindsList_class, KeyBinding.class);

			KeyBindingEntry_contructor = null;
		} else {
			String SIGNATURE_KeyBindingEntry_contructor = null;
			if (MinecraftVersionHelper.isMCVersionAtLeast("1.16")) {
				SIGNATURE_KeyBindingEntry_contructor = MappingHelper.createSignature("(%s%s%s)V", ControlsListWidget.class, KeyBinding.class, Text.class);
			} else {
				SIGNATURE_KeyBindingEntry_contructor = MappingHelper.createSignature("(%s%s)V", ControlsListWidget.class, KeyBinding.class);
			}
			KeyBindingEntry_contructor = (Constructor<KeyBindingEntry>) MappingHelper.getConstructor(KeyBindingEntry.class, SIGNATURE_KeyBindingEntry_contructor);

			KeyEntry_contructor = null;
		}
	}

	public static IKeyBindingEntry createKeyBindingEntry(IControlsListWidget listWidget, KeyBinding binding, Text bindingName) {
		try {
			Object[] instanceArgs = null;
			if (CompatibilityControlling.MOD_PRESENT) {
				// text not needed. See below
				instanceArgs = new Object[] {listWidget, binding};
				return (IKeyBindingEntry) KeyEntry_contructor.newInstance(instanceArgs);
			} else {
				if (MinecraftVersionHelper.isMCVersionAtLeast("1.16")) {
					instanceArgs = new Object[] {listWidget, binding, bindingName};
				} else {
					// if we are below minecraft 1.16 we can not parse the text to the constructors ...
					instanceArgs = new Object[] {listWidget, binding};
				}
				IKeyBindingEntry ret = (IKeyBindingEntry) KeyBindingEntry_contructor.newInstance(instanceArgs);
				// ... so we set it afterwards and hope for the best
				ret.nmuk$setBindingName(bindingName);
				return ret;
			}
		} catch (IllegalAccessException | InstantiationException | InvocationTargetException e) {
			NMUK.log(Level.ERROR, "Failed to create new instance of \"KeyBindingEntry\"");
			NMUK.logException(Level.ERROR, e);
		}
		return null;
	}

}
