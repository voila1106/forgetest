package com.voila.forge.mixin;

import com.google.common.collect.*;
import com.mojang.authlib.*;
import com.mojang.blaze3d.platform.*;
import com.mojang.blaze3d.systems.*;
import com.mojang.blaze3d.vertex.*;
import com.voila.forge.*;
import net.minecraft.*;
import net.minecraft.client.*;
import net.minecraft.client.gui.*;
import net.minecraft.client.gui.components.*;
import net.minecraft.client.gui.components.events.*;
import net.minecraft.client.gui.screens.*;
import net.minecraft.client.gui.screens.social.*;
import net.minecraft.client.multiplayer.*;
import net.minecraft.client.particle.*;
import net.minecraft.client.player.*;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.block.*;
import net.minecraft.client.renderer.blockentity.*;
import net.minecraft.client.renderer.culling.*;
import net.minecraft.client.renderer.entity.*;
import net.minecraft.client.resources.sounds.*;
import net.minecraft.client.sounds.*;
import net.minecraft.core.*;
import net.minecraft.core.particles.*;
import net.minecraft.network.*;
import net.minecraft.network.chat.*;
import net.minecraft.network.protocol.*;
import net.minecraft.sounds.*;
import net.minecraft.util.thread.*;
import net.minecraft.world.effect.*;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.decoration.*;
import net.minecraft.world.entity.player.*;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.*;
import net.minecraft.world.level.*;
import net.minecraft.world.level.biome.*;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.*;
import net.minecraft.world.level.block.state.*;
import net.minecraft.world.level.material.*;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.*;

import javax.annotation.*;
import java.lang.annotation.*;
import java.util.*;

@Mixin(ClientPacketListener.class)
public abstract class HookClientNetHandler {

}

/**
 * /.check command
 */
@Mixin(LocalPlayer.class)
abstract class HookClientPlayerEntity extends AbstractClientPlayer {
	private HookClientPlayerEntity(ClientLevel world, GameProfile profile){
		super(world, profile);
	}

	@Inject(method = "chat", at = @At("HEAD"), cancellable = true)
	private void chat(String message, CallbackInfo info){
		if(message.startsWith("/.check"))
			info.cancel();
		else
			return;
		String[] args = message.split(" ");
		if(args.length < 2){
			Forgetest.sendMessage(ChatFormatting.RED + "未输入玩家");
			return;
		}
		String name = args[1];
		for(Player t : clientLevel.players()){
			if(t.getName().getString().equals(name)){
				Forgetest.runDelay(5, () -> Forgetest.checkInv(t, false));
				return;
			}
		}
		Forgetest.sendMessage(ChatFormatting.RED + "找不到玩家");

	}
}

/**
 * disable damage screen shake
 */
@Mixin(GameRenderer.class)
abstract class HookGameRenderer {
	@Inject(method = "bobHurt", at = @At(value = "FIELD", target = "Lnet/minecraft/world/entity/LivingEntity;hurtDir:F"), cancellable = true)
	private void hurt(PoseStack matrix, float ticks, CallbackInfo ci){
		ci.cancel();
	}
}

/**
 * perform damage amount particle
 */
@Mixin(LivingEntityRenderer.class)
abstract class HookLivingRenderer {
	private Map<Integer, Float> hs = new HashMap<>();
	private Level world;

	@fold
	@Inject(method = "render(Lnet/minecraft/world/entity/LivingEntity;FFLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;I)V", at = @At("HEAD"))
	private void r(LivingEntity entity, float yaw, float ticks, PoseStack stack, MultiBufferSource buffer, int light, CallbackInfo info){
		if(!entity.level.equals(world)){
			hs = new HashMap<>();
			world = entity.level;
		}
		float health = entity.getHealth();
		if(!hs.containsKey(entity.hashCode())){
			hs.put(entity.hashCode(), health);
			return;
		}
		if(health < hs.get(entity.hashCode())){
			float damage = (hs.get(entity.hashCode()) - health);
			for(int i = 0; i < Math.min(100, damage); i++){
				Minecraft.getInstance().levelRenderer.addParticle(new DamageParticle.DamageParticleData((int)damage),
					true,
					entity.getX(),
					entity.getY(0.5),
					entity.getZ(), 0.1, 0, 0.1);
			}
		}
		hs.put(entity.hashCode(), health);
	}

