package com.voila.forge.mixin;

import com.voila.forge.*;
import net.minecraft.client.multiplayer.*;
import net.minecraft.core.*;
import net.minecraft.world.entity.player.*;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.*;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.*;

@Mixin(MultiPlayerGameMode.class)
public abstract class MixinPlayerController {
	@Shadow private int destroyDelay;

	/** prevent taking frames */
	@SuppressWarnings("all")  // if hasTag() is true, getTag() must not be null
	@Inject(method = "handleInventoryMouseClick", at = @At("HEAD"), cancellable = true)
	private void i(int windowId, int slotId, int mouseButton, ClickType type, Player player, CallbackInfo info){
		if(slotId < 0)
			return;
		ItemStack item = player.containerMenu.getSlot(slotId).getItem();
		if(windowId == "spy".hashCode() && item.getItem().equals(Items.GRAY_STAINED_GLASS_PANE) && item.hasTag() && item.getTag().contains("isFrame"))
			info.cancel();
	}

	/** remove destroy delay */
	@Inject(method = "continueDestroyBlock", at = @At("HEAD"))
	private void i(BlockPos p_105284_, Direction p_105285_, CallbackInfoReturnable<Boolean> cir){
		if(Forgetest.removeDestroyDelay)
			destroyDelay = 0;
	}

}
