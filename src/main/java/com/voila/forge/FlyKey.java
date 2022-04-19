package com.voila.forge;

import net.minecraft.client.*;
import net.minecraft.client.gui.screen.*;
import net.minecraft.client.gui.screen.inventory.*;
import net.minecraft.client.settings.*;
import net.minecraft.enchantment.*;
import net.minecraft.entity.player.*;
import net.minecraft.item.*;
import net.minecraftforge.client.event.*;
import net.minecraftforge.eventbus.api.*;
import net.minecraftforge.fml.client.registry.*;

public class FlyKey
{
	public static KeyBinding upKey;
	public static KeyBinding downKey;
	public static KeyBinding toggleKey;

//	private static final Logger LOGGER = LogManager.getLogger();

	public static void init()
	{
		upKey = register("key." + Forgetest.ID + ".flySpeedUp", 61, "key.categories.movement");
		downKey = register("key." + Forgetest.ID + ".flySpeedDown", 45, "key.categories.movement");
		toggleKey = register("key." + Forgetest.ID + ".toggleFly", 92, "key.categories.movement");
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
		PlayerAbilities ability;
		if(player != null)
			ability = player.abilities;
		else
			return;
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
	}

}
