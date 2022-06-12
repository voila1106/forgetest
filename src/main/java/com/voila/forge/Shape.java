package com.voila.forge;

import com.mojang.math.*;
import net.minecraft.world.phys.*;

import java.util.*;

public class Shape {
	interface LineConsumer {
		void accept(double minX, double minY, double minZ, double maxX, double maxY, double maxZ);
	}

	private final List<Line> lines = new ArrayList<>();
	private Vector4f color;

	public Shape setColor(float r, float g, float b, float a){
		color = new Vector4f(r, g, b, a);
		return this;
	}

	public Shape setColor(Vector4f color){
		this.color=color;
		return this;
	}

	public Vector4f getColor(){
		if(color == null)
			color = new Vector4f(1, 1, 1, 1);
		return color;
	}

	public Shape addLine(Line line){
		lines.add(line);
		return this;
	}

	public void forAllEdges(Shape.LineConsumer consumer){
		lines.forEach((l) -> consumer.accept(l.minX, l.minY, l.minZ, l.maxX, l.maxY, l.maxZ));
	}
}