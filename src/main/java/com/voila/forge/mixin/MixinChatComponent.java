package com.voila.forge.mixin;

import com.voila.forge.*;
import net.minecraft.client.*;
import net.minecraft.client.gui.components.*;
import net.minecraft.client.gui.screens.*;
import net.minecraft.network.chat.*;
import org.slf4j.*;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.*;

/**
 * <p>increase chat line limit</p>
 * <p>auto login</p>
 */
@Mixin(ChatComponent.class)
public abstract class MixinChatComponent {
	@Shadow @Final private static Logger LOGGER;

	@Inject(method = "addMessage(Lnet/minecraft/network/chat/Component;IIZ)V", at = @At("HEAD"), cancellable = true)
	private void i(Component content, int hiddenId, int id, boolean exclude, CallbackInfo info){
		// detail logging
		LOGGER.info("[CHAT_DETAIL] {}", content.toString());

		// no spam
		String str = content.getString();
		if(str.equals(Forgetest.last) && !(Minecraft.getInstance().screen instanceof ChatScreen)){
			info.cancel();
			return;
		}

		// auto login
		if(str.contains("登录") &&
			str.contains("密码") &&
			str.toLowerCase().contains("/l")){
			assert Minecraft.getInstance().player != null;
			Minecraft.getInstance().player.command("login 111111");
		}
		Forgetest.last = str;
	}

	@ModifyConstant(method = "addMessage(Lnet/minecraft/network/chat/Component;IIZ)V", constant = @Constant(intValue = 100))
	private int modifyMaxLine(int constant){
		return 2000;
	}
}
