package com.voila.forge.command;

import com.mojang.brigadier.*;
import com.mojang.brigadier.arguments.*;
import com.mojang.brigadier.builder.*;
import com.mojang.brigadier.context.*;
import com.mojang.brigadier.suggestion.*;
import net.minecraft.commands.*;
import net.minecraft.network.chat.*;

import java.lang.reflect.*;
import java.util.*;
import java.util.concurrent.*;

public class JavaCommand {
	private static final Map<String, Method> methods = new HashMap<>();
	private static final Map<String, Field> fields = new HashMap<>();
	private static final Map<String, Object> vars = new HashMap<>();
	private static final Map<String, Constructor<?>> constructors = new HashMap<>();
	private static final String[] basic = new String[]{"int", "long", "byte", "boolean", "short", "float", "double", "char", "String", "null"};

	public static void register(CommandDispatcher<SharedSuggestionProvider> commandDispatcher){
		LiteralArgumentBuilder command = Commands.literal("java")
			.then(Commands.literal("getconstr")
				.then(filterClass()
					.then(inputArgs(Class.class)
						.executes(arg -> {
							String[] args = SuggestionArgumentType.getString(arg, "args").split(" ");
							Class<?>[] c = new Class[0];
							try{
								Class<?> clazz = (Class<?>)vars.get(SuggestionArgumentType.getString(arg, "class"));
								c = castArgs(args, c);
								Constructor<?> con = clazz.getDeclaredConstructor(c);
								constructors.put(clazz.getSimpleName() + "_constr", con);
								arg.getSource().sendSuccess(Component.literal(con.toString()), false);
							}catch(Exception e){
								arg.getSource().sendFailure(Component.literal(e.toString()));
								e.printStackTrace();
							}
							return 0;
						}))))
			.then(Commands.literal("new")
				.then(Commands.argument("saveTo", new SuggestionArgumentType() {
						@Override
						protected <S> CompletableFuture<Suggestions> getSuggestions(CommandContext<S> context, SuggestionsBuilder builder){
							return builder.suggest("_no").buildFuture();
						}
					})
					.then(Commands.argument("constr", new SuggestionArgumentType() {
							@Override
							protected <S> CompletableFuture<Suggestions> getSuggestions(CommandContext<S> context, SuggestionsBuilder builder){
								return suggestList(builder, constructors.keySet());
							}
						})
						.then(inputArgs(Object.class)
							.executes(arg -> {
								try{
									String save = SuggestionArgumentType.getString(arg, "saveTo");
									Constructor<?> con = constructors.get(SuggestionArgumentType.getString(arg, "constr"));
									String[] args = SuggestionArgumentType.getString(arg, "args").split(" ");
									Object[] o = new Object[0];
									o = castArgs(args, o);
									Object ret = con.newInstance(o);
									if(!save.equals("_no")){
										vars.put(save, ret);
									}
									arg.getSource().sendSuccess(Component.literal(save + " = " + ret), false);
								}catch(Exception e){
									arg.getSource().sendFailure(Component.literal(e.toString()));
									e.printStackTrace();
								}
								return 0;
							})))))
			.then(Commands.literal("getfield")
				.then(filterClass()
					.then(Commands.argument("name", new SuggestionArgumentType() {
							@Override
							protected <S> CompletableFuture<Suggestions> getSuggestions(CommandContext<S> context, SuggestionsBuilder builder){
								if(vars.get(SuggestionArgumentType.getString(context, "class")) instanceof Class<?> clazz){
									for(Field f : clazz.getDeclaredFields()){
										builder.suggest(f.getName());
									}
								}
								return builder.buildFuture();
							}
						})
						.executes(arg -> {
							try{
								Class<?> clazz = (Class<?>)vars.get(SuggestionArgumentType.getString(arg, "class"));
								String name = StringArgumentType.getString(arg, "name");
								Field field = clazz.getDeclaredField(name);
								String storageName = clazz.getSimpleName() + "_" + field.getName();
								fields.put(storageName, field);
								arg.getSource().sendSuccess(Component.literal(storageName + ": " + field), false);
							}catch(Exception e){
								e.printStackTrace();
								arg.getSource().sendFailure(Component.literal(e.toString()));
							}
							return 0;
						}))))
			.then(Commands.literal("getvalue")
				.then(Commands.argument("field", new SuggestionArgumentType() {
						@Override
						protected <S> CompletableFuture<Suggestions> getSuggestions(CommandContext<S> context, SuggestionsBuilder builder){
							return suggestList(builder, fields.keySet());
						}
					})
					.then(Commands.argument("instance", new SuggestionArgumentType() {
							@Override
							protected <S> CompletableFuture<Suggestions> getSuggestions(CommandContext<S> context, SuggestionsBuilder builder){
								return suggestList(builder, vars.keySet());
							}
						})
						.then(Commands.argument("saveTo", new SuggestionArgumentType() {
								@Override
								protected <S> CompletableFuture<Suggestions> getSuggestions(CommandContext<S> context, SuggestionsBuilder builder){
									return builder.suggest("_no").buildFuture();
								}
							})
							.executes(arg -> {
								Field field = fields.get(SuggestionArgumentType.getString(arg, "field"));
								Object instance = vars.get(SuggestionArgumentType.getString(arg, "instance"));
								String save = SuggestionArgumentType.getString(arg, "saveTo");
								try{
									field.setAccessible(true);
									Object res = field.get(instance);
									if(!"_no".equals(save)){
										vars.put(save, res);
									}
									arg.getSource().sendSuccess(Component.literal(save + " = " + res), false);
								}catch(Exception e){
									e.printStackTrace();
									arg.getSource().sendFailure(Component.literal(e.toString()));
								}
								return 0;
							})))))
			.then(Commands.literal("setvar")
				.then(Commands.argument("type", new SuggestionArgumentType() {
						@Override
						protected <S> CompletableFuture<Suggestions> getSuggestions(CommandContext<S> context, SuggestionsBuilder builder){
							return suggestList(builder, Arrays.asList(basic));
						}
					})
					.then(Commands.argument("name", StringArgumentType.word())
						.then(Commands.argument("value", StringArgumentType.greedyString())
							.executes(arg -> {
								String type = SuggestionArgumentType.getString(arg, "type");
								String name = StringArgumentType.getString(arg, "name");
								String raw = StringArgumentType.getString(arg, "value");
								Object value;
								try{
									switch(type){
										case "int" -> value = Integer.parseInt(raw);
										case "long" -> value = Long.parseLong(raw);
										case "byte" -> value = Byte.parseByte(raw);
										case "boolean" -> value = Boolean.parseBoolean(raw);
										case "short" -> value = Short.parseShort(raw);
										case "float" -> value = Float.parseFloat(raw);
										case "double" -> value = Double.parseDouble(raw);
										case "char" -> value = raw.charAt(0);
										case "null" -> value = null;
										case "String" -> value = raw;
										default -> {
											arg.getSource().sendFailure(Component.literal("no basic type"));
											return -1;
										}
									}
									vars.put(name, value);
									arg.getSource().sendSuccess(Component.literal(String.valueOf(value)), false);
								}catch(Exception e){
									arg.getSource().sendFailure(Component.literal(e.toString()));
								}

								return 0;
							})))))
			.then(Commands.literal("basic")
				.executes(arg -> {
					vars.put("int", int.class);
					vars.put("long", long.class);
					vars.put("byte", byte.class);
					vars.put("boolean", boolean.class);
					vars.put("short", short.class);
					vars.put("float", float.class);
					vars.put("double", double.class);
					vars.put("char", char.class);
					vars.put("String", String.class);
					return 0;
				}))
			.then(Commands.literal("getclass")
				.then(Commands.argument("name", StringArgumentType.word())
					.executes(arg -> {
						String name = StringArgumentType.getString(arg, "name");
						try{
							Class<?> clazz = Class.forName(name);
							vars.put(clazz.getSimpleName(), clazz);
							arg.getSource().sendSuccess(Component.literal(clazz.toString()), false);
						}catch(ClassNotFoundException e){
							arg.getSource().sendFailure(Component.literal(e.toString()));
							e.printStackTrace();
						}
						return 0;
					})))
			.then(Commands.literal("getmethod")
				.then(filterClass()
					.then(Commands.argument("name", new SuggestionArgumentType() {
							@Override
							protected <S> CompletableFuture<Suggestions> getSuggestions(CommandContext<S> context, SuggestionsBuilder builder){
								if(vars.get(SuggestionArgumentType.getString(context, "class")) instanceof Class<?> clazz){
									for(Method m : clazz.getDeclaredMethods()){
										builder.suggest(m.getName());
									}
								}
								return builder.buildFuture();
							}
						})
						.then(inputArgs(Class.class)
							.executes(arg -> {
								try{
									Class<?> clazz = (Class<?>)vars.get(SuggestionArgumentType.getString(arg, "class"));
									String name = StringArgumentType.getString(arg, "name");
									String[] args = SuggestionArgumentType.getString(arg, "args").split(" ");
									Class<?>[] c;
									if(args[0].equals("_void")){
										c = new Class[0];
									}else{
										c = new Class<?>[args.length];
										for(int i = 0; i < args.length; i++){
											c[i] = (Class<?>)vars.get(args[i]);
										}
									}
									Method method = clazz.getDeclaredMethod(name, c);
									String storageName = clazz.getSimpleName() + "_" + name;
									methods.put(storageName, method);
									arg.getSource().sendSuccess(Component.literal(storageName + ": " + method), false);
								}catch(Exception e){
									arg.getSource().sendFailure(Component.literal(e.toString()));
									e.printStackTrace();
								}
								return 0;
							})))))
			.then(Commands.literal("invoke")
				.then(Commands.argument("name", new SuggestionArgumentType() {
						@Override
						protected <S> CompletableFuture<Suggestions> getSuggestions(CommandContext<S> context, SuggestionsBuilder builder){
							return suggestList(builder, methods.keySet());
						}
					})
					.then(Commands.argument("instance", new SuggestionArgumentType() {
						@Override
						protected <S> CompletableFuture<Suggestions> getSuggestions(CommandContext<S> context, SuggestionsBuilder builder){
							return suggestList(builder, vars.keySet());
						}
					}).then(Commands.argument("saveReturn", new SuggestionArgumentType() {
						@Override
						protected <S> CompletableFuture<Suggestions> getSuggestions(CommandContext<S> context, SuggestionsBuilder builder){
							return builder.suggest("_no").buildFuture();
						}
					}).then(inputArgs(Object.class)
						.executes(arg -> {
							String name = SuggestionArgumentType.getString(arg, "name");
							String[] args = StringArgumentType.getString(arg, "args").split(" ");
							Object instance = vars.get(SuggestionArgumentType.getString(arg, "instance"));
							String save = SuggestionArgumentType.getString(arg, "saveReturn");
							Method method = methods.get(name);
							Object[] objs;
							if(args[0].equals("_void")){
								objs = new Object[0];
							}else{
								objs = new Object[args.length];
								for(int i = 0; i < args.length; i++){
									objs[i] = vars.get(args[i]);
								}
							}
							try{
								method.setAccessible(true);
								Object ret = method.invoke(instance, objs);
								if(!"_no".equals(save) && method.getReturnType() != void.class){
									vars.put(save, ret);
								}
								arg.getSource().sendSuccess(Component.literal("Return: " + ret), false);
							}catch(Exception e){
								arg.getSource().sendFailure(Component.literal(e.toString()));
								e.printStackTrace();
							}
							return 0;
						}))))))
			.then(Commands.literal("showmethod")
				.then(showItem(methods)))
			.then(Commands.literal("showfield")
				.then(showItem(fields)))
			.then(Commands.literal("showvar")
				.then(showItem(vars)))
			.then(Commands.literal("methods")
				.executes(arg -> {
					methods.forEach((k, v) -> arg.getSource().sendSuccess(Component.literal(k + " = " + v), false));
					return 0;
				}))
			.then(Commands.literal("fields")
				.executes(arg -> {
					fields.forEach((k, v) -> arg.getSource().sendSuccess(Component.literal(k + " = " + v), false));
					return 0;
				}))
			.then(Commands.literal("vars")
				.executes(arg -> {
					vars.forEach((k, v) -> arg.getSource().sendSuccess(Component.literal(k + " = " + v), false));
					return 0;
				}))
			.then(Commands.literal("constructors")
				.executes(arg -> {
					constructors.forEach((k, v) -> arg.getSource().sendSuccess(Component.literal(k + " = " + v), false));
					return 0;
				}))
			.then(Commands.literal("clear")
				.then(Commands.literal("method")
					.executes(arg -> {
						methods.clear();
						return 0;
					}))
				.then(Commands.literal("field")
					.executes(arg -> {
						fields.clear();
						return 0;
					}))
				.then(Commands.literal("var")
					.executes(arg -> {
						vars.clear();
						return 0;
					})))
			.then(Commands.literal("remove")
				.then(Commands.literal("method")
					.then(remove(methods)))
				.then(Commands.literal("field")
					.then(remove(fields)))
				.then(Commands.literal("var")
					.then(remove(vars)))
				.then(Commands.literal("constr")
					.then(remove(constructors))));
		commandDispatcher.register(command);
	}

