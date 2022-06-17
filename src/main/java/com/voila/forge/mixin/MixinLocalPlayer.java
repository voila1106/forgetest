package com.voila.forge.mixin;

import com.mojang.authlib.*;
import com.mojang.brigadier.*;
import com.mojang.brigadier.exceptions.*;
import com.voila.forge.*;
import net.minecraft.*;
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

/** <p>check inventory</p>
 *  <p>spawn spheres</p> */
@Mixin(LocalPlayer.class)
abstract class MixinLocalPlayer extends AbstractClientPlayer {
	private MixinLocalPlayer(ClientLevel p_234112_, GameProfile p_234113_, @Nullable ProfilePublicKey p_234114_){
		super(p_234112_, p_234113_, p_234114_);
	}

	@Inject(method = "command(Ljava/lang/String;Lnet/minecraft/network/chat/Component;)V", at = @At("HEAD"), cancellable = true)
	private void chat(String message, Component p_234150_, CallbackInfo info){
		if(message.startsWith("sphere ") || message.startsWith("check ")){
			info.cancel();
			ClientPacketListener connection = Minecraft.getInstance().getConnection();
			if(connection != null){
				CommandDispatcher<SharedSuggestionProvider> commandDispatcher = connection.getCommands();
				CommandSourceStack commandSource = Minecraft.getInstance().player.createCommandSourceStack();
				try{
					commandDispatcher.execute(message, commandSource);
				}catch(CommandSyntaxException e){
					commandSource.sendFailure(Component.literal(e.getMessage()).withStyle(ChatFormatting.RED));
				}catch(Exception ne){
					commandSource.sendFailure(Component.literal("null").withStyle(ChatFormatting.RED));
					ne.printStackTrace();
				}

			}
		}
	}
}
