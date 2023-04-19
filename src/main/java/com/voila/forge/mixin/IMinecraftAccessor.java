package com.voila.forge.mixin;

import net.minecraft.client.*;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.gen.*;

@Mixin(Minecraft.class)
public interface IMinecraftAccessor {
	@Accessor
	int getRightClickDelay();

	@Invoker
	void callStartUseItem();

	@Invoker
	boolean callStartAttack();

	@Invoker
	void callPickBlock();
}
