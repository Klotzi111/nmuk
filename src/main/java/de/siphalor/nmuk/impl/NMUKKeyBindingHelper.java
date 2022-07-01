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

package de.siphalor.nmuk.impl;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

import org.apache.commons.lang3.ArrayUtils;
import org.jetbrains.annotations.ApiStatus;

import com.google.common.collect.ListMultimap;
import com.google.common.collect.Multimaps;

import de.klotzi111.fabricmultiversionhelper.api.text.TextWrapper;
import de.siphalor.nmuk.impl.compat.controlling.CompatibilityControlling;
import de.siphalor.nmuk.impl.compat.controlling.KeyBindingEntryVersionHelper;
import de.siphalor.nmuk.impl.duck.*;
import de.siphalor.nmuk.impl.duck.controlling.ICustomList;
import de.siphalor.nmuk.impl.mixin.GameOptionsAccessor;
import de.siphalor.nmuk.impl.mixin.KeyBindingAccessor;
import de.siphalor.nmuk.impl.mixin.KeyBindingRegistryImplAccessor;
import de.siphalor.nmuk.impl.version.KeybindsScreenVersionHelper;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.EntryListWidget.Entry;
import net.minecraft.client.option.GameOptions;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.client.util.InputUtil.Key;
import net.minecraft.text.Text;

@SuppressWarnings("unchecked")
@ApiStatus.Internal
public class NMUKKeyBindingHelper {

	private static void changeKeysAll(GameOptionsAccessor options, Function<KeyBinding[], KeyBinding[]> changeFunction) {
		options.setAllKeys(changeFunction.apply(options.getAllKeys()));
	}

	@SuppressWarnings("unused")
	private static void changeKeysAll(Function<KeyBinding[], KeyBinding[]> changeFunction) {
		changeKeysAll(getGameOptionsAccessor(), changeFunction);
	}

	@SuppressWarnings("resource")
	private static GameOptionsAccessor getGameOptionsAccessor() {
		return (GameOptionsAccessor) MinecraftClient.getInstance().options;
	}

	public static final ListMultimap<KeyBinding, KeyBinding> defaultAlternatives = Multimaps.newListMultimap(new HashMap<>(), ArrayList::new);

	// register/unregister keybindings
	public static void registerKeyBindingGUI(KeyBinding binding) {
		KeyBindingHelper.registerKeyBinding(binding); // this adds the keybinding to the moddedKeybindings list and adds the category to the options gui
		GameOptionsAccessor options = getGameOptionsAccessor();
		if (options != null) { // if options == null: Game is during initialization - this is handled by Fabric Api already
			// this adds the keybindings to the options gui
			changeKeysAll(options, allKeys -> ArrayUtils.add(allKeys, binding));
		}
	}

	public static void unregisterKeyBindingGUI(KeyBinding binding) {
		removeKeyBindingGUI(getGameOptionsAccessor(), binding);
	}

	public static void removeKeyBindingGUI(GameOptionsAccessor options, KeyBinding binding) {
		// remove it from fabrics moddedKeyBindings list. Or at least try to. Maybe it is not a modded keybinding
		List<KeyBinding> moddedKeyBindings = KeyBindingRegistryImplAccessor.getModdedKeyBindings();
		moddedKeyBindings.remove(binding);

		if (options != null) { // if options == null: Game is during initialization - this is handled by Fabric Api already
			// this removes the keybinding from the options gui
			changeKeysAll(options, allKeys -> ArrayUtils.removeElement(allKeys, binding));
		}
	}

	private static void changeKeysById(Consumer<Map<String, KeyBinding>> changeFunction) {
		// get the keybinding from the input query list
		Map<String, KeyBinding> keyBindings = KeyBindingAccessor.getKeysById();
		changeFunction.accept(keyBindings);
		// update keys from ids (this will result in the keybinding from now on/no longer being tiggered)
		KeyBinding.updateKeysByCode();
	}

