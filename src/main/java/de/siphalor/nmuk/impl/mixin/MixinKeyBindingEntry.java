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

import java.util.Collection;
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

import de.siphalor.nmuk.impl.NMUKKeyBindingHelper;
import de.siphalor.nmuk.impl.duck.IKeyBinding;
import de.siphalor.nmuk.impl.duck.IKeyBindingEntry;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.option.ControlsListWidget;
import net.minecraft.client.gui.screen.option.ControlsListWidget.KeyBindingEntry;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;

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
	public void setAlternativesButton(ButtonWidget alternativesButton) {
		this.alternativesButton = alternativesButton;
	}

	@Override
	public ButtonWidget getAlternativesButton() {
		return alternativesButton;
	}

	@Override
	public ButtonWidget getResetButton() {
		return resetButton;
	}

	@Inject(method = "method_19870(Lnet/minecraft/client/option/KeyBinding;Lnet/minecraft/client/gui/widget/ButtonWidget;)V", at = @At("HEAD"))
	private void resetButtonPressed(KeyBinding keyBinding, ButtonWidget widget, CallbackInfo ci) {
		if (((IKeyBinding) keyBinding).nmuk_getParent() == null && Screen.hasShiftDown()) {
			NMUKKeyBindingHelper.resetAlternativeKeyBindings_OptionsScreen(keyBinding, listWidget, (KeyBindingEntry) (Object) this);
		}
	}

	@ModifyVariable(method = {"render", "render(IIIIIIIZF)V"}, at = @At("HEAD"), ordinal = 2, argsOnly = true)
	private int adjustXPosition(int original) {
		return original - 30;
	}

	@Redirect(method = {"render", "render(IIIIIIIZF)V"}, at = @At(value = "INVOKE", target = "Lnet/minecraft/client/option/KeyBinding;isDefault()Z"))
	private boolean isDefaultOnRender(KeyBinding keyBinding) {
		IKeyBinding iKeyBinding = (IKeyBinding) keyBinding;
		if (iKeyBinding.nmuk_getParent() == null) {
			Collection<KeyBinding> defaults = NMUKKeyBindingHelper.defaultAlternatives.get(keyBinding);
			int childrenCount = iKeyBinding.nmuk_getAlternativesCount();

			if (defaults.size() == childrenCount) {
				List<KeyBinding> children = iKeyBinding.nmuk_getAlternatives();
				if (childrenCount > 0) {
					for (KeyBinding child : children) {
						if (!defaults.contains(child)) {
							return false;
						}
						if (!child.isDefault()) {
							return false;
						}
					}
				}
			} else {
				return false;
			}
		} else {
			if (keyBinding.getDefaultKey().equals(InputUtil.UNKNOWN_KEY)) {
				return true;
			}
		}
		return keyBinding.isDefault();
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
		if (alternativesButton.mouseReleased(mouseX, mouseY, button)) {
			cir.setReturnValue(true);
		}
	}
}
