package com.voila.forge.mixin;

import com.voila.forge.*;
import net.minecraft.client.*;
import net.minecraft.client.gui.*;
import net.minecraft.client.gui.components.*;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.*;

import java.util.*;

/** Script waitTicks */
@Mixin(DebugScreenOverlay.class)
public abstract class MixinDebugOverlay extends GuiComponent {
	@Shadow
	@Final
	private Minecraft minecraft;

	@Inject(method = "getGameInformation", at = @At("RETURN"), cancellable = true)
	private void i(CallbackInfoReturnable<List<String>> info){
		List<String> list = info.getReturnValue();
		if(Script.enabled){
			String str = "Script waitTicks: " + Keys.runningScript.waitTicks;
			list.add(str);
			list.add("Script progress: " + Keys.runningScript.getProgress());
		}
		list.add("Rain: " + minecraft.level.rainLevel);
		info.setReturnValue(list);
	}
}
