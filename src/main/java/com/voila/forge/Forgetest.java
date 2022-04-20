package com.voila.forge;

import net.minecraft.block.*;
import net.minecraft.client.*;
import net.minecraft.client.entity.player.*;
import net.minecraft.client.gui.*;
import net.minecraft.entity.*;
import net.minecraft.entity.player.*;
import net.minecraft.inventory.container.*;
import net.minecraft.item.*;
import net.minecraft.nbt.*;
import net.minecraft.util.Util;
import net.minecraft.util.text.*;
import net.minecraftforge.api.distmarker.*;
import net.minecraftforge.client.event.*;
import net.minecraftforge.common.*;
import net.minecraftforge.event.*;
import net.minecraftforge.event.entity.player.*;
import net.minecraftforge.eventbus.api.*;
import net.minecraftforge.fml.*;
import net.minecraftforge.fml.common.*;
import net.minecraftforge.fml.event.lifecycle.*;
import net.minecraftforge.fml.event.server.*;
import net.minecraftforge.fml.javafmlmod.*;
import org.apache.logging.log4j.*;

import java.util.*;
import java.util.stream.*;

// The value here should match an entry in the META-INF/mods.toml file
@Mod("forgetest")
public class Forgetest
{
	public static String ID = "forgetest";
	private static Map<Integer,Runnable> delayedTask=new HashMap<>();

	// Directly reference a log4j logger.
	private static final Logger LOGGER = LogManager.getLogger();

	public static String last;

	public Forgetest()
	{
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
		MinecraftForge.EVENT_BUS.register(FlyKey.class);
	}

	private void setup(final FMLCommonSetupEvent event)
	{
		// some preinit code
		LOGGER.info("HELLO FROM PREINIT");
		LOGGER.info("DIRT BLOCK >> {}", Blocks.DIRT.getRegistryName());
	}

	private void doClientStuff(final FMLClientSetupEvent event)
	{
		// do something that can only be done on the client
		LOGGER.info("Got game settings {}", event.getMinecraftSupplier().get().gameSettings);
		FlyKey.init();
	}

	private void enqueueIMC(final InterModEnqueueEvent event)
	{
		// some example code to dispatch IMC to another mod
		InterModComms.sendTo("forgetest", "helloworld", () ->
		{
			LOGGER.info("Hello world from the MDK");
			return "Hello world";
		});
	}

	private void processIMC(final InterModProcessEvent event)
	{
		// some example code to receive and process InterModComms from other mods
		LOGGER.info("Got IMC {}", event.getIMCStream().
			map(m -> m.getMessageSupplier().get()).
			collect(Collectors.toList()));
	}

	// You can use SubscribeEvent and let the Event Bus discover methods to call
	@SubscribeEvent
	public void onServerStarting(FMLServerStartingEvent event)
	{
		// do something when the server starts
		LOGGER.info("HELLO from server starting");
	}

	// You can use EventBusSubscriber to automatically subscribe events on the contained class (this is subscribing to the MOD
	// Event bus for receiving Registry Events)
	@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD)
	public static class RegistryEvents
	{
		@SubscribeEvent
		public static void onBlocksRegistry(final RegistryEvent.Register<Block> blockRegistryEvent)
		{
			// register a new block here
			LOGGER.info("HELLO from Register Block");
		}
	}

	@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
	public static class RegisterParticle
	{
		@SubscribeEvent
		public static void onParticleRegistry(ParticleFactoryRegisterEvent event)
		{
			Minecraft.getInstance().particles.registerFactory(TestItem.damage.get(), DamageParticle.Factory::new);
		}
	}

	/**
	 * remove burning and underwater overlay*/
	@SubscribeEvent
	public void onOverlay(RenderBlockOverlayEvent event)
	{
		if(event.getOverlayType() != RenderBlockOverlayEvent.OverlayType.BLOCK)
			event.setCanceled(true);
	}

	@SubscribeEvent
	public void onInteractEntity(PlayerInteractEvent.EntityInteract event)
	{
		checkInv(event.getTarget(),true);
	}

	public static void checkInv(Entity target,boolean fromEvent)
	{
		ClientPlayerEntity me=Minecraft.getInstance().player;
		if(me==null || !(target instanceof PlayerEntity))
		{
			sendMessage(TextFormatting.RED+ "玩家无效");
			return;
		}
		if(fromEvent && !me.isSneaking())
		{
			return;
		}
		PlayerEntity player=(PlayerEntity)target;
		PlayerInventory inv=player.inventory;
		ScreenManager.openScreen(ContainerType.GENERIC_9X5,Minecraft.getInstance(),"spy".hashCode(),player.getName());
		Container container=me.openContainer;
		ItemStack frame=Items.GRAY_STAINED_GLASS_PANE.getDefaultInstance().setDisplayName(StringTextComponent.EMPTY);
		frame.setTagInfo("isFrame", ByteNBT.valueOf(true));
		for(Slot t : container.inventorySlots)
		{
			container.putStackInSlot(t.getSlotIndex(),frame.copy());
		}
		container.putStackInSlot(1,frame.copy().setDisplayName(name("头盔")));
		container.putStackInSlot(3,frame.copy().setDisplayName(name("胸甲")));
		container.putStackInSlot(5,frame.copy().setDisplayName(name("护腿")));
		container.putStackInSlot(7,frame.copy().setDisplayName(name("靴子")));
		container.putStackInSlot(21,frame.copy().setDisplayName(name("副手")));
		container.putStackInSlot(23,frame.copy().setDisplayName(name("主手")));
		container.putStackInSlot(29,frame.copy().setDisplayName(name("副手")));
		container.putStackInSlot(33,frame.copy().setDisplayName(name("主手")));
		container.putStackInSlot(39,frame.copy().setDisplayName(name("副手")));
		container.putStackInSlot(41,frame.copy().setDisplayName(name("主手")));


		List<ItemStack> armor=inv.armorInventory;
		container.putStackInSlot(10,armor.get(3).copy());
		container.putStackInSlot(12,armor.get(2).copy());
		container.putStackInSlot(14,armor.get(1).copy());
		container.putStackInSlot(16,armor.get(0).copy());

		List<ItemStack> main=inv.mainInventory;
		container.putStackInSlot(32,main.get(0).copy());

		List<ItemStack> off=inv.offHandInventory;
		container.putStackInSlot(30,off.get(0).copy());

	}

	private static ITextComponent name(String str)
	{
		return ITextComponent.Serializer.getComponentFromJson("{\n" +
			"    \"text\":\""+str+"\",\n" +
			"    \"italic\":false,\n" +
			"    \"color\":\"aqua\"\n" +
			"}");
	}
	public static void sendMessage(String msg)
	{
		Minecraft.getInstance().ingameGUI.sendChatMessage(ChatType.SYSTEM, new StringTextComponent(msg), Util.DUMMY_UUID);
	}

	@OnlyIn(Dist.CLIENT)
	@SubscribeEvent
	public void tick(TickEvent.PlayerTickEvent event)
	{
		if(event.phase == TickEvent.Phase.START)
			return;
		Map<Integer,Runnable> map=new HashMap<>();
		for(int i : delayedTask.keySet())
		{
			if(i <= 0)
			{
				delayedTask.get(i).run();
				delayedTask.remove(i);
			}else
			{
				map.put(i - 1, delayedTask.get(i));
			}

		}
		delayedTask=map;
	}

	public static void runDelay(int ticks,Runnable task)
	{
		delayedTask.put(ticks,task);
	}

}
