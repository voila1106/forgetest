package com.voila.forge.mixin;

import com.voila.forge.*;
import net.minecraft.core.*;
import net.minecraft.world.level.*;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.*;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.*;

/** Xray block */
@Mixin(BlockBehaviour.BlockStateBase.class)
public abstract class MixinBlockState {
	@Shadow
	public abstract Block getBlock();

	@Inject(method = "getRenderShape", at = @At("HEAD"), cancellable = true)
	private void i(CallbackInfoReturnable<RenderShape> info){
		if(Keys.xray && !Keys.enabledBlocks.contains(getBlock()))
			info.setReturnValue(RenderShape.INVISIBLE);
	}

	@Inject(method = "getLightEmission", at = @At("HEAD"), cancellable = true)
	private void light(CallbackInfoReturnable<Integer> info){
		if(Keys.xray)
			info.setReturnValue(15);
	}

	/** force solid block to render all blocks when xray */
	@Inject(method = "isSolidRender", at = @At("HEAD"), cancellable = true)
	private void opaque(BlockGetter reader, BlockPos pos, CallbackInfoReturnable<Boolean> info){
		if(Keys.xray)
			info.setReturnValue(true);
	}
}
