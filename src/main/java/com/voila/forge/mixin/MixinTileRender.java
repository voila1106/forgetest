package com.voila.forge.mixin;

import com.mojang.blaze3d.vertex.*;
import com.voila.forge.*;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.blockentity.*;
import net.minecraft.world.level.block.entity.*;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.*;

import javax.annotation.*;

/** Xray Tile Entity */
@Mixin(BlockEntityRenderDispatcher.class)
public abstract class MixinTileRender {
	@Shadow
	@Nullable
	public abstract <E extends BlockEntity> BlockEntityRenderer<E> getRenderer(E tileEntityIn);

	@Shadow
	private static void tryRender(BlockEntity tileEntityIn, Runnable runnableIn){
	}

	@Shadow
	private static <T extends BlockEntity> void setupAndRender(BlockEntityRenderer<T> p_112285_, T p_112286_, float p_112287_, PoseStack p_112288_, MultiBufferSource p_112289_){
	}

	@Inject(method = "setupAndRender", at = @At("HEAD"), cancellable = true)
	private static <T extends BlockEntity> void i(BlockEntityRenderer<T> tileRenderer, T tileEntity, float p_112287_, PoseStack p_112288_, MultiBufferSource p_112289_, CallbackInfo info){
		if(Keys.xray && !Keys.enabledBlocks.contains(tileEntity.getBlockState().getBlock())){
			info.cancel();
		}
	}

	/** ignore render distance when xray enabled */
	@Rewrite
	@Inject(method = "render", at = @At("HEAD"), cancellable = true)
	private <E extends BlockEntity> void t(E tileEntityIn, float partialTicks, PoseStack matrixStackIn, MultiBufferSource bufferIn, CallbackInfo info){
		if(Keys.xray){
			info.cancel();
			BlockEntityRenderer<E> tileentityrenderer = this.getRenderer(tileEntityIn);
			if(tileentityrenderer != null){
				if(tileEntityIn.hasLevel() && tileEntityIn.getType().isValid(tileEntityIn.getBlockState())){
					tryRender(tileEntityIn, () ->  //here  no shouldRender() condition
						setupAndRender(tileentityrenderer, tileEntityIn, partialTicks, matrixStackIn, bufferIn));
				}
			}

		}
	}
}
