package com.voila.forge.mixin;

import com.mojang.authlib.*;
import com.mojang.blaze3d.matrix.*;
import com.voila.forge.*;
import net.minecraft.client.*;
import net.minecraft.client.gui.*;
import net.minecraft.client.gui.screen.*;
import net.minecraft.client.network.play.*;
import net.minecraft.client.particle.*;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.entity.*;
import net.minecraft.client.world.*;
import net.minecraft.entity.*;
import net.minecraft.particles.*;
import net.minecraft.scoreboard.*;
import net.minecraft.server.management.*;
import net.minecraft.tileentity.*;
import net.minecraft.util.*;
import net.minecraft.util.text.*;
import net.minecraft.world.*;
import net.minecraftforge.client.*;
import org.apache.logging.log4j.*;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.*;

import java.util.*;
import java.util.function.*;

@Mixin(ClientPlayNetHandler.class)
public abstract class HookClientNetHandler
{

}

@Mixin(GameRenderer.class)
abstract class HookGameRenderer  //disable damage screen shake
{
	@Inject(method = "hurtCameraEffect", at = @At(value = "FIELD", target = "net/minecraft/entity/LivingEntity.attackedAtYaw:F"), cancellable = true)
	private void hurt(MatrixStack matrix, float ticks, CallbackInfo ci)
	{
		ci.cancel();
	}
}

@Mixin(LivingRenderer.class)
abstract class HookLivingRenderer  //damage amount particle
{
	private Map<Integer,Float> hs=new HashMap<>();
	private World world;

	@Inject(method = "render(Lnet/minecraft/entity/LivingEntity;FFLcom/mojang/blaze3d/matrix/MatrixStack;Lnet/minecraft/client/renderer/IRenderTypeBuffer;I)V",at = @At("HEAD"))
	private void r(LivingEntity entity, float yaw, float ticks, MatrixStack stack, IRenderTypeBuffer buffer, int light, CallbackInfo info)
	{
		if(!entity.world.equals(world))
		{
			hs=new HashMap<>();
			world=entity.world;
		}
		float health=entity.getHealth();
		if(!hs.containsKey(entity.hashCode()))
		{
			hs.put(entity.hashCode(),health);
			return;
		}
		if(health<hs.get(entity.hashCode()))
		{
			float damage=(hs.get(entity.hashCode())-health);
			for(int i = 0; i < Math.min(100,damage); i++)
			{
				Minecraft.getInstance().worldRenderer.addParticle(new DamageParticle.DamageParticleData((int)damage),
					true,
					entity.getPosX(),
					entity.getPosYHeight(0.5),
					entity.getPosZ(),0.1,0,0.1);
			}
		}
		hs.put(entity.hashCode(),health);
	}
}

@Mixin(ParticleManager.class)
abstract class HookParticleManager  // remove some particles
{
	@Inject(method = "addParticle",at = @At("HEAD"),cancellable = true)
	private void i(IParticleData particleData, double x, double y, double z, double xSpeed, double ySpeed, double zSpeed, CallbackInfoReturnable<Particle> info)
	{
		if(particleData.equals(ParticleTypes.DAMAGE_INDICATOR) || particleData.equals(ParticleTypes.ELDER_GUARDIAN))
			info.setReturnValue(null);
	}
}


@Mixin(IngameGui.class)
abstract class HookIngameGui  // no spam
{
	@Inject(method = "sendChatMessage",at = @At("HEAD"),cancellable = true)
	private void i(ChatType type, ITextComponent cmp, UUID uuid,CallbackInfo info)
	{
		String str= cmp.getString();
		if(str.equals(Forgetest.last) && !(Minecraft.getInstance().currentScreen instanceof ChatScreen))
		{
			info.cancel();
		}
		Forgetest.last=str;
	}
}

@Mixin(Minecraft.class)
abstract class HookMinecraft  // no spam
{
	@Inject(method = "displayGuiScreen",at = @At("HEAD"))
	private void i(Screen guiScreenIn, CallbackInfo ci)
	{
		Forgetest.last="";
	}
}

/*
@Mixin(ItemStack.class)
abstract class HookItemStack
{
	@Shadow private CompoundNBT tag;

	@Inject(method = "addEnchantment",
		at = @At(value = "INVOKE",target = "Lnet/minecraft/nbt/CompoundNBT;getList(Ljava/lang/String;I)Lnet/minecraft/nbt/ListNBT;"),
		cancellable = true)
	private void i(Enchantment ench, int level, CallbackInfo ci)
	{
		ListNBT listnbt = this.tag.getList("Enchantments", 10);
		CompoundNBT compoundnbt = new CompoundNBT();
		compoundnbt.putString("id", String.valueOf((Object)Registry.ENCHANTMENT.getKey(ench)));
		compoundnbt.putShort("lvl", (short)level);
		listnbt.add(compoundnbt);
		ci.cancel();
	}
}
*/

@Mixin(Scoreboard.class)
abstract class HookScoreBoard  // fix score board NPE
{
	@Inject(method = "removeTeam",at = @At("HEAD"),cancellable = true)
	private void i(ScorePlayerTeam playerTeam, CallbackInfo ci)
	{
		if(playerTeam==null)
			ci.cancel();
	}
}

@Mixin(SkullTileEntity.class)
abstract class HookSkullTileEntity  // fix skull texture loading stuck
{
	@Shadow private static PlayerProfileCache profileCache;

	@Inject(method = "updateGameProfile",at = @At(value = "INVOKE",
		target = "Lcom/mojang/authlib/minecraft/MinecraftSessionService;fillProfileProperties(Lcom/mojang/authlib/GameProfile;Z)Lcom/mojang/authlib/GameProfile;"),
		cancellable = true)
	private static void i(GameProfile input, CallbackInfoReturnable<GameProfile> info)
	{
		GameProfile gameprofile = profileCache.getGameProfileForUsername(input.getName());
		info.setReturnValue(gameprofile);
	}
}

@Mixin(SharedConstants.class)
abstract class HookSharedConstants  // allow all characters
{
	@Inject(method = "isAllowedCharacter",at = @At("HEAD"),cancellable = true)
	private static void i(char character, CallbackInfoReturnable<Boolean> info)
	{
		info.setReturnValue(true);
	}
}

@Mixin(ClientWorld.class)
abstract class HookClientWorld
{
	@Shadow public abstract DimensionRenderInfo getDimensionRenderInfo();

	/**
	 * disable portal sound
	 */
	@Inject(method = "playSound(DDDLnet/minecraft/util/SoundEvent;Lnet/minecraft/util/SoundCategory;FFZ)V",at = @At("HEAD"),cancellable = true)
	private void i(double x, double y, double z, SoundEvent soundIn, SoundCategory category, float volume, float pitch, boolean distanceDelay, CallbackInfo info)
	{
		if(soundIn.equals(SoundEvents.BLOCK_PORTAL_AMBIENT))
			info.cancel();
	}
}

// TODO: WorldRenderer.renderRainSnow



// TODO: disable fire effect