package de.siphalor.nmuk.impl.duck;

import java.util.List;

import net.minecraft.client.gui.widget.EntryListWidget;

public interface IEntryListWidget {
	List<EntryListWidget.Entry<?>> getChildren();
}
