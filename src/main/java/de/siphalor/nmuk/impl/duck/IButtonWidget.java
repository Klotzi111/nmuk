package de.siphalor.nmuk.impl.duck;

import net.minecraft.client.gui.widget.ButtonWidget;

public interface IButtonWidget {

	/**
	 * This interface wraps {@link ButtonWidget.TooltipSupplier}.
	 * It is required for Minecraft verions lower than 1.16
	 *
	 */
	public static interface TooltipSupplier {
		/**
		 *
		 * @param button
		 * @param matrices (MatrixStack) should be {@code null} for Minecraft versions lower than 1.16. The type is Object because Minecraft versions lower than 1.15 do not have this class
		 * @param mouseX
		 * @param mouseY
		 */
		public void onTooltip(ButtonWidget button, /* MatrixStack */ Object matrices, int mouseX, int mouseY);
	}

	public static final TooltipSupplier EMPTY = (button, matrices, mouseX, mouseY) -> {
	};

	void setTooltipSupplier(TooltipSupplier tooltipSupplier);

	/**
	 * INTERNAL USE ONLY
	 *
	 * @return tooltipSupplier
	 */
	TooltipSupplier nmuk$getTooltipSupplier();

}
