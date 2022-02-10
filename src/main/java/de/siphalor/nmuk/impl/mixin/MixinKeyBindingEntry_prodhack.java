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

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import de.siphalor.nmuk.impl.duck.IKeyBindingEntry;
import net.minecraft.client.gui.screen.option.ControlsListWidget.KeyBindingEntry;
import net.minecraft.client.gui.widget.ButtonWidget;

@Mixin(KeyBindingEntry.class)
public abstract class MixinKeyBindingEntry_prodhack implements IKeyBindingEntry {

	@Redirect(method = "mouseReleased(DDI)Z", remap = false, at = @At(value = "INVOKE", target = "Lnet/minecraft/class_4185;mouseReleased(DDI)Z", ordinal = 1))
	public boolean redirect_mouseReleased(ButtonWidget buttonWidget, double mouseX, double mouseY, int button) {
		return false; // is returned when handler does not return
	}

}