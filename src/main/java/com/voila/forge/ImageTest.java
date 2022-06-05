package com.voila.forge;

import com.mojang.blaze3d.platform.*;
import com.mojang.util.*;
import net.minecraft.client.renderer.texture.*;

import java.io.*;
import java.util.*;

public class ImageTest {
	static NativeImage n;
	static NativeImage _0;
	static NativeImage _1;
	static NativeImage _2;
	static NativeImage _3;
	static NativeImage _4;
	static NativeImage _5;
	static NativeImage _6;
	static NativeImage _7;
	static NativeImage _8;
	static NativeImage _9;

	public static void main(String[] args) throws Throwable{
		try{
			n = NativeImage.read(new FileInputStream("nums\\n.png"));
			_0 = NativeImage.read(new FileInputStream("nums\\0.png"));
			_1 = NativeImage.read(new FileInputStream("nums\\1.png"));
			_2 = NativeImage.read(new FileInputStream("nums\\2.png"));
			_3 = NativeImage.read(new FileInputStream("nums\\3.png"));
			_4 = NativeImage.read(new FileInputStream("nums\\4.png"));
			_5 = NativeImage.read(new FileInputStream("nums\\5.png"));
			_6 = NativeImage.read(new FileInputStream("nums\\6.png"));
			_7 = NativeImage.read(new FileInputStream("nums\\7.png"));
			_8 = NativeImage.read(new FileInputStream("nums\\8.png"));
			_9 = NativeImage.read(new FileInputStream("nums\\9.png"));

			for(int i = 0; i <= 2048; i++){
				ArrayList<String> bs = new ArrayList<>(Arrays.asList((i + "").split(""))); // 分割每一位数字
				bs.removeIf(String::isEmpty); //移除空项
				NativeImage f = new NativeImage(128, 128, true); // width，height，clear
				copy(0, n, f); //写入负号
				int offset = n.getWidth() + 2; //偏移负号的宽度 +2
				for(int j = 0; j < bs.size(); j++){
					String nowstr = "_" + bs.get(j);
					NativeImage now = (NativeImage)ImageTest.class.getDeclaredField(nowstr).get(null);
					copy(offset, now, f); //写入当前数字
					offset += now.getWidth() + 2; //偏移数字宽度 +2
				}

				f.writeToFile(new File("f\\" + ("" + i) + ".png")); //写到文件
			}
		}catch(Exception e){
			e.printStackTrace();
		}
	}

	/** 逐像素写入 */
	static void copy(int offset, NativeImage from, NativeImage dest){
		int x = from.getWidth();
		int y = from.getHeight();
		for(int i = 0; i < x; i++){
			for(int j = 0; j < y; j++){
				dest.setPixelRGBA(i + offset, j, from.getPixelRGBA(i, j));
			}
		}
	}


}
