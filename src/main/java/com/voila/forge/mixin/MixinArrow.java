package com.voila.forge.mixin;

import net.minecraft.client.*;
import net.minecraft.client.player.*;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.projectile.*;
import net.minecraft.world.item.*;
import net.minecraft.world.item.enchantment.*;
import net.minecraft.world.level.*;
import org.spongepowered.asm.mixin.*;

@Mixin(Arrow.class)
public abstract class MixinArrow extends AbstractArrow {
	private MixinArrow(EntityType<? extends AbstractArrow> p_36721_, Level p_36722_){
		super(p_36721_, p_36722_);
	}

	private int[] infinity = null;

	@Override
	public boolean shouldRender(double x, double y, double z){
		LocalPlayer player = Minecraft.getInstance().player;
		if(player == null || !inGround){ //in air
			return super.shouldRender(x, y, z);
		}

		if(pickup == Pickup.DISALLOWED){ //shot by skeleton
			return false;
		}

		if(player.isCreative()){
			return super.shouldRender(x, y, z);
		}
		if(pickup == Pickup.CREATIVE_ONLY){
			return false;
		}

		//non-creative
		//ALLOW
		if(infinity == null){
			infinity = new int[]{-1, -1}; //{canGetOwner, infinityLevel}
			if(getOwner() instanceof LivingEntity entity){
				infinity[0] = 1;
				ItemStack item = entity.getItemInHand(entity.getUsedItemHand());
				if(item.getItem() instanceof BowItem){
					infinity[1] = item.getEnchantmentLevel(Enchantments.INFINITY_ARROWS);
				}
			}else{
				infinity[0] = 0;
			}
		}
		if(infinity[0] == 1 && infinity[1] > 0){
			return false;
		}
		return super.shouldRender(x, y, z);
	}
}
