package com.voila.forge;

import net.minecraft.client.*;

public interface IMinecraft {
	void pick();

	void use();

	void attack();

	User getSession();

	void setSession(User se);
}
