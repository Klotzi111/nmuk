package de.siphalor.nmuk.impl.mixinimpl;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.apache.logging.log4j.Level;

import de.siphalor.nmuk.impl.NMUK;
import de.siphalor.nmuk.impl.NMUKKeyBindingHelper;
import de.siphalor.nmuk.impl.duck.IButtonWidget;
import de.siphalor.nmuk.impl.duck.IControlsListWidget;
import de.siphalor.nmuk.impl.duck.IKeyBinding;
import de.siphalor.nmuk.impl.duck.IKeyBindingEntry;
import de.siphalor.nmuk.impl.mapping.MappingHelper;
import de.siphalor.nmuk.impl.mixin.KeyBindingEntryAccessor;
import de.siphalor.nmuk.impl.version.MinecraftVersionHelper;
import net.minecraft.client.gui.screen.option.ControlsListWidget;
import net.minecraft.client.gui.screen.option.ControlsListWidget.KeyBindingEntry;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.ButtonWidget.PressAction;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;

@SuppressWarnings("unchecked")
public class MixinKeyBindingEntryImpl {

	private static final Method AbstractButtonWidget_render;
	private static final Constructor<ButtonWidget> ButtonWidget_constructor;

	private static final String ABSTRACTBUTTONWIDGET_CLASS_NAME = "net.minecraft.class_339"; // "net.minecraft.client.gui.widget.AbstractButtonWidget";
	private static final String INTERMEDIARY_AbstractButtonWidget_render = "render"; // NOT "method_25424" because this old ( <= 1.15) have mappings that directly map to yarn mappings
	private static final String SIGNATURE_AbstractButtonWidget_render = "(IIF)V";
	private static String REMAPPED_AbstractButtonWidget_render;

	private static final String SIGNATURE_ButtonWidget_constructor_1_14 = MappingHelper.createSignature("(IIII%s%s)V", String.class, PressAction.class);
	private static final String SIGNATURE_ButtonWidget_constructor_1_16 = MappingHelper.createSignature("(IIII%s%s)V", Text.class, PressAction.class);

	private static void resolveIntermediaryNames() {
		REMAPPED_AbstractButtonWidget_render = MappingHelper.mapMethod(ABSTRACTBUTTONWIDGET_CLASS_NAME, INTERMEDIARY_AbstractButtonWidget_render, SIGNATURE_AbstractButtonWidget_render);
	}

	static {
		if (!MinecraftVersionHelper.IS_AT_LEAST_V1_16) {
			resolveIntermediaryNames();

			Class<?> AbstractButtonWidget_class = MappingHelper.mapAndLoadClass(ABSTRACTBUTTONWIDGET_CLASS_NAME, MappingHelper.CLASS_MAPPER_FUNCTION);
			AbstractButtonWidget_render = MappingHelper.getMethod(AbstractButtonWidget_class, REMAPPED_AbstractButtonWidget_render, SIGNATURE_AbstractButtonWidget_render);
		} else {
			AbstractButtonWidget_render = null;
		}

		String signature = SIGNATURE_ButtonWidget_constructor_1_14;
		if (MinecraftVersionHelper.IS_AT_LEAST_V1_16) {
			signature = SIGNATURE_ButtonWidget_constructor_1_16;
		}
		ButtonWidget_constructor = (Constructor<ButtonWidget>) MappingHelper.getConstructor(ButtonWidget.class, signature);
	}

	public static ButtonWidget createButtonWidget(int x, int y, int width, int height, Text message, PressAction onPress) {
		try {
			Object[] instanceArgs = new Object[] {x, y, width, height, message, onPress};
			if (!MinecraftVersionHelper.IS_AT_LEAST_V1_16) {
				instanceArgs[4] = message.asString();
			}
			return ButtonWidget_constructor.newInstance(instanceArgs);
		} catch (IllegalAccessException | InstantiationException | InvocationTargetException e) {
			NMUK.log(Level.ERROR, "Failed to create new instance of \"ButtonWidget\"");
			e.printStackTrace();
		}
		return null;
	}

	public static void init(KeyBindingEntry _this, ControlsListWidget outer, KeyBinding binding) {
		IKeyBinding iKeyBinding = (IKeyBinding) binding;
		IKeyBindingEntry _thisI = (IKeyBindingEntry) _this;
		if (iKeyBinding.nmuk_isAlternative()) {
			_thisI.setBindingName(NMUKKeyBindingHelper.ENTRY_NAME);
			_thisI.setAlternativesButton(createButtonWidget(0, 0, 20, 20, NMUKKeyBindingHelper.REMOVE_ALTERNATIVE_TEXT, button -> {
				NMUKKeyBindingHelper.removeAlternativeKeyBinding_OptionsScreen(binding, outer, _this);
			}));
		} else {
			_thisI.setAlternativesButton(createButtonWidget(0, 0, 20, 20, NMUKKeyBindingHelper.ADD_ALTERNATIVE_TEXT, button -> {
				ControlsListWidget.KeyBindingEntry newAltEntry = NMUKKeyBindingHelper.addNewAlternativeKeyBinding_OptionsScreen(binding, outer, _this);
				if (newAltEntry != null) {
					((KeyBindingEntryAccessor) newAltEntry).getEditButton().onPress();
				}
			}));
		}
	}

	public static void render(KeyBindingEntry _this, MatrixStack matrices, int index, int y, int x, int entryWidth, int entryHeight, int mouseX, int mouseY, boolean hovered, float tickDelta) {
		IKeyBindingEntry _thisI = (IKeyBindingEntry) _this;
		ButtonWidget alternativesButton = _thisI.getAlternativesButton();
		ButtonWidget resetButton = _thisI.getResetButton();

		alternativesButton.y = resetButton.y;
		alternativesButton.x = resetButton.x + resetButton.getWidth() + 10;

		if (MinecraftVersionHelper.IS_AT_LEAST_V1_16) {
			alternativesButton.render(matrices, mouseX, mouseY, tickDelta);
		} else {
			try {
				AbstractButtonWidget_render.invoke(alternativesButton, mouseX, mouseY, tickDelta);
			} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
				NMUK.log(Level.ERROR, "Failed to call method \"render\"");
				e.printStackTrace();
			}
		}
	}

	public static void setResetButtonActive(KeyBindingEntry _this, ControlsListWidget listWidget, KeyBinding binding) {
		IKeyBindingEntry _thisI = (IKeyBindingEntry) _this;
		IKeyBinding iKeyBinding = (IKeyBinding) binding;
		ButtonWidget resetButton = _thisI.getResetButton();

		if (!iKeyBinding.nmuk_isAlternative()) {
			if (resetButton.active) {
				((IButtonWidget) resetButton).setTooltipSupplier((button, matrices, mouseX, mouseY) -> {
					MixinScreenHelper.renderTooltip(((IControlsListWidget) listWidget).nmuk_getParent(), matrices, NMUKKeyBindingHelper.RESET_TOOLTIP, mouseX, mouseY);
				});
			} else {
				((IButtonWidget) resetButton).setTooltipSupplier(IButtonWidget.EMPTY);
			}
		}
	}

}