	@Inject(method = "shouldShowName(Lnet/minecraft/world/entity/LivingEntity;)Z", at = @At("HEAD"), cancellable = true)
	private void t(LivingEntity entity, CallbackInfoReturnable<Boolean> info){
		LocalPlayer player = Minecraft.getInstance().player;
		if(player == null)
			return;
		if(!(entity instanceof ArmorStand) && player.position().distanceTo(entity.position()) < Forgetest.getNameDistance()){
			info.setReturnValue(true);
		}
	}
}

/**
 * remove some particles
 */
@Mixin(ParticleEngine.class)
abstract class HookParticleManager {
	@Inject(method = "createParticle", at = @At("HEAD"), cancellable = true)
	private void i(ParticleOptions particleData, double x, double y, double z, double xSpeed, double ySpeed, double zSpeed, CallbackInfoReturnable<Particle> info){
		if(particleData.equals(ParticleTypes.DAMAGE_INDICATOR) || particleData.equals(ParticleTypes.ELDER_GUARDIAN))
			info.setReturnValue(null);
	}
}

/**
 * no spam
 */
@Mixin(Gui.class)
abstract class HookIngameGui {
	@Shadow
	protected int screenWidth;

	@Shadow
	protected int screenHeight;


	@Inject(method = "handleChat", at = @At("HEAD"), cancellable = true)
	private void i(ChatType type, Component cmp, UUID uuid, CallbackInfo info){
		String str = cmp.getString();
		if(str.equals(Forgetest.last) && !(Minecraft.getInstance().screen instanceof ChatScreen)){
			info.cancel();
		}
		if(str.contains("登录") &&
			str.contains("密码") &&
			str.toLowerCase().contains("/l")){
			assert Minecraft.getInstance().player != null;
			Minecraft.getInstance().player.chat("/login 111111");
		}
		Forgetest.last = str;
	}

	@Inject(method = "renderHotbar", at = @At("HEAD"))
	private void h(float partialTicks, PoseStack stack, CallbackInfo info){
		Minecraft mc = Minecraft.getInstance();
		assert mc.player != null;
		if(mc.player.isSpectator())
			return;
		Inventory inv = mc.player.getInventory();
		ItemStack main = inv.getSelected();
		ItemStack off = inv.offhand.get(0);
		int left = screenWidth / 2 - 91;
		int top = screenHeight - 19;
		RenderSystem.disableBlend();
		if(main.getItem() != Items.AIR && !arrow(main,true,left,top)){
			int amount = main.getCount();
			boolean flag = false;
			NonNullList<ItemStack> items = inv.items;
			for(int i = 0; i < items.size(); i++){
				if(i == inv.selected)
					continue;
				ItemStack t = items.get(i);
				if(ItemStack.isSameItemSameTags(main, t)){
					amount += t.getCount();
					flag = true;
				}
			}
			if(ItemStack.isSameItemSameTags(main, off)){
				amount += off.getCount();
				flag = true;
			}
			if(flag){
				mc.getItemRenderer().renderAndDecorateItem(main, left + 182 + 20, top);
				mc.getItemRenderer().renderGuiItemDecorations(mc.font, main, left + 182 + 20, top, amount + "");
			}

		}
		if(off.getItem() != Items.AIR && !arrow(off,false,left,top)){
			int amount = off.getCount();
			boolean flag = false;
			for(ItemStack t : inv.items){
				if(ItemStack.isSameItemSameTags(off, t)){
					amount += t.getCount();
					flag = true;
				}
			}
			if(flag){
				mc.getItemRenderer().renderAndDecorateItem(off, left - 55, top);
				mc.getItemRenderer().renderGuiItemDecorations(mc.font, off, left - 55, top, amount + "");

			}
		}
		RenderSystem.enableBlend();

	}
	private static boolean arrow(ItemStack item, boolean mainHand, int left, int top){
		if(item.getItem()==Items.BOW || item.getItem() == Items.CROSSBOW){
			Minecraft mc=Minecraft.getInstance();
			LocalPlayer player=mc.player;
			assert player != null;
			Inventory inv= player.getInventory();
			int amount=0;
			for(ItemStack t: inv.items){
				if(t.getItem()==Items.ARROW)
					amount+=t.getCount();
			}
			if(inv.offhand.get(0).getItem()==Items.ARROW)
				amount+=inv.offhand.get(0).getCount();
			if(mainHand){
				mc.getItemRenderer().renderAndDecorateItem(Items.ARROW.getDefaultInstance(), left + 182 + 20, top);
				mc.getItemRenderer().renderGuiItemDecorations(mc.font, Items.ARROW.getDefaultInstance(), left + 182 + 20, top, amount + "");
			}else {
				mc.getItemRenderer().renderAndDecorateItem(Items.ARROW.getDefaultInstance(), left - 55, top);
				mc.getItemRenderer().renderGuiItemDecorations(mc.font, Items.ARROW.getDefaultInstance(), left - 55, top, amount + "");
			}

			return true;
		}

		return false;
	}


}

