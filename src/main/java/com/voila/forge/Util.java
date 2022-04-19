package com.voila.forge;

import net.minecraft.client.renderer.texture.*;
import org.apache.logging.log4j.*;

import java.io.*;
import java.lang.reflect.*;

public class Util
{
	public static Object get(Field field,Object obj) throws Exception
	{
		field.setAccessible(true);
		return field.get(obj);
	}

	public static TextureAtlasSprite get(TextureAtlasSprite sprite, NativeImage image)
	{
		Class<?> tascl=TextureAtlasSprite.class;
		try
		{
			AtlasTexture at= (AtlasTexture)Util.get(tascl.getDeclaredField("atlasTexture"),sprite);
			TextureAtlasSprite.Info info= (TextureAtlasSprite.Info)Util.get(tascl.getDeclaredField("spriteInfo"),sprite);
			int tx= (int)Util.get(tascl.getDeclaredField("x"),sprite);
			int ty= (int)Util.get(tascl.getDeclaredField("y"),sprite);
			int spriteWidth=info.getSpriteWidth();
			int spriteHeight=info.getSpriteHeight();
			float minU= (float)Util.get(tascl.getDeclaredField("minU"),sprite);
			float minV= (float)Util.get(tascl.getDeclaredField("minV"),sprite);
			float atlasWidth=tx*minU;
			float atlasHeight=ty*minV;
			Constructor<?> con= tascl.getDeclaredConstructor(AtlasTexture.class,TextureAtlasSprite.Info.class,int.class,int.class,int.class,int.class,int.class,NativeImage.class);
			con.setAccessible(true);
			return (TextureAtlasSprite)con.newInstance(at,info,0,(int)atlasWidth,(int)atlasHeight,tx,ty,image);

		}catch(Exception e)
		{
			StringWriter sw=new StringWriter();
			PrintWriter pw=new PrintWriter(sw);
			e.printStackTrace(pw);
			LogManager.getLogger().error(sw.toString());
			return null;
		}
	}

}
