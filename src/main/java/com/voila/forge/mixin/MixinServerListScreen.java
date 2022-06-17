package com.voila.forge.mixin;

import net.minecraft.client.*;
import net.minecraft.client.gui.screens.*;
import net.minecraft.client.gui.screens.multiplayer.*;
import net.minecraft.network.chat.*;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.*;

/** add username to multiplayer screen */
@Mixin(JoinMultiplayerScreen.class)
public abstract class MixinServerListScreen extends Screen {
	private MixinServerListScreen(Component p_96550_){
		super(p_96550_);
	}

	@Inject(method = "<init>", at = @At("RETURN"))
	private void init(Screen p_99688_, CallbackInfo ci){
		((MutableComponent)title).append(" (").append(Minecraft.getInstance().getUser().getName()).append(")");
	}
}
