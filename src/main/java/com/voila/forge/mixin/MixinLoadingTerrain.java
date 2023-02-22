package com.voila.forge.mixin;

import com.voila.forge.*;
import net.minecraft.client.gui.screens.*;
import net.minecraft.core.*;
import net.minecraft.network.chat.*;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.*;

/** remove 2s "loading terrain" */
@Mixin(ReceivingLevelScreen.class)
public abstract class MixinLoadingTerrain extends Screen {
	@Shadow private boolean oneTickSkipped;
	@Shadow @Final private long createdAt;
	@Shadow private boolean loadingPacketsReceived;

	private MixinLoadingTerrain(Component p_96550_){
		super(p_96550_);
	}

	@Rewrite
	@Inject(method = "tick", at = @At("HEAD"), cancellable = true)
	private void i(CallbackInfo info){
		info.cancel();

		boolean flag = this.oneTickSkipped || System.currentTimeMillis() > this.createdAt; //here
		if(flag && this.minecraft != null && this.minecraft.player != null){
			BlockPos blockpos = this.minecraft.player.blockPosition();
			boolean flag1 = this.minecraft.level != null && this.minecraft.level.isOutsideBuildHeight(blockpos.getY());
			if(flag1 || this.minecraft.levelRenderer.isChunkCompiled(blockpos)){
				this.onClose();
			}

			if(this.loadingPacketsReceived){
				this.oneTickSkipped = true;
			}

		}
	}
}

