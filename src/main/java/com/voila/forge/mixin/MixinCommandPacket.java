package com.voila.forge.mixin;

import com.voila.forge.command.*;
import net.minecraft.client.multiplayer.*;
import net.minecraft.network.protocol.game.*;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.*;

/** add sphere command to client */
@Mixin(ClientboundCommandsPacket.class)
public abstract class MixinCommandPacket {
	@Inject(method = "handle(Lnet/minecraft/network/protocol/game/ClientGamePacketListener;)V", at = @At("RETURN"))
	private void handle(ClientGamePacketListener handler, CallbackInfo ci){
		if(handler instanceof ClientPacketListener listener){
			SphereCommand.register(listener.getCommands());
			CheckCommand.register(listener.getCommands());
		}
	}
}
