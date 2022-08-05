package com.voila.forge.mixin;

import net.minecraft.world.entity.*;
import net.minecraft.world.entity.projectile.*;
import net.minecraft.world.item.*;
import net.minecraft.world.level.*;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.*;

import javax.annotation.*;

/** hide tridents that cannot be picked up */
@Mixin(ThrownTrident.class)
public abstract class MixinThrownTrident extends AbstractArrow {
	@Shadow
	@Nonnull
	protected abstract ItemStack getPickupItem();

	private MixinThrownTrident(EntityType<? extends AbstractArrow> p_36721_, Level p_36722_){
		super(p_36721_, p_36722_);
	}

	@Inject(method = "shouldRender", at = @At("RETURN"), cancellable = true)
	private void shouldRender(double x, double y, double z, CallbackInfoReturnable<Boolean> info){
		if(inGround && pickup == Pickup.DISALLOWED){
			info.setReturnValue(false);
		}
	}

}
