package com.voila.forge.mixin;

import com.voila.forge.*;
import net.minecraft.client.*;
import net.minecraft.client.gui.components.*;
import net.minecraft.client.gui.screens.*;
import net.minecraft.network.chat.*;
import net.minecraft.util.*;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.*;

import java.util.*;

/**
 * <p>increase chat line limit</p>
 * <p>auto login</p>
 */
@Mixin(ChatComponent.class)
public abstract class MixinChatComponent {
	@Shadow
	protected abstract void removeById(int p_93804_);

	@Shadow
	public abstract int getWidth();

	@Shadow
	public abstract double getScale();

	@Shadow
	@Final
	private Minecraft minecraft;

	@Shadow
	protected abstract boolean isChatFocused();

	@Shadow
	private int chatScrollbarPos;
	@Shadow
	private boolean newMessageSinceScroll;

	@Shadow
	public abstract void scrollChat(int p_205361_);

	@Shadow
	@Final
	private List<GuiMessage<FormattedCharSequence>> trimmedMessages;
	@Shadow
	@Final
	private List<GuiMessage<Component>> allMessages;

	@Rewrite
	@Inject(method = "addMessage(Lnet/minecraft/network/chat/Component;IIZ)V", at = @At("HEAD"), cancellable = true)
	private void i(Component content, int hiddenId, int id, boolean exclude, CallbackInfo info){
		info.cancel();

		//no spam
		String str = content.getString();
		if(str.equals(Forgetest.last) && !(Minecraft.getInstance().screen instanceof ChatScreen)){
			return;
		}
		if(str.contains("登录") &&
			str.contains("密码") &&
			str.toLowerCase().contains("/l")){
			assert Minecraft.getInstance().player != null;
			Minecraft.getInstance().player.command("login 111111");
		}
		Forgetest.last = str;


		if(hiddenId != 0){
			this.removeById(hiddenId);
		}
		int i = Mth.floor((double)this.getWidth() / this.getScale());
		List<FormattedCharSequence> list = ComponentRenderUtils.wrapComponents(content, i, this.minecraft.font);
		boolean flag = this.isChatFocused();
		for(FormattedCharSequence formattedcharsequence : list){
			if(flag && this.chatScrollbarPos > 0){
				this.newMessageSinceScroll = true;
				this.scrollChat(1);
			}
			this.trimmedMessages.add(0, new GuiMessage<>(id, formattedcharsequence, hiddenId));
		}
		while(this.trimmedMessages.size() > 2000){
			this.trimmedMessages.remove(this.trimmedMessages.size() - 1);
		}
		if(!exclude){
			this.allMessages.add(0, new GuiMessage<>(id, content, hiddenId));

			while(this.allMessages.size() > 2000){
				this.allMessages.remove(this.allMessages.size() - 1);
			}
		}
	}
}
