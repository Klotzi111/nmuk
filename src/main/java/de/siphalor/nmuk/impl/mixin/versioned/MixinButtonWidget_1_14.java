package de.siphalor.nmuk.impl.mixin.versioned;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

import de.siphalor.nmuk.impl.duck.IButtonWidget;
import net.minecraft.client.gui.widget.ButtonWidget;

// ClickableWidget is AbstractButtonWidget in mc version <= 1.15
// but it has the same intermediary so it should be fine
@Mixin(ButtonWidget.class)
public abstract class MixinButtonWidget_1_14 implements IButtonWidget {

	@Unique
	private TooltipSupplier tooltipSupplier;

	@Override
	public void setTooltipSupplier(TooltipSupplier tooltipSupplier) {
		this.tooltipSupplier = tooltipSupplier;
	}

	@Override
	public TooltipSupplier nmuk$getTooltipSupplier() {
		return tooltipSupplier;
	}

}
