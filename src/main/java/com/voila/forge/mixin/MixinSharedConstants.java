package com.voila.forge.mixin;

import net.minecraft.*;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.*;

/** allow all characters */
@Mixin(SharedConstants.class)
public abstract class MixinSharedConstants
{
	@Inject(method = "isAllowedChatCharacter", at = @At("HEAD"), cancellable = true)
	private static void i(char character, CallbackInfoReturnable<Boolean> info){
		info.setReturnValue(true);
	}
}
