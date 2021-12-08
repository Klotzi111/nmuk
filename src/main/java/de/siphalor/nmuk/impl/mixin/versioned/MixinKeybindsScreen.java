package de.siphalor.nmuk.impl.mixin.versioned;

import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import de.siphalor.nmuk.impl.duck.IKeybindsScreen;
import net.minecraft.client.gui.screen.option.ControlsListWidget;
import net.minecraft.client.gui.screen.option.KeybindsScreen;
import net.minecraft.client.option.KeyBinding;

@Mixin(KeybindsScreen.class)
public abstract class MixinKeybindsScreen implements IKeybindsScreen {

	@Shadow
	@Nullable
	public KeyBinding selectedKeyBinding;

	@Shadow
	private ControlsListWidget controlsList;

	@Override
	public ControlsListWidget nmuk_getControlsList() {
		return controlsList;
	}

	@Override
	public KeyBinding nmuk_getSelectedKeyBinding() {
		return selectedKeyBinding;
	}

}
