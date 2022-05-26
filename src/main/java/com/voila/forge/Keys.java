package com.voila.forge;

import net.minecraft.*;
import net.minecraft.client.*;
import net.minecraft.client.gui.screens.social.*;
import net.minecraft.client.player.*;
import net.minecraft.locale.*;
import net.minecraft.world.entity.player.*;
import net.minecraft.world.level.block.*;
import net.minecraftforge.client.*;
import net.minecraftforge.client.event.*;
import net.minecraftforge.eventbus.api.*;
import org.lwjgl.glfw.*;

import java.io.*;
import java.lang.reflect.*;
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


	public static boolean xray = false;
	public static Set<Block> enabledBlocks = new HashSet<>();
	public static Script runningScript;

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
		LocalPlayer player = Minecraft.getInstance().player;
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
			Minecraft.getInstance().options.gamma = Minecraft.getInstance().options.gamma > 0.5 ? 0.5 : 30;
		}
		if(xrayKey.consumeClick()){
			toggleXray();
		}
		if(configXrayKey.consumeClick()){
			Minecraft mc = Minecraft.getInstance();
			if(mc.screen == null){
				mc.setScreen(new ConfigXrayScreen());
			}
		}
		if(scriptKey.consumeClick()){
			toggleScript();
		}
		if(configScriptKey.consumeClick()){
			Minecraft.getInstance().setScreen(new SelectScriptScreen());
		}


	}

	private static void toggleXray(){
		if(!xray){          //will enable
			enabledBlocks = ConfigXrayScreen.readConfig();
		}
		Minecraft.getInstance().levelRenderer.allChanged();
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
