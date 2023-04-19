package com.voila.forge.mixin;

import com.mojang.authlib.*;
import com.mojang.brigadier.*;
import com.mojang.brigadier.exceptions.*;
import com.voila.forge.*;
import net.minecraft.client.*;
import net.minecraft.client.multiplayer.*;
import net.minecraft.client.player.*;
import net.minecraft.commands.*;
import net.minecraft.network.chat.*;
import net.minecraft.world.entity.player.*;
import net.minecraft.world.phys.*;
import org.jetbrains.annotations.*;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.*;

/** client commands */
@Mixin(LocalPlayer.class)
abstract class MixinLocalPlayer extends AbstractClientPlayer {
	@Shadow
	public abstract void setSprinting(boolean p_108751_);

	@Shadow
	@Final
	protected Minecraft minecraft;
	@Shadow public Input input;
	private final LocalPlayer _this = (LocalPlayer) ((Object) this);

	private MixinLocalPlayer(ClientLevel p_234112_, GameProfile p_234113_, @Nullable ProfilePublicKey p_234114_){
		super(p_234112_, p_234113_, p_234114_);
	}

	@Inject(method = "sendCommand(Ljava/lang/String;Lnet/minecraft/network/chat/Component;)V", at = @At("HEAD"), cancellable = true)
	private void command(String message, Component p_234150_, CallbackInfo info){
		if(message.startsWith("sphere ") || message.startsWith("check ") || message.startsWith("steal ") || message.startsWith("stolen ")
			|| message.startsWith("java ")){
			info.cancel();
			ClientPacketListener connection = Minecraft.getInstance().getConnection();
			if(connection != null){
				CommandDispatcher<SharedSuggestionProvider> commandDispatcher = connection.getCommands();
				CommandSourceStack commandSource = Minecraft.getInstance().player.createCommandSourceStack();
				try{
					commandDispatcher.execute(message, commandSource);
				}catch(CommandSyntaxException e){
					commandSource.sendFailure(Component.literal(e.getMessage()));
				}catch(Throwable ne){
					commandSource.sendFailure(Component.literal("Internal Error"));
					ne.printStackTrace();
				}

			}
		}
	}

	@Inject(method = "isShiftKeyDown", at = @At("HEAD"), cancellable = true)
	private void isShiftKeyDown(CallbackInfoReturnable<Boolean> info){
		if(_this == Minecraft.getInstance().player && Script.enabled && Keys.runningScript != null && Keys.runningScript.crouch){
			info.setReturnValue(true);
		}
	}

	/** No knock back */
	@Override
	public void lerpMotion(double x, double y, double z){
		if(!Forgetest.noKnockBack || new Vec3(x, y, z).length() > 1){
			super.lerpMotion(x, y, z);
		}
	}

	@Override
	public boolean isScoping(){
		return Keys.scoping || super.isScoping();
	}

	/** Do script */
	@Inject(method = "aiStep", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/tutorial/Tutorial;onInput(Lnet/minecraft/client/player/Input;)V"))
	private void aiStep(CallbackInfo info){
		if(_this == minecraft.player && Script.enabled){
			assert Keys.runningScript != null;
			setSprinting(Keys.runningScript.sprint);
			if(Keys.runningScript.travelling){
				input.up = Keys.runningScript.travelZ > 0;
				input.down = Keys.runningScript.travelZ < 0;
				input.left = Keys.runningScript.travelX > 0;
				input.right = Keys.runningScript.travelX < 0;
			}
			if(Keys.runningScript.use && (((IMinecraftAccessor) minecraft).getRightClickDelay() <= 0 || Forgetest.removeUseDelay)){
				((IMinecraftAccessor) minecraft).callStartUseItem();
			}
			if(Keys.runningScript.attack){
				((IMinecraft) minecraft).continueAttack();
			}
			input.jumping = Keys.runningScript.jump;
			input.shiftKeyDown = Keys.runningScript.crouch;
		}
	}

	@Inject(method = "serverAiStep", at = @At("HEAD"), cancellable = true)
	private void serverAiStep(CallbackInfo info){
		if(_this == minecraft.player && Script.enabled){
			info.cancel();
			super.serverAiStep();
			assert Keys.runningScript != null;
			xxa = Keys.runningScript.travelX;
			zza = Keys.runningScript.travelZ;
			jumping = Keys.runningScript.jump;
			setSprinting(Keys.runningScript.sprint);
		}
	}
}
