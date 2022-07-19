package com.voila.forge.command;

import com.mojang.brigadier.*;
import com.mojang.brigadier.arguments.*;
import com.mojang.brigadier.builder.*;
import com.voila.forge.*;
import net.minecraft.*;
import net.minecraft.client.*;
import net.minecraft.client.multiplayer.*;
import net.minecraft.commands.*;
import net.minecraft.core.*;
import net.minecraft.network.chat.*;
import net.minecraft.world.entity.player.*;
import net.minecraft.world.level.block.state.*;

import java.util.*;

public class StealMapCommand {
	public static void register(CommandDispatcher<SharedSuggestionProvider> commandDispatcher){
		ClientLevel world = Minecraft.getInstance().level;
		LiteralArgumentBuilder command = Commands.literal("steal")
			.then(Commands.argument("range", IntegerArgumentType.integer())
				.then(Commands.argument("name", StringArgumentType.word())
					.executes(arg -> {
						int range = IntegerArgumentType.getInteger(arg, "range");
						String name = StringArgumentType.getString(arg, "name");
						new Thread(() -> steal(range, name, world.getMinBuildHeight(), world.getMaxBuildHeight())).start();
						return 0;
					})
					.then(Commands.argument("bottom", IntegerArgumentType.integer())
						.then(Commands.argument("top", IntegerArgumentType.integer())
							.executes(arg -> {
								int range = IntegerArgumentType.getInteger(arg, "range");
								String name = StringArgumentType.getString(arg, "name");
								int bottom = IntegerArgumentType.getInteger(arg, "bottom");
								int top = IntegerArgumentType.getInteger(arg, "top");
								new Thread(() -> steal(range, name, bottom, top), "Steal thread").start();
								return 0;
							})))));
		commandDispatcher.register(command);
	}

	private static void steal(int range, String name, int bottom, int top){
		Minecraft mc = Minecraft.getInstance();
		if(range < 0 || name.isEmpty() || bottom >= top){
			mc.gui.getChat().addMessage(Component.literal("Invalid argument").withStyle(ChatFormatting.RED));
			return;
		}
		ClientLevel world = mc.level;
		Player me = mc.player;
		int _x = me.getBlockX();
		int _z = me.getBlockZ();
		Map<BlockPos, BlockState> save = new HashMap<>();
		try{
			for(int i = -range; i < range; i++){
				for(int j = -range; j < range; j++){
					for(int y = bottom; y <= top; y++){
						int x = _x + i;
						int z = _z + j;
						BlockState block = world.getBlockState(new BlockPos(x, y, z));
						save.put(new BlockPos(i, y, j), block);
					}
				}
			}
		}catch(Exception e){
			e.printStackTrace();
		}
		Forgetest.stolen.put(name, save);
		mc.gui.getChat().addMessage(Component.literal("Done. "));
	}
}
