package de.siphalor.nmuk.mixin;

import net.minecraft.client.options.KeyBinding;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.Map;

@Mixin(KeyBinding.class)
public interface KeyBindingAccessor {
	@Accessor
	static Map<String, KeyBinding> getKeysById() {
		return null;
	}

	@Accessor
	int getTimesPressed();
	@Accessor
	void setTimesPressed(int timesPressed);
}
