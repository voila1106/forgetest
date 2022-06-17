package com.voila.forge.mixin;

import com.voila.forge.*;
import net.minecraft.client.*;
import net.minecraft.client.gui.components.*;
import net.minecraft.client.gui.components.events.*;
import net.minecraft.client.gui.screens.*;
import net.minecraft.network.chat.*;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.*;

/** add switch account button */
@Mixin(TitleScreen.class)
public abstract class MixinTitleScreen extends Screen {

	private MixinTitleScreen(Component title){
		super(title);
	}

	@Inject(method = "init", at = @At("TAIL"))
	private void i(CallbackInfo info){
		Button optionButton = null;
		for(GuiEventListener t : children()){
			if(t instanceof Button b){
				if(Component.translatable("menu.options").equals(b.getMessage())){
					optionButton = b;
					break;
				}
			}
		}
		if(optionButton == null){
			System.out.println("button not found");
			return;
		}
		Button switchButton = new Button(optionButton.x, optionButton.y + 24, optionButton.getWidth(), optionButton.getHeight(),
			Component.translatable("menu." + Forgetest.ID + ".switchAccount"),
			button -> Minecraft.getInstance().setScreen(new SwitchAccountScreen(this)));

		addRenderableWidget(switchButton);

	}
}