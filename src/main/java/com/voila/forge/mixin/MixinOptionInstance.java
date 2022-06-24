package com.voila.forge.mixin;

import com.voila.forge.*;
import net.minecraft.client.*;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.*;

import java.util.*;
import java.util.function.*;

/** don't validate value */
@Mixin(OptionInstance.class)
public class MixinOptionInstance<T> {
	@Shadow T value;
	@Shadow @Final private Consumer<T> onValueUpdate;

	@Rewrite
	@Inject(method = "set",at = @At("HEAD"),cancellable = true)
	private void set(T t, CallbackInfo info){
		info.cancel();
		if (!Minecraft.getInstance().isRunning()) {
			this.value = t;
		} else {
			if (!Objects.equals(this.value, t)) {
				this.value = t;
				this.onValueUpdate.accept(this.value);
			}
		}
	}
}
