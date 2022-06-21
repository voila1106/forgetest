package com.voila.forge.mixin;

import com.mojang.blaze3d.platform.*;
import com.voila.forge.*;
import net.minecraft.client.*;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.*;

/** script press key */
@Mixin(KeyMapping.class)
public abstract class MixinKeyMapping {
	private final KeyMapping _this=(KeyMapping)(Object)this;

	@Inject(method = "isDown", at = @At("HEAD"), cancellable = true)
	private void i(CallbackInfoReturnable<Boolean> info){
		if(!Script.enabled){
			return;
		}
		Options settings = Minecraft.getInstance().options;
		if(Keys.runningScript.forward && _this == settings.keyUp ||
			Keys.runningScript.backward && _this == settings.keyDown ||
			Keys.runningScript.left && _this == settings.keyLeft ||
			Keys.runningScript.right && _this == settings.keyRight ||
			Keys.runningScript.jump && _this == settings.keyJump ||
			Keys.runningScript.crouch && _this == settings.keyShift ||
			Keys.runningScript.use && _this == settings.keyUse ||
			Keys.runningScript.attack && _this == settings.keyAttack){
			info.setReturnValue(true);
		}
	}

	/** Optifine zoom key */
	@Inject(method = "<init>(Ljava/lang/String;Lcom/mojang/blaze3d/platform/InputConstants$Type;ILjava/lang/String;)V", at = @At("RETURN"))
	private void reg(String name, InputConstants.Type p_90826_, int p_90827_, String p_90828_, CallbackInfo info){
		KeyMapping k = (KeyMapping)(Object)this;
		if(name.equals("of.key.zoom")){
			Forgetest.ofZoom = k;
		}
	}
}