	public static void registerKeyBindingQuerying(KeyBinding binding) {
		// TODO: use methods from amecs??
		changeKeysById(keyBindings -> keyBindings.put(binding.getTranslationKey(), binding));
	}

	public static void unregisterKeyBindingQuerying(KeyBinding binding) {
		// TODO: use methods from amecs??
		changeKeysById(keyBindings -> keyBindings.remove(binding.getTranslationKey(), binding));
	}

	public static void registerKeyBindingsBoth(GameOptions gameOptions, Collection<KeyBinding> bindings) {
		for (KeyBinding binding : bindings) {
			KeyBindingHelper.registerKeyBinding(binding);
			registerKeyBindingQuerying(binding);
		}
		GameOptionsAccessor options = (GameOptionsAccessor) gameOptions;
		if (options != null) { // if options == null: Game is during initialization - this is handled by Fabric Api already
			// this adds the keybindings to the options gui
			changeKeysAll(options, allKeys -> ArrayUtils.addAll(allKeys, bindings.toArray(new KeyBinding[0])));
		}
	}
	// - register/unregister keybindings

	public static void resetSingleKeyBinding(KeyBinding keyBinding) {
		keyBinding.setBoundKey(keyBinding.getDefaultKey());
	}

	// used in GameOptions.load
	public static KeyBinding findMatchingAlternativeInBase(KeyBinding base, int alternativeId) {
		IKeyBinding parent = (IKeyBinding) base;
		List<KeyBinding> alternatives = parent.nmuk$getAlternatives();
		return findMatchingAlternative(alternatives, base.getTranslationKey(), alternativeId);
	}

	// used in GameOptions.load
	public static KeyBinding findMatchingAlternative(List<KeyBinding> alternatives, String tanslationKey, int alternativeId) {
		if (alternatives == null) {
			return null;
		}
		String searchTranslationKey = AlternativeKeyBinding.makeAlternativeKeyTranslationKey(tanslationKey, alternativeId);
		for (KeyBinding alternative : alternatives) {
			if (alternative.getTranslationKey().equals(searchTranslationKey)) {
				return alternative;
			}
		}
		return null;
	}

	/**
	 *
	 * @param base
	 * @return the keybinding (get or new). It is registered for input querying and is added to the parent
	 */
	public static KeyBinding getOrCreateAlternativeKeyBinding(KeyBinding base) {
		IKeyBinding parent = (IKeyBinding) base;

		// get the next default alternative if available
		List<KeyBinding> defaultAlternatives = NMUKKeyBindingHelper.defaultAlternatives.get(base);
		if (defaultAlternatives.size() > parent.nmuk$getAlternativesCount()) {
			KeyBinding defaultAlternative = defaultAlternatives.get(parent.nmuk$getAlternativesCount());
			makeKeyBindingAlternativeOf(base, defaultAlternative, AlternativeKeyBinding.NO_ALTERNATIVE_ID, false);
			registerKeyBindingQuerying(defaultAlternative);

			parent.nmuk$addAlternative(defaultAlternative);
			return defaultAlternative;
		}

		// if not we create a new alternative keybinding
		KeyBinding alternative = new AlternativeKeyBinding(base, base.getTranslationKey(), parent.nmuk$getNextChildId(), base.getCategory());
		parent.nmuk$addAlternative(alternative);
		return alternative;
	}

	/**
	 *
	 * @param base
	 * @param type
	 * @param code
	 * @return the newly created keybinding. It is registered for input querying
	 */
	public static KeyBinding createAndAddAlternativeKeyBinding(KeyBinding base, InputUtil.Type type, int code) {
		IKeyBinding parent = (IKeyBinding) base;
		KeyBinding alternative = new AlternativeKeyBinding(base, base.getTranslationKey(), parent.nmuk$getNextChildId(), type, code, base.getCategory());
		parent.nmuk$addAlternative(alternative);
		return alternative;
	}

