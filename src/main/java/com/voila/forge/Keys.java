package com.voila.forge;

import net.minecraft.client.*;
import net.minecraft.client.settings.*;
import net.minecraft.entity.ai.attributes.*;
import net.minecraft.entity.player.*;
import net.minecraftforge.client.event.*;
import net.minecraftforge.eventbus.api.*;
import net.minecraftforge.fml.client.registry.*;

public class Keys
{
	public static KeyBinding upKey;
	public static KeyBinding downKey;
	public static KeyBinding toggleKey;
	public static KeyBinding lightKey;

//	private static final Logger LOGGER = LogManager.getLogger();

	public static void init()
	{
		upKey = register("key." + Forgetest.ID + ".flySpeedUp", 61, "key.categories.movement");
		downKey = register("key." + Forgetest.ID + ".flySpeedDown", 45, "key.categories.movement");
		toggleKey = register("key." + Forgetest.ID + ".toggleFly", 92, "key.categories.movement");
		lightKey = register("key." + Forgetest.ID + ".light", 76, "key.categories.misc");
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
			Minecraft.getInstance().gameSettings.gamma=Minecraft.getInstance().gameSettings.gamma>1d?1:30;
		}
	}

}
