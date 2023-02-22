package com.voila.forge.mixin;

import com.voila.forge.*;
import net.minecraft.client.*;
import net.minecraft.client.gui.screens.*;
import net.minecraft.client.multiplayer.*;
import net.minecraft.client.player.*;
import net.minecraft.client.renderer.*;
import net.minecraft.world.*;
import net.minecraft.world.entity.*;
import net.minecraft.world.phys.*;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.*;

import javax.annotation.*;
import java.util.concurrent.*;

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

	@Shadow
	@Nullable
	public Screen screen;

	@Shadow
	@Nullable
	public HitResult hitResult;

	@Shadow
	@Nullable
	public MultiPlayerGameMode gameMode;

	@Shadow
	@Nullable
	public Entity cameraEntity;

	@Shadow
	@Final
	public LevelRenderer levelRenderer;

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
	private void setScreen(Screen screen, CallbackInfo info){
		// reset spam state
		Forgetest.last = "";

		Keys.scoping = false;

		//respawn immediately
		if(screen instanceof DeathScreen){
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

		// paving assist
		if(rightClickDelay <= 0 && Keys.clickForwardKey.isDown()){
			if(hitResult instanceof BlockHitResult pointing && pointing.getType() == HitResult.Type.BLOCK){
				Vec3 target;
				switch(cameraEntity.getDirection()){
					case NORTH -> target = pointing.getLocation().add(0, 0, -1);
					case SOUTH -> target = pointing.getLocation().add(0, 0, 1);
					case EAST -> target = pointing.getLocation().add(1, 0, 0);
					case WEST -> target = pointing.getLocation().add(-1, 0, 0);
					default -> target = pointing.getLocation();
				}
				BlockHitResult forward = new BlockHitResult(target, cameraEntity.getDirection(), pointing.getBlockPos().relative(cameraEntity.getDirection()), pointing.isInside());
				if(gameMode.useItemOn(player, InteractionHand.MAIN_HAND, forward) == InteractionResult.PASS){
					gameMode.useItemOn(player, InteractionHand.OFF_HAND, forward);
				}
				if(!Forgetest.removeUseDelay){
					rightClickDelay = 4;
				}
			}
		}
	}

	@ModifyArg(method = "handleKeybinds",at = @At(value = "INVOKE",target = "Lnet/minecraft/client/Minecraft;continueAttack(Z)V"),index = 0)
	private boolean continueAttackArg(boolean p_91387_){
		if(Script.enabled && Keys.runningScript.attack){
			missTime=0;
			return true;
		}
		return p_91387_;
	}

	/** fix render error */
	@Inject(method = "clearLevel(Lnet/minecraft/client/gui/screens/Screen;)V", at = @At("HEAD"))
	private void clearLevel(Screen p_91321_, CallbackInfo ci){
		Keys.xray = false;
		Keys.scoping = false;
	}

	@Inject(method = "tick", at = @At("HEAD"))
	private void tick(CallbackInfo info){
		if(Script.enabled && screen != null){
			screen.passEvents = true;
		}
	}

	@Inject(method = "delayTextureReload", at = @At("HEAD"), cancellable = true)
	private void reloadTex(CallbackInfoReturnable<CompletableFuture<Void>> info){
		for(StackTraceElement element : Thread.currentThread().getStackTrace()){
			if(element.toString().contains("loadShaderPack")){
				info.setReturnValue(null);
				levelRenderer.allChanged();
				return;
			}
		}

	}
}