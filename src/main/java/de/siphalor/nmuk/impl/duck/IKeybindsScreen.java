package de.siphalor.nmuk.impl.duck;

import net.minecraft.client.gui.screen.option.ControlsListWidget;
import net.minecraft.client.option.KeyBinding;

public interface IKeybindsScreen {
	ControlsListWidget nmuk_getControlsList();

	KeyBinding nmuk_getSelectedKeyBinding();
}
