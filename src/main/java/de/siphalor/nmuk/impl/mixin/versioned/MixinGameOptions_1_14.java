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

import java.util.Arrays;

import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import de.siphalor.nmuk.impl.duck.IKeyBinding;
import net.minecraft.client.option.GameOptions;
import net.minecraft.client.option.KeyBinding;

@Mixin(value = GameOptions.class, priority = 800)
public class MixinGameOptions_1_14 {
	@Unique
	private KeyBinding[] tempKeysAll;

	@Mutable
	@Shadow
	@Final
	public KeyBinding[] allKeys;

	// Prevent nmuk keybindings from getting saved to the Vanilla options file
	// target = "Lnet/minecraft/client/options/GameOptions;allKeys:[Lnet/minecraft/client/options/KeyBinding;"
	@Inject(method = "write", at = @At(value = "FIELD", opcode = Opcodes.GETFIELD, target = "Lnet/minecraft/class_315;field_1839:[Lnet/minecraft/class_304;"))
	public void removeNMUKBindings(CallbackInfo ci) {
		tempKeysAll = allKeys;
		allKeys = Arrays.stream(allKeys).filter(binding -> !((IKeyBinding) binding).nmuk$isAlternative()).toArray(KeyBinding[]::new);
	}

	@Inject(method = "write", at = @At(value = "INVOKE", target = "Lnet/minecraft/sound/SoundCategory;values()[Lnet/minecraft/sound/SoundCategory;"))
	public void resetAllKeys(CallbackInfo ci) {
		allKeys = tempKeysAll;
	}

}
