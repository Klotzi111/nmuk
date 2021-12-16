package de.siphalor.nmuk.impl.duck;

import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;

public interface IKeyBindingEntry {
	void setBindingName(Text bindingName);

	void setAlternativesButton(ButtonWidget alternativesButton);

	ButtonWidget getAlternativesButton();

	ButtonWidget getResetButton();
}
