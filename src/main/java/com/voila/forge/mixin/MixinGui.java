package com.voila.forge.mixin;

import com.mojang.blaze3d.systems.*;
import com.mojang.blaze3d.vertex.*;
import com.voila.forge.*;
import net.minecraft.client.*;
import net.minecraft.client.gui.*;
import net.minecraft.client.player.*;
import net.minecraft.core.*;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.player.*;
import net.minecraft.world.item.*;
import net.minecraft.world.phys.*;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.*;

/** show item total amount */
@Mixin(Gui.class)
public abstract class MixinGui {
	@Shadow protected int screenWidth;
	@Shadow protected int screenHeight;

	@Inject(method = "renderHotbar", at = @At("HEAD"))
	private void h(float partialTicks, PoseStack stack, CallbackInfo info){
		Minecraft mc = Minecraft.getInstance();
		assert mc.player != null;
		if(mc.player.isSpectator())
			return;
		Inventory inv = mc.player.getInventory();
		ItemStack main = inv.getSelected();
		ItemStack off = inv.offhand.get(0);
		int left = screenWidth / 2 - 91;
		int top = screenHeight - 19;
		RenderSystem.disableBlend();
		if(main.getItem() != Items.AIR && !arrow(main, true, left, top)){
			int amount = main.getCount();
			boolean flag = false;
			NonNullList<ItemStack> items = inv.items;
			for(int i = 0; i < items.size(); i++){
				if(i == inv.selected)
					continue;
				ItemStack t = items.get(i);
				if(ItemStack.isSameItemSameTags(main, t)){
					amount += t.getCount();
					flag = true;
				}
			}
			if(ItemStack.isSameItemSameTags(main, off)){
				amount += off.getCount();
				flag = true;
			}
			if(flag){
				mc.getItemRenderer().renderAndDecorateItem(main, left + 182 + 20, top);
				mc.getItemRenderer().renderGuiItemDecorations(mc.font, main, left + 182 + 20, top, amount + "");
			}
		}
		if(off.getItem() != Items.AIR && !arrow(off, false, left, top)){
			int amount = off.getCount();
			boolean flag = false;
			for(ItemStack t : inv.items){
				if(ItemStack.isSameItemSameTags(off, t)){
					amount += t.getCount();
					flag = true;
				}
			}
			if(flag){
				mc.getItemRenderer().renderAndDecorateItem(off, left - 55, top);
				mc.getItemRenderer().renderGuiItemDecorations(mc.font, off, left - 55, top, amount + "");
			}
		}
		RenderSystem.enableBlend();
	}

	private static boolean arrow(ItemStack item, boolean mainHand, int left, int top){
		if(item.getItem() == Items.BOW || item.getItem() == Items.CROSSBOW){
			Minecraft mc = Minecraft.getInstance();
			LocalPlayer player = mc.player;
			assert player != null;
			Inventory inv = player.getInventory();
			int amount = 0;
			for(ItemStack t : inv.items){
				if(t.getItem() == Items.ARROW)
					amount += t.getCount();
			}
			if(inv.offhand.get(0).getItem() == Items.ARROW)
				amount += inv.offhand.get(0).getCount();
			if(mainHand){
				mc.getItemRenderer().renderAndDecorateItem(Items.ARROW.getDefaultInstance(), left + 182 + 20, top);
				mc.getItemRenderer().renderGuiItemDecorations(mc.font, Items.ARROW.getDefaultInstance(), left + 182 + 20, top, amount + "");
			}else{
				mc.getItemRenderer().renderAndDecorateItem(Items.ARROW.getDefaultInstance(), left - 55, top);
				mc.getItemRenderer().renderGuiItemDecorations(mc.font, Items.ARROW.getDefaultInstance(), left - 55, top, amount + "");
			}

			return true;
		}

		return false;
	}

	/** no scope overlay */
	@Inject(method = "renderSpyglassOverlay",at = @At("HEAD"), cancellable = true)
	private void spyglassOverlay(float p_168676_, CallbackInfo info){
		info.cancel();
	}

	/** no vignette */
	@Inject(method = "renderVignette",at = @At("HEAD"),cancellable = true)
	private void vignette(Entity p_93068_, CallbackInfo info){
		info.cancel();
	}

	@Inject(method = "canRenderCrosshairForSpectator",at = @At("RETURN"),cancellable = true)
	private void canRenderSpectatorCross(HitResult p_93025_, CallbackInfoReturnable<Boolean> info){
		if(Keys.xray){
			info.setReturnValue(true);
		}
	}

}
