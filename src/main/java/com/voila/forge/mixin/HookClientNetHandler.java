package com.voila.forge.mixin;

import com.google.common.collect.*;
import com.mojang.authlib.*;
import com.mojang.authlib.minecraft.*;
import com.mojang.authlib.properties.*;
import com.mojang.blaze3d.matrix.*;
import com.mojang.blaze3d.vertex.*;
import com.voila.forge.*;
import net.minecraft.block.*;
import net.minecraft.client.*;
import net.minecraft.client.entity.player.*;
import net.minecraft.client.gui.*;
import net.minecraft.client.gui.screen.*;
import net.minecraft.client.gui.social.*;
import net.minecraft.client.gui.widget.button.*;
import net.minecraft.client.multiplayer.*;
import net.minecraft.client.network.play.*;
import net.minecraft.client.particle.*;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.culling.*;
import net.minecraft.client.renderer.entity.*;
import net.minecraft.client.renderer.tileentity.*;
import net.minecraft.client.world.*;
import net.minecraft.entity.*;
import net.minecraft.entity.player.*;
import net.minecraft.fluid.*;
import net.minecraft.inventory.container.*;
import net.minecraft.item.*;
import net.minecraft.particles.*;
import net.minecraft.potion.*;
import net.minecraft.scoreboard.*;
import net.minecraft.server.management.*;
import net.minecraft.tileentity.*;
import net.minecraft.util.*;
import net.minecraft.util.math.*;
import net.minecraft.util.text.*;
import net.minecraft.world.*;
import net.minecraft.world.biome.*;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.*;

import javax.annotation.*;
import java.util.*;

@Mixin(ClientPlayNetHandler.class)
public abstract class HookClientNetHandler
{

}

/**
 * /.check command
 */
@Mixin(ClientPlayerEntity.class)
abstract class HookClientPlayerEntity extends AbstractClientPlayerEntity
{
	private HookClientPlayerEntity(ClientWorld world, GameProfile profile)
	{
		super(world, profile);
	}

	@Inject(method = "sendChatMessage", at = @At("HEAD"), cancellable = true)
	private void chat(String message, CallbackInfo info)
	{
		if(message.startsWith("/.check"))
			info.cancel();
		else
			return;
		String[] args = message.split(" ");
		if(args.length < 2)
		{
			Forgetest.sendMessage(TextFormatting.RED + "未输入玩家");
			return;
		}
		String name = args[1];
		for(Entity t : worldClient.getAllEntities())
		{
			if((t instanceof PlayerEntity) && t.getName().getString().equals(name))
			{
				Forgetest.runDelay(10, () -> Forgetest.checkInv(t, false));
				return;
			}
		}
		Forgetest.sendMessage(TextFormatting.RED + "找不到玩家");

	}
}

/**
 * disable damage screen shake
 */
@Mixin(GameRenderer.class)
abstract class HookGameRenderer
{
	@Inject(method = "hurtCameraEffect", at = @At(value = "FIELD", target = "net/minecraft/entity/LivingEntity.attackedAtYaw:F"), cancellable = true)
	private void hurt(MatrixStack matrix, float ticks, CallbackInfo ci)
	{
		ci.cancel();
	}
}

/**
 * perform damage amount particle
 */
@Mixin(LivingRenderer.class)
abstract class HookLivingRenderer
{
	private Map<Integer, Float> hs = new HashMap<>();
	private World world;

