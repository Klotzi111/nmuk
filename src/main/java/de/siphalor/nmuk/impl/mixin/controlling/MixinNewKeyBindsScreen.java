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

import java.util.Iterator;
import java.util.List;
import java.util.function.Predicate;

import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.At.Shift;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import com.blamejared.controlling.client.FancyCheckbox;
import com.blamejared.controlling.client.NewKeyBindsList.KeyEntry;
import com.blamejared.controlling.client.NewKeyBindsScreen;

import de.siphalor.nmuk.impl.compat.controlling.KeyBindingEntryVersionHelper;
import de.siphalor.nmuk.impl.duck.IKeyBinding;
import de.siphalor.nmuk.impl.duck.IKeyBindingEntry;
import de.siphalor.nmuk.impl.duck.IKeybindsScreen;
import de.siphalor.nmuk.impl.duck.controlling.ICustomList;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.option.ControlsListWidget;
import net.minecraft.client.gui.screen.option.ControlsListWidget.Entry;
import net.minecraft.client.gui.screen.option.KeybindsScreen;
import net.minecraft.client.option.GameOptions;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.text.LiteralText;
import net.minecraft.text.StringVisitable;
import net.minecraft.text.TranslatableText;

@Pseudo
@Mixin(NewKeyBindsScreen.class)
public abstract class MixinNewKeyBindsScreen extends KeybindsScreen implements IKeybindsScreen {

	// ignored
	public MixinNewKeyBindsScreen(Screen parent, GameOptions gameOptions) {
		super(parent, gameOptions);
	}

	@Unique
	private boolean lastMatched;
	@Unique
	private Entry lastBaseEntry;
	@Unique
	private boolean lastBaseEntryAdded;
	@Unique
	private Entry entry;

	@Inject(method = "filterKeys", at = @At("HEAD"), remap = false)
	public void filterKeys_head(CallbackInfo ci) {
		lastMatched = false;
		lastBaseEntry = null;
		lastBaseEntryAdded = false;
	}

	// we need this to get the actual entry from the iterator
	// Mixin somehow can not retrieve the Entry local variable even though it is valid at that position
	@Redirect(
		method = "filterKeys",
		slice = @Slice(
			from = @At(
				value = "INVOKE",
				target = "Lcom/blamejared/controlling/client/CustomList;getAllEntries()Ljava/util/List;",
				ordinal = 1),
			to = @At(
				value = "FIELD",
				opcode = Opcodes.GETSTATIC,
				target = "Lcom/blamejared/controlling/api/SearchType;CATEGORY:Lcom/blamejared/controlling/api/SearchType;",
				ordinal = 0)),
		at = @At(
			value = "INVOKE",
			target = "Ljava/util/Iterator;next()Ljava/lang/Object;",
			ordinal = 0),
		remap = false)
	public Object filterKeys_Iterator_next(Iterator<Object> iterator) {
		entry = (Entry) iterator.next();
		return entry;
	}

	// inside foreach loop that checks whether to add the entry to the children
	// at bottom one opcode before jump back up to new iteration of loop
	@Inject(
		method = "filterKeys",
		slice = @Slice(
			from = @At(
				value = "INVOKE",
				target = "Lcom/blamejared/controlling/client/CustomList;getAllEntries()Ljava/util/List;",
				ordinal = 1),
			to = @At(
				value = "FIELD",
				opcode = Opcodes.GETSTATIC,
				target = "Lcom/blamejared/controlling/api/SearchType;CATEGORY:Lcom/blamejared/controlling/api/SearchType;",
				ordinal = 1)),
		at = @At(
			value = "JUMP",
			opcode = Opcodes.GOTO,
			ordinal = 2),
		locals = LocalCapture.CAPTURE_FAILSOFT,
		remap = false)
	public void filterKeys_forIteration(CallbackInfo ci, Predicate<?> filters, Iterator<?> allEntriesIterator/* , Entry entry */) {
		List<Entry> children = ((ControlsListWidget) nmuk$getControlsList()).children();
		int lastChildrenIndex = children.size() - 1; // will be -1 if size == 0, meaning not found
		Entry lastChild = lastChildrenIndex >= 0 ? children.get(lastChildrenIndex) : null;

		boolean wasAdded = lastChild == entry;

		// code mostly copied from 'SearchFieldControlsListWidget'.'filterChildrenList' in amecs
		/* here would be the foreach loop */ {
			// only do this if the entry is a KeyEntry
			if (!KeyBindingEntryVersionHelper.KeyEntry_class.isAssignableFrom(entry.getClass())) {
				return; // does effectively continue;
			}

			KeyBinding binding = ((IKeyBindingEntry) entry).nmuk$getBinding();
			IKeyBinding iBinding = (IKeyBinding) binding;

			// if we only show alternatives we remove the non alternatives
			// but we might need to add the base later when at least one alternative comes
			if (alternativesOnly && wasAdded && !iBinding.nmuk$isAlternative()) {
				children.remove(lastChildrenIndex);
				wasAdded = false;
			}

			// only add the alternatives of a base if the base was matched itself
			if (lastMatched && !lastBaseEntryAdded && iBinding.nmuk$isAlternative()) {
				if (!wasAdded) {
					children.add(entry);
				}
				return; // does effectively continue;
			}

			lastMatched = wasAdded;
			if (lastMatched) {

				// if we found an alternative make sure we also add the base
				if (iBinding.nmuk$isAlternative()) {
					if (lastBaseEntry != null && !lastBaseEntryAdded) {
						// add base before actual alternative keybinding
						// but the keybinding was already added. We need to add it one index before the keybinding
						children.add(Math.max(children.size() - 1, 0), lastBaseEntry);
						lastBaseEntryAdded = true;
					}
				}

				// do not add because it was already added
				// children.add(entry);
			}

			KeyBinding base = iBinding.nmuk$getParent();
			if (base == null && (lastBaseEntry == null || binding != ((IKeyBindingEntry) lastBaseEntry).nmuk$getBinding())) {
				lastBaseEntry = entry;
				lastBaseEntryAdded = false;
			}
		}
	}

