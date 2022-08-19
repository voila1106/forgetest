package com.voila.forge.mixin;

import net.minecraft.client.renderer.blockentity.*;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;

/** glowing signs fix */
@Mixin(SignRenderer.class)
public class MixinSignRenderer {
	@ModifyVariable(method = "render(Lnet/minecraft/world/level/block/entity/SignBlockEntity;FLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;II)V",
		at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/Font;width(Lnet/minecraft/util/FormattedCharSequence;)I"), index = 18)
	private boolean flag(boolean value){
		return false;
	}
}
