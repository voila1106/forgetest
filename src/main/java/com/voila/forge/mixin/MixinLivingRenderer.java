package com.voila.forge.mixin;

import com.mojang.blaze3d.vertex.*;
import com.voila.forge.*;
import net.minecraft.client.*;
import net.minecraft.client.player.*;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.entity.*;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.decoration.*;
import net.minecraft.world.level.*;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.*;

import java.util.*;

/** perform damage amount particle */
@Mixin(LivingEntityRenderer.class)
public abstract class MixinLivingRenderer {
	private Map<Integer, Float> hs = new HashMap<>();
	private Level world;

	@fold
	@Inject(method = "render(Lnet/minecraft/world/entity/LivingEntity;FFLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;I)V", at = @At("HEAD"))
	private void r(LivingEntity entity, float yaw, float ticks, PoseStack stack, MultiBufferSource buffer, int light, CallbackInfo info){
		if(!entity.level.equals(world)){
			hs = new HashMap<>();
			world = entity.level;
		}
		float health = entity.getHealth();
		if(!hs.containsKey(entity.hashCode())){
			hs.put(entity.hashCode(), health);
			return;
		}
		if(health < hs.get(entity.hashCode())){
			float damage = (hs.get(entity.hashCode()) - health);
			for(int i = 0; i < Math.min(100, damage); i++){
				Minecraft.getInstance().levelRenderer.addParticle(new DamageParticle.DamageParticleData((int)damage),
					true,
					entity.getX(),
					entity.getY(0.5),
					entity.getZ(), 0.1, 0, 0.1);
			}
		}
		hs.put(entity.hashCode(), health);
	}

	/** health */
	@Inject(method = "shouldShowName(Lnet/minecraft/world/entity/LivingEntity;)Z", at = @At("HEAD"), cancellable = true)
	private void t(LivingEntity entity, CallbackInfoReturnable<Boolean> info){
		LocalPlayer player = Minecraft.getInstance().player;
		if(player == null)
			return;
		if(!(entity instanceof ArmorStand) && player.position().distanceTo(entity.position()) < Forgetest.getNameDistance()){
			info.setReturnValue(true);
		}
	}
}
