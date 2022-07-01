package de.siphalor.nmuk.impl.transform;

import de.klotzi111.fabricmultiversionhelper.api.version.MinecraftVersionHelper;
import de.klotzi111.transformerinterceptor.api.basic.BasicTransformerInterceptor;
import de.klotzi111.transformerinterceptor.api.entrypoint.PreMixinLoadEntrypoint;
import de.klotzi111.transformerinterceptor.api.raw.RawTransformerInterceptor;
import de.klotzi111.transformerinterceptor.api.util.PriorityMapHelper;

public class TransformationEntryPoint implements PreMixinLoadEntrypoint {

	@Override
	public void preMixinLoad() {
		if (!MinecraftVersionHelper.isMCVersionAtLeast("1.16")) {
			// register the class transformer listener after default actions
			ReplaceNewerVersionAccessNamesClassTransformer transformer = new ReplaceNewerVersionAccessNamesClassTransformer();
			// raw for mixin classes themselves
			PriorityMapHelper.addSafe(RawTransformerInterceptor.CLASS_TRANSFORMERS, -1000, transformer);
			// basic still required for non mixins to be persistent
			PriorityMapHelper.addSafe(BasicTransformerInterceptor.CLASS_TRANSFORMERS, -1000, transformer);
		}
	}

}
