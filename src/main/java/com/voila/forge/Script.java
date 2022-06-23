package com.voila.forge;

import net.minecraft.*;
import net.minecraft.client.*;
import net.minecraft.client.player.*;
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
	public boolean use;
	public boolean attack;

	private final Map<Long, List<Runnable>> tasks = new HashMap<>();
	private Map<Long, List<Runnable>> running = new HashMap<>();
	private long ticks = 1;
	public long waitTicks = 0;
	private boolean loop = false;

	public static boolean enabled = false;
	public static String filename = "script.txt";


	public Script(String[] commands) throws ScriptSyntaxException{
		LocalPlayer player = Minecraft.getInstance().player;
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
						Minecraft.getInstance().options.keyShift.setDown(true);
						player.setShiftKeyDown(true);
					});
					break;
				case "-crouch":
					put(ticks, () -> {
						crouch = false;
						Minecraft.getInstance().options.keyShift.setDown(false);
						player.setShiftKeyDown(false);
					});
					break;
				case "+sprint":
					put(ticks, () -> player.setSprinting(true));
					break;
				case "-sprint":
					put(ticks, () -> player.setSprinting(false));
					break;
				case "+use":
					put(ticks, () -> use = true);
					break;
				case "-use":
					put(ticks, () -> use = false);
					break;
				case "use":
					put(ticks, () -> ((IMinecraft)Minecraft.getInstance()).use());
					break;
				case "+attack":
					put(ticks, () -> attack = true);
					break;
				case "-attack":
					put(ticks, () -> attack = false);
					break;
				case "attack":
					put(ticks, () -> ((IMinecraft)Minecraft.getInstance()).attack());
					break;
				case "pick":
					put(ticks, () -> ((IMinecraft)Minecraft.getInstance()).pick());
					break;
				default:
					try{
						if(cmd.startsWith("wait")){
							ticks += Integer.parseInt(cmd.substring(5));
							put(ticks, () -> waitTicks = 0);
						}else if(cmd.startsWith("pitch")){
							put(ticks, () -> player.setXRot(Float.parseFloat(cmd.substring(6))));
						}else if(cmd.startsWith("yaw")){
							put(ticks, () -> player.setYRot(Float.parseFloat(cmd.substring(4))));
						}else if(!cmd.trim().isEmpty() && !cmd.startsWith("#") && !cmd.startsWith("\t") && !cmd.equals("loop")){
							Forgetest.sendMessage(ChatFormatting.GOLD + "Warning: Cannot resolve line " + (i + 1) + ": " + cmd);
						}
					}catch(Exception e){
						throw new ScriptSyntaxException("Syntax error in line " + (i + 1));
					}
			}
		}
		if(commands[commands.length - 1].equals("loop")){
			loop = true;
		}

	}

	public Script run(){
		MinecraftForge.EVENT_BUS.register(this);
		enabled = true;
		runDelay(tasks);
		if(!loop)
			runDelay(ticks, this::cancel);
		return this;
	}

	private void put(long ticks, Runnable task){
		if(tasks.get(ticks) == null){
			ArrayList<Runnable> list = new ArrayList<>();
			list.add(task);
			tasks.put(ticks, list);
		}else{
			tasks.get(ticks).add(task);
		}
	}

	private void runDelay(long ticks, Runnable task){
		if(running.get(ticks) == null){
			ArrayList<Runnable> list = new ArrayList<>();
			list.add(task);
			running.put(ticks, list);
		}else{
			running.get(ticks).add(task);
		}
	}

	private void runDelay(Map<Long, List<Runnable>> task){
		running.putAll(task);
	}


	@OnlyIn(Dist.CLIENT)
	@SubscribeEvent
	public void tick(TickEvent.ClientTickEvent event){
		if(event.phase == TickEvent.Phase.START)
			return;
		if(!enabled)
			return;
		if(Minecraft.getInstance().player == null){
			cancel();
			return;
		}

		//TODO: optimize performance
		Map<Long, List<Runnable>> map = new HashMap<>();
		for(long i : running.keySet()){
			if(i <= 0){
				running.get(i).forEach(runnable -> {
					try{
						runnable.run();
					}catch(Exception e){
						e.printStackTrace();
					}
				});
			}else{
				map.put(i - 1, running.get(i));
			}
		}
		running = map;
		if(running.size() == 0 && loop){
			running.putAll(tasks);
		}
		waitTicks++;
	}

	public void cancel(){
		running.clear();
		enabled = false;
		MinecraftForge.EVENT_BUS.unregister(this);
	}

	public String getProgress(){
		return (tasks.size() - running.size()) + "/" + tasks.size();
	}

}