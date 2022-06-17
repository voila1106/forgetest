package com.voila.forge.mixin;

import com.voila.forge.*;
import net.minecraft.client.*;
import net.minecraft.client.player.*;
import net.minecraft.world.effect.*;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.decoration.*;
import net.minecraft.world.level.*;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.*;

@Mixin(LivingEntity.class)
public abstract class MixinLivingEntity extends Entity {
	private MixinLivingEntity(EntityType<?> p_i48580_1_, Level p_i48580_2_){
		super(p_i48580_1_, p_i48580_2_);
	}

	/** ignore blindness effect */
	@Inject(method = "hasEffect", at = @At("HEAD"), cancellable = true)
	private void i(MobEffect effect, CallbackInfoReturnable<Boolean> info){
		if(effect == MobEffects.BLINDNESS)
			info.setReturnValue(false);
	}

	/** always render name tag to display health value (CustomNameVisible) */
	@Inject(method = "shouldShowName", at = @At("HEAD"), cancellable = true)
	private void t(CallbackInfoReturnable<Boolean> info){
		LocalPlayer player = Minecraft.getInstance().player;
		if(player == null)
			return;
		if(!((Object)this instanceof ArmorStand) && position().distanceTo(player.position()) < Forgetest.getNameDistance())
			info.setReturnValue(true);
	}
}
