package de.siphalor.nmuk.impl.mixinimpl;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.apache.logging.log4j.Level;

import de.siphalor.nmuk.impl.NMUK;
import de.siphalor.nmuk.impl.mapping.MappingHelper;
import de.siphalor.nmuk.impl.version.MinecraftVersionHelper;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;

public class MixinScreenHelper {

	private static final Method Screen_renderTooltip;

	private static final String INTERMEDIARY_Screen_renderTooltip = "renderTooltip"; // NOT "method_25424" because this old ( <= 1.15) have mappings that directly map to yarn mappings
	private static final String SIGNATURE_Screen_renderTooltip = MappingHelper.createSignature("(%sII)V", String.class);
	private static String REMAPPED_Screen_renderTooltip;

	private static void resolveIntermediaryNames() {
		REMAPPED_Screen_renderTooltip = MappingHelper.mapMethod(Screen.class.getName(), INTERMEDIARY_Screen_renderTooltip, SIGNATURE_Screen_renderTooltip);
	}

	static {
		if (!MinecraftVersionHelper.IS_AT_LEAST_V1_16) {
			resolveIntermediaryNames();

			Screen_renderTooltip = MappingHelper.getMethod(Screen.class, REMAPPED_Screen_renderTooltip, SIGNATURE_Screen_renderTooltip);
		} else {
			Screen_renderTooltip = null;
		}
	}

	public static void renderTooltip(Screen screen, /* MatrixStack */ Object matrices, Text text, int x, int y) {
		if (MinecraftVersionHelper.IS_AT_LEAST_V1_16) {
			screen.renderTooltip((MatrixStack) matrices, text, x, y);
		} else {
			try {
				Screen_renderTooltip.invoke(screen, text.getString(), x, y);
			} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
				NMUK.log(Level.ERROR, "Failed to call method \"renderTooltip\"");
				e.printStackTrace();
			}
		}
	}

}
