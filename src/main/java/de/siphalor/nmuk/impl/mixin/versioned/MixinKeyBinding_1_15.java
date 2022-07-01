package de.siphalor.nmuk.impl.mixin.versioned;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import de.siphalor.nmuk.impl.duck.IKeyBinding;
import net.minecraft.client.option.KeyBinding;

@Mixin(KeyBinding.class)
public abstract class MixinKeyBinding_1_15 implements IKeyBinding {

	@Override
	public void nmuk$setPressed(boolean pressed) {
		// intentionally call the method even though we could just set the field
		// to be more compatible with other mixins in that method
		((KeyBinding) (Object) this).setPressed(pressed);
	}

	@Inject(method = "setPressed", at = @At("HEAD"), cancellable = true)
	public void setPressedInjection(boolean pressed, CallbackInfo callbackInfo) {
		KeyBinding parent = nmuk$getParent();
		if (parent != null) {
			// set the parent pressed instead of the child key binding
			parent.setPressed(pressed);
			callbackInfo.cancel();
		}
	}

}
