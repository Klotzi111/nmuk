package de.siphalor.nmuk.impl.mixin.versioned;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import de.siphalor.nmuk.impl.duck.IKeyBinding;
import net.minecraft.client.option.KeyBinding;

// this mixin adds features missing in mc 1.14 (only)
@Mixin(KeyBinding.class)
public abstract class MixinKeyBinding_1_14 implements IKeyBinding {

	@Shadow
	private boolean pressed;

	@Override
	public void nmuk$setPressed(boolean pressed) {
		this.pressed = pressed;
	}

}
