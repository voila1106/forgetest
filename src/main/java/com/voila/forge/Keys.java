package com.voila.forge;

import com.mojang.blaze3d.platform.*;
import com.mojang.logging.*;
import net.minecraft.*;
import net.minecraft.client.*;
import net.minecraft.client.gui.screens.*;
import net.minecraft.client.player.*;
import net.minecraft.core.*;
import net.minecraft.world.*;
import net.minecraft.world.entity.player.*;
import net.minecraft.world.level.block.*;
import net.minecraft.world.phys.*;
import net.minecraftforge.client.event.*;
import net.minecraftforge.eventbus.api.*;
import org.lwjgl.glfw.*;
import org.slf4j.*;

import javax.annotation.*;
import java.io.*;
import java.util.*;

public class Keys {
	public static final KeyMapping upKey = new KeyMapping("key." + Forgetest.ID + ".flySpeedUp", 61, "key.categories.movement");
	public static final KeyMapping downKey = new KeyMapping("key." + Forgetest.ID + ".flySpeedDown", 45, "key.categories.movement");
	public static final KeyMapping toggleFlyKey = new KeyMapping("key." + Forgetest.ID + ".toggleFly", 92, "key.categories.movement");
	public static final KeyMapping lightKey = new KeyMapping("key." + Forgetest.ID + ".light", 76, "key.categories.misc");
	public static final KeyMapping xrayKey = new KeyMapping("key." + Forgetest.ID + ".xray", 66, "key.categories.misc");
	public static final KeyMapping configXrayKey = new KeyMapping("key." + Forgetest.ID + ".configXray", GLFW.GLFW_KEY_H, "key.categories.misc");
	public static final KeyMapping scriptKey = new KeyMapping("key." + Forgetest.ID + ".script", GLFW.GLFW_KEY_Y, "key.categories.misc");
	public static final KeyMapping configScriptKey = new KeyMapping("key." + Forgetest.ID + ".configScript", GLFW.GLFW_KEY_K, "key.categories.misc");
	public static final KeyMapping zoomKey = new KeyMapping("key." + Forgetest.ID + ".zoom", GLFW.GLFW_KEY_C, "key.categories.misc");
	public static final KeyMapping clickAboveKey = new KeyMapping("key." + Forgetest.ID + ".clickAbove", GLFW.GLFW_KEY_UP, "key.categories.misc");
	public static final KeyMapping clickBelowKey = new KeyMapping("key." + Forgetest.ID + ".clickBelow", GLFW.GLFW_KEY_DOWN, "key.categories.misc");
	public static final KeyMapping clickForwardKey = new KeyMapping("key." + Forgetest.ID + ".clickForward", InputConstants.Type.MOUSE, 4, "key.categories.misc");
	public static final KeyMapping clickBehindKey = new KeyMapping("key." + Forgetest.ID + ".clickBehind", InputConstants.Type.MOUSE, 3, "key.categories.misc");

	public static boolean xray = false;
	public static boolean scoping = false;
	public static float scopingScale = 0.15f;
	public static Set<Block> enabledBlocks = new HashSet<>();
	@Nullable
	public static Script runningScript;

	private static Minecraft mc;

	private static final Logger LOGGER = LogUtils.getLogger();

	public static void init(RegisterKeyMappingsEvent event){
		event.register(upKey);
		event.register(downKey);
		event.register(toggleFlyKey);
		event.register(lightKey);
		event.register(xrayKey);
		event.register(configXrayKey);
		event.register(scriptKey);
		event.register(configScriptKey);
		event.register(zoomKey);
		event.register(clickBelowKey);
		event.register(clickAboveKey);
		event.register(clickForwardKey);
		event.register(clickBehindKey);
		new File("config/" + Forgetest.ID).mkdirs();
	}

	@SubscribeEvent
	public static void onKeyDown(InputEvent.Key event){
//		LOGGER.info(event.getKey()+" "+event.getAction());
		if(mc == null){
			mc = Minecraft.getInstance();
		}
		LocalPlayer player = mc.player;
		if(player == null)
			return;
		Abilities ability = player.getAbilities();
		if(upKey.consumeClick()){
			ability.setFlyingSpeed(ability.getFlyingSpeed() + 0.005F);
		}else if(downKey.consumeClick()){
			ability.setFlyingSpeed(ability.getFlyingSpeed() - 0.005F);
		}else if(toggleFlyKey.consumeClick()){
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
				//BlockHitResult below=new BlockHitResult(pointing.getLocation().add(0,-1,0),pointing.getDirection(),pointing.getBlockPos().below(),pointing.isInside());
				BlockHitResult below = Screen.hasControlDown() ? new BlockHitResult(pointing.getLocation().add(0, -1, 0), Direction.UP, pointing.getBlockPos().below(), pointing.isInside()) : pointing.withDirection(Direction.DOWN);
				mc.gameMode.useItemOn(player, InteractionHand.MAIN_HAND, below);
			}
		}
		if(clickAboveKey.consumeClick()){
			if(mc.hitResult instanceof BlockHitResult pointing && pointing.getType()== HitResult.Type.BLOCK){
				BlockHitResult above = Screen.hasControlDown() ? new BlockHitResult(pointing.getLocation().add(0, 1, 0), Direction.DOWN, pointing.getBlockPos().above(), pointing.isInside()) : pointing.withDirection(Direction.UP);
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
