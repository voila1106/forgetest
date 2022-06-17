package com.voila.forge;

import com.mojang.brigadier.*;
import com.mojang.brigadier.exceptions.*;
import com.mojang.serialization.*;
import net.minecraft.client.multiplayer.*;
import net.minecraft.client.particle.*;
import net.minecraft.core.particles.*;
import net.minecraft.network.*;
import net.minecraftforge.api.distmarker.*;
import org.apache.logging.log4j.*;

import javax.annotation.*;

public class DamageParticle extends TextureSheetParticle {
	int amount;
	int ma = 15;
	static Logger Log = LogManager.getLogger();
	SpriteSet sprite;

	protected DamageParticle(ClientLevel world, double x, double y, double z, double xd, double yd, double zd, int amount, SpriteSet as){
		super(world, x, y, z, xd, yd, zd);
		lifetime = 2048;
		hasPhysics = false;
		this.amount = Math.max(amount, 0);
		sprite = as;
		scale(2);
		setSprite(sprite.get(amount, 2048));
		if(this.yd < 0)
			this.yd = -this.yd;

	}

	@Override
	public void tick(){
		super.tick();
		this.xd *= 0.9;
		this.yd *= 0.7;
		this.zd *= 0.9;
		this.yd -= 0.02;
		setSprite(sprite.get(amount, 2048));
		if(age > ma)
			remove();
	}

	@Override
	public ParticleRenderType getRenderType(){
		return ParticleRenderType.PARTICLE_SHEET_LIT;
	}

	public static class DamageParticleData extends ParticleType<DamageParticleData> implements ParticleOptions {
		int amount;
		public static final Deserializer<DamageParticleData> deserializer = new Deserializer<DamageParticleData>() {
			@Override
			public DamageParticleData fromCommand(ParticleType<DamageParticleData> particleTypeIn, StringReader reader) throws CommandSyntaxException{
				reader.expect(' ');
				int amount = reader.readInt();
				return new DamageParticleData(amount);
			}

			@Override
			public DamageParticleData fromNetwork(ParticleType<DamageParticleData> particleTypeIn, FriendlyByteBuf buffer){
				return new DamageParticleData(buffer.readInt());
			}
		};

		public DamageParticleData(int amount){
			this();
			this.amount = amount;
		}

		public DamageParticleData(){
			super(true, deserializer);
		}

		@Override
		public ParticleType<?> getType(){
			return TestItem.damage.get();
		}

		@Override
		public void writeToNetwork(FriendlyByteBuf buffer){
			buffer.writeInt(this.amount);
		}

		@Override
		public String writeToString(){
			return String.format("%s %d", this.getType().toString(), this.amount);
		}

		@Override
		public Codec<DamageParticleData> codec(){
			return Codec.unit(() -> DamageParticleData.this);
		}

		public int getAmount(){
			return amount;
		}
	}

	@OnlyIn(Dist.CLIENT)
	public static class Factory implements ParticleProvider<DamageParticleData> {
		private final SpriteSet sprites;

		public Factory(SpriteSet sprite){
			this.sprites = sprite;
		}

		@Nullable
		@Override
		public Particle createParticle(DamageParticleData typeIn, ClientLevel worldIn, double x, double y, double z, double xSpeed, double ySpeed, double zSpeed){
			DamageParticle particle = new DamageParticle(worldIn, x, y, z, xSpeed, ySpeed, zSpeed, typeIn.getAmount(), sprites);
			return particle;
		}
	}

}
