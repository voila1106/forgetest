package com.voila.forge;

import net.minecraft.*;
import net.minecraft.client.*;
import net.minecraft.client.player.*;
import net.minecraft.world.*;
import net.minecraft.world.entity.player.*;
import net.minecraft.world.level.block.*;
import net.minecraft.world.phys.*;
import net.minecraftforge.client.*;
import net.minecraftforge.client.event.*;
import net.minecraftforge.eventbus.api.*;
import org.lwjgl.glfw.*;

import javax.annotation.*;
import java.io.*;
import java.util.*;

public class Keys {
	public static KeyMapping upKey;
	public static KeyMapping downKey;
	public static KeyMapping toggleKey;
	public static KeyMapping lightKey;
	public static KeyMapping xrayKey;
	public static KeyMapping configXrayKey;
	public static KeyMapping scriptKey;
	public static KeyMapping configScriptKey;
	public static KeyMapping zoomKey;
	public static KeyMapping clickAboveKey;
	public static KeyMapping clickBelowKey;


	public static boolean xray = false;
	public static boolean scoping=false;
	public static float scopingScale=0.15f;
	public static Set<Block> enabledBlocks = new HashSet<>();
	@Nullable
	public static Script runningScript;

	private static Minecraft mc;

//	private static final Logger LOGGER = LogManager.getLogger();

	public static void init(){
		upKey = register("key." + Forgetest.ID + ".flySpeedUp", 61, "key.categories.movement");
		downKey = register("key." + Forgetest.ID + ".flySpeedDown", 45, "key.categories.movement");
		toggleKey = register("key." + Forgetest.ID + ".toggleFly", 92, "key.categories.movement");
		lightKey = register("key." + Forgetest.ID + ".light", 76, "key.categories.misc");
		xrayKey = register("key." + Forgetest.ID + ".xray", 66, "key.categories.misc");
		configXrayKey = register("key." + Forgetest.ID + ".configXray", GLFW.GLFW_KEY_H, "key.categories.misc");
		scriptKey = register("key." + Forgetest.ID + ".script", GLFW.GLFW_KEY_Y, "key.categories.misc");
		configScriptKey = register("key." + Forgetest.ID + ".configScript", GLFW.GLFW_KEY_K, "key.categories.misc");
		zoomKey=register("key."+Forgetest.ID+".zoom",GLFW.GLFW_KEY_C,"key.categories.misc");
		clickAboveKey =register("key."+Forgetest.ID+".clickAbove",GLFW.GLFW_KEY_UP,"key.categories.misc");
		clickBelowKey =register("key."+Forgetest.ID+".clickBelow",GLFW.GLFW_KEY_DOWN,"key.categories.misc");

		new File("config/" + Forgetest.ID).mkdirs();
	}

	private static KeyMapping register(String name, int code, String category){
		KeyMapping key = new KeyMapping(name, code, category);
		ClientRegistry.registerKeyBinding(key);
		return key;
	}

	@SubscribeEvent
	public static void onKeyDown(InputEvent.KeyInputEvent event){
//		LOGGER.info(event.getKey()+" "+event.getAction());
		if(mc==null){
			mc=Minecraft.getInstance();
		}
		LocalPlayer player = mc.player;
		if(player == null)
			return;
		Abilities ability = player.getAbilities();
		if(upKey.consumeClick()){
			ability.setFlyingSpeed(ability.getFlyingSpeed() + 0.005F);
		}else if(downKey.consumeClick()){
			ability.setFlyingSpeed(ability.getFlyingSpeed() - 0.005F);
		}else if(toggleKey.consumeClick()){
			ability.setFlyingSpeed(0.05F);
			ability.mayfly = !ability.mayfly;
		}
		if(lightKey.consumeClick()){
			mc.options.gamma().set(mc.options.gamma().get() > 1 ? 1d : 30d);
		}
		if(xrayKey.consumeClick()){
			toggleXray();
		}
		if(configXrayKey.consumeClick()){
			if(mc.screen == null){
				mc.setScreen(new ConfigXrayScreen());
			}
		}
		if(scriptKey.consumeClick()){
			toggleScript();
		}
		if(configScriptKey.consumeClick()){
			mc.setScreen(new SelectScriptScreen());
		}
		if(event.getKey() == zoomKey.getKey().getValue() && mc.screen==null)
			scoping = event.getAction() != 0;
		if(clickBelowKey.consumeClick()){
			if(mc.hitResult instanceof BlockHitResult pointing && pointing.getType()== HitResult.Type.BLOCK){
				BlockHitResult below=new BlockHitResult(pointing.getLocation().add(0,-1,0),pointing.getDirection(),pointing.getBlockPos().below(),pointing.isInside());
				mc.gameMode.useItemOn(player,InteractionHand.MAIN_HAND,below);
			}
		}
		if(clickAboveKey.consumeClick()){
			if(mc.hitResult instanceof BlockHitResult pointing && pointing.getType()== HitResult.Type.BLOCK){
				BlockHitResult above=new BlockHitResult(pointing.getLocation().add(0,1,0),pointing.getDirection(),pointing.getBlockPos().above(),pointing.isInside());
				mc.gameMode.useItemOn(player,InteractionHand.MAIN_HAND,above);
			}
		}
	}

	private static void toggleXray(){
		if(!xray){          //will enable
			enabledBlocks = ConfigXrayScreen.readConfig();
		}
		mc.levelRenderer.allChanged();
		xray = !xray;
	}

	private static void toggleScript(){
		if(Script.enabled){
			runningScript.cancel();
			return;
		}
		ArrayList<String> cmd = new ArrayList<>();
		try{
			new File("scripts").mkdirs();
			File sc = new File("scripts/" + Script.filename);
			sc.createNewFile();
			BufferedReader br = new BufferedReader(new FileReader(sc));
			String line;
			while((line = br.readLine()) != null){
				cmd.add(line);
			}
			br.close();
			runningScript = new Script(cmd.toArray(new String[0])).run();
		}catch(Exception e){
			e.printStackTrace();
			Forgetest.sendMessage(ChatFormatting.RED + e.getMessage());
		}
	}

}
