package de.siphalor.nmuk.impl.mixinimpl;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.List;

import org.apache.logging.log4j.Level;

import de.klotzi111.fabricmultiversionhelper.api.mapping.MappingHelper;
import de.klotzi111.fabricmultiversionhelper.api.version.MinecraftVersionHelper;
import de.siphalor.nmuk.impl.NMUK;
import de.siphalor.nmuk.impl.NMUKKeyBindingHelper;
import de.siphalor.nmuk.impl.duck.IButtonWidget;
import de.siphalor.nmuk.impl.duck.IControlsListWidget;
import de.siphalor.nmuk.impl.duck.IKeyBinding;
import de.siphalor.nmuk.impl.duck.IKeyBindingEntry;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.ButtonWidget.PressAction;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;

public class MixinKeyBindingEntryImpl {

	private static final Method AbstractButtonWidget_render;
	private static final Constructor<ButtonWidget> ButtonWidget_constructor;

	static {
		if (!MinecraftVersionHelper.isMCVersionAtLeast("1.16")) {
			String CLASS_NAME_AbstractButtonWidget = "net.minecraft.class_339"; // "net.minecraft.client.gui.widget.AbstractButtonWidget";
			Class<?> AbstractButtonWidget_class = MappingHelper.mapAndLoadClass(CLASS_NAME_AbstractButtonWidget, MappingHelper.CLASS_MAPPER_FUNCTION);

			String INTERMEDIARY_AbstractButtonWidget_render = "render"; // NOT "method_25424" because this old ( <= 1.15) have mappings that directly map to yarn mappings
			String SIGNATURE_AbstractButtonWidget_render = "(IIF)V";
			String REMAPPED_AbstractButtonWidget_render = MappingHelper.mapMethod(CLASS_NAME_AbstractButtonWidget, INTERMEDIARY_AbstractButtonWidget_render, SIGNATURE_AbstractButtonWidget_render);
			AbstractButtonWidget_render = MappingHelper.getMethod(AbstractButtonWidget_class, REMAPPED_AbstractButtonWidget_render, SIGNATURE_AbstractButtonWidget_render);
		} else {
			AbstractButtonWidget_render = null;
		}

		String SIGNATURE_ButtonWidget_constructor = null;
		if (MinecraftVersionHelper.isMCVersionAtLeast("1.16")) {
			SIGNATURE_ButtonWidget_constructor = MappingHelper.createSignature("(IIII%s%s)V", Text.class, PressAction.class);
		} else {
			SIGNATURE_ButtonWidget_constructor = MappingHelper.createSignature("(IIII%s%s)V", String.class, PressAction.class);
		}
		ButtonWidget_constructor = (Constructor<ButtonWidget>) MappingHelper.getConstructor(ButtonWidget.class, SIGNATURE_ButtonWidget_constructor);
	}

	public static ButtonWidget createButtonWidget(int x, int y, int width, int height, Text message, PressAction onPress) {
		try {
			Object[] instanceArgs = new Object[] {x, y, width, height, message, onPress};
			if (!MinecraftVersionHelper.isMCVersionAtLeast("1.16")) {
				instanceArgs[4] = message.getString();
			}
			return ButtonWidget_constructor.newInstance(instanceArgs);
		} catch (IllegalAccessException | InstantiationException | InvocationTargetException e) {
			NMUK.log(Level.ERROR, "Failed to create new instance of \"ButtonWidget\"");
			NMUK.logException(Level.ERROR, e);
		}
		return null;
	}

