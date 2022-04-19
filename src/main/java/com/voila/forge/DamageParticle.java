package com.voila.forge;

import com.mojang.brigadier.*;
import com.mojang.brigadier.exceptions.*;
import com.mojang.serialization.*;
import net.minecraft.client.particle.*;
import net.minecraft.client.world.*;
import net.minecraft.network.*;
import net.minecraft.particles.*;
import net.minecraftforge.api.distmarker.*;
import org.apache.logging.log4j.*;

import javax.annotation.*;

public class DamageParticle extends SpriteTexturedParticle
{
	int amount;
	int ma=15;
	static Logger Log=LogManager.getLogger();
	IAnimatedSprite sprite;
	protected DamageParticle(ClientWorld world, double x, double y, double z, double motionX, double motionY, double motionZ,int amount,IAnimatedSprite as)
	{
		super(world,x,y,z,motionX,motionY,motionZ);
		maxAge=2048;
		canCollide=false;
		this.amount= Math.max(amount, 0);
		sprite=as;
		setSprite(sprite.get(amount,2048));
		if(this.motionY<0)
			this.motionY=-this.motionY;

	}

	@Override
	public void tick()
	{
		super.tick();
		this.motionX *= 0.9;
		this.motionY *= 0.7;
		this.motionZ *= 0.9;
		this.motionY -= 0.02;
		setSprite(sprite.get(amount,2048));
		if(age>ma)
			setExpired();
	}

	@Override
	public IParticleRenderType getRenderType()
	{
		return IParticleRenderType.PARTICLE_SHEET_LIT;
	}

	public static class DamageParticleData extends ParticleType<DamageParticleData> implements IParticleData
	{
		int amount;
		public static final IDeserializer<DamageParticleData> deserializer=new IDeserializer<DamageParticleData>()
		{
			@Override
			public DamageParticleData deserialize(ParticleType<DamageParticleData> particleTypeIn, StringReader reader) throws CommandSyntaxException
			{
				reader.expect(' ');
				int amount=reader.readInt();
				return new DamageParticleData(amount);
			}

			@Override
			public DamageParticleData read(ParticleType<DamageParticleData> particleTypeIn, PacketBuffer buffer)
			{
				return new DamageParticleData(buffer.readInt());
			}
		};

		public DamageParticleData(int amount)
		{
			this();
			this.amount=amount;
		}

		public DamageParticleData()
		{
			super(true, deserializer);
		}

		@Override
		public ParticleType<?> getType()
		{
			return TestItem.damage.get();
		}

		@Override
		public void write(PacketBuffer buffer)
		{
			buffer.writeInt(this.amount);
		}

		@Override
		public String getParameters()
		{
			return String.format("%s %d", this.getType().getRegistryName(),this.amount);
		}

		@Override
		public Codec<DamageParticleData> func_230522_e_()
		{
			return Codec.unit(() -> DamageParticleData.this);
		}

		public int getAmount()
		{
			return amount;
		}
	}

	@OnlyIn(Dist.CLIENT)
	public static class Factory implements IParticleFactory<DamageParticleData>
	{
		private final IAnimatedSprite sprites;

		public Factory(IAnimatedSprite sprite)
		{
			this.sprites=sprite;
		}

		@Nullable
		@Override
		public Particle makeParticle(DamageParticleData typeIn, ClientWorld worldIn, double x, double y, double z, double xSpeed, double ySpeed, double zSpeed)
		{
			DamageParticle particle=new DamageParticle(worldIn,x,y,z,xSpeed,ySpeed,zSpeed, typeIn.getAmount(),sprites);
			return particle;
		}
	}

}
