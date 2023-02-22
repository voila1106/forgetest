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
import org.jetbrains.annotations.*;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.*;

/** client commands */
@Mixin(LocalPlayer.class)
abstract class MixinLocalPlayer extends AbstractClientPlayer {
	private final LocalPlayer _this = (LocalPlayer)((Object)this);

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

}
