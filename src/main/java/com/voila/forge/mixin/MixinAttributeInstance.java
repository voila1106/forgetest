package com.voila.forge.mixin;

import com.voila.forge.*;
import net.minecraft.world.effect.*;
import net.minecraft.world.entity.ai.attributes.*;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.*;

import java.util.*;

/** can ignore slowness effect */
@Mixin(AttributeInstance.class)
public class MixinAttributeInstance {
	@Inject(method = "addModifier", at = @At("HEAD"), cancellable = true)
	private void addModifier(AttributeModifier modifier, CallbackInfo info){
		UUID slowId = MobEffects.MOVEMENT_SLOWDOWN.getAttributeModifiers().get(Attributes.MOVEMENT_SPEED).getId();
		if(Forgetest.ignoreSlowness && modifier.getId().equals(slowId)){
			info.cancel();
		}
	}
}
