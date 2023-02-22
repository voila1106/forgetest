package com.voila.forge.mixin;

import com.mojang.blaze3d.*;
import com.voila.forge.*;
import net.minecraft.client.*;
import net.minecraft.util.*;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.*;

@Mixin(MouseHandler.class)
public abstract class MixinMouseHandler {
	@Shadow private double lastMouseEventTime;
	@Shadow @Final private Minecraft minecraft;
	@Shadow @Final private SmoothDouble smoothTurnX;
	@Shadow @Final private SmoothDouble smoothTurnY;
	@Shadow private double accumulatedDX;
	@Shadow private double accumulatedDY;

	@Shadow
	public abstract boolean isMouseGrabbed();

	/** adjust mouse speed with scoping scale */
	@Rewrite
	@Inject(method = "turnPlayer", at = @At("HEAD"), cancellable = true)
	private void turnPlayer(CallbackInfo info){
		info.cancel();

		double d0 = Blaze3D.getTime();
		double d1 = d0 - this.lastMouseEventTime;
		this.lastMouseEventTime = d0;
		if(this.isMouseGrabbed() && this.minecraft.isWindowActive()){
			double d4 = this.minecraft.options.sensitivity().get() * (double)0.6F + (double)0.2F;
			double d5 = d4 * d4 * d4;
			double d6 = d5 * 8.0D;
			double d2;
			double d3;
			if(this.minecraft.options.smoothCamera){
				double d7 = this.smoothTurnX.getNewDeltaValue(this.accumulatedDX * d6, d1 * d6);
				double d8 = this.smoothTurnY.getNewDeltaValue(this.accumulatedDY * d6, d1 * d6);
				d2 = d7;
				d3 = d8;
			}else if(this.minecraft.options.getCameraType().isFirstPerson() && this.minecraft.player.isScoping()){
				this.smoothTurnX.reset();
				this.smoothTurnY.reset();
				d2 = this.accumulatedDX * d5 * (Keys.scopingScale * 70 / 9 + (2 / 9.0)); //here
				d3 = this.accumulatedDY * d5 * (Keys.scopingScale * 70 / 9 + (2 / 9.0)); //here
			}else{
				this.smoothTurnX.reset();
				this.smoothTurnY.reset();
				d2 = this.accumulatedDX * d6;
				d3 = this.accumulatedDY * d6;
			}
			this.accumulatedDX = 0.0D;
			this.accumulatedDY = 0.0D;
			int i = 1;
			if(this.minecraft.options.invertYMouse().get()){
				i = -1;
			}
			this.minecraft.getTutorial().onMouse(d2, d3);
			if(this.minecraft.player != null){
				this.minecraft.player.turn(d2, d3 * (double)i);
			}
		}else{
			this.accumulatedDX = 0.0D;
			this.accumulatedDY = 0.0D;
		}
	}
}
