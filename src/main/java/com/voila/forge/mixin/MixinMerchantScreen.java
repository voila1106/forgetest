package com.voila.forge.mixin;

import net.minecraft.client.gui.components.*;
import net.minecraft.client.gui.screens.inventory.*;
import net.minecraft.network.chat.*;
import net.minecraft.world.entity.player.*;
import net.minecraft.world.inventory.*;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.*;

/** make villager following player */
@Mixin(MerchantScreen.class)
public abstract class MixinMerchantScreen extends AbstractContainerScreen<MerchantMenu> {
	private LockIconButton lockButton;

	private MixinMerchantScreen(MerchantMenu p_97741_, Inventory p_97742_, Component p_97743_){
		super(p_97741_, p_97742_, p_97743_);
	}

	@Inject(method = "init", at = @At("RETURN"))
	private void init(CallbackInfo info){
		int x2 = (width + imageWidth) / 2;
		int y1 = (height - imageHeight) / 2;
		lockButton = new LockIconButton(x2, y1, (button) -> {
			lockButton.setLocked(!lockButton.isLocked());
		});
		addRenderableWidget(lockButton);
	}

	@Override
	public void onClose(){
		if(!lockButton.isLocked()){
			super.onClose();
		}else{
			minecraft.popGuiLayer();
		}
	}
}

