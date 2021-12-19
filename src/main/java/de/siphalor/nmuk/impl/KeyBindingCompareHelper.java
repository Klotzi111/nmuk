package de.siphalor.nmuk.impl;

import de.siphalor.nmuk.impl.duck.IKeyBinding;
import de.siphalor.nmuk.impl.mixin.KeyBindingAccessor;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.resource.language.I18n;

public class KeyBindingCompareHelper {

	public static int compareKeyBindingsCategoryOrder(KeyBinding keyBinding1, KeyBinding keyBinding2) {
		return KeyBindingAccessor.getCategoryOrderMap().get(keyBinding1.getCategory()).compareTo(KeyBindingAccessor.getCategoryOrderMap().get(keyBinding2.getCategory()));
	}

	public static int compareKeyBindingsBaseTranslationKey(KeyBinding keyBinding1, KeyBinding keyBinding2, boolean reverseNameCompare) {
		String keyName1 = I18n.translate(AlternativeKeyBinding.getBaseTranslationKey(keyBinding1.getTranslationKey()));
		String keyName2 = I18n.translate(AlternativeKeyBinding.getBaseTranslationKey(keyBinding2.getTranslationKey()));
		if (reverseNameCompare) {
			return keyName2.compareTo(keyName1);
		} else {
			return keyName1.compareTo(keyName2);
		}
	}

	public static int compareKeyBindingsNormal(KeyBinding keyBinding1, KeyBinding keyBinding2, boolean ignoreCategory, boolean reverseNameCompare) {
		if (ignoreCategory || keyBinding1.getCategory().equals(keyBinding2.getCategory())) {
			return compareKeyBindingsBaseTranslationKey(keyBinding1, keyBinding2, reverseNameCompare);
		} else {
			// categories are different and not ignored
			// so we compare them now with category order
			return compareKeyBindingsCategoryOrder(keyBinding1, keyBinding2);
		}
	}

	public static int compareKeyBindings(KeyBinding keyBinding1, KeyBinding keyBinding2, boolean ignoreCategory, boolean reverseNameCompare) {
		// keyBinding1 is 'me', 'I'
		IKeyBinding iKeyBinding1 = (IKeyBinding) keyBinding1;
		IKeyBinding iKeyBinding2 = (IKeyBinding) keyBinding2;

		KeyBinding parent = iKeyBinding1.nmuk$getParent();
		if (parent != null) {
			// if I have a parent
			if (keyBinding2 == parent) {
				// if he is my parent: I follow him
				return 1;
			} else {
				KeyBinding otherParent = iKeyBinding2.nmuk$getParent();
				if (otherParent == parent) {
					// if we have same parent: order by child id
					return Integer.compare(iKeyBinding1.nmuk$getAlternativeId(), iKeyBinding2.nmuk$getAlternativeId());
				}
				// if not: default compare
			}
		} else {
			// if I have no parent
			KeyBinding otherParent = iKeyBinding2.nmuk$getParent();
			if (otherParent != null) {
				// but he has a parent
				if (keyBinding1 == otherParent) {
					// if I am his parent: he follows me
					return -1;
				}
				// if not: default compare
			}
			// if he has no parent: We both have no parent: default compare
		}
		return compareKeyBindingsNormal(keyBinding1, keyBinding2, ignoreCategory, reverseNameCompare);
	}

}
