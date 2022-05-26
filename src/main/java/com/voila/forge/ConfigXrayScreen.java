package com.voila.forge;

import com.mojang.blaze3d.vertex.*;
import net.minecraft.client.*;
import net.minecraft.client.gui.*;
import net.minecraft.client.gui.components.*;
import net.minecraft.client.gui.screens.*;
import net.minecraft.network.chat.*;
import net.minecraft.resources.*;
import net.minecraft.world.level.block.*;
import net.minecraftforge.registries.*;
import org.jetbrains.annotations.*;
import org.lwjgl.glfw.*;

import javax.annotation.Nullable;
import java.io.*;
import java.util.*;

public class ConfigXrayScreen extends Screen {
	Button saveButton;
	BlockList blockList;
	EditBox addField;
	Button addButton;


	public ConfigXrayScreen(){
		super(new TranslatableComponent("title." + Forgetest.ID + ".configXray"));
	}

	@Override
	protected void init(){
		int fieldWidth = this.width / 3;
		addField = new EditBox(font, (width - fieldWidth) / 2, height - 32 - 20 - 5, fieldWidth, 20,
			new TextComponent("block")) {
			@Override
			public boolean keyPressed(int keyCode, int scanCode, int modifier){
				if(keyCode == GLFW.GLFW_KEY_ENTER){
					addButton.onPress();
					return true;
				}
				return super.keyPressed(keyCode, scanCode, modifier);
			}
		};
		addButton = new Button(addField.x + addField.getWidth() + 2, addField.y, 30, 20,
			new TranslatableComponent("title." + Forgetest.ID + ".add"), button ->
		{
			String name = addField.getValue();
			Block block;
			try{
				block = ForgeRegistries.BLOCKS.getValue(new ResourceLocation(name));
			}catch(Exception e){
				return;
			}
			if(block != null && block != Blocks.AIR){
				Keys.enabledBlocks.add(block);
				addField.setValue("");
				writeConfig();
				blockList.updateEntry();
				if(Keys.xray)
					Minecraft.getInstance().levelRenderer.allChanged();
			}


		});
		saveButton = new Button((width - 150) / 2, height - 32, 150, 20,
			new TranslatableComponent("title." + Forgetest.ID + ".save"), (button) ->
			Minecraft.getInstance().setScreen(null));
		blockList = new BlockList();
		addWidget(blockList);
		addWidget(addField);

		addRenderableWidget(addButton);
		addRenderableWidget(saveButton);
		addField.setFocus(true);
		blockList.updateEntry();
		super.init();
	}

	void writeConfig(){
		try{
			StringBuilder sb = new StringBuilder();
			for(Block t : Keys.enabledBlocks){
				sb.append(t.getRegistryName()).append('\n');
			}
			FileWriter fw = new FileWriter("config/" + Forgetest.ID + "/xray.txt");
			fw.write(sb.toString());
			fw.close();
		}catch(Exception e){
			e.printStackTrace();
		}
	}

	public static Set<Block> readConfig(){
		Set<Block> set = new HashSet<>();
		try{
			new File("config/" + Forgetest.ID).mkdirs();
			new File("config/" + Forgetest.ID + "/xray.txt").createNewFile();
			BufferedReader br = new BufferedReader(new FileReader("config/" + Forgetest.ID + "/xray.txt"));
			String line;
			while((line = br.readLine()) != null){
				Block block = ForgeRegistries.BLOCKS.getValue(new ResourceLocation(line));
				if(block != null)
					set.add(block);
			}
			br.close();
		}catch(Exception e){
			e.printStackTrace();
		}
		return set;

	}

	@Override
	public void render(PoseStack stack, int mouseX, int mouseY, float partialTicks){
		renderBackground(stack);
		int w = font.width(new TranslatableComponent("title." + Forgetest.ID + ".configXray"));
		drawString(stack, font, new TranslatableComponent("title." + Forgetest.ID + ".configXray"),
			(width - w) / 2, 20, 0xffffff);
		saveButton.render(stack, mouseX, mouseY, partialTicks);
		blockList.render(stack, mouseX, mouseY, partialTicks);
		addButton.render(stack, mouseX, mouseY, partialTicks);
		addField.render(stack, mouseX, mouseY, partialTicks);
		super.render(stack, mouseX, mouseY, partialTicks);
	}

	class BlockList extends ObjectSelectionList<BlockList.BlockListEntry> {
		public BlockList(){
			super(Minecraft.getInstance(),
				ConfigXrayScreen.this.width,
				ConfigXrayScreen.this.height,
				48,
				ConfigXrayScreen.this.height - 20 - 32 - 30,
				20);
			this.setRenderBackground(false);
			this.setRenderTopAndBottom(false);
		}

		public void addEntry(Block block){
			super.addEntry(new BlockListEntry(block));
		}

		public void updateEntry(){
			clearEntries();
			for(Block t : ConfigXrayScreen.readConfig()){
				addEntry(t);
			}
		}

		@Override
		public void setSelected(@Nullable BlockListEntry entry){
			super.setSelected(entry);
			if(entry != null){
				removeEntry(entry);
				Keys.enabledBlocks.remove(entry.block);
				writeConfig();
				if(Keys.xray)
					Minecraft.getInstance().levelRenderer.allChanged();
			}
		}

		class BlockListEntry extends ObjectSelectionList.Entry<BlockListEntry> {
			public Block block;

			public BlockListEntry(Block block){
				this.block = block;
			}

			@Override
			public void render(PoseStack stack, int index, int top, int left, int width, int height, int mouseX, int mouseY, boolean isMouseOver, float partialTicks){
				Font fontRenderer = ConfigXrayScreen.this.font;
				GuiComponent.drawString(stack, fontRenderer, block.getName(), left, top + 5, 16777215);
				if(isMouseOver)
					renderTooltip(stack, new TextComponent(block.getRegistryName().toString()), mouseX, mouseY);
			}

			@Override
			public boolean mouseClicked(double p_231044_1_, double p_231044_3_, int p_231044_5_){
				BlockList.this.setSelected(this);
				return super.mouseClicked(p_231044_1_, p_231044_3_, p_231044_5_);
			}

			@Override
			@NotNull
			public Component getNarration(){
				return TextComponent.EMPTY;
			}
		}

	}

	@Override
	public boolean isPauseScreen(){
		return false;
	}

	@Override
	public boolean keyPressed(int keyCode, int scanCode, int modifiers){
		setFocused(addField);
		return super.keyPressed(keyCode, scanCode, modifiers);
	}
}
