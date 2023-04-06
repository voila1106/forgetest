package com.voila.forge.mixin;

import net.minecraft.client.gui.screens.*;
import net.minecraft.network.chat.*;
import org.spongepowered.asm.mixin.*;

/** no force res pack */
@Mixin(ConfirmScreen.class)
public class MixinConfirmScreen extends Screen {

	private MixinConfirmScreen(Component p_96550_){
		super(p_96550_);
	}

	@Override
	public boolean shouldCloseOnEsc(){
		return true;
	}

	@Override
	public boolean keyPressed(int p_96552_, int p_96553_, int p_96554_){
		return super.keyPressed(p_96552_, p_96553_, p_96554_);
	}
}
