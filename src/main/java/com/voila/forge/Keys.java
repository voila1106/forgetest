package com.voila.forge;

import net.minecraft.block.*;
import net.minecraft.client.*;
import net.minecraft.client.settings.*;
import net.minecraft.entity.ai.attributes.*;
import net.minecraft.entity.player.*;
import net.minecraft.util.*;
import net.minecraft.util.text.*;
import net.minecraftforge.client.event.*;
import net.minecraftforge.eventbus.api.*;
import net.minecraftforge.fml.client.registry.*;
import net.minecraftforge.registries.*;
import org.lwjgl.glfw.*;

import java.io.*;
import java.util.*;

public class Keys
{
	public static KeyBinding upKey;
	public static KeyBinding downKey;
	public static KeyBinding toggleKey;
	public static KeyBinding lightKey;
	public static KeyBinding xrayKey;
	public static KeyBinding configXrayKey;


	public static boolean xray = false;
	public static Set<Block> enabledBlocks = new HashSet<>();

//	private static final Logger LOGGER = LogManager.getLogger();

	public static void init()
	{
		upKey = register("key." + Forgetest.ID + ".flySpeedUp", 61, "key.categories.movement");
		downKey = register("key." + Forgetest.ID + ".flySpeedDown", 45, "key.categories.movement");
		toggleKey = register("key." + Forgetest.ID + ".toggleFly", 92, "key.categories.movement");
		lightKey = register("key." + Forgetest.ID + ".light", 76, "key.categories.misc");
		xrayKey = register("key." + Forgetest.ID + ".xray", 66, "key.categories.misc");
		configXrayKey = register("key." + Forgetest.ID + ".configXray", GLFW.GLFW_KEY_H, "key.categories.misc");


		new File("config/" + Forgetest.ID).mkdirs();
	}

	private static KeyBinding register(String name, int code, String category)
	{
		KeyBinding key = new KeyBinding(name, code, category);
		ClientRegistry.registerKeyBinding(key);
		return key;
	}

	@SubscribeEvent
	public static void onKeyDown(InputEvent.KeyInputEvent event)
	{
//		LOGGER.info(event.getKey()+" "+event.getAction());
		PlayerEntity player = Minecraft.getInstance().player;
		if(player == null)
			return;
		PlayerAbilities ability = player.abilities;
		if(upKey.isPressed())
		{
			ability.setFlySpeed(ability.getFlySpeed() + 0.005F);
		}else if(downKey.isPressed())
		{
			ability.setFlySpeed(ability.getFlySpeed() - 0.005F);
		}else if(toggleKey.isPressed())
		{
			ability.setFlySpeed(0.05F);
			ability.allowFlying = !ability.allowFlying;
		}
		if(lightKey.isPressed())
		{
			Minecraft.getInstance().gameSettings.gamma = Minecraft.getInstance().gameSettings.gamma > 1d ? 1 : 30;
		}
		if(xrayKey.isPressed())
		{
			toggleXray();
		}
		if(configXrayKey.isPressed())
		{
//			try
//			{
//				Runtime.getRuntime().exec("cmd /c "+new File("config/"+Forgetest.ID+"/xray.txt").getAbsolutePath());
//			}catch(IOException e)
//			{
//				e.printStackTrace();
//			}

			Minecraft mc = Minecraft.getInstance();
			if(mc.currentScreen == null)
			{
				mc.displayGuiScreen(new ConfigXrayScreen());
			}
		}
	}

	private static void toggleXray()
	{
		if(!xray) //will enable
		{
			enabledBlocks=ConfigXrayScreen.readConfig();
		}
		Minecraft.getInstance().worldRenderer.loadRenderers();
		xray = !xray;

	}

}