package de.siphalor.nmuk.impl.mixin.versioned;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import de.siphalor.nmuk.impl.duck.IKeybindsScreen;
import net.minecraft.client.gui.screen.option.ControlsListWidget;
import net.minecraft.client.gui.screen.option.ControlsOptionsScreen;
import net.minecraft.client.option.KeyBinding;

@Mixin(ControlsOptionsScreen.class)
public abstract class MixinControlsOptionsScreen implements IKeybindsScreen {

	@Shadow
	public KeyBinding focusedBinding;

	@Shadow
	private ControlsListWidget keyBindingListWidget;

	@Override
	public KeyBinding nmuk_getSelectedKeyBinding() {
		return focusedBinding;
	}

	@Override
	public ControlsListWidget nmuk_getControlsList() {
		return keyBindingListWidget;
	}

}