	/**
	 * The keybinding {@code alternative} is NEITHER registered for querying NOR in the gui
	 *
	 * @param base
	 * @param alternative
	 * @param alternativeId
	 * @param addToBase
	 */
	public static void makeKeyBindingAlternativeOf(KeyBinding base, KeyBinding alternative, int alternativeId, boolean addToBase) {
		IKeyBinding parent = (IKeyBinding) base;

		// now the keybinding is a complete ghost and we can give it a new identity
		// this code here should set all the values in the same way than the AlternativeKeyBinding contructor does
		if (alternativeId == AlternativeKeyBinding.NO_ALTERNATIVE_ID) {
			alternativeId = parent.nmuk$getNextChildId();
		}
		String newTranslationKey = AlternativeKeyBinding.makeAlternativeKeyTranslationKey(base.getTranslationKey(), alternativeId);
		((KeyBindingAccessor) alternative).setTranslationKey(newTranslationKey);
		((KeyBindingAccessor) alternative).setCategory(base.getCategory());
		((IKeyBinding) alternative).nmuk$setAlternativeId(alternativeId);
		((IKeyBinding) alternative).nmuk$setParent(base);

		if (addToBase) {
			// and finally we give the parent its new child
			parent.nmuk$addAlternative(alternative);
		}
	}

	// for options gui
	public static final Text ENTRY_NAME = TextWrapper.literal("    ->");
	public static final Text ADD_ALTERNATIVE_TEXT = TextWrapper.literal("+");
	public static final Text REMOVE_ALTERNATIVE_TEXT = TextWrapper.literal("x");
	// the constructor with the Object[] is required because older minecraft versions do not have a constructor with a String only
	public static final Text RESET_TOOLTIP = TextWrapper.translatable("nmuk.options.controls.reset.tooltip", new Object[0]);

	// gui only
	@SuppressWarnings("resource")
	private static void saveOptions() {
		MinecraftClient.getInstance().options.write();
	}

	// gui only
	@SuppressWarnings("resource")
	public static IControlsListWidget getControlsListWidgetFromCurrentScreen(Predicate<IKeybindsScreen> ifFunction) {
		Screen currentScreen = MinecraftClient.getInstance().currentScreen;
		if (currentScreen != null && KeybindsScreenVersionHelper.ACTUAL_KEYBINDS_SCREEN_CLASS.isAssignableFrom(currentScreen.getClass())) {
			if (ifFunction.test((IKeybindsScreen) currentScreen)) {
				return ((IKeybindsScreen) currentScreen).nmuk$getControlsList();
			}
		}
		return null;
	}

	/**
	 *
	 * @param entries
	 * @param fromIndex inclusive. A negative value means: Start at the beginning
	 * @param toIndex exclusive. A negative value means: Stop at the end
	 * @param ifFunction
	 * @return the index after {@code fromIndex} of the first keybinding that matches the {@code ifFunction}
	 */
	// gui only
	public static int findKeyBindingEntryIndex(List<Entry<?>> entries, int fromIndex, int toIndex, Predicate<IKeyBindingEntry> ifFunction) {
		int end = toIndex < 0 ? entries.size() : Math.min(entries.size(), toIndex);
		for (int i = Math.max(fromIndex, 0); i < end; i++) {
			Entry<?> entry = entries.get(i);
			if (entry != null && KeyBindingEntryVersionHelper.ACTUAL_KEYBINDING_ENTRY_CLASS.isAssignableFrom(entry.getClass())) {
				if (ifFunction.test((IKeyBindingEntry) entry)) {
					return i;
				}
			}
		}
		return -1;
	}

	private static class EntryListWithIndex {
		public List<Entry<?>> entries;
		public int index;

		public EntryListWithIndex(List<Entry<?>> entries, int index) {
			this.entries = entries;
			this.index = index;
		}

		public static EntryListWithIndex[] createFromEntryLists(List<Entry<?>>[] entries, Consumer<EntryListWithIndex> eachTask) {
			EntryListWithIndex[] ret = new EntryListWithIndex[entries.length];
			for (int i = 0; i < entries.length; i++) {
				ret[i] = new EntryListWithIndex(entries[i], 0);
				if (eachTask != null) {
					eachTask.accept(ret[i]);
				}
			}
			return ret;
		}
	}

