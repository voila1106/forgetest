package com.voila.forge.command;

import com.mojang.brigadier.*;
import com.mojang.brigadier.arguments.*;
import com.mojang.brigadier.context.*;
import com.mojang.brigadier.exceptions.*;
import com.mojang.brigadier.suggestion.*;

import java.util.concurrent.*;
import java.util.function.*;

public abstract class SuggestionArgumentType implements ArgumentType<String> {
	private boolean isGreedy = false;

	@Override
	public String parse(StringReader reader) throws CommandSyntaxException{
		if(isGreedy){
			String text = reader.getRemaining();
			reader.setCursor(reader.getTotalLength());
			return text;
		}
		return reader.readString();
	}

	public static String getString(CommandContext<?> context, String name){
		return context.getArgument(name, String.class);
	}

	public SuggestionArgumentType greedy(){
		isGreedy = true;
		return this;
	}

	@Override
	public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> context, SuggestionsBuilder builder){
		return getSuggestions(context, builder);
	}

	protected CompletableFuture<Suggestions> suggestList(SuggestionsBuilder builder, Iterable<?> list){
		for(Object o : list){
			builder.suggest(String.valueOf(o));
		}
		return builder.buildFuture();
	}

	protected <T> CompletableFuture<Suggestions> suggestList(SuggestionsBuilder builder, Iterable<T> list, Predicate<T> filter, Function<T, Object> pick){
		for(T o : list){
			if(filter.test(o)){
				builder.suggest(String.valueOf(pick.apply(o)));
			}
		}
		return builder.buildFuture();
	}

	protected abstract <S> CompletableFuture<Suggestions> getSuggestions(CommandContext<S> context, SuggestionsBuilder builder);
}
