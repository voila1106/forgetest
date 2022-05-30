package com.voila.forge;

import net.minecraft.Util;
import net.minecraft.*;
import net.minecraft.client.*;
import net.minecraft.client.gui.screens.*;
import net.minecraft.client.player.*;
import net.minecraft.core.*;
import net.minecraft.nbt.*;
import net.minecraft.network.chat.*;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.player.*;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.*;
import net.minecraft.world.level.block.*;
import net.minecraftforge.api.distmarker.*;
import net.minecraftforge.client.event.*;
import net.minecraftforge.common.*;
import net.minecraftforge.event.*;
import net.minecraftforge.event.entity.player.*;
import net.minecraftforge.eventbus.api.*;
import net.minecraftforge.fml.*;
import net.minecraftforge.fml.common.*;
import net.minecraftforge.fml.event.lifecycle.*;
import net.minecraftforge.fml.javafmlmod.*;
import org.apache.logging.log4j.*;
import org.jetbrains.annotations.*;

import java.util.*;
import java.util.stream.*;

// The value here should match an entry in the META-INF/mods.toml file
@Mod("forgetest")
public class Forgetest {
	public static String ID = "forgetest";
	private static Map<Integer, List<Runnable>> delayedTask = new HashMap<>();

	// Directly reference a log4j logger.
	private static final Logger LOGGER = LogManager.getLogger();

	public static String last;
	@Nullable
	public static KeyMapping ofZoom;

	public Forgetest(){
		// Register the setup method for modloading
		IEventBus eventBus = FMLJavaModLoadingContext.get().getModEventBus();

		TestItem.registry(eventBus);

		eventBus.addListener(this::setup);
		// Register the enqueueIMC method for modloading
		eventBus.addListener(this::enqueueIMC);
		// Register the processIMC method for modloading
		eventBus.addListener(this::processIMC);
		// Register the doClientStuff method for modloading
		eventBus.addListener(this::doClientStuff);

		//eventBus.addListener(this::onHurt);

		// Register ourselves for server and other game events we are interested in
		MinecraftForge.EVENT_BUS.register(this);
		MinecraftForge.EVENT_BUS.register(Keys.class);
	}

	private void setup(final FMLCommonSetupEvent event){
		// some preinit code
		LOGGER.info("HELLO FROM PREINIT");
		LOGGER.info("DIRT BLOCK >> {}", Blocks.DIRT.getRegistryName());
	}

	private void doClientStuff(final FMLClientSetupEvent event){
		Keys.init();
	}

	private void enqueueIMC(final InterModEnqueueEvent event){
		// some example code to dispatch IMC to another mod
		InterModComms.sendTo("forgetest", "helloworld", () ->
		{
			LOGGER.info("Hello world from the MDK");
			return "Hello world";
		});
	}

	private void processIMC(final InterModProcessEvent event){
		// some example code to receive and process InterModComms from other mods
		LOGGER.info("Got IMC {}", event.getIMCStream().
			map(m -> m.getMessageSupplier().get()).
			collect(Collectors.toList()));
	}


