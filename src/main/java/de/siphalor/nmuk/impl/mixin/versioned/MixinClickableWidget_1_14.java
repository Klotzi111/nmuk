package de.siphalor.nmuk.impl.mixin.versioned;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import de.siphalor.nmuk.impl.duck.IButtonWidget;
import de.siphalor.nmuk.impl.duck.IButtonWidget.TooltipSupplier;
import de.siphalor.nmuk.impl.duck.IClickableWidget;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.ClickableWidget;

// ClickableWidget is AbstractButtonWidget in mc version <= 1.15
// but it has the same intermediary so it should be fine
@Mixin(ClickableWidget.class)
public abstract class MixinClickableWidget_1_14 implements IClickableWidget {

	@Shadow(remap = false)
	public abstract void renderToolTip(int mouseX, int mouseY);

	@Shadow(remap = false)
	public abstract boolean isHovered();

	// {"method_25352", "renderToolTip"}, remap = false
	@Inject(method = "renderToolTip(II)V", at = @At("HEAD"), remap = false)
	public void inject_renderToolTip(int mouseX, int mouseY, CallbackInfo callbackInfo) {
		if (((Object) this) instanceof ButtonWidget) {
			TooltipSupplier tooltipSupplier = ((IButtonWidget) this).getTooltipSupplier();
			if (tooltipSupplier != null) {
				tooltipSupplier.onTooltip((ButtonWidget) (Object) this, null, mouseX, mouseY);
			}
		}
	}

	// @Inject(method = "render(IIF)V", at = @At("TAIL"))
	@Override
	public void nmuk$renderToolTipIfHovered(int mouseX, int mouseY) {
		if (isHovered()) {
			renderToolTip(mouseX, mouseY);
		}
	}

}
