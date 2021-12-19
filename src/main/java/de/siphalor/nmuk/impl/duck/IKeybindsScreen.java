package de.siphalor.nmuk.impl.duck;

import net.minecraft.client.option.KeyBinding;

public interface IKeybindsScreen {
	IControlsListWidget nmuk$getControlsList();

	KeyBinding nmuk$getSelectedKeyBinding();
}
