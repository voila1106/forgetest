package com.voila.forge.mixin;

import com.mojang.blaze3d.vertex.*;
import net.minecraft.client.*;
import net.minecraft.client.player.*;
import net.minecraft.client.renderer.*;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.*;

/** disable damage screen shake */
@Mixin(GameRenderer.class)
public abstract class MixinGameRenderer {
	@Shadow @Final private Minecraft minecraft;

	@Shadow private float fov;

	@Shadow private float oldFov;

	@Inject(method = "bobHurt", at = @At(value = "FIELD", target = "Lnet/minecraft/world/entity/LivingEntity;hurtDir:F"), cancellable = true)
	private void hurt(PoseStack matrix, float ticks, CallbackInfo ci){
		ci.cancel();
	}

	/** remove fov range check */
	@Inject(method = "tickFov",at = @At("HEAD"),cancellable = true)
	private void tickFov(CallbackInfo info){
		info.cancel();

		float f = 1.0F;
		if (this.minecraft.getCameraEntity() instanceof AbstractClientPlayer abstractclientplayer) {
			f = abstractclientplayer.getFieldOfViewModifier();
		}

		this.oldFov = this.fov;
		this.fov += (f - this.fov) * 0.5F;
	}
}