	public static void init(IKeyBindingEntry _this, IControlsListWidget outer, KeyBinding binding) {
		IKeyBinding iKeyBinding = (IKeyBinding) binding;
		if (iKeyBinding.nmuk$isAlternative()) {
			_this.nmuk$setBindingName(NMUKKeyBindingHelper.ENTRY_NAME);
			_this.nmuk$setAlternativesButton(createButtonWidget(0, 0, 20, 20, NMUKKeyBindingHelper.REMOVE_ALTERNATIVE_TEXT, button -> {
				NMUKKeyBindingHelper.removeAlternativeKeyBinding_OptionsScreen(binding, outer);
			}));
		} else {
			_this.nmuk$setAlternativesButton(createButtonWidget(0, 0, 20, 20, NMUKKeyBindingHelper.ADD_ALTERNATIVE_TEXT, button -> {
				IKeyBindingEntry newAltEntry = NMUKKeyBindingHelper.addNewAlternativeKeyBinding_OptionsScreen(binding, outer, _this);
				if (newAltEntry != null) {
					newAltEntry.nmuk$getEditButton().onPress();
				}
			}));
		}
	}

	public static void render(IKeyBindingEntry _this, MatrixStack matrices, int index, int y, int x, int entryWidth, int entryHeight, int mouseX, int mouseY, boolean hovered, float tickDelta) {
		ButtonWidget alternativesButton = _this.nmuk$getAlternativesButton();
		ButtonWidget resetButton = _this.nmuk$getResetButton();

		alternativesButton.y = resetButton.y;
		alternativesButton.x = resetButton.x + resetButton.getWidth() + 10;

		if (MinecraftVersionHelper.isMCVersionAtLeast("1.16")) {
			alternativesButton.render(matrices, mouseX, mouseY, tickDelta);
		} else {
			try {
				AbstractButtonWidget_render.invoke(alternativesButton, mouseX, mouseY, tickDelta);
			} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
				NMUK.log(Level.ERROR, "Failed to call method \"render\"");
				NMUK.logException(Level.ERROR, e);
			}
		}
	}

	public static void setResetButtonActive(IKeyBindingEntry _this, IControlsListWidget listWidget, KeyBinding binding) {
		IKeyBinding iKeyBinding = (IKeyBinding) binding;
		ButtonWidget resetButton = _this.nmuk$getResetButton();

		if (!iKeyBinding.nmuk$isAlternative()) {
			if (resetButton.active) {
				((IButtonWidget) resetButton).setTooltipSupplier((button, matrices, mouseX, mouseY) -> {
					MixinScreenHelper.renderTooltip(listWidget.nmuk$getParent(), matrices, NMUKKeyBindingHelper.RESET_TOOLTIP, mouseX, mouseY);
				});
			} else {
				((IButtonWidget) resetButton).setTooltipSupplier(IButtonWidget.EMPTY);
			}
		}
	}

	public static void resetButtonPressed(IKeyBindingEntry _this, KeyBinding keyBinding, ButtonWidget widget, IControlsListWidget listWidget) {
		if (((IKeyBinding) keyBinding).nmuk$getParent() == null && Screen.hasShiftDown()) {
			NMUKKeyBindingHelper.resetAlternativeKeyBindings_OptionsScreen(keyBinding, listWidget, _this);
		}
	}

	public static boolean isDefaultOnRender(IKeyBindingEntry _this, KeyBinding keyBinding) {
		IKeyBinding iKeyBinding = (IKeyBinding) keyBinding;
		if (iKeyBinding.nmuk$getParent() == null) {
			Collection<KeyBinding> defaults = NMUKKeyBindingHelper.defaultAlternatives.get(keyBinding);
			int childrenCount = iKeyBinding.nmuk$getAlternativesCount();

			if (defaults.size() == childrenCount) {
				List<KeyBinding> children = iKeyBinding.nmuk$getAlternatives();
				if (childrenCount > 0) {
					for (KeyBinding child : children) {
						if (!defaults.contains(child)) {
							return false;
						}
						if (!child.isDefault()) {
							return false;
						}
					}
				}
			} else {
				return false;
			}
		} else {
			if (keyBinding.getDefaultKey().equals(InputUtil.UNKNOWN_KEY)) {
				return true;
			}
		}
		return keyBinding.isDefault();
	}

}
