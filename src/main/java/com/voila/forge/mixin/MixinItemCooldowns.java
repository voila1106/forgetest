package com.voila.forge.mixin;

import net.minecraft.world.item.*;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.*;

@Mixin(ItemCooldowns.class)
public class MixinItemCooldowns {
	@Inject(method = "addCooldown", at = @At("HEAD"), cancellable = true)
	private void addCooldown(Item p_41525_, int p_41526_, CallbackInfo info){
		info.cancel();
	}
}
