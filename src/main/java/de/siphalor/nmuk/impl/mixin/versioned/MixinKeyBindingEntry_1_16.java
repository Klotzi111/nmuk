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

package de.siphalor.nmuk.impl.mixin.versioned;

import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.At.Shift;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import de.siphalor.nmuk.impl.duck.IControlsListWidget;
import de.siphalor.nmuk.impl.duck.IKeyBindingEntry;
import de.siphalor.nmuk.impl.mixinimpl.MixinKeyBindingEntryImpl;
import net.minecraft.client.gui.screen.option.ControlsListWidget;
import net.minecraft.client.gui.screen.option.ControlsListWidget.KeyBindingEntry;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;

@Mixin(KeyBindingEntry.class)
public abstract class MixinKeyBindingEntry_1_16 implements IKeyBindingEntry {
	@Mutable
	@Shadow
	@Final
	private Text bindingName;

	@Shadow
	@Final
	private KeyBinding binding;
	// This is a synthetic field containing the outer class instance
	@Shadow(aliases = "field_2742", remap = false)
	@Final
	private ControlsListWidget listWidget;

	@Override
	public void nmuk$setBindingName(Text bindingName) {
		this.bindingName = bindingName;
	}

	@Override
	public Text nmuk$getBindingName() {
		return bindingName;
	}

	@Inject(method = "<init>", at = @At("RETURN"))
	public void onConstruct(ControlsListWidget outer, KeyBinding binding, Text text, CallbackInfo ci) {
		MixinKeyBindingEntryImpl.init((IKeyBindingEntry) this, (IControlsListWidget) outer, binding);
	}

	@Inject(method = "render", at = @At("RETURN"))
	public void render(MatrixStack matrices, int index, int y, int x, int entryWidth, int entryHeight, int mouseX, int mouseY, boolean hovered, float tickDelta, CallbackInfo callbackInfo) {
		MixinKeyBindingEntryImpl.render((IKeyBindingEntry) this, matrices, index, y, x, entryWidth, entryHeight, mouseX, mouseY, hovered, tickDelta);
	}

	@Inject(
		method = "render",
		at = @At(value = "FIELD", opcode = Opcodes.PUTFIELD, target = "Lnet/minecraft/client/gui/widget/ButtonWidget;active:Z", shift = Shift.AFTER))
	private void setResetButtonActive(CallbackInfo callbackInfo) {
		MixinKeyBindingEntryImpl.setResetButtonActive((IKeyBindingEntry) this, (IControlsListWidget) listWidget, binding);
	}

	@Redirect(method = "mouseReleased(DDI)Z", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/widget/ButtonWidget;mouseReleased(DDI)Z", ordinal = 1))
	public boolean redirect_mouseReleased(ButtonWidget buttonWidget, double mouseX, double mouseY, int button) {
		return false; // is returned when handler does not return
	}

}
