package com.voila.forge.mixin;

import com.mojang.blaze3d.vertex.*;
import com.mojang.math.*;
import com.voila.forge.*;
import net.minecraft.client.*;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.culling.*;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.*;

@Mixin(LevelRenderer.class)
public abstract class MixinLevelRenderer {
	/** force spectator to render all blocks when xray */
	@fold
	@ModifyArg(method = "renderLevel", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/LevelRenderer;setupRender(Lnet/minecraft/client/Camera;Lnet/minecraft/client/renderer/culling/Frustum;ZZ)V"), index = 3)
	private boolean d(Camera p_194339_, Frustum p_194340_, boolean p_194341_, boolean playerSpectator){
		if(Keys.xray)
			return true;
		return playerSpectator;
	}

	/** render spheres */
	@fold
	@Inject(method = "renderLevel", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/systems/RenderSystem;getModelViewStack()Lcom/mojang/blaze3d/vertex/PoseStack;", shift = At.Shift.BEFORE))
	private void line(PoseStack stack,
					  float partialTicks,
					  long finishTime,
					  boolean drawBlockOutline,
					  Camera camera,
					  GameRenderer gameRenderer,
					  LightTexture lightmap,
					  Matrix4f projection,
					  CallbackInfo info){
		Forgetest.shapes.forEach((shape, pos) -> Forgetest.renderShape(stack, shape, pos.x, pos.y, pos.z));
	}
}
