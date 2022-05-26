package com.voila.forge;

import net.minecraft.core.particles.*;
import net.minecraft.world.item.*;
import net.minecraftforge.eventbus.api.*;
import net.minecraftforge.registries.*;

public class TestItem
{
	public static final DeferredRegister<Item> ITEM=DeferredRegister.create(ForgeRegistries.ITEMS,Forgetest.ID);
	public static final DeferredRegister<ParticleType<?>> P=DeferredRegister.create(ForgeRegistries.PARTICLE_TYPES,Forgetest.ID);

	public static final RegistryObject<Item> DUMMY=ITEM.register("dummy",()->new Item(new Item.Properties().tab(CreativeModeTab.TAB_MISC)));
	public static final RegistryObject<ParticleType<DamageParticle.DamageParticleData>> damage=P.register("damage", DamageParticle.DamageParticleData::new);

	public static void registry(IEventBus eventBus)
	{
		ITEM.register(eventBus);
		P.register(eventBus);
	}
}
