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

package de.siphalor.nmuk.impl.mixin.controlling;

import java.util.List;

import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.At.Shift;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.blamejared.controlling.client.NewKeyBindsList;
import com.blamejared.controlling.client.NewKeyBindsList.KeyEntry;
import com.google.common.collect.ImmutableList;

import de.klotzi111.fabricmultiversionhelper.api.text.TextWrapper;
import de.siphalor.nmuk.impl.duck.IControlsListWidget;
import de.siphalor.nmuk.impl.duck.IKeyBindingEntry;
import de.siphalor.nmuk.impl.mixinimpl.MixinKeyBindingEntryImpl;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;

// versioned aka 'MixinKeyBindingEntry_1_16' and normal mixin aka 'MixinKeyBindingEntry' combined
@Pseudo
@Mixin(KeyEntry.class)
public abstract class MixinKeyEntry implements IKeyBindingEntry {
	@Mutable
	@Shadow
	@Final
	private String keyDesc;

	@Shadow
	@Final
	private KeyBinding keybinding;
	// This is a synthetic field containing the outer class instance
	@Shadow(aliases = "this$0", remap = false)
	@Final
	private NewKeyBindsList listWidget; // outer

	@Shadow
	@Final
	private ButtonWidget btnResetKeyBinding;

	@Shadow
	@Final
	private ButtonWidget btnChangeKeyBinding;

	// + interface methods
	@Override
	public void nmuk$setBindingName(Text bindingName) {
		keyDesc = bindingName.getString();
	}

	@Override
	public Text nmuk$getBindingName() {
		return TextWrapper.literal(keyDesc);
	}

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
		return btnResetKeyBinding;
	}

	@Override
	public KeyBinding nmuk$getBinding() {
		return keybinding;
	}

	@Override
	public ButtonWidget nmuk$getEditButton() {
		return btnChangeKeyBinding;
	}
	// - interface methods

	// + versioned part
	@Inject(method = "<init>", at = @At("RETURN"))
	private void onConstruct(NewKeyBindsList outer, KeyBinding binding, CallbackInfo ci) {
		MixinKeyBindingEntryImpl.init((IKeyBindingEntry) this, (IControlsListWidget) outer, binding);
	}

	@Inject(method = "render", at = @At("RETURN"))
	private void render(MatrixStack matrices, int index, int y, int x, int entryWidth, int entryHeight, int mouseX, int mouseY, boolean hovered, float tickDelta, CallbackInfo callbackInfo) {
		MixinKeyBindingEntryImpl.render((IKeyBindingEntry) this, matrices, index, y, x, entryWidth, entryHeight, mouseX, mouseY, hovered, tickDelta);
	}

	@Inject(
		method = "render",
		at = @At(
			value = "FIELD",
			opcode = Opcodes.PUTFIELD,
			target = "Lnet/minecraft/client/gui/widget/ButtonWidget;active:Z",
			shift = Shift.AFTER))
	private void setResetButtonActive(CallbackInfo callbackInfo) {
		MixinKeyBindingEntryImpl.setResetButtonActive((IKeyBindingEntry) this, (IControlsListWidget) listWidget, keybinding);
	}
	// - versioned part

	// + normal mixin
	@Inject(method = "lambda$new$1(Lnet/minecraft/client/option/KeyBinding;Lnet/minecraft/client/gui/widget/ButtonWidget;)V", at = @At("HEAD"))
	private void resetButtonPressed(KeyBinding keyBinding, ButtonWidget widget, CallbackInfo ci) {
		MixinKeyBindingEntryImpl.resetButtonPressed((IKeyBindingEntry) this, keyBinding, widget, (IControlsListWidget) listWidget);
	}

	@ModifyVariable(method = "render", at = @At("HEAD"), ordinal = 2, argsOnly = true)
	private int adjustXPosition(int original) {
		return original - 30;
	}

	@Redirect(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/option/KeyBinding;isDefault()Z"))
	private boolean isDefaultOnRender(KeyBinding keyBinding) {
		return MixinKeyBindingEntryImpl.isDefaultOnRender((IKeyBindingEntry) this, keyBinding);
	}

	@Inject(method = {"method_25396()Ljava/util/List;", "children()Ljava/util/List;"}, remap = false, at = @At("RETURN"), cancellable = true)
	public void children(CallbackInfoReturnable<List<? extends Element>> callbackInfoReturnable) {
		callbackInfoReturnable.setReturnValue(ImmutableList.of(btnChangeKeyBinding, btnResetKeyBinding, alternativesButton));
	}

	// + fix mouse* return values

	// ordinal 2 is required because in the byte code the second return statement is unfolded to a condition with two constant returns
	@Inject(method = "mouseClicked(DDI)Z", at = @At(value = "RETURN", ordinal = 2), require = 1, cancellable = true)
	public void mouseClicked(double mouseX, double mouseY, int button, CallbackInfoReturnable<Boolean> cir) {
		// btnResetKeyBinding must be called and checked in here again because we need to redirect the real call because we must only call it once but do not want throw away the result of it
		if (btnResetKeyBinding.mouseClicked(mouseX, mouseY, button) || alternativesButton.mouseClicked(mouseX, mouseY, button)) {
			cir.setReturnValue(true);
		}
	}

	@Redirect(method = "mouseClicked(DDI)Z", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/widget/ButtonWidget;mouseClicked(DDI)Z", ordinal = 1))
	public boolean redirect_mouseClicked(ButtonWidget buttonWidget, double mouseX, double mouseY, int button) {
		return false; // is returned when handler does not return
	}

	@Inject(method = "mouseReleased(DDI)Z", at = @At(value = "RETURN", ordinal = 1), require = 1, cancellable = true)
	public void mouseReleased(double mouseX, double mouseY, int button, CallbackInfoReturnable<Boolean> cir) {
		// btnChangeKeyBinding must be called and checked in here again because we need to redirect the real call because we must only call it once but do not want throw away the result of it
		// btnResetKeyBinding must be called because it is not called in the original code of controlling
		if (btnChangeKeyBinding.mouseReleased(mouseX, mouseY, button) || btnResetKeyBinding.mouseReleased(mouseX, mouseY, button) || alternativesButton.mouseReleased(mouseX, mouseY, button)) {
			cir.setReturnValue(true);
		}
	}

	@Redirect(method = "mouseReleased(DDI)Z", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/widget/ButtonWidget;mouseReleased(DDI)Z", ordinal = 0))
	public boolean redirect_mouseReleased(ButtonWidget buttonWidget, double mouseX, double mouseY, int button) {
		return false; // is returned when handler does not return
	}
	// - fix mouse* return values

	// - normal mixin

}
