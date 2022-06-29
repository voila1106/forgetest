package com.voila.forge.mixin;

import com.mojang.blaze3d.vertex.*;
import com.voila.forge.*;
import net.minecraft.client.gui.screens.*;
import net.minecraft.network.chat.*;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.*;

import java.text.*;
import java.util.*;

@Mixin(DisconnectedScreen.class)
public class MixinDisconnectedScreen extends Screen {
	@Shadow private int textHeight;
	private String time="";

	private MixinDisconnectedScreen(Component p_96550_){
		super(p_96550_);
	}

	@Inject(method = "<init>",at = @At("RETURN"))
	private void init(Screen p_95993_, Component p_95994_, Component p_95995_, CallbackInfo ci){
		SimpleDateFormat format=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		time=format.format(new Date());
	}

	@Inject(method = "render",at = @At("RETURN"))
	private void renderTime(PoseStack p_95997_, int p_95998_, int p_95999_, float p_96000_, CallbackInfo ci){
		drawCenteredString(p_95997_, this.font, this.time, this.width / 2, (this.height / 2 - this.textHeight / 2 - 9 * 2)-font.lineHeight, 11184810);
	}
}