	// fixed multi entries
	public static void removeAlternativeKeyBinding_OptionsScreen(KeyBinding keyBinding, IControlsListWidget listWidget) {
		// if this method is called outside of the gui code and search the gui
		if (listWidget == null) {
			listWidget = getControlsListWidgetFromCurrentScreen(keybindsScreen -> keybindsScreen.nmuk$getSelectedKeyBinding() == keyBinding);
			// not found
			if (listWidget == null) {
				return;
			}
		}

		// we need to search the index in the entries
		List<Entry<?>>[] entries = getControlsListWidgetEntries(listWidget);
		int entryIndexInEntriesFirst = findKeyBindingEntryIndex(entries[0], -1, -1, keyBindingEntry -> keyBindingEntry.nmuk$getBinding() == keyBinding);
		if (entryIndexInEntriesFirst == -1) {
			return;
		}
		int[] finalWrapper = new int[] {entryIndexInEntriesFirst};
		EntryListWithIndex[] entriesWithIndex = EntryListWithIndex.createFromEntryLists(entries, (entryWithIndex) -> {
			entryWithIndex.index = finalWrapper[0] != -1 ? finalWrapper[0] : findKeyBindingEntryIndex(entryWithIndex.entries, -1, -1,
				keyBindingEntry -> keyBindingEntry.nmuk$getBinding() == keyBinding);
			finalWrapper[0] = -1;
		});

		KeyBinding base = ((IKeyBinding) keyBinding).nmuk$getParent();
		int indexInBase = ((IKeyBinding) base).nmuk$removeAlternative(keyBinding);
		unregisterKeyBindingQuerying(keyBinding);
		unregisterKeyBindingGUI(keyBinding);

		for (EntryListWithIndex entryWithIndex : entriesWithIndex) {
			entryWithIndex.entries.remove(entryWithIndex.index);
		}
		updateDefaultAlternativesInBase(base, indexInBase, listWidget, entriesWithIndex);

		// do it like vanilla and save directly
		saveOptions();
	}

	// fixed multi entries
	private static void updateDefaultAlternativesInBase(KeyBinding base, int indexInBase, IControlsListWidget listWidget, EntryListWithIndex[] entriesWithIndex) {
		if (indexInBase == -1) {
			// not removed: nothing changed and maybe even the list is empty
			return;
		}
		List<KeyBinding> defaultAlternatives = NMUKKeyBindingHelper.defaultAlternatives.get(base);
		if (indexInBase >= defaultAlternatives.size()) {
			// the removed keybinding was after all the default ones
			return;
		}
		// update the defaults
		List<KeyBinding> alternatives = ((IKeyBinding) base).nmuk$getAlternatives();
		int minSize = Math.min(alternatives.size(), defaultAlternatives.size());
		for (int i = indexInBase; i < minSize; i++) {
			KeyBinding currentAlt = alternatives.get(i);
			KeyBinding newAlt = defaultAlternatives.get(i);
			if (currentAlt == newAlt) {
				continue;
			}
			Key boundKey = ((KeyBindingAccessor) currentAlt).getBoundKey();
			makeKeyBindingAlternativeOf(base, newAlt, ((IKeyBinding) currentAlt).nmuk$getAlternativeId(), false);
			newAlt.setBoundKey(boundKey);
			// important to first unregister the current one before registering the new one
			unregisterKeyBindingQuerying(currentAlt);
			unregisterKeyBindingGUI(currentAlt);

			registerKeyBindingQuerying(newAlt);
			registerKeyBindingGUI(newAlt);
			alternatives.set(i, newAlt);

			// update gui entries
			IKeyBindingEntry newEntry = KeyBindingEntryVersionHelper.createKeyBindingEntry(listWidget, newAlt, ENTRY_NAME);
			int iRelToStart = (i - indexInBase);

			for (EntryListWithIndex entryWithIndex : entriesWithIndex) {
				int indexEntry = entryWithIndex.index + iRelToStart;
				entryWithIndex.entries.remove(indexEntry);
				entryWithIndex.entries.add(indexEntry, (Entry<?>) newEntry);
			}
		}
	}

