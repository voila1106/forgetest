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
	@Shadow
	@Final
	private static Logger LOGGER;

	@Inject(method = "addMessage(Lnet/minecraft/network/chat/Component;Lnet/minecraft/network/chat/MessageSignature;ILnet/minecraft/client/GuiMessageTag;Z)V", at = @At("HEAD"), cancellable = true)
	private void i(Component content, MessageSignature p_241566_, int p_240583_, GuiMessageTag p_240624_, boolean p_240558_, CallbackInfo info){
		// detail logging
		LOGGER.info("[CHAT_DETAIL] {}", content.toString());

		// no spam
		String str = content.getString();
		if(str.equals(Forgetest.last) && !(Minecraft.getInstance().screen instanceof ChatScreen)){
			info.cancel();
			return;
		}

		// auto login
		if(str.contains("登录") && str.contains("密码") && str.toLowerCase().contains("/l")){
			String pass = Forgetest.getConfig("passwd");
			if(!pass.isEmpty()){
				assert Minecraft.getInstance().player != null;
				Minecraft.getInstance().player.commandUnsigned("login " + pass);
			}
		}
		Forgetest.last = str;
	}

	@ModifyConstant(method = "addMessage(Lnet/minecraft/network/chat/Component;Lnet/minecraft/network/chat/MessageSignature;ILnet/minecraft/client/GuiMessageTag;Z)V", constant = @Constant(intValue = 100))
	private int modifyMaxLine(int constant){
		return 2000;
	}
}
