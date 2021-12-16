package de.siphalor.nmuk.impl.mixin.versioned;

import java.util.List;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import de.siphalor.nmuk.impl.duck.IClickableWidget;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ClickableWidget;

// we currently not even use what this class adds

// this is only required for mc versions <= 15 because they do not have the tooltip supplier
@Mixin(Screen.class)
public abstract class MixinScreen_1_14 implements IClickableWidget {

	@Shadow(remap = false)
	@Final
	protected List<ClickableWidget> buttons;

	@Inject(method = "render(IIF)V", at = @At("TAIL"), remap = false)
	public void render(int mouseX, int mouseY, float delta, CallbackInfo callbackInfo) {
		for (int i = 0; i < buttons.size(); ++i) {
			((IClickableWidget) buttons.get(i)).nmuk$renderToolTipIfHovered(mouseX, mouseY);
		}
	}

}