	// gui only
	private static int getExistingKeyIsUnboundIndex(KeyBinding binding) {
		if (binding.isUnbound()) {
			return -1;
		}
		List<KeyBinding> alternatives = ((IKeyBinding) binding).nmuk$getAlternatives();
		if (alternatives != null) {
			Optional<KeyBinding> unboundAlternative = alternatives.stream().filter(alternative -> alternative.isUnbound()).findFirst();
			if (unboundAlternative.isPresent()) {
				return ((IKeyBinding) unboundAlternative.get()).nmuk$getIndexInParent();
			}
		}
		return -2;
	}

	// gui only
	@SuppressWarnings("resource")
	private static boolean showToastIfExistingKeyIsUnbound(KeyBinding binding) {
		int index = getExistingKeyIsUnboundIndex(binding);
		if (index != -2) {
			MinecraftClient client = MinecraftClient.getInstance();
			boolean isMainKey = index == -1;
			Object[] args = new Object[isMainKey ? 0 : 1];
			if (!isMainKey) {
				args[0] = index;
			}
			ErrorMessageToast.show(client.getToastManager(), isMainKey ? ErrorMessageToast.Type.MAIN_KEY_UNBOUND : ErrorMessageToast.Type.CHILDREN_KEY_UNBOUND_TRANSLATION_KEY, args);
			return true;
		}
		return false;
	}

	// gui only
	private static void addEntryAsLastAlternativeEntry(List<Entry<?>> entries, IKeyBindingEntry addToEntry, int maxSearchRange, Entry<?> addEntry) {
		int entryIndexInEntries = getLastAlternativeEntryIndex(entries, addToEntry, maxSearchRange);
		if (entryIndexInEntries == -1) {
			// should never happen since the add button was pressend on an entry
			entryIndexInEntries = 0;
		}
		entries.add(entryIndexInEntries, addEntry);
	}

	// gui only
	private static int getLastAlternativeEntryIndex(List<Entry<?>> entries, IKeyBindingEntry addToEntry, int maxSearchRange) {
		for (int i = 0, entriesSize = entries.size(); i < entriesSize; i++) {
			if (entries.get(i) == addToEntry) {
				// found baseKeyBinding's entry
				// now search for insert position
				int fromIndex = i + 1;
				int toIndex = Math.min(entriesSize, fromIndex + maxSearchRange);
				int entryIndexInEntries = findKeyBindingEntryIndex(entries, fromIndex, toIndex, keyBindingEntry -> {
					return ((IKeyBinding) keyBindingEntry.nmuk$getBinding()).nmuk$getParent() != addToEntry.nmuk$getBinding();
				});
				// if it gets not be found that means we reached the search end (list size or upper limit index)
				if (entryIndexInEntries == -1) {
					// if toIndex was was not the list end
					// we stopped earlier because of the upper limit
					// in that case we return the upper limit. (is done implicit with toIndex. Because toIndex == entriesSize)

					// if it was the list end
					// we return the list end
					entryIndexInEntries = toIndex;
				}
				return entryIndexInEntries;
			}
		}
		return -1;
	}

	// fixed multi entries
	public static IKeyBindingEntry addNewAlternativeKeyBinding_OptionsScreen(KeyBinding baseKeyBinding, IControlsListWidget listWidget, IKeyBindingEntry entry) {
		if (showToastIfExistingKeyIsUnbound(baseKeyBinding)) {
			return null;
		}

		KeyBinding altBinding = getOrCreateAlternativeKeyBinding(baseKeyBinding);
		registerKeyBindingGUI(altBinding);
		// upper limit
		int baseKeyBindingAlternativeCount = ((IKeyBinding) baseKeyBinding).nmuk$getAlternativesCount();
		IKeyBindingEntry altEntry = KeyBindingEntryVersionHelper.createKeyBindingEntry(listWidget, altBinding, ENTRY_NAME);

		for (List<Entry<?>> entries : getControlsListWidgetEntries(listWidget)) {
			addEntryAsLastAlternativeEntry(entries, entry, baseKeyBindingAlternativeCount, (Entry<?>) altEntry);
		}
		return altEntry;
	}

