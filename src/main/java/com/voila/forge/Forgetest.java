package com.voila.forge;

import com.google.common.reflect.*;
import com.mojang.blaze3d.systems.*;
import com.mojang.blaze3d.vertex.*;
import net.minecraft.*;
import net.minecraft.client.*;
import net.minecraft.client.gui.screens.*;
import net.minecraft.client.player.*;
import net.minecraft.client.renderer.*;
import net.minecraft.core.*;
import net.minecraft.nbt.*;
import net.minecraft.network.chat.*;
import net.minecraft.util.*;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.player.*;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.*;
import net.minecraft.world.phys.*;
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

import java.io.*;
import java.lang.annotation.*;
import java.lang.reflect.*;
import java.nio.file.*;
import java.util.*;
import java.util.stream.*;

// The value here should match an entry in the META-INF/mods.toml file
@Mod("forgetest")
public class Forgetest {
	public static String ID = "forgetest";
	private static Map<Integer, List<Runnable>> delayedTask = new HashMap<>();

	private static final Logger LOGGER = LogManager.getLogger();

	public static String last;
	public static boolean removeUseDelay;
	public static boolean removeDestroyDelay;
	public static Map<Shape,Vec3> shapes=new HashMap<>();

	@Nullable
	public static KeyMapping ofZoom;

	public Forgetest(){
		// Register the setup method for modloading
		IEventBus eventBus = FMLJavaModLoadingContext.get().getModEventBus();

		TestItem.registry(eventBus);

		eventBus.addListener(this::enqueueIMC);
		eventBus.addListener(this::processIMC);
		eventBus.addListener(this::doClientStuff);
		MinecraftForge.EVENT_BUS.register(this);
		MinecraftForge.EVENT_BUS.register(Keys.class);

		removeUseDelay = Boolean.parseBoolean(getConfig("removeUseDelay"));
		removeDestroyDelay = Boolean.parseBoolean(getConfig("removeDestroyDelay"));

		StringBuilder sb=new StringBuilder("Re-written methods: ").append('\n');
		try{
			Set<Class<?>> mixinClasses = findAllClasses(getClass().getPackageName()+".mixin");
			for(Class<?> clazz:mixinClasses){
				for(Method m:clazz.getDeclaredMethods()){
					ms:
					for(Annotation a:m.getAnnotations()){
						for(Class<?> c:a.getClass().getInterfaces()){
							if(c.getName().equals(getClass().getPackageName()+".Rewrite")){
								sb.append(m).append('\n');
								break ms;
							}
						}


					}
				}
			}
			Files.write(new File("config/"+ID+"/rewrite.txt").toPath(),sb.toString().getBytes());
		}catch(IOException e){
			e.printStackTrace();
		}

	}

	public Set<Class<?>> findAllClasses(String packageName) throws IOException {
		return ClassPath.from(ClassLoader.getSystemClassLoader())
			.getAllClasses()
			.stream()
			.filter(clazz -> clazz.getPackageName()
				.equalsIgnoreCase(packageName))
			.map(ClassPath.ClassInfo::load)
			.collect(Collectors.toSet());
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
			sendMessage(Component.translatable("msg."+ID+".invalidPlayer").withStyle(ChatFormatting.RED));
			return;
		}
		if(fromEvent && !me.isCrouching()){
			return;
		}
		Inventory inv = player.getInventory();
		MenuScreens.create(MenuType.GENERIC_9x5, Minecraft.getInstance(), "spy".hashCode(), player.getName());
		AbstractContainerMenu container = me.containerMenu;
		ItemStack frame = Items.GRAY_STAINED_GLASS_PANE.getDefaultInstance().setHoverName(Component.empty());
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
		Minecraft.getInstance().gui.getChat().addMessage(Component.literal(msg));
	}

	public static void sendMessage(Component msg){
		Minecraft.getInstance().gui.getChat().addMessage(msg);
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

	public static String getConfig(String key){
		new File("config/" + ID + "/").mkdirs();
		File config = new File("config/" + ID + "/config.txt");
		try{
			config.createNewFile();
			String line;
			BufferedReader br = new BufferedReader(new FileReader(config));
			while((line = br.readLine()) != null){
				String[] c = line.split("=", 2);
				if(c.length < 2)
					continue;
				if(c[0].equals(key)){
					return c[1];
				}
			}
			br.close();
		}catch(IOException e){
			e.printStackTrace();
		}
		return "";
	}

	public static void setConfig(String key, String value){
		new File("config/" + ID + "/").mkdirs();
		File config = new File("config/" + ID + "/config.txt");
		try{
			config.createNewFile();
			String line;
			boolean found = false;
			StringBuilder sb = new StringBuilder();
			BufferedReader br = new BufferedReader(new FileReader(config));
			while((line = br.readLine()) != null){
				String[] c = line.split("=", 2);
				if(c.length < 2)
					continue;
				if(c[0].equals(key)){
					c[1] = value;
					found = true;
				}
				sb.append(c[0]).append('=').append(c[1]).append('\n');
			}
			if(!found){
				sb.append(key).append('=').append(value).append('\n');
			}
			br.close();
			FileWriter fr = new FileWriter(config);
			fr.write(sb.toString());
			fr.flush();
			fr.close();
		}catch(IOException e){
			e.printStackTrace();
		}
	}

	public static void renderShape(PoseStack stack, Shape shape, double x, double y, double z){
		RenderSystem.enableDepthTest();
		RenderSystem.setShader(GameRenderer::getPositionColorShader);
		VertexConsumer buffer= Minecraft.getInstance().renderBuffers().bufferSource().getBuffer(RenderType.LINES);
		RenderSystem.disableTexture();
		RenderSystem.disableBlend();
		RenderSystem.lineWidth(1.0F);
		Vec3 pos=Minecraft.getInstance().gameRenderer.getMainCamera().getPosition();
		float r=shape.getColor().x();
		float g=shape.getColor().y();
		float b=shape.getColor().z();
		float a=shape.getColor().w();

		shape.forAllEdges((minX, minY, minZ, maxX, maxY, maxZ) -> {
			float f = (float)(maxX-minX);
			float f1 = (float)(maxY-minY);
			float f2 = (float)(maxZ-minZ);
			float f3 = Mth.sqrt(f * f + f1 * f1 + f2 * f2);
			f /= f3;
			f1 /= f3;
			f2 /= f3;

			buffer.vertex(stack.last().pose(), (float)(minX + x-pos.x), (float)(minY + y-pos.y),(float)(minZ + z-pos.z)).color(r, g, b, a).normal(stack.last().normal(),f,f1,f2).endVertex();
			buffer.vertex(stack.last().pose(),(float)(maxX + x-pos.x), (float)(maxY + y-pos.y),(float)(maxZ + z-pos.z)).color(r, g, b, a).normal(stack.last().normal(),f,f1,f2).endVertex();
		});
		RenderSystem.enableBlend();
		RenderSystem.enableTexture();
	}

	@SubscribeEvent
	public void onScroll(InputEvent.MouseScrollEvent event){
		if(Minecraft.getInstance().player.isScoping()){
			Keys.scopingScale= (float)Mth.clamp(Keys.scopingScale-event.getScrollDelta()*0.05,0.05,1.2);
			//System.out.println("scale: "+Keys.scopingScale+" fovModifier: "+Minecraft.getInstance().player.getFieldOfViewModifier());
			event.setCanceled(true);
		}
	}

}
