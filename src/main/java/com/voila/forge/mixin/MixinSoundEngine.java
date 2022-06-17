package com.voila.forge.mixin;

import net.minecraft.client.resources.sounds.*;
import net.minecraft.client.sounds.*;
import net.minecraft.sounds.*;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.*;

/** disable portal sound */
@Mixin(SoundEngine.class)
public abstract class MixinSoundEngine {
	@Inject(method = "play", at = @At("HEAD"), cancellable = true)
	private void i(SoundInstance sound, CallbackInfo info){
		if(sound.getLocation().equals(SoundEvents.PORTAL_AMBIENT.getLocation()))
			info.cancel();
	}
}