	private static ArgumentBuilder<CommandSourceStack, ?> showItem(Map<String, ?> list){
		return Commands.argument("name", new SuggestionArgumentType() {
				@Override
				protected <U> CompletableFuture<Suggestions> getSuggestions(CommandContext<U> context, SuggestionsBuilder builder){
					return suggestList(builder, list.keySet());
				}
			})
			.executes(arg -> {
				String name = SuggestionArgumentType.getString(arg, "name");
				arg.getSource().sendSuccess(Component.literal(name + " = " + list.get(name)), false);
				return 0;
			});
	}

	private static ArgumentBuilder<CommandSourceStack, ?> filterClass(){
		return Commands.argument("class", new SuggestionArgumentType() {
			@Override
			protected <S> CompletableFuture<Suggestions> getSuggestions(CommandContext<S> context, SuggestionsBuilder builder){
				return suggestList(builder, vars.entrySet(), entry -> entry.getValue() instanceof Class<?>, Map.Entry::getKey);
			}
		});
	}

	private static ArgumentBuilder<CommandSourceStack, ?> inputArgs(Class<?> type){
		return Commands.argument("args", new SuggestionArgumentType() {
			@Override
			protected <S> CompletableFuture<Suggestions> getSuggestions(CommandContext<S> context, SuggestionsBuilder builder){
				suggestList(builder, vars.entrySet(), e -> type.isAssignableFrom(e.getValue().getClass()), Map.Entry::getKey);
				builder.suggest("_void");
				return builder.buildFuture();
			}
		}.greedy());
	}

	private static <T> T[] castArgs(String[] args, T[] to){
		if(args[0].equals("_void")){
			return to;
		}
		ArrayList<T> list = new ArrayList<>(Arrays.asList(to));
		for(String arg : args){
			list.add((T)vars.get(arg));
		}
		return list.toArray(to);
	}

	private static ArgumentBuilder<CommandSourceStack, ?> remove(Map<String, ?> list){
		return Commands.argument("name", new SuggestionArgumentType() {
				@Override
				protected <S> CompletableFuture<Suggestions> getSuggestions(CommandContext<S> context, SuggestionsBuilder builder){
					return suggestList(builder, list.keySet());
				}
			})
			.executes(arg -> {
				list.remove(SuggestionArgumentType.getString(arg, "name"));
				return 0;
			});
	}
}
