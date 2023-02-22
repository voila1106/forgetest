package com.voila.forge.mixin;

import com.voila.forge.*;
import net.minecraft.*;
import net.minecraft.network.chat.*;
import net.minecraft.world.item.enchantment.*;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.*;

/** show level correctly when >10 */
@Mixin(Enchantment.class)
public abstract class MixinEnchantment {
	@Shadow public abstract String getDescriptionId();

	@Shadow public abstract boolean isCurse();

	@Shadow public abstract int getMaxLevel();

	@Rewrite
	@Inject(method = "getFullname",at = @At("HEAD"),cancellable = true)
	private void getFullName(int level, CallbackInfoReturnable<Component> info){
		MutableComponent name = Component.translatable(this.getDescriptionId());
		if (this.isCurse()) {
			name.withStyle(ChatFormatting.RED);
		} else {
			name.withStyle(ChatFormatting.GRAY);
		}

		if (level != 1 || this.getMaxLevel() != 1) {
			if(level <= 10 && level >= 1){ //here
				name.append(" ").append(Component.translatable("enchantment.level." + level));
			}else{
				name.append(" " + level);
			}
		}

		info.setReturnValue(name);

	}
}
