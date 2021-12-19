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

package de.siphalor.nmuk.impl.duck;

import java.util.List;

import org.jetbrains.annotations.ApiStatus;

import net.minecraft.client.option.KeyBinding;

@ApiStatus.Internal
public interface IKeyBinding {
	// only required because mc verions 1.14 does not have this
	void nmuk$setPressed(boolean pressed);

	int nmuk$getNextChildId();

	void nmuk$setNextChildId(int nextChildId);

	boolean nmuk$isAlternative();

	KeyBinding nmuk$getParent();

	void nmuk$setParent(KeyBinding binding);

	List<KeyBinding> nmuk$getAlternatives();

	int nmuk$getAlternativesCount();

	/**
	 *
	 * @param binding
	 * @return the index at which the binding was found in the parent's alternatives
	 */
	int nmuk$removeAlternative(KeyBinding binding);

	void nmuk$addAlternative(KeyBinding binding);

	/**
	 * This method should only be used for the controls gui to determine the entry position
	 *
	 * @return the index in the parent's children list
	 */
	int nmuk$getIndexInParent();

	int nmuk$getAlternativeId();

	void nmuk$setAlternativeId(int alternativeId);
}
