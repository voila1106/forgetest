package com.voila.forge.mixin;

import com.mojang.blaze3d.vertex.*;
import com.voila.forge.*;
import net.minecraft.client.renderer.block.*;
import net.minecraft.core.*;
import net.minecraft.world.level.*;
import net.minecraft.world.level.block.state.*;
import net.minecraft.world.level.material.*;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.*;

/** Xray Fluid */
@Mixin(LiquidBlockRenderer.class)
public abstract class MixinFluidRenderer {
	@Inject(method = "tesselate", at = @At("HEAD"), cancellable = true)
	private void i(BlockAndTintGetter reader, BlockPos pos, VertexConsumer p_234372_, BlockState p_234373_, FluidState p_234374_, CallbackInfo info){
		//if block is not included, hide it.
		if(Keys.xray && !Keys.enabledBlocks.contains(reader.getBlockState(pos).getBlock())){
			info.cancel();
		}
	}

	/** render fluid sides */
	@Inject(method = "isFaceOccludedByState", at = @At("HEAD"), cancellable = true)
	private static void d(BlockGetter reader,
						  Direction direction,
						  float p_239284_2_,
						  BlockPos blockPos,
						  BlockState blockState,
						  CallbackInfoReturnable<Boolean> info){
		if(Keys.xray && !Keys.enabledBlocks.contains(reader.getBlockState(blockPos).getBlock()))
			info.setReturnValue(false);
	}
}
