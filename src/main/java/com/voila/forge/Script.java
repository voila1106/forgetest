package com.voila.forge;

import net.minecraft.client.*;
import net.minecraft.client.entity.player.*;
import net.minecraft.util.text.*;
import net.minecraftforge.api.distmarker.*;
import net.minecraftforge.common.*;
import net.minecraftforge.event.*;
import net.minecraftforge.eventbus.api.*;

import java.util.*;

public class Script {
	public boolean forward;
	public boolean backward;
	public boolean left;
	public boolean right;
	public boolean jump;
	public boolean crouch;
	// TODO: fix sneak not work
	// TODO: add mouse operation

	private final Map<Integer, List<Runnable>> tasks = new HashMap<>();
	private Map<Integer, List<Runnable>> running = new HashMap<>();
	private int ticks = 1;
	public int waitTicks = 0;

	public static boolean enabled = false;
	public static String filename = "script.txt";


	public Script(String[] commands){
		MinecraftForge.EVENT_BUS.register(this);
		ClientPlayerEntity player = Minecraft.getInstance().player;
		for(int i = 0; i < commands.length; i++){
			String cmd = commands[i];
			switch(cmd){
				case "+for":
					put(ticks, () -> forward = true);
					break;
				case "-for":
					put(ticks, () -> forward = false);
					break;
				case "+back":
					put(ticks, () -> backward = true);
					break;
				case "-back":
					put(ticks, () -> backward = false);
					break;
				case "+left":
					put(ticks, () -> left = true);
					break;
				case "-left":
					put(ticks, () -> left = false);
					break;
				case "+right":
					put(ticks, () -> right = true);
					break;
				case "-right":
					put(ticks, () -> right = false);
					break;
				case "+jump":
					put(ticks, () -> jump = true);
					break;
				case "-jump":
					put(ticks, () -> jump = false);
					break;
				case "+crouch":
					put(ticks, () -> {
						crouch = true;
						Minecraft.getInstance().gameSettings.keyBindSneak.setPressed(true);
						player.setSneaking(true);
					});
					break;
				case "-crouch":
					put(ticks, () -> {
						crouch = false;
						Minecraft.getInstance().gameSettings.keyBindSneak.setPressed(false);
						player.setSneaking(false);
					});
					break;
				case "+sprint":
					put(ticks, () -> player.setSprinting(true));
					break;
				case "-sprint":
					put(ticks, () -> player.setSprinting(false));
					break;
				default:
					try{
						if(cmd.startsWith("wait")){
							ticks += Integer.parseInt(cmd.substring(5));
							put(ticks, () -> waitTicks = 0);
						}else if(cmd.startsWith("pitch")){
							put(ticks, () -> player.rotationPitch = Float.parseFloat(cmd.substring(6)));
						}else if(cmd.startsWith("yaw")){
							put(ticks, () -> player.rotationYaw = Float.parseFloat(cmd.substring(4)));
						}else if(!cmd.trim().isEmpty() && !cmd.startsWith("#") && !cmd.startsWith("\t")){
							Forgetest.sendMessage(TextFormatting.GOLD + "Warning: Cannot resolve line " + (i + 1) + ": " + cmd);
						}
					}catch(Exception e){
						throw new RuntimeException("Syntax error in line " + (i + 1));
					}
			}
		}

	}

	public Script run(){
		enabled = true;
		runDelay(tasks);
		runDelay(ticks, () -> enabled = false);
		return this;
	}

	private void put(int ticks, Runnable task){
		if(tasks.get(ticks) == null){
			ArrayList<Runnable> list = new ArrayList<>();
			list.add(task);
			tasks.put(ticks, list);
		}else{
			tasks.get(ticks).add(task);
		}
	}

	private void runDelay(int ticks, Runnable task){
		if(running.get(ticks) == null){
			ArrayList<Runnable> list = new ArrayList<>();
			list.add(task);
			running.put(ticks, list);
		}else{
			running.get(ticks).add(task);
		}
	}

	private void runDelay(Map<Integer, List<Runnable>> task){
		running.putAll(task);
	}


	@OnlyIn(Dist.CLIENT)
	@SubscribeEvent
	public void tick(TickEvent.ClientTickEvent event){
		if(event.phase == TickEvent.Phase.START)
			return;
		Map<Integer, List<Runnable>> map = new HashMap<>();
		for(int i : running.keySet()){
			if(i <= 0){
				running.get(i).forEach(Runnable::run);
			}else{
				map.put(i - 1, running.get(i));
			}
		}
		running = map;
		if(enabled)
			waitTicks++;
	}

	public void cancel(){
		running.clear();
		enabled = false;
	}

}