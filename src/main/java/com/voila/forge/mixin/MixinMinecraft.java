package com.voila.forge.mixin;

import com.voila.forge.*;
import net.minecraft.client.*;
import net.minecraft.client.gui.screens.*;
import net.minecraft.client.player.*;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.*;

@Mixin(Minecraft.class)
public abstract class MixinMinecraft implements IMinecraft {
	@Shadow public LocalPlayer player;
	@Shadow protected int missTime;
	@Shadow @Final @Mutable private User user;
	@Shadow private int rightClickDelay;

	@Shadow
	protected abstract void startUseItem();

	@Shadow
	protected abstract boolean startAttack();

	@Shadow
	protected abstract void pickBlock();

	@Override
	public void pick(){
		pickBlock();
	}

	@Override
	public void use(){
		startUseItem();
	}

	@Override
	public void attack(){
		startAttack();
	}

	@Override
	public User getSession(){
		return user;
	}

	@Override
	public void setSession(User se){
		user = se;
	}

	@Inject(method = "setScreen", at = @At("HEAD"), cancellable = true)
	private void i(Screen guiScreenIn, CallbackInfo info){
		// reset spam state
		Forgetest.last = "";

		//respawn immediately
		if(guiScreenIn instanceof DeathScreen){
			player.respawn();
			info.cancel();
		}
	}

	@Inject(method = "startAttack", at = @At("HEAD"))
	private void sa(CallbackInfoReturnable<Boolean> cir){
		missTime = 0;
	}

	/** remove use delay */
	@Inject(method = "handleKeybinds", at = @At("HEAD"))
	private void k(CallbackInfo info){
		if(Forgetest.removeUseDelay)
			rightClickDelay = 0;
	}

	/** fix render error */
	@Inject(method = "clearLevel(Lnet/minecraft/client/gui/screens/Screen;)V", at = @At("HEAD"))
	private void clearLevel(Screen p_91321_, CallbackInfo ci){
		Keys.xray = false;
		Keys.scoping=false;
	}
}