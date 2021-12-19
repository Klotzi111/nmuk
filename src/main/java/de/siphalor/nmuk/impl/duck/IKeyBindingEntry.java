package de.siphalor.nmuk.impl.duck;

import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.text.Text;

public interface IKeyBindingEntry {
	void nmuk$setBindingName(Text bindingName);

	Text nmuk$getBindingName();

	void nmuk$setAlternativesButton(ButtonWidget alternativesButton);

	ButtonWidget nmuk$getAlternativesButton();

	ButtonWidget nmuk$getResetButton();

	KeyBinding nmuk$getBinding();

	ButtonWidget nmuk$getEditButton();
}
