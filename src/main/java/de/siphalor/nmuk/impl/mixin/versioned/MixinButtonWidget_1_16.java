package de.siphalor.nmuk.impl.mixin.versioned;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;

import de.siphalor.nmuk.impl.duck.IButtonWidget;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.util.math.MatrixStack;

@Mixin(ButtonWidget.class)
public abstract class MixinButtonWidget_1_16 implements IButtonWidget {

	@Shadow
	@Final
	@Mutable
	protected net.minecraft.client.gui.widget.ButtonWidget.TooltipSupplier tooltipSupplier;

	@Override
	public void setTooltipSupplier(TooltipSupplier tooltipSupplier) {
		this.tooltipSupplier = tooltipSupplier::onTooltip;
	}

	@Override
	public TooltipSupplier nmuk$getTooltipSupplier() {
		return (button, matrices, mouseX, mouseY) -> tooltipSupplier.onTooltip(button, (MatrixStack) matrices, mouseX, mouseY);
	}

}
