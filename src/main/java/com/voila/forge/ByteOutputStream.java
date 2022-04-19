package com.voila.forge;

import java.io.*;
import java.util.*;

public class ByteOutputStream extends OutputStream
{
	private byte[] content;
	private int size=0;
	@Override
	public void write(int b)
	{
		ensure(1);
		content[size]= (byte)b;
		size++;

	}

	@Override
	public void write(byte[] b)
	{
		ensure(b.length);
		System.arraycopy(b, 0, content, size, b.length);
		size+=b.length;
	}

	private void ensure(int l)
	{
		int newSize=content.length;
		while(newSize<content.length+l)
		{
			newSize+=1024;
		}
		if(content.length!=newSize)
		{
			content= Arrays.copyOf(content,newSize);
		}

	}

	public byte[] bytes()
	{
		return Arrays.copyOf(content,size);
	}

	public ByteOutputStream()
	{
		content=new byte[1024];
	}
}