	@Unique
	private boolean alternativesOnly = false;

	@Redirect(
		method = "lambda$filterKeys$8",
		at = @At(
			value = "INVOKE",
			target = "Lcom/blamejared/controlling/client/NewKeyBindsList$KeyEntry;getKeyDesc()Ljava/lang/String;",
			ordinal = 0),
		remap = false)
	private String predicateFilters_getKeyDesc(KeyEntry keyEntry) {
		IKeyBindingEntry iKeyBindingEntry = (IKeyBindingEntry) keyEntry;

		// if this is a entry for a alternative keybinding
		// we return an empty string since a empty string can never contain anything
		// and that is exactly what we want: the .contains(...) to evalute to false
		IKeyBinding keyBinding = (IKeyBinding) iKeyBindingEntry.nmuk$getBinding();
		if (keyBinding.nmuk$isAlternative()) {
			if (alternativesOnly) {
				// search backwards to find KeyBindingEntry of parent keybinding
				KeyBinding parent = keyBinding.nmuk$getParent();
				// important to search in all entries from CustomList because the actual list is empty at this point
				List<Entry> children = ((ICustomList) nmuk$getControlsList()).nmuk$getAllEntries();
				int childIndex = children.indexOf(keyEntry);
				for (int i = childIndex - 1; i >= 0; i--) {
					IKeyBindingEntry entry = (IKeyBindingEntry) children.get(i);
					if (entry.nmuk$getBinding() == parent) {
						return entry.nmuk$getBindingName().getString();
					}
				}
				// not found?!?
			}
			return "";
		}

		return iKeyBindingEntry.nmuk$getBindingName().getString();
	}

	@Unique
	private FancyCheckbox buttonAlternativesOnly;

	@Unique
	private static final String ALTERNATIVES_ONLY_TRANSLATION_KEY = "nmuk.options.controls.compat.controlling.alternatives_only";

	@SuppressWarnings("resource")
	@Inject(
		method = "init",
		slice = @Slice(
			from = @At(
				value = "FIELD",
				opcode = Opcodes.PUTFIELD,
				target = "Lcom/blamejared/controlling/client/NewKeyBindsScreen;buttonCat:Lcom/blamejared/controlling/client/FancyCheckbox;",
				ordinal = 0),
			to = @At(
				value = "FIELD",
				opcode = Opcodes.PUTFIELD,
				target = "Lcom/blamejared/controlling/client/NewKeyBindsScreen;sortOrder:Lcom/blamejared/controlling/api/SortOrder;",
				ordinal = 0)),
		at = @At(
			value = "FIELD",
			opcode = Opcodes.PUTFIELD,
			target = "Lcom/blamejared/controlling/client/NewKeyBindsScreen;sortOrder:Lcom/blamejared/controlling/api/SortOrder;",
			shift = Shift.BY,
			by = -2,
			ordinal = 0),
		remap = false)
	public void init_addButtons(CallbackInfo ci) {
		NewKeyBindsScreen _this = ((NewKeyBindsScreen) (Object) this);

		TranslatableText alternativesOnlyText = new TranslatableText(ALTERNATIVES_ONLY_TRANSLATION_KEY);
		StringVisitable trimmedString = MinecraftClient.getInstance().textRenderer.trimToWidth(alternativesOnlyText, 155 - 5);

		buttonAlternativesOnly = this.addDrawableChild(new FancyCheckbox(width / 2 - 155, height - 29 - 37, 11, 11, new LiteralText(trimmedString.getString()), false, btn -> {
			alternativesOnly = btn.selected();
			_this.filterKeys();
		}));
	}

	@Redirect(
		method = "filterKeys",
		slice = @Slice(
			from = @At(
				value = "INVOKE",
				target = "Ljava/util/List;clear()V",
				ordinal = 0),
			to = @At(
				value = "FIELD",
				opcode = Opcodes.GETFIELD,
				target = "Lcom/blamejared/controlling/client/NewKeyBindsScreen;displayMode:Lcom/blamejared/controlling/api/DisplayMode;",
				ordinal = 0)),
		at = @At(
			value = "INVOKE",
			target = "Ljava/lang/String;isEmpty()Z",
			ordinal = 0),
		remap = false)
	public boolean filterKeys_filteringRequired(String string) {
		if (alternativesOnly) {
			return false;
		}
		return string.isEmpty();
	}

	// move the text up a bit
	@SuppressWarnings("resource")
	@ModifyConstant(
		method = "render",
		slice = @Slice(
			from = @At(
				value = "FIELD",
				opcode = Opcodes.GETFIELD,
				target = "Lcom/blamejared/controlling/client/NewKeyBindsScreen;height:I",
				ordinal = 0),
			to = @At(
				value = "INVOKE",
				target = "Lcom/blamejared/controlling/client/NewKeyBindsScreen;getScreenAccess()Lcom/blamejared/controlling/mixin/AccessScreen;",
				ordinal = 0)),
		constant = @Constant(intValue = 42))
	public int render_moveText(int original) {
		// center the text vertically between the checkbox edges
		return -(-50 + 11 / 2 - MinecraftClient.getInstance().textRenderer.fontHeight / 2);
	}

}
