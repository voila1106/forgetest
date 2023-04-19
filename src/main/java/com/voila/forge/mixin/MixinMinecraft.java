package com.voila.forge.mixin;

import com.voila.forge.*;
import net.minecraft.client.*;
import net.minecraft.client.gui.screens.*;
import net.minecraft.client.gui.screens.inventory.*;
import net.minecraft.client.multiplayer.*;
import net.minecraft.client.player.*;
import net.minecraft.client.renderer.*;
import net.minecraft.world.*;
import net.minecraft.world.entity.*;
import net.minecraft.world.phys.*;
import org.slf4j.*;
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

	@Shadow
	@Final
	private static Logger LOGGER;

	@Shadow
	protected abstract void continueAttack(boolean p_91387_);


	@Override
	public void continueAttack(){
		missTime = 0;
		continueAttack(true);
	}

	@Override
	public void setUser(User user){
		this.user = user;
	}

	@Override
	public User getUser(){
		return user;
	}

	private final Minecraft _this = (Minecraft) (Object) this;

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

		//fast sign
		if((screen instanceof SignEditScreen) && Forgetest.fastSign){
			screen.init(_this, 0, 0);
			screen.onClose();
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
		if(Forgetest.removeUseDelay){
			rightClickDelay = 0;
		}

		assert cameraEntity != null;
		// paving assist
		if(rightClickDelay <= 0 && Keys.clickForwardKey.isDown()){
			if(hitResult instanceof BlockHitResult pointing && pointing.getType() == HitResult.Type.BLOCK){
				BlockHitResult forward = Screen.hasControlDown() ? pointing.withPosition(pointing.getBlockPos().relative(cameraEntity.getDirection())).withDirection(cameraEntity.getDirection().getOpposite()) : pointing.withDirection(cameraEntity.getDirection());
				if(gameMode.useItemOn(player, InteractionHand.MAIN_HAND, forward) == InteractionResult.PASS){
					gameMode.useItemOn(player, InteractionHand.OFF_HAND, forward);
				}
				if(!Forgetest.removeUseDelay){
					rightClickDelay = 4;
				}
			}
		}
		if(rightClickDelay <= 0 && Keys.clickBehindKey.isDown()){
			if(hitResult instanceof BlockHitResult pointing && pointing.getType() == HitResult.Type.BLOCK){
				BlockHitResult forward = Screen.hasControlDown() ? pointing.withPosition(pointing.getBlockPos().relative(cameraEntity.getDirection().getOpposite())).withDirection(cameraEntity.getDirection()) : pointing.withDirection(cameraEntity.getDirection().getOpposite());
				if(gameMode.useItemOn(player, InteractionHand.MAIN_HAND, forward) == InteractionResult.PASS){
					gameMode.useItemOn(player, InteractionHand.OFF_HAND, forward);
				}
				if(!Forgetest.removeUseDelay){
					rightClickDelay = 4;
				}
			}
		}
	}

	/** fix render error */
	@Inject(method = "clearLevel(Lnet/minecraft/client/gui/screens/Screen;)V", at = @At("HEAD"))
	private void clearLevel(Screen p_91321_, CallbackInfo ci){
		Keys.xray = false;
		Keys.scoping = false;
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