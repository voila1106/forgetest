package com.voila.forge;

import com.mojang.blaze3d.vertex.*;
import net.minecraft.client.gui.screens.social.*;
import net.minecraft.network.chat.*;
import net.minecraft.util.*;

import java.util.*;

public interface IPlayerEntry {
	float getTooltipHoverTime();

	Component getHideText();

	void setTooltipHoverTime(float time);

	void postTooltip(SocialInteractionsScreen socialScreen, PoseStack poseStack, List<FormattedCharSequence> tips, int x, int y);

	List<FormattedCharSequence> getHideTooltip();
}
