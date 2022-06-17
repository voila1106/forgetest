package com.voila.forge.command;

import com.mojang.brigadier.*;
import com.mojang.brigadier.arguments.*;
import com.mojang.brigadier.builder.*;
import com.mojang.brigadier.exceptions.*;
import com.mojang.math.*;
import com.voila.forge.*;
import net.minecraft.*;
import net.minecraft.commands.*;
import net.minecraft.commands.arguments.coordinates.*;
import net.minecraft.network.chat.*;
import net.minecraft.world.phys.*;

public class SphereCommand {
	public static void register(CommandDispatcher<SharedSuggestionProvider> commandDispatcher){
		LiteralArgumentBuilder command = Commands.literal("sphere")
			.then(Commands.literal("clear").executes((arg) -> {
				Forgetest.shapes.clear();
				return 0;
			}))
			.then(Commands.argument("pos", Vec3Argument.vec3())
				.then(Commands.argument("radius", DoubleArgumentType.doubleArg()).executes((arg) -> {
					Forgetest.shapes.put(getSphere(DoubleArgumentType.getDouble(arg, "radius"),0),Vec3Argument.getVec3(arg, "pos"));
					return 0;
				}).then(Commands.argument("interval", DoubleArgumentType.doubleArg()).executes((arg) -> {
					double interval=DoubleArgumentType.getDouble(arg,"interval");
					Forgetest.shapes.put(getSphere(DoubleArgumentType.getDouble(arg, "radius"),interval),Vec3Argument.getVec3(arg, "pos"));
					return 0;
				}).then(Commands.argument("RGBA", StringArgumentType.string()).executes((arg) -> {
					double interval=DoubleArgumentType.getDouble(arg,"interval");
					String colorStr = StringArgumentType.getString(arg, "RGBA");
					Vector4f color = new Vector4f();
					try{
						color.setX(Integer.parseInt(colorStr.substring(0, 2), 16) / 255.0f);
						color.setY(Integer.parseInt(colorStr.substring(2, 4), 16) / 255.0f);
						color.setZ(Integer.parseInt(colorStr.substring(4, 6), 16) / 255.0f);
						if(colorStr.length()>6)
							color.setW(Integer.parseInt(colorStr.substring(6, 8), 16) / 255.0f);
						else
							color.setW(1);
					}catch(Exception e){
						MutableComponent err = Component.translatable("msg."+Forgetest.ID+".invalidColor").withStyle(ChatFormatting.RED);
						throw new CommandSyntaxException(new SimpleCommandExceptionType(err), err);
					}
					Forgetest.shapes.put(getSphere(DoubleArgumentType.getDouble(arg, "radius"),interval).setColor(color),Vec3Argument.getVec3(arg, "pos"));
					return 0;
				})))));

		commandDispatcher.register(command);
	}

	private static Shape getSphere(double radius, double interval){
		final int samples = 256;

		if(interval<=0){
			interval = radius / 6.0;
			while(interval > 5)
				interval /= 2.0;
		}
		final float step = (float)((Math.PI * 2) / samples);
		Shape result = new Shape();
		Vec3 first = new Vec3(radius, 0, 0);
		Vec3 last = first;
		//球心截面
		for(int i = 0; i < samples; i++){
			Vec3 current = last.yRot(step);
			result.addLine(new Line(last, current));
			last = current;
		}
		result.addLine(new Line(last, first));

		//逐层添加
		double layer = interval;
		double r;
		while((r = Math.sqrt(radius * radius - layer * layer)) > 0){
			first = new Vec3(r, layer, 0);
			last = first;
			for(int i = 0; i < samples; i++){
				Vec3 current = last.yRot(step);
				result.addLine(new Line(last, current));
				result.addLine(new Line(last.subtract(0, last.y * 2, 0), current.subtract(0, current.y * 2, 0)));
				last = current;
			}
			result.addLine(new Line(last, first));
			result.addLine(new Line(last.subtract(0, last.y * 2, 0), first.subtract(0, first.y * 2, 0)));
			layer += interval;
		}

		return result;
	}
}
