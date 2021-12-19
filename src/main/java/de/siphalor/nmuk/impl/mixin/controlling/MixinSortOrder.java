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

import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.blamejared.controlling.api.ISort;
import com.blamejared.controlling.api.SortOrder;

import de.siphalor.nmuk.impl.KeyBindingCompareHelper;
import de.siphalor.nmuk.impl.duck.IKeyBindingEntry;

@Pseudo
@Mixin(SortOrder.class)
public abstract class MixinSortOrder {

	@Shadow
	@Final
	@Mutable
	private ISort sorter;

	@Inject(method = "<init>", at = @At(value = "TAIL"))
	private void onConstruct(String name, int ordinal, ISort sorter, CallbackInfo ci) {
		switch (name) {
			case "AZ":
				this.sorter = (entries) -> {
					entries.sort((entry1, entry2) -> {
						return KeyBindingCompareHelper.compareKeyBindings(((IKeyBindingEntry) entry1).nmuk$getBinding(), ((IKeyBindingEntry) entry2).nmuk$getBinding(), true, false);
					});
				};
				break;

			case "ZA":
				this.sorter = (entries) -> {
					entries.sort((entry1, entry2) -> {
						return KeyBindingCompareHelper.compareKeyBindings(((IKeyBindingEntry) entry1).nmuk$getBinding(), ((IKeyBindingEntry) entry2).nmuk$getBinding(), true, true);
					});
				};
				break;
		}
	}

}
