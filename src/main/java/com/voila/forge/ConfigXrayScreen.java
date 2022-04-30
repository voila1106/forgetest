package com.voila.forge;

import com.mojang.blaze3d.matrix.*;
import com.mojang.blaze3d.systems.*;
import net.minecraft.block.*;
import net.minecraft.client.*;
import net.minecraft.client.gui.*;
import net.minecraft.client.gui.screen.*;
import net.minecraft.client.gui.widget.*;
import net.minecraft.client.gui.widget.button.*;
import net.minecraft.client.gui.widget.list.*;
import net.minecraft.client.gui.widget.list.AbstractList;
import net.minecraft.item.*;
import net.minecraft.util.*;
import net.minecraft.util.text.*;
import net.minecraftforge.registries.*;
import org.lwjgl.glfw.*;

import javax.annotation.*;
import java.io.*;
import java.util.*;

public class ConfigXrayScreen extends Screen
{
	Button saveButton;
	BlockList blockList;
	TextFieldWidget addField;
	Button addButton;


	public ConfigXrayScreen()
	{
		super(new TranslationTextComponent("title." + Forgetest.ID + ".configXray"));
	}

	@Override
	protected void init()
	{
		int fieldWidth = this.width / 3;
		addField = new TextFieldWidget(font, (width - fieldWidth) / 2, height - 32 - 20 - 5, fieldWidth, 20,
			new StringTextComponent("block"))
		{
			@Override
			public boolean keyPressed(int keyCode, int scanCode, int modifier)
			{
				if(keyCode == GLFW.GLFW_KEY_ENTER)
				{
					addButton.onPress();
					return true;
				}
				return super.keyPressed(keyCode, scanCode, modifier);
			}
		};
		addButton = new Button(addField.x + addField.getWidth() + 2, addField.y, 30, 20,
			new TranslationTextComponent("title." + Forgetest.ID + ".add"), button ->
		{
			String name = addField.getText();
			Block block;
			try
			{
				block = ForgeRegistries.BLOCKS.getValue(new ResourceLocation(name));
			}catch(Exception e)
			{
				return;
			}
			if(block != null && block != Blocks.AIR)
			{
				Keys.enabledBlocks.add(block);
				addField.setText("");
				writeConfig();
				blockList.updateEntry();
				if(Keys.xray)
					Minecraft.getInstance().worldRenderer.loadRenderers();
			}


		});
		saveButton = new Button((width - 150) / 2, height - 32, 150, 20,
			new TranslationTextComponent("title." + Forgetest.ID + ".save"), (button) ->
			Minecraft.getInstance().displayGuiScreen(null));
		blockList = new BlockList();
		children.add(blockList);
		children.add(addField);
		addButton(addButton);
		addButton(saveButton);
		addField.setFocused2(true);
		blockList.updateEntry();
		super.init();
	}

	void writeConfig()
	{
		try
		{
			StringBuilder sb = new StringBuilder();
			for(Block t : Keys.enabledBlocks)
			{
				sb.append(t.getRegistryName()).append('\n');
			}
			FileWriter fw = new FileWriter("config/" + Forgetest.ID + "/xray.txt");
			fw.write(sb.toString());
			fw.close();
		}catch(Exception e)
		{
			e.printStackTrace();
		}
	}

	public static Set<Block> readConfig()
	{
		Set<Block> set = new HashSet<>();
		try
		{
			new File("config/" + Forgetest.ID).mkdirs();
			new File("config/" + Forgetest.ID + "/xray.txt").createNewFile();
			BufferedReader br = new BufferedReader(new FileReader("config/" + Forgetest.ID + "/xray.txt"));
			String line;
			while((line = br.readLine()) != null)
			{
				Block block = ForgeRegistries.BLOCKS.getValue(new ResourceLocation(line));
				if(block != null)
					set.add(block);
			}
			br.close();
		}catch(Exception e)
		{
			e.printStackTrace();
		}
		return set;

	}

	@Override
	public void render(MatrixStack matrixStackIn, int mouseX, int mouseY, float partialTicks)
	{
		RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
		renderBackground(matrixStackIn);
		int w = font.getStringPropertyWidth(new TranslationTextComponent("title." + Forgetest.ID + ".configXray"));
		drawString(matrixStackIn, font, new TranslationTextComponent("title." + Forgetest.ID + ".configXray"),
			(width - w) / 2, 20, 0xffffff);
		saveButton.render(matrixStackIn, mouseX, mouseY, partialTicks);
		blockList.render(matrixStackIn, mouseX, mouseY, partialTicks);
		addButton.render(matrixStackIn, mouseX, mouseY, partialTicks);
		addField.render(matrixStackIn, mouseX, mouseY, partialTicks);
		super.render(matrixStackIn, mouseX, mouseY, partialTicks);
	}

	class BlockList extends ExtendedList<BlockList.BlockListEntry>
	{
		public BlockList()
		{
			super(Minecraft.getInstance(),
				ConfigXrayScreen.this.width,
				ConfigXrayScreen.this.height,
				48,
				ConfigXrayScreen.this.height - 20 - 32 - 30,
				20);
			this.func_244605_b(false);
			this.func_244606_c(false);
		}

		public void addEntry(Block block)
		{
			super.addEntry(new BlockListEntry(block));
		}

		public void updateEntry()
		{
			clearEntries();
			for(Block t : ConfigXrayScreen.readConfig())
			{
				addEntry(t);
			}
		}

		@Override
		public void setSelected(@Nullable BlockListEntry entry)
		{
			super.setSelected(entry);
			if(entry != null)
			{
				removeEntry(entry);
				Keys.enabledBlocks.remove(entry.block);
				writeConfig();
				if(Keys.xray)
					Minecraft.getInstance().worldRenderer.loadRenderers();
			}
		}

		class BlockListEntry extends AbstractList.AbstractListEntry<BlockListEntry>
		{
			public Block block;

			public BlockListEntry(Block block)
			{
				this.block = block;
			}

			@Override
			public void render(MatrixStack matrixStack, int index, int top, int left, int width, int height, int mouseX, int mouseY, boolean isMouseOver, float partialTicks)
			{
				FontRenderer fontRenderer = Minecraft.getInstance().fontRenderer;
				//fontRenderer.drawString(matrixStack, block, left + 200, top, 0xffffff);
				//drawString(matrixStack,font,block,index,10,0xffffff);
				AbstractGui.drawString(matrixStack, fontRenderer, block.getTranslatedName(), left, top + 5, 16777215);
			}

			@Override
			public boolean mouseClicked(double p_231044_1_, double p_231044_3_, int p_231044_5_)
			{
				BlockList.this.setSelected(this);
				return super.mouseClicked(p_231044_1_, p_231044_3_, p_231044_5_);
			}
		}

	}

	@Override
	public boolean isPauseScreen()
	{
		return false;
	}
}