/**
 * reset spam state
 */
@Mixin(Minecraft.class)
abstract class HookMinecraft implements IMinecraft {
	@Shadow
	public LocalPlayer player;

	@Shadow
	protected abstract void startUseItem();


	@Shadow
	protected int missTime;

	@Shadow
	@Final
	@Mutable
	private User user;

	@Shadow
	protected abstract boolean startAttack();

	@Shadow
	protected abstract void pickBlock();

	@Override
	public void pick(){
		pickBlock();
	}


	@Override
	public void use(){
		startUseItem();
	}

	@Override
	public void attack(){
		startAttack();
		pick();
	}

	@Override
	public User getSession(){
		return user;
	}

	@Override
	public void setSession(User se){
		user = se;
	}

	@Inject(method = "setScreen", at = @At("HEAD"), cancellable = true)
	private void i(Screen guiScreenIn, CallbackInfo info){
		// reset spam state
		Forgetest.last = "";

		//respawn immediately
		if(guiScreenIn instanceof DeathScreen){
			player.respawn();
			info.cancel();
		}
	}

	@Inject(method = "startAttack", at = @At("HEAD"))
	private void i(CallbackInfoReturnable<Boolean> cir){
		missTime = 0;
	}
}

/*
 * fix score board NPE
 */
//@Mixin(Scoreboard.class)
//abstract class HookScoreBoard {
//	@Inject(method = "removeTeam", at = @At("HEAD"), cancellable = true)
//	private void i(ScorePlayerTeam playerTeam, CallbackInfo ci){
//		if(playerTeam == null)
//			ci.cancel();
//	}
//}

@Mixin(SharedConstants.class)
abstract class HookSharedConstants  // allow all characters
{
	@Inject(method = "isAllowedChatCharacter", at = @At("HEAD"), cancellable = true)
	private static void i(char character, CallbackInfoReturnable<Boolean> info){
		info.setReturnValue(true);
	}
}

@Mixin(SoundEngine.class)
class HookSoundEngine {
	@Inject(method = "play", at = @At("HEAD"), cancellable = true)
	private void i(SoundInstance sound, CallbackInfo info){
		if(sound.getLocation().equals(SoundEvents.PORTAL_AMBIENT.getLocation()))
			info.cancel();
		if(sound.getLocation().equals(SoundEvents.VILLAGER_HURT.getLocation())){
			if(Minecraft.getInstance().options.getSoundSourceVolume(SoundSource.HOSTILE) == 0f){
				info.cancel();
			}
		}
	}
}

/**
 * never raining
 */
@Mixin(Biome.class)
abstract class HookBiome {
	@Inject(method = "getPrecipitation", at = @At("HEAD"), cancellable = true)
	private void i(CallbackInfoReturnable<Biome.Precipitation> info){
		info.setReturnValue(Biome.Precipitation.NONE);
	}
}

/**
 * prevent taking frames
 */
@Mixin(MultiPlayerGameMode.class)
abstract class HookPlayerController {
	@SuppressWarnings("all")  // if hasTag() is true, getTag() must not be null
	@Inject(method = "handleInventoryMouseClick", at = @At("HEAD"), cancellable = true)
	private void i(int windowId, int slotId, int mouseButton, ClickType type, Player player, CallbackInfo info){
		if(slotId < 0)
			return;
		ItemStack item = player.containerMenu.getSlot(slotId).getItem();
		if(windowId == "spy".hashCode() && item.getItem().equals(Items.GRAY_STAINED_GLASS_PANE) && item.hasTag() && item.getTag().contains("isFrame"))
			info.cancel();
	}
}