	@Inject(method = "render(Lnet/minecraft/entity/LivingEntity;FFLcom/mojang/blaze3d/matrix/MatrixStack;Lnet/minecraft/client/renderer/IRenderTypeBuffer;I)V", at = @At("HEAD"))
	private void r(LivingEntity entity, float yaw, float ticks, MatrixStack stack, IRenderTypeBuffer buffer, int light, CallbackInfo info)
	{
		if(!entity.world.equals(world))
		{
			hs = new HashMap<>();
			world = entity.world;
		}
		float health = entity.getHealth();
		if(!hs.containsKey(entity.hashCode()))
		{
			hs.put(entity.hashCode(), health);
			return;
		}
		if(health < hs.get(entity.hashCode()))
		{
			float damage = (hs.get(entity.hashCode()) - health);
			for(int i = 0; i < Math.min(100, damage); i++)
			{
				Minecraft.getInstance().worldRenderer.addParticle(new DamageParticle.DamageParticleData((int)damage),
					true,
					entity.getPosX(),
					entity.getPosYHeight(0.5),
					entity.getPosZ(), 0.1, 0, 0.1);
			}
		}
		hs.put(entity.hashCode(), health);
	}

	@Inject(method = "canRenderName(Lnet/minecraft/entity/LivingEntity;)Z", at = @At("HEAD"), cancellable = true)
	private void t(LivingEntity entity, CallbackInfoReturnable<Boolean> info)
	{
		info.setReturnValue(true);
	}
}

/**
 * remove some particles
 */
@Mixin(ParticleManager.class)
abstract class HookParticleManager
{
	@Inject(method = "addParticle", at = @At("HEAD"), cancellable = true)
	private void i(IParticleData particleData, double x, double y, double z, double xSpeed, double ySpeed, double zSpeed, CallbackInfoReturnable<Particle> info)
	{
		if(particleData.equals(ParticleTypes.DAMAGE_INDICATOR) || particleData.equals(ParticleTypes.ELDER_GUARDIAN))
			info.setReturnValue(null);
	}
}

/**
 * no spam
 */
@Mixin(IngameGui.class)
abstract class HookIngameGui
{
	@Inject(method = "sendChatMessage", at = @At("HEAD"), cancellable = true)
	private void i(ChatType type, ITextComponent cmp, UUID uuid, CallbackInfo info)
	{
		String str = cmp.getString();
		if(str.equals(Forgetest.last) && !(Minecraft.getInstance().currentScreen instanceof ChatScreen))
		{
			info.cancel();
		}
		if(str.contains("登录") && str.contains("密码") && str.toLowerCase().contains("/l"))
		{
			Minecraft.getInstance().player.sendChatMessage("/login 111111");
		}
		Forgetest.last = str;
	}
}

/**
 * reset spam state
 */
@Mixin(Minecraft.class)
abstract class HookMinecraft
{
	@Shadow
	@Nullable
	public ClientPlayerEntity player;

	@Inject(method = "displayGuiScreen", at = @At("HEAD"), cancellable = true)
	private void i(Screen guiScreenIn, CallbackInfo info)
	{
		Forgetest.last = "";

		//respawn immediately
		if(guiScreenIn instanceof DeathScreen)
		{
			player.respawnPlayer();
			info.cancel();
		}
	}
}

/**
 * fix score board NPE
 */
@Mixin(Scoreboard.class)
abstract class HookScoreBoard
{
	@Inject(method = "removeTeam", at = @At("HEAD"), cancellable = true)
	private void i(ScorePlayerTeam playerTeam, CallbackInfo ci)
	{
		if(playerTeam == null)
			ci.cancel();
	}
}

/**
 * fix skull texture loading stuck
 */
@Mixin(SkullTileEntity.class)
abstract class HookSkullTileEntity
{
	@Shadow
	private static PlayerProfileCache profileCache;

	@Shadow @Nullable private static MinecraftSessionService sessionService;
	/**
	 * @author  v
	 */
	@Nullable
	@Overwrite
	public static GameProfile updateGameProfile(GameProfile input)
	{
		if (input != null && !StringUtils.isNullOrEmpty(input.getName())) {
			if (input.isComplete() && input.getProperties().containsKey("textures")) {
				return input;
			} else if (profileCache != null && sessionService != null) {
				GameProfile gameprofile = profileCache.getGameProfileForUsername(input.getName());
				if (gameprofile == null) {
					return input;
				} else {
					Property property = Iterables.getFirst(gameprofile.getProperties().get("textures"), (Property)null);
					if (property == null) {
						return gameprofile;
					}

					return gameprofile;
				}
			} else {
				return input;
			}
		} else {
			return input;
		}

	}

}

