package de.siphalor.nmuk.impl.mixin.versioned;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import de.siphalor.nmuk.impl.duck.IControlsListWidget;
import de.siphalor.nmuk.impl.duck.IKeybindsScreen;
import net.minecraft.client.gui.screen.option.ControlsListWidget;
import net.minecraft.client.gui.screen.option.ControlsOptionsScreen;
import net.minecraft.client.option.KeyBinding;

@Mixin(ControlsOptionsScreen.class)
public abstract class MixinControlsOptionsScreen implements IKeybindsScreen {

	// we can NOT use the name of the field because we are not allowed to add aliases when the field is public
	@Shadow
	public KeyBinding field_2727; // focusedBinding

	@Shadow(aliases = "field_2728", remap = false)
	private ControlsListWidget keyBindingListWidget;

	// @Shadow(aliases = {"field_2727", "focusedBinding"}, remap = false)
	// public KeyBinding focusedBinding;
	//
	// @Shadow(aliases = {"field_2728", "keyBindingListWidget"}, remap = false)
	// private ControlsListWidget keyBindingListWidget;

	@Override
	public KeyBinding nmuk$getSelectedKeyBinding() {
		return field_2727;
	}

	@Override
	public IControlsListWidget nmuk$getControlsList() {
		return (IControlsListWidget) keyBindingListWidget;
	}

}
