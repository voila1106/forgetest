package com.voila.forge.mixin;

import com.voila.forge.*;
import net.minecraft.client.*;
import net.minecraft.client.gui.components.*;
import net.minecraft.client.gui.screens.*;
import net.minecraft.client.gui.screens.social.*;
import net.minecraft.world.entity.player.*;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.*;

import java.util.*;

/** check inventory from social screen */
@Mixin(PlayerEntry.class)
public abstract class MixinPlayerEntry {
	@Dynamic
	@Inject(method = {"lambda$new$0", "m_100608_"}, at = @At(value = "HEAD"), cancellable = true)
	private void i(PlayerSocialManager manager, UUID uuid, String name, Button button, CallbackInfo info){
		if(Screen.hasShiftDown()){
			info.cancel();
			assert Minecraft.getInstance().level != null;
			Player player = Minecraft.getInstance().level.getPlayerByUUID(uuid);
			Forgetest.checkInv(player, false);
		}
	}
}
