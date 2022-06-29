package com.voila.forge.mixin;

import net.minecraft.client.gui.screens.inventory.*;
import net.minecraft.network.chat.*;
import net.minecraft.world.effect.*;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.*;

/** show level when >10 */
@Mixin(EffectRenderingInventoryScreen.class)
public class MixinEffectName {
	@Inject(method = "getEffectName",at = @At("RETURN"),cancellable = true)
	private void getEffectName(MobEffectInstance effect, CallbackInfoReturnable<Component> info){
		if(effect.getAmplifier()>9){
			MutableComponent name= (MutableComponent)info.getReturnValue();
			name.append(" "+(effect.getAmplifier()+1));
			info.setReturnValue(name);
		}
	}
}
