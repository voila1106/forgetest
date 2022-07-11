package com.voila.forge.command;

import com.mojang.brigadier.*;
import com.mojang.brigadier.arguments.*;
import com.mojang.brigadier.context.*;
import com.mojang.brigadier.exceptions.*;
import com.mojang.brigadier.suggestion.*;
import com.voila.forge.*;

import java.util.concurrent.*;

public class StolenArgumentType implements ArgumentType<String> {
	@Override
	public String parse(StringReader reader) throws CommandSyntaxException{
		return reader.readString();
	}

	public static String getString(CommandContext<?> context, String name){
		return context.getArgument(name, String.class);
	}

	@Override
	public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> context, SuggestionsBuilder builder){
		for(String name : Forgetest.stolen.keySet()){
			builder.suggest(name);
		}
		return builder.buildFuture();
	}
}
