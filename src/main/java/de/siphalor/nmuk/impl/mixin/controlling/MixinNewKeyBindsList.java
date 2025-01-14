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

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Shadow;

import com.blamejared.controlling.client.NewKeyBindsList;

import de.siphalor.nmuk.impl.duck.IControlsListWidget;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.option.KeybindsScreen;

@Pseudo
@Mixin(NewKeyBindsList.class)
public abstract class MixinNewKeyBindsList implements IControlsListWidget {

	@Shadow
	@Final
	KeybindsScreen controlsScreen;

	@Override
	public Screen nmuk$getParent() {
		return controlsScreen;
	}

}
