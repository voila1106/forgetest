package com.voila.forge.command;

import com.mojang.brigadier.*;
import com.mojang.brigadier.builder.*;
import com.mojang.brigadier.exceptions.*;
import com.voila.forge.*;
import net.minecraft.*;
import net.minecraft.client.*;
import net.minecraft.commands.*;
import net.minecraft.commands.arguments.*;
import net.minecraft.network.chat.*;
import net.minecraft.world.entity.player.*;

public class CheckCommand {
	public static void register(CommandDispatcher<SharedSuggestionProvider> commandDispatcher){
		LiteralArgumentBuilder command = Commands.literal("check")
			.then(Commands.argument("player", EntityArgument.player()).executes((arg) -> {
				MutableComponent error=Component.translatable("msg."+Forgetest.ID+".invalidPlayer").withStyle(ChatFormatting.RED);
				String name;
				try{
					name = arg.getInput().substring(6);
				}catch(Exception e){
					throw new CommandSyntaxException(new SimpleCommandExceptionType(error),error);
				}
				for(Player t : Minecraft.getInstance().level.players()){
					if(t.getName().getString().equals(name)){
						Forgetest.runDelay(2, () -> Forgetest.checkInv(t, false));
						return 0;
					}
				}
				throw new CommandSyntaxException(new SimpleCommandExceptionType(error),error);
			}));

		commandDispatcher.register(command);
	}
}
