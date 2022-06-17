package com.voila.forge.mixin;

import com.voila.forge.*;
import net.minecraft.world.entity.player.*;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.*;

@Mixin(Player.class)
public abstract class MixinPlayer {
	@Inject(method = "isScoping",at = @At("HEAD"), cancellable = true)
	private void scoping(CallbackInfoReturnable<Boolean> info){
		if(Keys.scoping){
			info.setReturnValue(true);
		}
	}
}
