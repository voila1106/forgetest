package com.voila.forge;

import com.mojang.blaze3d.vertex.*;
import net.minecraft.client.*;
import net.minecraft.client.gui.components.*;
import net.minecraft.client.gui.screens.social.*;
import net.minecraft.network.chat.*;

import java.util.function.*;

public class OnToolTip implements Button.OnTooltip {
	private final IPlayerEntry sup;
	private final Minecraft mc;
	private final SocialInteractionsScreen socialScreen;

	@Override
	public void onTooltip(Button p_93753_, PoseStack poseStack, int x, int y){
		sup.setTooltipHoverTime(mc.getDeltaFrameTime() + sup.getTooltipHoverTime());
		if(sup.getTooltipHoverTime() >= 10.0F){
			socialScreen.setPostRenderRunnable(() -> {
				sup.postTooltip(socialScreen, poseStack, sup.getHideTooltip(), x, y);
			});
		}
	}

	@Override
	public void narrateTooltip(Consumer<Component> consumer){
		consumer.accept(sup.getHideText());
	}

	public OnToolTip(PlayerEntry sup, Minecraft mc, SocialInteractionsScreen socialScreen){
		this.sup = (IPlayerEntry)sup;
		this.mc = mc;
		this.socialScreen = socialScreen;
	}
}
