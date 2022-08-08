package com.voila.forge.mixin;

import com.google.common.collect.*;
import com.mojang.blaze3d.vertex.*;
import com.voila.forge.*;
import net.minecraft.client.*;
import net.minecraft.client.gui.components.*;
import net.minecraft.client.gui.screens.*;
import net.minecraft.client.gui.screens.social.*;
import net.minecraft.network.chat.*;
import net.minecraft.resources.*;
import net.minecraft.util.*;
import net.minecraft.world.entity.player.*;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.*;

import javax.annotation.*;
import java.util.*;
import java.util.function.*;

/** check inventory from social screen */
@Mixin(PlayerEntry.class)
public abstract class MixinPlayerEntry implements IPlayerEntry {
//	@Dynamic
//	@Inject(method = {"lambda$new$0", "m_100608_"}, at = @At(value = "HEAD"), cancellable = true)
//	private void i(PlayerSocialManager manager, UUID uuid, String name, Button button, CallbackInfo info){
//		if(Screen.hasShiftDown()){
//			info.cancel();
//			assert Minecraft.getInstance().level != null;
//			Player player = Minecraft.getInstance().level.getPlayerByUUID(uuid);
//			Forgetest.checkInv(player, false);
//		}
//	}

	@Shadow float tooltipHoverTime;
	@Shadow @Final Component hideText;
	@Shadow @Nullable private Button hideButton;
	@Shadow @Final List<FormattedCharSequence> hideTooltip;
	@Shadow @Final @Mutable private List<AbstractWidget> children;
	@Shadow @Nullable private Button showButton;
	final ResourceLocation SOCIAL_INTERACTIONS_LOCATION = new ResourceLocation("textures/gui/social_interactions.png");

	@Shadow
	protected abstract void onHiddenOrShown(boolean p_100597_, Component p_100598_);

	@Shadow
	static void postRenderTooltip(SocialInteractionsScreen p_100589_, PoseStack p_100590_, List<FormattedCharSequence> p_100591_, int p_100592_, int p_100593_){
	}

	@Shadow
	abstract MutableComponent getEntryNarationMessage(MutableComponent p_100595_);

	@Override
	public float getTooltipHoverTime(){
		return tooltipHoverTime;
	}

	@Override
	public Component getHideText(){
		return hideText;
	}

	@Override
	public void setTooltipHoverTime(float time){
		this.tooltipHoverTime = time;
	}

	@Override
	public void postTooltip(SocialInteractionsScreen socialScreen, PoseStack poseStack, List<FormattedCharSequence> tips, int x, int y){
		postRenderTooltip(socialScreen, poseStack, tips, x, y);
	}

	@Override
	public List<FormattedCharSequence> getHideTooltip(){
		return hideTooltip;
	}

	@Rewrite
	@Inject(method = "<init>", at = @At("RETURN"))
	private void init(Minecraft mc, SocialInteractionsScreen socialScreen, UUID uuid, String p_100555_, Supplier<ResourceLocation> p_100556_, CallbackInfo ci){
		PlayerSocialManager playersocialmanager = mc.getPlayerSocialManager();
		assert mc.player != null;
		if(!mc.player.getGameProfile().getId().equals(uuid) && !playersocialmanager.isBlocked(uuid)){
			this.hideButton = new ImageButton(0, 0, 20, 20, 0, 38, 20, SOCIAL_INTERACTIONS_LOCATION, 256, 256, (p_100612_) -> {
				if(Screen.hasShiftDown()){
					assert Minecraft.getInstance().level != null;
					Player player = Minecraft.getInstance().level.getPlayerByUUID(uuid);
					Forgetest.checkInv(player, false);
					return;
				}
				playersocialmanager.hidePlayer(uuid);
				this.onHiddenOrShown(true, Component.translatable("gui.socialInteractions.hidden_in_chat", p_100555_));
			}, new OnToolTip((PlayerEntry)(Object)this, mc, socialScreen), Component.translatable("gui.socialInteractions.hide")) {
				protected MutableComponent createNarrationMessage(){
					return MixinPlayerEntry.this.getEntryNarationMessage(super.createNarrationMessage());
				}
			};
			this.children = ImmutableList.of(this.hideButton, this.showButton);
		}
	}
}
