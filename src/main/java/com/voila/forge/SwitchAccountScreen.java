package com.voila.forge;

import com.mojang.blaze3d.matrix.*;
import com.mojang.util.*;
import net.minecraft.client.*;
import net.minecraft.client.gui.screen.*;
import net.minecraft.client.gui.widget.*;
import net.minecraft.client.gui.widget.button.*;
import net.minecraft.entity.player.*;
import net.minecraft.util.*;
import net.minecraft.util.text.*;

public class SwitchAccountScreen extends Screen
{
	private static Session origSession;
	private final Screen parent;
	private String error = "";

	private TextFieldWidget usernameField;
	private TextFieldWidget uuidField;
	private TextFieldWidget tokenField;

	public SwitchAccountScreen(Screen parent)
	{
		super(new TranslationTextComponent("menu." + Forgetest.ID + ".switchAccount"));
		this.parent = parent;
		if(origSession == null)
		{
			origSession = Minecraft.getInstance().getSession();
		}
	}

	@Override
	protected void init()
	{
		super.init();
		final int fieldWidth = width / 3;
		final int buttonWidth = fieldWidth / 2;
		usernameField = new TextFieldWidget(font, (width - fieldWidth) / 2, 60, fieldWidth, 20, StringTextComponent.EMPTY);
		children.add(usernameField);
		uuidField = new TextFieldWidget(font, (width - fieldWidth) / 2, 100, fieldWidth, 20, StringTextComponent.EMPTY);
		children.add(uuidField);
		tokenField = new TextFieldWidget(font, (width - fieldWidth) / 2, 140, fieldWidth, 20, StringTextComponent.EMPTY);
		tokenField.setMaxStringLength(1024);
		children.add(tokenField);

		addButton(new Button((width - buttonWidth) / 2, 168, fieldWidth / 2, 20,
			new TranslationTextComponent("title." + Forgetest.ID + ".switch"), button ->
		{
			try
			{
				String username = usernameField.getText();
				String uuid;
				if(uuidField.getText().isEmpty())
					uuid = PlayerEntity.getOfflineUUID(username).toString();
				else
					uuid = UUIDTypeAdapter.fromString(uuidField.getText()).toString();
				String token = tokenField.getText();
				if(token.isEmpty())
					token = uuid;
				Minecraft.getInstance().session = new Session(username, uuid, token, "mojang");
				error = "";
			}catch(Exception e)
			{
				error = e.getMessage();
				e.printStackTrace();
			}
		}));

		addButton(new Button((width - buttonWidth) / 2, 192, fieldWidth / 2, 20,
			new TranslationTextComponent("title." + Forgetest.ID + ".restore"), button ->
		{
			try
			{
				Minecraft.getInstance().session = origSession;
				error = "";
			}catch(Exception e)
			{
				error = e.getMessage();
				e.printStackTrace();
			}
		}));

		addButton(new Button((width - buttonWidth) / 2, height - 32, fieldWidth / 2, 20,
			new TranslationTextComponent("title." + Forgetest.ID + ".save"), button ->
			Minecraft.getInstance().displayGuiScreen(parent)));
	}

	@Override
	public void render(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks)
	{
		super.render(matrixStack, mouseX, mouseY, partialTicks);
		renderDirtBackground(0);
		usernameField.render(matrixStack, mouseX, mouseY, partialTicks);
		uuidField.render(matrixStack, mouseX, mouseY, partialTicks);
		tokenField.render(matrixStack, mouseX, mouseY, partialTicks);

		for(Widget button : buttons)
		{
			button.render(matrixStack, mouseX, mouseY, partialTicks);
		}

		font.drawText(matrixStack, title, (width - font.getStringPropertyWidth(title)) / 2.0f, 15, 0xffffff);
		font.drawText(matrixStack, new TranslationTextComponent("title." + Forgetest.ID + ".username"),
			usernameField.x, usernameField.y - 12, 0xffffff);
		font.drawText(matrixStack, new TranslationTextComponent("title." + Forgetest.ID + ".uuid"),
			uuidField.x, uuidField.y - 12, 0xffffff);
		font.drawText(matrixStack, new TranslationTextComponent("title." + Forgetest.ID + ".token"),
			tokenField.x, tokenField.y - 12, 0xffffff);

		Session session = Minecraft.getInstance().getSession();
		font.drawText(matrixStack, new TranslationTextComponent("title." + Forgetest.ID + ".current")
				.appendString(session.getUsername() + "  UUID: " + session.getPlayerID()),
			tokenField.x, 250, 0xffffff);
		font.drawText(matrixStack, new StringTextComponent(TextFormatting.RED + error),
			tokenField.x, 270, 0xffffff);

	}

	@Override
	public void closeScreen()
	{
		Minecraft.getInstance().displayGuiScreen(parent);
	}
}