/**
 * check inventory from social screen
 */
@Mixin(PlayerEntry.class)
abstract class HookPlayerEntry {

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

@Mixin(LivingEntity.class)
abstract class HookLivingEntity extends Entity {
	private HookLivingEntity(EntityType<?> p_i48580_1_, Level p_i48580_2_){
		super(p_i48580_1_, p_i48580_2_);
	}

	/**
	 * ignore blindness effect
	 */
	@Inject(method = "hasEffect", at = @At("HEAD"), cancellable = true)
	private void i(MobEffect effect, CallbackInfoReturnable<Boolean> info){
		if(effect == MobEffects.BLINDNESS)
			info.setReturnValue(false);
	}

	/**
	 * always render name tag to display health value (CustomNameVisible)
	 */
	@Inject(method = "shouldShowName", at = @At("HEAD"), cancellable = true)
	private void t(CallbackInfoReturnable<Boolean> info){
		LocalPlayer player = Minecraft.getInstance().player;
		if(player == null)
			return;
		if(!((Object)this instanceof ArmorStand) && position().distanceTo(player.position()) < Forgetest.getNameDistance())
			info.setReturnValue(true);
	}

}

/**
 * show health
 */
@Mixin(EntityRenderer.class)
abstract class HookEntityRenderer {
	@Inject(method = "renderNameTag", at = @At("HEAD"))
	private void i(Entity entityIn, Component nameIn, PoseStack matrixStack, MultiBufferSource buffer, int packedLight, CallbackInfo ci){
		if(!(entityIn instanceof LivingEntity living) || !(nameIn instanceof MutableComponent name) || (entityIn instanceof ArmorStand))
			return;
		float health = living.getHealth();
		int maxHealth = (int)living.getMaxHealth();
		String healthText;
		if(health / maxHealth >= 0.66)
			healthText = " " + ChatFormatting.GREEN + String.format("%.2f", health) + " / " + maxHealth;
		else if(health / maxHealth >= 0.33)
			healthText = " " + ChatFormatting.GOLD + String.format("%.2f", health) + " / " + maxHealth;
		else
			healthText = " " + ChatFormatting.RED + String.format("%.2f", health) + " / " + maxHealth;
		name.append(new TextComponent(healthText));
		nameIn = name;
	}
}

/**
 * Xray block
 */
@Mixin(BlockBehaviour.BlockStateBase.class)
abstract class HookBlockState {
	@Shadow
	public abstract Block getBlock();

	@Inject(method = "getRenderShape", at = @At("HEAD"), cancellable = true)
	private void i(CallbackInfoReturnable<RenderShape> info){
		if(Keys.xray && !Keys.enabledBlocks.contains(getBlock()))
			info.setReturnValue(RenderShape.INVISIBLE);
	}

	@Inject(method = "getLightEmission", at = @At("HEAD"), cancellable = true)
	private void light(CallbackInfoReturnable<Integer> info){
		if(Keys.xray)
			info.setReturnValue(15);
	}

	@Inject(method = "isSolidRender", at = @At("HEAD"), cancellable = true)
	private void opaque(BlockGetter reader, BlockPos pos, CallbackInfoReturnable<Boolean> info){
		if(Keys.xray)
			info.setReturnValue(true);
	}
}

/**
 * Xray Tile Entity
 */
@Mixin(BlockEntityRenderDispatcher.class)
abstract class HookTileEntityRenderer {
	@Shadow
	@Nullable
	public abstract <E extends BlockEntity> BlockEntityRenderer<E> getRenderer(E tileEntityIn);

	@Shadow
	private static void tryRender(BlockEntity tileEntityIn, Runnable runnableIn){
	}

	@Shadow
	private static <T extends BlockEntity> void setupAndRender(BlockEntityRenderer<T> p_112285_, T p_112286_, float p_112287_, PoseStack p_112288_, MultiBufferSource p_112289_){
	}

