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
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import de.siphalor.nmuk.impl.duck.IClickableWidget;
import de.siphalor.nmuk.impl.duck.IKeyBindingEntry;
import de.siphalor.nmuk.impl.mixinimpl.MixinKeyBindingEntryImpl;
import net.minecraft.client.gui.screen.option.ControlsListWidget;
import net.minecraft.client.gui.screen.option.ControlsListWidget.KeyBindingEntry;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.text.Text;

@Mixin(KeyBindingEntry.class)
public abstract class MixinKeyBindingEntry_1_14 implements IKeyBindingEntry {
	@Mutable
	@Shadow(aliases = {"field_2741", "bindingName"}, remap = false)
	@Final
	private String bindingName;

	@Shadow
	@Final
	private KeyBinding binding;
	// This is a synthetic field containing the outer class instance
	@Shadow(aliases = "field_2742", remap = false)
	@Final
	private ControlsListWidget listWidget;

	@Override
	public void setBindingName(Text bindingName) {
		this.bindingName = bindingName.asString();
	}

	@Inject(method = "<init>", at = @At("RETURN"))
	public void onConstruct(ControlsListWidget outer, KeyBinding binding, CallbackInfo ci) {
		MixinKeyBindingEntryImpl.init((ControlsListWidget.KeyBindingEntry) (Object) this, outer, binding);
	}

	@Inject(method = "render(IIIIIIIZF)V", at = @At("RETURN"))
	public void render(int index, int y, int x, int entryWidth, int entryHeight, int mouseX, int mouseY, boolean hovered, float tickDelta, CallbackInfo callbackInfo) {
		MixinKeyBindingEntryImpl.render((KeyBindingEntry) (Object) this, null, index, y, x, entryWidth, entryHeight, mouseX, mouseY, hovered, tickDelta);

		// render tooltips
		// TODO: in Minecraft version 1.14: When the tooltip is rendered the rest of the gui has a dark shade
		ButtonWidget resetButton = ((IKeyBindingEntry) this).getResetButton();
		((IClickableWidget) resetButton).nmuk$renderToolTipIfHovered(mouseX, mouseY);

		// TODO
		// if(!MinecraftVersionHelper.IS_AT_LEAST_V1_15) {
		// DiffuseLighting.disable();
		// GlStateManager.disableLighting();
		// }
	}

	@Inject(
		method = "render(IIIIIIIZF)V",
		at = @At(value = "FIELD", opcode = Opcodes.PUTFIELD, target = "Lnet/minecraft/class_4185;active:Z", shift = Shift.AFTER, remap = false))
	private void setResetButtonActive(CallbackInfo callbackInfo) {
		MixinKeyBindingEntryImpl.setResetButtonActive((KeyBindingEntry) (Object) this, listWidget, binding);
	}

}
