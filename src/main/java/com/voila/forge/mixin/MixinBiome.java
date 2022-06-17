package com.voila.forge.mixin;

import net.minecraft.world.level.biome.*;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.*;

/** never raining */
@Mixin(Biome.class)
public abstract class MixinBiome {
	@Inject(method = "getPrecipitation", at = @At("HEAD"), cancellable = true)
	private void i(CallbackInfoReturnable<Biome.Precipitation> info){
		info.setReturnValue(Biome.Precipitation.NONE);
	}
}