	@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD)
	public static class RegistryEvents {
		@SubscribeEvent
		public static void onBlocksRegistry(final RegistryEvent.Register<Block> blockRegistryEvent){
			// register a new block here
			LOGGER.info("HELLO from Register Block");
		}
	}

	@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
	public static class RegisterParticle {
		@SubscribeEvent
		public static void onParticleRegistry(ParticleFactoryRegisterEvent event){
			Minecraft.getInstance().particleEngine.register(TestItem.damage.get(), DamageParticle.Factory::new);
		}
	}

	/**
	 * remove burning and underwater overlay
	 */
	@SubscribeEvent
	public void onOverlay(RenderBlockOverlayEvent event){
		//if(event.getOverlayType() != RenderBlockOverlayEvent.OverlayType.BLOCK)
			event.setCanceled(true);
	}

	@SubscribeEvent
	public void onInteractEntity(PlayerInteractEvent.EntityInteract event){
		if(event.getTarget() instanceof Player)
			checkInv(event.getTarget(), true);
	}

	public static void checkInv(Entity target, boolean fromEvent){
		LocalPlayer me = Minecraft.getInstance().player;
		if(me == null || !(target instanceof Player player)){
			sendMessage(ChatFormatting.RED + "玩家无效");
			return;
		}
		if(fromEvent && !me.isCrouching()){
			return;
		}
		Inventory inv = player.getInventory();
		MenuScreens.create(MenuType.GENERIC_9x5, Minecraft.getInstance(), "spy".hashCode(), player.getName());
		AbstractContainerMenu container = me.containerMenu;
		ItemStack frame = Items.GRAY_STAINED_GLASS_PANE.getDefaultInstance().setHoverName(TextComponent.EMPTY);
		frame.getOrCreateTag().put("isFrame", ByteTag.valueOf(true));
		NonNullList<Slot> slots = container.slots;
		for(int i = 0; i < 45; i++){
			Slot t = slots.get(i);
			t.set(frame.copy());
		}
		container.getSlot(1).set(frame.copy().setHoverName(name("头盔")));
		container.getSlot(3).set(frame.copy().setHoverName(name("胸甲")));
		container.getSlot(5).set(frame.copy().setHoverName(name("护腿")));
		container.getSlot(7).set(frame.copy().setHoverName(name("靴子")));
		container.getSlot(21).set(frame.copy().setHoverName(name("副手")));
		container.getSlot(23).set(frame.copy().setHoverName(name("主手")));
		container.getSlot(29).set(frame.copy().setHoverName(name("副手")));
		container.getSlot(33).set(frame.copy().setHoverName(name("主手")));
		container.getSlot(39).set(frame.copy().setHoverName(name("副手")));
		container.getSlot(41).set(frame.copy().setHoverName(name("主手")));


		List<ItemStack> armor = inv.armor;
		container.getSlot(10).set(armor.get(3).copy());
		container.getSlot(12).set(armor.get(2).copy());
		container.getSlot(14).set(armor.get(1).copy());
		container.getSlot(16).set(armor.get(0).copy());

		List<ItemStack> main = inv.items;
		container.getSlot(32).set(main.get(0).copy());

		List<ItemStack> off = inv.offhand;
		container.getSlot(30).set(off.get(0).copy());

	}

	private static Component name(String str){
		return Component.Serializer.fromJson("{\n" +
			"    \"text\":\"" + str + "\",\n" +
			"    \"italic\":false,\n" +
			"    \"color\":\"aqua\"\n" +
			"}");
	}

	public static void sendMessage(String msg){
		Minecraft.getInstance().gui.handleChat(ChatType.SYSTEM, new TextComponent(msg), Util.NIL_UUID);
	}

	public static void sendMessage(Component msg){
		Minecraft.getInstance().gui.handleChat(ChatType.SYSTEM, msg, Util.NIL_UUID);
	}

	@OnlyIn(Dist.CLIENT)
	@SubscribeEvent
	public void tick(TickEvent.ClientTickEvent event){
		if(event.phase == TickEvent.Phase.START)
			return;
		Map<Integer, List<Runnable>> map = new HashMap<>();
		for(int i : delayedTask.keySet()){
			if(i <= 0){
				delayedTask.get(i).forEach(Runnable::run);
			}else{
				map.put(i - 1, delayedTask.get(i));
			}

		}
		delayedTask = map;
	}

	public static void runDelay(int ticks, Runnable task){
//		delayedTask.put(ticks, task);
		if(delayedTask.get(ticks) == null){
			ArrayList<Runnable> list = new ArrayList<>();
			list.add(task);
			delayedTask.put(ticks, list);
		}else{
			delayedTask.get(ticks).add(task);
		}
	}

	public static void runDelay(Map<Integer, List<Runnable>> task){
		delayedTask.putAll(task);
	}

	public static int getNameDistance(){
		LocalPlayer player = Minecraft.getInstance().player;
		int distance = 25;
		if(ofZoom != null && ofZoom.isDown()){
			distance *= 2;
		}
		assert player != null;
		if(player.isScoping()){
			distance *= 2;
		}
		return distance;
	}


}
