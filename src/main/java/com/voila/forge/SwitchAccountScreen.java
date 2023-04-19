package com.voila.forge;

import com.mojang.blaze3d.vertex.*;
import com.mojang.util.*;
import net.minecraft.*;
import net.minecraft.client.*;
import net.minecraft.client.gui.components.*;
import net.minecraft.client.gui.screens.*;
import net.minecraft.core.*;
import net.minecraft.network.chat.*;

import java.util.*;

public class SwitchAccountScreen extends Screen {
	private static User origSession;
	private final Screen parent;
	private String error = "";

	private EditBox usernameField;
	private EditBox uuidField;
	private EditBox tokenField;
	private EditBox passwordField;

	public SwitchAccountScreen(Screen parent){
		super(Component.translatable("menu." + Forgetest.ID + ".switchAccount"));
		this.parent = parent;
		if(origSession == null){
			origSession = ((IMinecraft) Minecraft.getInstance()).getUser();
		}
	}

	@Override
	protected void init(){
		super.init();
		final int fieldWidth = width / 3;
		final int buttonWidth = fieldWidth / 2;
		usernameField = new EditBox(font, (width - fieldWidth) / 2, 60, fieldWidth, 20, Component.empty());
		addWidget(usernameField);
		uuidField = new EditBox(font, (width - fieldWidth) / 2, 100, fieldWidth, 20, Component.empty());
		uuidField.setMaxLength(1024);
		addWidget(uuidField);
		tokenField = new EditBox(font, (width - fieldWidth) / 2, 140, fieldWidth, 20, Component.empty());
		tokenField.setMaxLength(1024);
		addWidget(tokenField);
		passwordField = new EditBox(font, (width - fieldWidth) / 2, 217, fieldWidth, 20, Component.empty());
		passwordField.setValue(Forgetest.getConfig("passwd"));
		addWidget(passwordField);

		addRenderableWidget(new Button((width - buttonWidth) / 2, 168, fieldWidth / 2, 20,
			Component.translatable("title." + Forgetest.ID + ".switch"), button ->
		{
			try{
				String username = usernameField.getValue();
				String uuid;
				if(uuidField.getValue().isEmpty())
					uuid = UUIDUtil.createOfflinePlayerUUID(username).toString();
				else
					uuid = UUIDTypeAdapter.fromString(uuidField.getValue()).toString();
				String token = tokenField.getValue();
				if(token.isEmpty())
					token = uuid;
				((IMinecraft) Minecraft.getInstance()).setUser(new User(username, uuid, token, Optional.empty(), Optional.empty(), User.Type.MOJANG));
				error = "";
			}catch(Exception e){
				error = e.getMessage();
				e.printStackTrace();
			}
		}));

		addRenderableWidget(new Button((width - buttonWidth) / 2, 192, fieldWidth / 2, 20,
			Component.translatable("title." + Forgetest.ID + ".restore"), button ->
		{
			try{
				((IMinecraft) Minecraft.getInstance()).setUser(origSession);
				error = "";
			}catch(Exception e){
				error = e.getMessage();
				e.printStackTrace();
			}
		}));

		addRenderableWidget(new Button((width - buttonWidth) / 2, height - 32, fieldWidth / 2, 20,
			Component.translatable("title." + Forgetest.ID + ".save"), button ->
			this.onClose()));
	}

	@Override
	public void render(PoseStack matrixStack, int mouseX, int mouseY, float partialTicks){
		super.render(matrixStack, mouseX, mouseY, partialTicks);
		renderDirtBackground(0);
		usernameField.render(matrixStack, mouseX, mouseY, partialTicks);
		uuidField.render(matrixStack, mouseX, mouseY, partialTicks);
		tokenField.render(matrixStack, mouseX, mouseY, partialTicks);
		passwordField.render(matrixStack, mouseX, mouseY, partialTicks);

		for(Widget button : renderables){
			button.render(matrixStack, mouseX, mouseY, partialTicks);
		}

		font.draw(matrixStack, title, (width - font.width(title)) / 2.0f, 15, 0xffffff);
		font.draw(matrixStack, Component.translatable("title." + Forgetest.ID + ".username"),
			usernameField.x, usernameField.y - 12, 0xffffff);
		font.draw(matrixStack, Component.translatable("title." + Forgetest.ID + ".uuid"),
			uuidField.x, uuidField.y - 12, 0xffffff);
		font.draw(matrixStack, Component.translatable("title." + Forgetest.ID + ".token"),
			tokenField.x, tokenField.y - 12, 0xffffff);

		User user = Minecraft.getInstance().getUser();
		font.draw(matrixStack, Component.translatable("title." + Forgetest.ID + ".current")
				.append(user.getName() + "  UUID: " + user.getUuid()),
			tokenField.x, 250, 0xffffff);
		font.draw(matrixStack, Component.literal(ChatFormatting.RED + error),
			tokenField.x, 270, 0xffffff);

	}

	@Override
	public void onClose(){
		Minecraft.getInstance().setScreen(parent);
		Forgetest.setConfig("passwd", passwordField.getValue());
	}
}
