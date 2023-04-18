package com.voila.forge.mixin;

import com.voila.forge.*;
import net.minecraft.client.multiplayer.*;
import net.minecraft.core.*;
import net.minecraft.resources.*;
import net.minecraft.util.profiling.*;
import net.minecraft.world.level.*;
import net.minecraft.world.level.dimension.*;
import net.minecraft.world.level.storage.*;
import org.spongepowered.asm.mixin.*;

import java.util.function.*;

@Mixin(ClientLevel.class)
public abstract class MixinClientLevel extends Level {
	private MixinClientLevel(WritableLevelData p_220352_, ResourceKey<Level> p_220353_, Holder<DimensionType> p_220354_, Supplier<ProfilerFiller> p_220355_, boolean p_220356_, boolean p_220357_, long p_220358_, int p_220359_){
		super(p_220352_, p_220353_, p_220354_, p_220355_, p_220356_, p_220357_, p_220358_, p_220359_);
	}

	/** No rain */
	@Override
	public float getRainLevel(float partialTick){
		if(Forgetest.noRain){
			return 0;
		}
		return super.getRainLevel(partialTick);
	}
}