	// fixed multi entries
	@SuppressWarnings("resource")
	public static void resetAlternativeKeyBindings_OptionsScreen(KeyBinding baseKeyBinding, IControlsListWidget listWidget, IKeyBindingEntry entry) {
		// TODO: if the just added entry is not visible in the screen (because scrolled too far up)
		// set the srollAmount so that is is visible

		List<KeyBinding> alternatives = ((IKeyBinding) baseKeyBinding).nmuk$getAlternatives();
		// we make a copy of the defaultAlternatives here because we remove some elements for calculation
		List<KeyBinding> defaultAlternatives = new ArrayList<>(NMUKKeyBindingHelper.defaultAlternatives.get(baseKeyBinding));

		List<KeyBinding> defaultAlternativesAlreadyKnown = Collections.emptyList();
		int alternativesSize = alternatives == null ? 0 : alternatives.size();
		boolean changed = false;
		if (alternativesSize > 0) {
			Iterator<KeyBinding> iterator = alternatives.iterator();
			while (iterator.hasNext()) {
				KeyBinding alternative = iterator.next();
				if (!defaultAlternatives.contains(alternative)) {
					// if alternative is not a default alternative
					// unregister it
					unregisterKeyBindingQuerying(alternative);
					unregisterKeyBindingGUI(alternative);
					iterator.remove();
					changed = true;
				}
			}

			// alternatives now only contains the default alternatives we already knew
			defaultAlternativesAlreadyKnown = new ArrayList<>(alternatives);
			// clear alternatives of base
			alternatives.clear();
		}

		// remove all alternative entries from gui
		List<Entry<?>>[] entries = getControlsListWidgetEntries(listWidget);
		@SuppressWarnings("unlikely-arg-type")
		EntryListWithIndex[] entriesWithIndex = EntryListWithIndex.createFromEntryLists(entries, (entryWithIndex) -> {
			entryWithIndex.index = entryWithIndex.entries.indexOf(entry) + 1;
			int entryIndexInEntries = getLastAlternativeEntryIndex(entryWithIndex.entries, entry, alternativesSize);
			entryWithIndex.entries.subList(entryWithIndex.index, entryIndexInEntries).clear();
		});

		for (KeyBinding defaultAlternative : defaultAlternatives) {
			IKeyBindingEntry newEntry = KeyBindingEntryVersionHelper.createKeyBindingEntry(listWidget, defaultAlternative, ENTRY_NAME);

			for (EntryListWithIndex entryWithIndex : entriesWithIndex) {
				entryWithIndex.entries.add(entryWithIndex.index++, (Entry<?>) newEntry);
			}

			resetSingleKeyBinding(defaultAlternative);
			((IKeyBinding) baseKeyBinding).nmuk$addAlternative(defaultAlternative);
			changed = true;
		}

		defaultAlternatives.removeAll(defaultAlternativesAlreadyKnown);
		registerKeyBindingsBoth(MinecraftClient.getInstance().options, defaultAlternatives);

		if (changed) {
			// do it like vanilla and save directly
			saveOptions();
		}
	}

	// gui only
	public static List<Entry<?>>[] getControlsListWidgetEntries(IControlsListWidget controlsList) {
		List<Entry<?>>[] ret = null;
		if (CompatibilityControlling.MOD_PRESENT) {
			ret = new List[2];
			ret[1] = (List<Entry<?>>) (Object) ((ICustomList) controlsList).nmuk$getAllEntries();
		} else {
			ret = new List[1];
		}
		ret[0] = ((IEntryListWidget) controlsList).nmuk$getChildren();
		return ret;
	}

}
