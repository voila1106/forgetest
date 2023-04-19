package com.voila.forge.mixin;

import net.minecraft.world.entity.*;
import net.minecraft.world.entity.vehicle.*;
import net.minecraft.world.level.*;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.*;

@Mixin(Boat.class)
public abstract class MixinBoat extends Entity {
	private MixinBoat(EntityType<?> p_19870_, Level p_19871_){
		super(p_19870_, p_19871_);
	}

	@Inject(method = "clampRotation", at = @At("HEAD"), cancellable = true)
	private void clampRot(Entity p_38322_, CallbackInfo info){
		info.cancel();
	}
}