	@Inject(method = "setupAndRender", at = @At("HEAD"), cancellable = true)
	private static <T extends BlockEntity> void i(BlockEntityRenderer<T> tileRenderer, T tileEntity, float p_112287_, PoseStack p_112288_, MultiBufferSource p_112289_, CallbackInfo info){
		if(Keys.xray && !Keys.enabledBlocks.contains(tileEntity.getBlockState().getBlock())){
			info.cancel();
		}
	}

	/**
	 * ignore render distance when xray enabled
	 */
	@Inject(method = "render", at = @At("HEAD"), cancellable = true)
	private <E extends BlockEntity> void t(E tileEntityIn, float partialTicks, PoseStack matrixStackIn, MultiBufferSource bufferIn, CallbackInfo info){
		if(Keys.xray){
			info.cancel();
			BlockEntityRenderer<E> tileentityrenderer = this.getRenderer(tileEntityIn);
			if(tileentityrenderer != null){
				if(tileEntityIn.hasLevel() && tileEntityIn.getType().isValid(tileEntityIn.getBlockState())){
					tryRender(tileEntityIn, () ->
						setupAndRender(tileentityrenderer, tileEntityIn, partialTicks, matrixStackIn, bufferIn));
				}
			}

		}
	}
}

/**
 * Xray Fluid
 */
@Mixin(LiquidBlockRenderer.class)
abstract class HookFluidBlockRenderer {
	@Inject(method = "tesselate", at = @At("HEAD"), cancellable = true)
	private void i(BlockAndTintGetter reader, BlockPos pos, VertexConsumer p_203176_, BlockState p_203177_, FluidState p_203178_, CallbackInfoReturnable<Boolean> info){
		//if block is not included, hide it.
		if(Keys.xray && !Keys.enabledBlocks.contains(reader.getBlockState(pos).getBlock())){
			info.setReturnValue(false);
		}
	}

	/**
	 * render sides
	 */
	@Inject(method = "isFaceOccludedByState", at = @At("HEAD"), cancellable = true)
	private static void d(BlockGetter reader,
						  Direction direction,
						  float p_239284_2_,
						  BlockPos blockPos,
						  BlockState blockState,
						  CallbackInfoReturnable<Boolean> info){
		if(Keys.xray && !Keys.enabledBlocks.contains(reader.getBlockState(blockPos).getBlock()))
			info.setReturnValue(false);
	}
}

/**
 * compatible with OptiFine
 */
@Mixin(targets = "net.minecraft.client.renderer.chunk.ChunkRenderDispatcher$RenderChunk$RebuildTask")
abstract class HookChunkRender {
	//render model arg
	@fold
	@ModifyArg(method = "compile", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/block/BlockRenderDispatcher;renderBatched(Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/BlockAndTintGetter;Lcom/mojang/blaze3d/vertex/PoseStack;Lcom/mojang/blaze3d/vertex/VertexConsumer;ZLjava/util/Random;Lnet/minecraftforge/client/model/data/IModelData;)Z"), index = 5)
	public boolean renderModel(boolean checkSides){
		if(Keys.xray)
			return false;
		return checkSides;
	}
}

@Mixin(LevelRenderer.class)
abstract class HookWorldRenderer {
	//setupTerrain arg
	@fold
	@ModifyArg(method = "renderLevel", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/LevelRenderer;setupRender(Lnet/minecraft/client/Camera;Lnet/minecraft/client/renderer/culling/Frustum;ZZ)V"), index = 3)
	private boolean d(Camera p_194339_, Frustum p_194340_, boolean p_194341_, boolean playerSpectator){
		if(Keys.xray)
			return true;
		return playerSpectator;
	}
}

/**
 * speed doesn't effect fov
 */
@Mixin(AbstractClientPlayer.class)
abstract class HookAbsClientPlayer extends Player {
	private HookAbsClientPlayer(Level p_i241920_1_, BlockPos p_i241920_2_, float p_i241920_3_, GameProfile p_i241920_4_){
		super(p_i241920_1_, p_i241920_2_, p_i241920_3_, p_i241920_4_);
	}

