package com.voila.forge;

import net.minecraft.block.*;
import net.minecraft.client.*;
import net.minecraft.client.entity.player.*;
import net.minecraft.client.settings.*;
import net.minecraft.entity.player.*;
import net.minecraft.util.text.*;
import net.minecraftforge.client.event.*;
import net.minecraftforge.eventbus.api.*;
import net.minecraftforge.fml.client.registry.*;
import org.lwjgl.glfw.*;

import java.io.*;
import java.util.*;

public class Keys {
	public static KeyBinding upKey;
	public static KeyBinding downKey;
	public static KeyBinding toggleKey;
	public static KeyBinding lightKey;
	public static KeyBinding xrayKey;
	public static KeyBinding configXrayKey;
	public static KeyBinding scriptKey;
	public static KeyBinding configScriptKey;


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

	private static KeyBinding register(String name, int code, String category){
		KeyBinding key = new KeyBinding(name, code, category);
		ClientRegistry.registerKeyBinding(key);
		return key;
	}

	@SubscribeEvent
	public static void onKeyDown(InputEvent.KeyInputEvent event){
//		LOGGER.info(event.getKey()+" "+event.getAction());
		ClientPlayerEntity player = Minecraft.getInstance().player;
		if(player == null)
			return;
		PlayerAbilities ability = player.abilities;
		if(upKey.isPressed()){
			ability.setFlySpeed(ability.getFlySpeed() + 0.005F);
		}else if(downKey.isPressed()){
			ability.setFlySpeed(ability.getFlySpeed() - 0.005F);
		}else if(toggleKey.isPressed()){
			ability.setFlySpeed(0.05F);
			ability.allowFlying = !ability.allowFlying;
		}
		if(lightKey.isPressed()){
			Minecraft.getInstance().gameSettings.gamma = Minecraft.getInstance().gameSettings.gamma > 0.5 ? 0.5 : 30;
		}
		if(xrayKey.isPressed()){
			toggleXray();
		}
		if(configXrayKey.isPressed()){
			Minecraft mc = Minecraft.getInstance();
			if(mc.currentScreen == null){
				mc.displayGuiScreen(new ConfigXrayScreen());
			}
		}
		if(scriptKey.isPressed()){
			toggleScript();
		}
		if(configScriptKey.isPressed()){
			Minecraft.getInstance().displayGuiScreen(new SelectScriptScreen());
		}
	}

	private static void toggleXray(){
		if(!xray){          //will enable
			enabledBlocks = ConfigXrayScreen.readConfig();
		}
		Minecraft.getInstance().worldRenderer.loadRenderers();
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
			Forgetest.sendMessage(TextFormatting.RED + e.getMessage());
		}


	}

}