@Mixin(SharedConstants.class)
abstract class HookSharedConstants  // allow all characters
{
	@Inject(method = "isAllowedCharacter", at = @At("HEAD"), cancellable = true)
	private static void i(char character, CallbackInfoReturnable<Boolean> info)
	{
		info.setReturnValue(true);
	}
}


/**
 * disable portal sound
 */
@Mixin(ClientWorld.class)
abstract class HookClientWorld
{
	@Inject(method = "playSound(DDDLnet/minecraft/util/SoundEvent;Lnet/minecraft/util/SoundCategory;FFZ)V", at = @At("HEAD"), cancellable = true)
	private void i(double x, double y, double z, SoundEvent soundIn, SoundCategory category, float volume, float pitch, boolean distanceDelay, CallbackInfo info)
	{
		if(soundIn.equals(SoundEvents.BLOCK_PORTAL_AMBIENT))
			info.cancel();
	}
}

/**
 * never raining
 */
@Mixin(Biome.class)
abstract class HookBiome
{
	@Inject(method = "getPrecipitation", at = @At("HEAD"), cancellable = true)
	private void i(CallbackInfoReturnable<Biome.RainType> info)
	{
		info.setReturnValue(Biome.RainType.NONE);
	}
}

/**
 * prevent taking frames
 */
@Mixin(PlayerController.class)
abstract class HookPlayerController
{
	@SuppressWarnings("all")  // if hasTag() is true, getTag() must not be null
	@Inject(method = "windowClick", at = @At("HEAD"), cancellable = true)
	private void i(int windowId, int slotId, int mouseButton, ClickType type, PlayerEntity player, CallbackInfoReturnable<ItemStack> info)
	{
		if(slotId < 0)
			return;
		ItemStack item = player.openContainer.getInventory().get(slotId);
		if(windowId == "spy".hashCode() && item.getItem().equals(Items.GRAY_STAINED_GLASS_PANE) && item.hasTag() && item.getTag().contains("isFrame"))
			info.setReturnValue(ItemStack.EMPTY);
	}
}

/**
 * check inventory from social screen
 */
@SuppressWarnings("all")  // social screen must be opened in a game, so world must not be null
@Mixin(FilterListEntry.class)
abstract class HookFilterListEntry
{
	@Inject(method = "func_244751_b", at = @At(value = "HEAD"), cancellable = true)
	private void i(FilterManager filtermanager, UUID uuid, String name, Button button, CallbackInfo info)
	{
		if(Screen.hasShiftDown())
			info.cancel();
		else
			return;
		PlayerEntity player = Minecraft.getInstance().world.getPlayerByUuid(uuid);
		Forgetest.checkInv(player, false);
	}

}

@Mixin(LivingEntity.class)
abstract class HookLivingEntity
{
	@Inject(method = "isPotionActive", at = @At("HEAD"), cancellable = true)
	private void i(Effect effect, CallbackInfoReturnable<Boolean> info)
	{
		if(effect == Effects.BLINDNESS)
			info.setReturnValue(false);
	}

	@Inject(method = "getAlwaysRenderNameTagForRender", at = @At("HEAD"), cancellable = true)
	private void t(CallbackInfoReturnable<Boolean> info)
	{
		info.setReturnValue(true);
	}

}