	@Inject(method = "getFieldOfViewModifier", at = @At("HEAD"), cancellable = true)
	private void i(CallbackInfoReturnable<Float> info){
		float f = 1.0F;
		if(this.getAbilities().flying){
			f *= 1.1F;
		}

		f = (float)((double)f * (((isSprinting() ? 0.13 : 0.1) / 0.1 + 1.0D) / 2.0D));
		if(this.getAbilities().getWalkingSpeed() == 0.0F || Float.isNaN(f) || Float.isInfinite(f)){
			f = 1.0F;
		}

		if(this.isUsingItem() && this.getUseItem().getItem() == Items.BOW){
			int i = this.getTicksUsingItem();
			float f1 = (float)i / 20.0F;
			if(f1 > 1.0F){
				f1 = 1.0F;
			}else{
				f1 = f1 * f1;
			}

			f *= 1.0F - f1 * 0.15F;
		}else if(Minecraft.getInstance().options.getCameraType().isFirstPerson() && this.isScoping()){
			info.setReturnValue(0.1f);
			return;
		}
		float fov = net.minecraftforge.client.ForgeHooksClient.getFieldOfView(this, f);
		info.setReturnValue(fov);
	}
}

/**
 * add switch account button
 */
@Mixin(TitleScreen.class)
abstract class HookMainMenu extends Screen {

	private HookMainMenu(Component title){
		super(title);
	}

	@Inject(method = "init", at = @At("TAIL"))
	private void i(CallbackInfo info){
		Button optionButton = null;
		for(GuiEventListener t : children()){
			if(t instanceof Button b){
				if(new TranslatableComponent("menu.options").equals(b.getMessage())){
					optionButton = b;
					break;
				}
			}
		}
		if(optionButton == null){
			System.out.println("button not found");
			return;
		}
		Button switchButton = new Button(optionButton.x, optionButton.y + 24, optionButton.getWidth(), optionButton.getHeight(),
			new TranslatableComponent("menu." + Forgetest.ID + ".switchAccount"),
			button -> Minecraft.getInstance().setScreen(new SwitchAccountScreen(this)));

		addRenderableWidget(switchButton);

	}
}

/*
 * fix recipe book NPE crash (baritone)
 */
//@Mixin(RecipeBookGui.class)
//abstract class HookRecipeBookGui {
//	@Shadow
//	private TextFieldWidget searchBar;
//
//	@Inject(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/widget/TextFieldWidget;tick()V"), cancellable = true)
//	private void i(CallbackInfo info){
//		if(searchBar == null)
//			info.cancel();
//	}
//}


/** script press key */
@SuppressWarnings("all")
@Mixin(KeyMapping.class)
abstract class HookKeyBinding {
	@Inject(method = "isDown", at = @At("HEAD"), cancellable = true)
	private void i(CallbackInfoReturnable<Boolean> info){
		if(!Script.enabled){
			return;
		}
		Object key = this;
		Options settings = Minecraft.getInstance().options;
		if(Keys.runningScript.forward && key == settings.keyUp ||
			Keys.runningScript.backward && key == settings.keyDown ||
			Keys.runningScript.left && key == settings.keyLeft ||
			Keys.runningScript.right && key == settings.keyRight ||
			Keys.runningScript.jump && key == settings.keyJump ||
			Keys.runningScript.crouch && key == settings.keyShift ||
			Keys.runningScript.use && key == settings.keyUse ||
			Keys.runningScript.attack && key == settings.keyAttack){
			info.setReturnValue(true);
		}
	}

	/** Optifine zoom key */
	@Inject(method = "<init>(Ljava/lang/String;Lcom/mojang/blaze3d/platform/InputConstants$Type;ILjava/lang/String;)V", at = @At("RETURN"))
	private void reg(String name, InputConstants.Type p_90826_, int p_90827_, String p_90828_, CallbackInfo info){
		KeyMapping k = (KeyMapping)(Object)this;
		if(name.equals("of.key.zoom")){
			Forgetest.ofZoom = k;
		}
	}
}

/** Script waitTicks */
@Mixin(DebugScreenOverlay.class)
abstract class HookDebugGui extends GuiComponent {
	@Inject(method = "getGameInformation", at = @At("RETURN"), cancellable = true)
	private void i(CallbackInfoReturnable<List<String>> info){
		if(Script.enabled){
			List<String> list = info.getReturnValue();
			String str = "Script waitTicks: " + Keys.runningScript.waitTicks;
			list.add(str);
			info.setReturnValue(list);
		}
	}
}

/** make no sense, just make IDE can fold annotations to hide long sentence */
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.SOURCE)
@interface fold {
}