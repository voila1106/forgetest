package com.voila.forge;

import com.mojang.blaze3d.matrix.*;
import net.minecraft.client.*;
import net.minecraft.client.gui.screen.*;
import net.minecraft.client.gui.widget.button.*;
import net.minecraft.client.gui.widget.list.*;
import net.minecraft.util.text.*;

import java.io.*;

public class SelectScriptScreen extends Screen {
	ScriptList list;
	Button doneButton;

	public SelectScriptScreen(){
		super(new TranslationTextComponent("title." + Forgetest.ID + ".selectScript"));
	}

	@Override
	protected void init(){
		super.init();
		doneButton = new Button((width - 150) / 2, height - 32, 150, 20,
			new TranslationTextComponent("title." + Forgetest.ID + ".save"), (button) ->
			Minecraft.getInstance().displayGuiScreen(null));
		list = new ScriptList();

		children.add(list);
		addButton(doneButton);


	}

	@Override
	public void render(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks){
		super.render(matrixStack, mouseX, mouseY, partialTicks);
		renderBackground(matrixStack);
		font.drawText(matrixStack, title, (width - font.getStringPropertyWidth(title)) / 2.0f, 20, 0xffffff);
		list.render(matrixStack, mouseX, mouseY, partialTicks);
		doneButton.render(matrixStack, mouseX, mouseY, partialTicks);
	}

	@Override
	public boolean isPauseScreen(){
		return false;
	}

	class ScriptList extends ExtendedList<ScriptList.ScriptEntry> {

		public ScriptList(){
			super(Minecraft.getInstance(), SelectScriptScreen.this.width, SelectScriptScreen.this.height,
				48, SelectScriptScreen.this.height - 20 - 32 - 30, 20);
			this.func_244605_b(false);
			this.func_244606_c(false);

			File scriptDir = new File("scripts");
			scriptDir.mkdirs();
			for(String t : scriptDir.list()){
				ScriptEntry e = new ScriptEntry(t);
				addEntry(e);
				if(t.equals(Script.filename))
					setSelected(e);
			}

		}

		class ScriptEntry extends ExtendedList.AbstractListEntry<ScriptEntry> {

			String name;

			@Override
			public void render(MatrixStack matrixStack, int index, int top, int left, int width, int height, int mouseX, int mouseY, boolean isMouseOver, float partialTicks){
				font.drawString(matrixStack, name, left + 2, top + 3, 0xffffff);
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


		}

	}


}
