package com.voila.forge.mixin;

import com.mojang.blaze3d.vertex.*;
import net.minecraft.*;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.entity.*;
import net.minecraft.network.chat.*;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.decoration.*;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.*;

/** show health */
@Mixin(EntityRenderer.class)
public abstract class MixinEntityRenderer {
	@Inject(method = "renderNameTag", at = @At("HEAD"))
	private void i(Entity entityIn, Component nameIn, PoseStack matrixStack, MultiBufferSource buffer, int packedLight, CallbackInfo ci){
		if(!(entityIn instanceof LivingEntity living) || !(nameIn instanceof MutableComponent name) || (entityIn instanceof ArmorStand))
			return;
		float health = living.getHealth();
		int maxHealth = (int)living.getMaxHealth();
		String healthText;
		if(health / maxHealth >= 0.66)
			healthText = " " + ChatFormatting.GREEN + String.format("%.2f", health) + " / " + maxHealth;
		else if(health / maxHealth >= 0.33)
			healthText = " " + ChatFormatting.GOLD + String.format("%.2f", health) + " / " + maxHealth;
		else
			healthText = " " + ChatFormatting.RED + String.format("%.2f", health) + " / " + maxHealth;
		name.append(Component.literal(healthText));
		nameIn = name;
	}
}
