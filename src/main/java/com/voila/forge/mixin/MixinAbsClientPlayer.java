package com.voila.forge.mixin;

import com.mojang.authlib.*;
import com.voila.forge.*;
import net.minecraft.client.player.*;
import net.minecraft.core.*;
import net.minecraft.world.entity.player.*;
import net.minecraft.world.item.*;
import net.minecraft.world.level.*;
import org.jetbrains.annotations.*;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.*;

/** speed doesn't effect fov */
@Mixin(AbstractClientPlayer.class)
public abstract class MixinAbsClientPlayer extends Player {
	private MixinAbsClientPlayer(Level p_219727_, BlockPos p_219728_, float p_219729_, GameProfile p_219730_, @Nullable ProfilePublicKey p_219731_){
		super(p_219727_, p_219728_, p_219729_, p_219730_, p_219731_);
	}

	@Rewrite
	@Inject(method = "getFieldOfViewModifier", at = @At("HEAD"), cancellable = true)
	private void fov(CallbackInfoReturnable<Float> info){
		float f = 1.0F;
		if(this.getAbilities().flying){
			f *= 1.1F;
		}

		f = (float) ((double) f * (((isSprinting() ? 0.13 : 0.1) / 0.1 + 1.0D) / 2.0D));  //here
		if(this.getAbilities().getWalkingSpeed() == 0.0F || Float.isNaN(f) || Float.isInfinite(f)){
			f = 1.0F;
		}

		if(this.isUsingItem() && this.getUseItem().getItem() == Items.BOW){
			int i = this.getTicksUsingItem();
			float f1 = (float)i / 20.0F;
			if(f1 > 1.0F){
				f1 = 1.0F;
			}else{
				f1 = f1 * f1;
			}

			f *= 1.0F - f1 * 0.15F;
			if(this.isScoping()){
				f *= Keys.scopingScale;
			}
		}else if(this.isScoping()){  //here
			info.setReturnValue(Keys.scopingScale);
			return;
		}
		float fov = net.minecraftforge.client.ForgeHooksClient.getFieldOfViewModifier(this, f);
		info.setReturnValue(fov);
	}
}
