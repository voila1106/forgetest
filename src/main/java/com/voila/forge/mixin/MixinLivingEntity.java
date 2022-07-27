package com.voila.forge.mixin;

import com.voila.forge.*;
import net.minecraft.client.*;
import net.minecraft.client.player.*;
import net.minecraft.network.syncher.*;
import net.minecraft.world.effect.*;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.decoration.*;
import net.minecraft.world.level.*;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.*;

@Mixin(LivingEntity.class)
public abstract class MixinLivingEntity extends Entity {
	@Shadow @Final private static EntityDataAccessor<Integer> DATA_EFFECT_COLOR_ID;
	private final LivingEntity _this=(LivingEntity)(Object)this;
	private MixinLivingEntity(EntityType<?> p_i48580_1_, Level p_i48580_2_){
		super(p_i48580_1_, p_i48580_2_);
	}

	/** ignore some effects */
	@Inject(method = "hasEffect", at = @At("HEAD"), cancellable = true)
	private void hasEffect(MobEffect effect, CallbackInfoReturnable<Boolean> info){
		if(effect == MobEffects.BLINDNESS || effect == MobEffects.DARKNESS || effect == MobEffects.CONFUSION)
			info.setReturnValue(false);
	}

	/** always render name tag to display health value (CustomNameVisible) */
	@Inject(method = "shouldShowName", at = @At("HEAD"), cancellable = true)
	private void shouldShowName(CallbackInfoReturnable<Boolean> info){
		LocalPlayer player = Minecraft.getInstance().player;
		if(player == null)
			return;
		if(!(_this instanceof ArmorStand) && position().distanceTo(player.position()) < Forgetest.getNameDistance())
			info.setReturnValue(true);
	}

	/** remove potion particle */
	@Inject(method = "tickEffects", at = @At("HEAD"))
	private void tickEffects(CallbackInfo info){
		if(_this == Minecraft.getInstance().player)
			entityData.set(DATA_EFFECT_COLOR_ID,0);
	}
}
