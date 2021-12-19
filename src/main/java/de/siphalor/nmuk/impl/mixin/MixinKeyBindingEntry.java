/*
 * Copyright 2021 Siphalor
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied.
 * See the License for the specific language governing
 * permissions and limitations under the License.
 */

package de.siphalor.nmuk.impl.mixin;

import java.util.List;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.google.common.collect.ImmutableList;

import de.siphalor.nmuk.impl.duck.IControlsListWidget;
import de.siphalor.nmuk.impl.duck.IKeyBindingEntry;
import de.siphalor.nmuk.impl.mixinimpl.MixinKeyBindingEntryImpl;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.screen.option.ControlsListWidget;
import net.minecraft.client.gui.screen.option.ControlsListWidget.KeyBindingEntry;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.option.KeyBinding;

@Mixin(KeyBindingEntry.class)
public abstract class MixinKeyBindingEntry implements IKeyBindingEntry {
	@Shadow
	@Final
	private KeyBinding binding;

	@Shadow
	@Final
	private ButtonWidget resetButton;

	@Shadow
	@Final
	private ButtonWidget editButton;

	// This is a synthetic field containing the outer class instance
	@Shadow(aliases = "field_2742", remap = false)
	@Final
	private ControlsListWidget listWidget;

	@Unique
	private ButtonWidget alternativesButton;

	@Override
	public void nmuk$setAlternativesButton(ButtonWidget alternativesButton) {
		this.alternativesButton = alternativesButton;
	}

	@Override
	public ButtonWidget nmuk$getAlternativesButton() {
		return alternativesButton;
	}

	@Override
	public ButtonWidget nmuk$getResetButton() {
		return resetButton;
	}

	@Override
	public KeyBinding nmuk$getBinding() {
		return binding;
	}

	@Override
	public ButtonWidget nmuk$getEditButton() {
		return editButton;
	}

	@Inject(method = "method_19870(Lnet/minecraft/client/option/KeyBinding;Lnet/minecraft/client/gui/widget/ButtonWidget;)V", at = @At("HEAD"))
	private void resetButtonPressed(KeyBinding keyBinding, ButtonWidget widget, CallbackInfo ci) {
		MixinKeyBindingEntryImpl.resetButtonPressed((IKeyBindingEntry) this, keyBinding, widget, (IControlsListWidget) listWidget);
	}

	@ModifyVariable(method = {"render", "render(IIIIIIIZF)V"}, at = @At("HEAD"), ordinal = 2, argsOnly = true)
	private int adjustXPosition(int original) {
		return Math.max(original - 30, 0);
	}

	@Redirect(method = {"render", "render(IIIIIIIZF)V"}, at = @At(value = "INVOKE", target = "Lnet/minecraft/client/option/KeyBinding;isDefault()Z"))
	private boolean isDefaultOnRender(KeyBinding keyBinding) {
		return MixinKeyBindingEntryImpl.isDefaultOnRender((IKeyBindingEntry) this, keyBinding);
	}

	@Inject(method = {"method_25396()Ljava/util/List;", "children()Ljava/util/List;"}, remap = false, at = @At("RETURN"), cancellable = true)
	public void children(CallbackInfoReturnable<List<? extends Element>> callbackInfoReturnable) {
		callbackInfoReturnable.setReturnValue(ImmutableList.of(editButton, resetButton, alternativesButton));
	}

	// ordinal 2 is required because in the byte code the second return statement is unfolded to a condition with two constant returns
	@Inject(method = {"method_25402(DDI)Z", "mouseClicked(DDI)Z"}, remap = false, at = @At(value = "RETURN", ordinal = 2), require = 1, cancellable = true)
	public void mouseClicked(double mouseX, double mouseY, int button, CallbackInfoReturnable<Boolean> cir) {
		if (alternativesButton.mouseClicked(mouseX, mouseY, button)) {
			cir.setReturnValue(true);
		}
	}

	@Inject(method = {"method_25406(DDI)Z", "mouseReleased(DDI)Z"}, remap = false, at = @At("RETURN"), cancellable = true)
	public void mouseReleased(double mouseX, double mouseY, int button, CallbackInfoReturnable<Boolean> cir) {
		// btnResetKeyBinding must be called because it is not called in the original code of controlling
		if (resetButton.mouseReleased(mouseX, mouseY, button) || alternativesButton.mouseReleased(mouseX, mouseY, button)) {
			cir.setReturnValue(true);
		}
	}

}
