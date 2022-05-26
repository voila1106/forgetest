package com.voila.forge;

import com.mojang.blaze3d.vertex.*;
import net.minecraft.client.*;
import net.minecraft.client.gui.components.*;
import net.minecraft.client.gui.screens.*;
import net.minecraft.network.chat.*;

import java.io.*;

public class SelectScriptScreen extends Screen {
	ScriptList list;
	Button doneButton;

	public SelectScriptScreen(){
		super(new TranslatableComponent("title." + Forgetest.ID + ".selectScript"));
	}

	@Override
	protected void init(){
		super.init();
		doneButton = new Button((width - 150) / 2, height - 32, 150, 20,
			new TranslatableComponent("title." + Forgetest.ID + ".save"), (button) ->
			Minecraft.getInstance().setScreen(null));
		list = new ScriptList();

		addWidget(list);
		addRenderableWidget(doneButton);


	}

	@Override
	public void render(PoseStack matrixStack, int mouseX, int mouseY, float partialTicks){
		super.render(matrixStack, mouseX, mouseY, partialTicks);
		renderBackground(matrixStack);
		font.draw(matrixStack, title, (width - font.width(title)) / 2.0f, 20, 0xffffff);
		list.render(matrixStack, mouseX, mouseY, partialTicks);
		doneButton.render(matrixStack, mouseX, mouseY, partialTicks);
	}

	@Override
	public boolean isPauseScreen(){
		return false;
	}

	class ScriptList extends ObjectSelectionList<ScriptList.ScriptEntry> {

		public ScriptList(){
			super(Minecraft.getInstance(), SelectScriptScreen.this.width, SelectScriptScreen.this.height,
				48, SelectScriptScreen.this.height - 20 - 32 - 30, 20);
			setRenderBackground(false);
			setRenderTopAndBottom(false);

			File scriptDir = new File("scripts");
			scriptDir.mkdirs();
			for(String t : scriptDir.list()){
				ScriptEntry e = new ScriptEntry(t);
				addEntry(e);
				if(t.equals(Script.filename))
					setSelected(e);
			}

		}

		class ScriptEntry extends ObjectSelectionList.Entry<ScriptEntry> {

			String name;

			@Override
			public void render(PoseStack matrixStack, int index, int top, int left, int width, int height, int mouseX, int mouseY, boolean isMouseOver, float partialTicks){
				font.draw(matrixStack, name, left + 2, top + 3, 0xffffff);
			}

			public ScriptEntry(String filename){
				name = filename;
			}

			@Override
			public boolean mouseClicked(double p_231044_1_, double p_231044_3_, int p_231044_5_){
				ScriptList.this.setSelected(this);
				Script.filename = name;
				return true;
			}


			@Override
			public Component getNarration(){
				return TextComponent.EMPTY;
			}
		}

	}


}
