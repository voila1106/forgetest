package com.voila.forge.mixin;

import com.mojang.blaze3d.platform.*;
import com.voila.forge.*;
import net.minecraft.client.*;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.*;

/** script press key */
@Mixin(KeyMapping.class)
public abstract class MixinKeyMapping {
	/** Optifine zoom key */
	@Inject(method = "<init>(Ljava/lang/String;Lcom/mojang/blaze3d/platform/InputConstants$Type;ILjava/lang/String;)V", at = @At("RETURN"))
	private void reg(String name, InputConstants.Type p_90826_, int p_90827_, String p_90828_, CallbackInfo info){
		KeyMapping k = (KeyMapping)(Object)this;
		if(name.equals("of.key.zoom")){
			Forgetest.ofZoom = k;
		}
	}
}
