package com.voila.forge.mixin;

import com.voila.forge.*;
import io.netty.channel.*;
import io.netty.handler.timeout.*;
import net.minecraft.network.*;
import net.minecraft.network.chat.*;
import net.minecraft.network.protocol.*;
import net.minecraft.network.protocol.game.*;
import org.jetbrains.annotations.*;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.*;

@Mixin(Connection.class)
public abstract class MixinConnection {
	private double lastY = Double.MAX_VALUE;
	private long ticks = 0;

	@Shadow
	public abstract void disconnect(Component p_129508_);

	@Shadow
	public abstract void send(Packet<?> p_243248_, @Nullable PacketSendListener p_243316_);

	/** Don't disconnect when decode error */
	@Rewrite
	@Inject(method = "exceptionCaught", at = @At("HEAD"), cancellable = true)
	private void caught(ChannelHandlerContext p_129533_, Throwable th, CallbackInfo info){
		info.cancel();
		if(th instanceof TimeoutException){
			disconnect(Component.translatable("disconnect.timeout"));
		}
	}

	/** No fall damage */
	@Inject(method = "send(Lnet/minecraft/network/protocol/Packet;)V", at = @At("HEAD"), cancellable = true)
	private void send(Packet<?> packet, CallbackInfo info){
		if(packet instanceof ServerboundMovePlayerPacket p && Forgetest.noFallDamage){
			if(!p.hasPosition()){
				return;
			}
			double y = p.getY(Double.MAX_VALUE);
			if(lastY == Double.MAX_VALUE){
				lastY = y;
			}
			if(++ticks % 2 == 0 && y < lastY){
				packet = new ServerboundMovePlayerPacket.Pos(p.getX(0), lastY + 0.01, p.getZ(0), p.isOnGround());
				send(packet, (PacketSendListener) null);
				info.cancel();
			}
			lastY = y;
		}
	}
}
