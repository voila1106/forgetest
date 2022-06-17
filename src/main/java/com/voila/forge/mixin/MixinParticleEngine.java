package com.voila.forge.mixin;

import net.minecraft.client.particle.*;
import net.minecraft.core.particles.*;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.*;

/** remove some particles */
@Mixin(ParticleEngine.class)
public abstract class MixinParticleEngine {
	@Inject(method = "createParticle", at = @At("HEAD"), cancellable = true)
	private void i(ParticleOptions particleData,
				   double x,
				   double y,
				   double z,
				   double xSpeed,
				   double ySpeed,
				   double zSpeed,
				   CallbackInfoReturnable<Particle> info){
		if(particleData.equals(ParticleTypes.DAMAGE_INDICATOR) || particleData.equals(ParticleTypes.ELDER_GUARDIAN))
			info.setReturnValue(null);
	}
}