@Mixin(EntityRenderer.class)
abstract class HookEntityRenderer
{
	@Inject(method = "renderName", at = @At("HEAD"))
	private void i(Entity entityIn, ITextComponent nameIn, MatrixStack matrixStack, IRenderTypeBuffer buffer, int packedLight, CallbackInfo ci)
	{
		if(!(entityIn instanceof LivingEntity) || !(nameIn instanceof IFormattableTextComponent))
			return;
		LivingEntity living = (LivingEntity)entityIn;
		IFormattableTextComponent name = (IFormattableTextComponent)nameIn;
		float health = living.getHealth();
		int maxHealth = (int)living.getMaxHealth();
		String healthText;
		if(health / maxHealth >= 0.66)
			healthText = " " + TextFormatting.GREEN + String.format("%.2f", health) + " / " + maxHealth;
		else if(health / maxHealth >= 0.33)
			healthText = " " + TextFormatting.GOLD + String.format("%.2f", health) + " / " + maxHealth;
		else
			healthText = " " + TextFormatting.RED + String.format("%.2f", health) + " / " + maxHealth;
		name.appendSibling(new StringTextComponent(healthText));
		nameIn = name;
	}
}

@Mixin(AbstractBlock.AbstractBlockState.class)
abstract class HookBlockState
{
	@Shadow
	public abstract Block getBlock();

	@Inject(method = "getRenderType", at = @At("HEAD"), cancellable = true)
	private void i(CallbackInfoReturnable<BlockRenderType> info)
	{
		if(Keys.xray)
		{
			if(!Keys.enabledBlocks.contains(getBlock()))
				info.setReturnValue(BlockRenderType.INVISIBLE);
		}
	}

	@Inject(method = "getLightValue", at = @At("HEAD"), cancellable = true)
	private void light(CallbackInfoReturnable<Integer> info)
	{
		if(Keys.xray)
			info.setReturnValue(15);
	}

	@Inject(method = "isOpaqueCube", at = @At("HEAD"), cancellable = true)
	private void opaque(IBlockReader reader, BlockPos pos, CallbackInfoReturnable<Boolean> info)
	{
		if(Keys.xray)
			info.setReturnValue(true);
	}
}

@Mixin(TileEntityRendererDispatcher.class)
abstract class HookTileEntityRenderer
{
	@Inject(method = "render", at = @At("HEAD"), cancellable = true)
	private static void i(TileEntityRenderer<TileEntity> renderer,
						  TileEntity tileEntity,
						  float partialTicks,
						  MatrixStack matrixStack,
						  IRenderTypeBuffer buffer,
						  CallbackInfo info)
	{
		if(Keys.xray)
		{
			if(!Keys.enabledBlocks.contains(tileEntity.getBlockState().getBlock()))
			{
				info.cancel();
			}
		}
	}
}

@Mixin(FluidBlockRenderer.class)
abstract class HookFluidBlockRenderer
{
	@Inject(method = "render", at = @At("HEAD"), cancellable = true)
	private void i(IBlockDisplayReader reader, BlockPos pos, IVertexBuilder builder, FluidState state, CallbackInfoReturnable<Boolean> info)
	{
		if(Keys.xray)
		{
			if(!Keys.enabledBlocks.contains(reader.getBlockState(pos).getBlock())) //if block is not included, hide it.
			{
				info.setReturnValue(false);
			}
		}
	}
}

@Mixin(Block.class)
abstract class HookBlock
{
	@Inject(method = "shouldSideBeRendered", at = @At("HEAD"), cancellable = true)
	private static void i(BlockState blockState, IBlockReader reader, BlockPos pos, Direction dir, CallbackInfoReturnable<Boolean> info)
	{
		if(Keys.xray)
			info.setReturnValue(true);
	}
}

@Mixin(WorldRenderer.class)
abstract class HookWorldRenderer
{
	@ModifyArg(method = "updateCameraAndRender",at = @At(value = "INVOKE",target = "Lnet/minecraft/client/renderer/WorldRenderer;setupTerrain(Lnet/minecraft/client/renderer/ActiveRenderInfo;Lnet/minecraft/client/renderer/culling/ClippingHelper;ZIZ)V"),index = 4)
	private boolean d(ActiveRenderInfo activeRenderInfoIn, ClippingHelper camera, boolean debugCamera, int frameCount, boolean playerSpectator)
	{
		if(Keys.xray)
			return true;
		else
			return playerSpectator;
	}
}