package com.voila.forge.command;

import com.mojang.brigadier.*;
import com.mojang.brigadier.builder.*;
import com.mojang.brigadier.context.*;
import com.mojang.brigadier.exceptions.*;
import com.mojang.brigadier.suggestion.*;
import com.voila.forge.*;
import net.minecraft.client.*;
import net.minecraft.client.server.*;
import net.minecraft.commands.*;
import net.minecraft.core.*;
import net.minecraft.network.chat.*;
import net.minecraft.server.level.*;
import net.minecraft.world.entity.player.*;
import net.minecraft.world.level.block.state.*;

import java.util.*;
import java.util.concurrent.*;

public class StolenCommand {
	public static void register(CommandDispatcher<SharedSuggestionProvider> commandDispatcher){
		LiteralArgumentBuilder command = Commands.literal("stolen")
			.then(Commands.literal("clear")
				.executes(arg -> {
					Forgetest.stolen.clear();
					System.gc();
					return 0;
				}))
			.then(Commands.literal("place")
				.then(Commands.argument("name", new SuggestionArgumentType() {
						@Override
						protected <S> CompletableFuture<Suggestions> getSuggestions(CommandContext<S> context, SuggestionsBuilder builder){
							return suggestList(builder, Forgetest.stolen.keySet());
						}
					})
					.executes(arg -> {
						Minecraft mc = Minecraft.getInstance();
						Player me = mc.player;
						IntegratedServer server = mc.getSingleplayerServer();
						if(server == null){
							Component error = Component.literal("No single player");
							throw new CommandSyntaxException(new SimpleCommandExceptionType(error), error);
						}
						ServerLevel level = null;
						for(ServerLevel l : server.getAllLevels()){
							if(l.getPlayerByUUID(me.getUUID()) != null){
								level = l;
								break;
							}
						}
						if(level == null){
							Component error = Component.literal("No player found");
							throw new CommandSyntaxException(new SimpleCommandExceptionType(error), error);
						}
						String name = SuggestionArgumentType.getString(arg, "name");
						Map<BlockPos, BlockState> save = Forgetest.stolen.get(name);
						if(save == null){
							Component err = Component.literal("No stolen map found");
							throw new CommandSyntaxException(new SimpleCommandExceptionType(err), err);
						}
						ServerLevel finalLevel = level;
						new Thread(() -> place(finalLevel, save), "Place thread").start();
						return 0;
					})));

		commandDispatcher.register(command);
	}

	private static void place(ServerLevel level, Map<BlockPos, BlockState> save){
		try{
			Player me = Minecraft.getInstance().player;
			int _x = me.getBlockX();
			int _z = me.getBlockZ();
			for(Map.Entry<BlockPos, BlockState> entry : save.entrySet()){
				BlockPos pos = entry.getKey();
				BlockState block = entry.getValue();
				pos = pos.offset(_x, 0, _z);
				level.setBlock(pos, block, 2 | 16);
			}
		}catch(Exception e){
			e.printStackTrace();
		}
		Minecraft.getInstance().gui.getChat().addMessage(Component.literal("Done. "));
	}
}
