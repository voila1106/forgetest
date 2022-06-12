package com.voila.forge;

import net.minecraft.world.phys.*;

public class Line {
	double minX, minY, minZ, maxX, maxY, maxZ;

	public Line(double minX, double minY, double minZ, double maxX, double maxY, double maxZ){
		this.minX = minX;
		this.minY = minY;
		this.minZ = minZ;
		this.maxX = maxX;
		this.maxY = maxY;
		this.maxZ = maxZ;
	}

	public Line(Vec3 from,Vec3 to){
		minX=from.x;
		minY=from.y;
		minZ=from.z;
		maxX=to.x;
		maxY=to.y;
		maxZ=to.z;
	}
}