package com.voila.forge;

import com.mojang.blaze3d.systems.*;
import com.mojang.blaze3d.vertex.*;
import net.minecraft.client.*;
import net.minecraft.client.gui.*;
import net.minecraft.client.gui.components.*;
import net.minecraft.client.gui.screens.*;
import net.minecraft.client.player.*;
import net.minecraft.core.*;
import net.minecraft.network.chat.*;
import net.minecraft.resources.*;
import net.minecraft.util.*;
import net.minecraft.world.item.*;
import net.minecraft.world.level.block.*;
import net.minecraft.world.phys.*;
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
	public SugList sugList;

	private static final List<String> allBlocks = new ArrayList<>();

	static{
		for(Block block : Registry.BLOCK){
			allBlocks.add(block.getRegistryName().toString());
		}
		allBlocks.sort(String::compareTo);
	}

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
				}else if(keyCode == GLFW.GLFW_KEY_UP){
					sugList.keyPressed(GLFW.GLFW_KEY_UP, 0, 0);
					return true;
				}else if(keyCode == GLFW.GLFW_KEY_DOWN){
					sugList.keyPressed(GLFW.GLFW_KEY_DOWN, 0, 0);
					return true;
				}else if(keyCode == GLFW.GLFW_KEY_TAB){
					SugList.SugEntry entry = sugList.getSelected();
					if(entry != null){
						setValue(entry.block);
						sugList.update(getValue());
					}
					return false;
				}
				return super.keyPressed(keyCode, scanCode, modifier);
			}
		};
		addField.setMaxLength(200);
		addField.setResponder(content -> sugList.update(content));
		addButton = new Button(addField.x + addField.getWidth() + 2, addField.y, 30, 20,
			new TranslatableComponent("title." + Forgetest.ID + ".add"), button ->
		{
			String name = addField.getValue();
			LocalPlayer player=Minecraft.getInstance().player;
			assert player != null;
			if(name.equals("hand")){
				ItemStack item=player.getInventory().getSelected();
				Block block=Block.byItem(item.getItem());
				if(block!=Blocks.AIR){
					addXrayBlock(block);
				}
			}else if(name.equals("look")){
				HitResult result= player.pick(20,0,false);
				if(result.getType()== HitResult.Type.BLOCK){
					addXrayBlock(Minecraft.getInstance().level.getBlockState(((BlockHitResult)result).getBlockPos()).getBlock());
				}
			}
			Block block;
			try{
				block = ForgeRegistries.BLOCKS.getValue(new ResourceLocation(name));
			}catch(Exception e){
				return;
			}
			if(block != null && block != Blocks.AIR){
				addXrayBlock(block);
			}


		});
		saveButton = new Button((width - 150) / 2, height - 32, 150, 20,
			new TranslatableComponent("title." + Forgetest.ID + ".save"), (button) ->
			Minecraft.getInstance().setScreen(null));
		blockList = new BlockList();
		sugList = new SugList();
		addWidget(sugList);
		addWidget(blockList);
		addWidget(addField);

		addRenderableWidget(addButton);
		addRenderableWidget(saveButton);
		addField.setFocus(true);
		blockList.updateEntry();
		super.init();
	}

	private void addXrayBlock(Block block){
		Keys.enabledBlocks.add(block);
		addField.setValue("");
		writeConfig();
		blockList.updateEntry();
		addField.setFocus(true);
		ConfigXrayScreen.this.setFocused(addField);
		if(Keys.xray)
			Minecraft.getInstance().levelRenderer.allChanged();
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

	static Set<String> readStringConfig(){
		Set<String> set = new HashSet<>();
		try{
			new File("config/" + Forgetest.ID).mkdirs();
			new File("config/" + Forgetest.ID + "/xray.txt").createNewFile();
			BufferedReader br = new BufferedReader(new FileReader("config/" + Forgetest.ID + "/xray.txt"));
			String line;
			while((line = br.readLine()) != null){
				if(!line.isEmpty())
					set.add(line);
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
		sugList.render(stack, mouseX, mouseY, partialTicks);
		super.render(stack, mouseX, mouseY, partialTicks);
	}

	class SugList extends ObjectSelectionList<SugList.SugEntry> {
		int w = 0;

		public SugList(){
			super(Minecraft.getInstance(), ConfigXrayScreen.this.width,
				ConfigXrayScreen.this.height,
				addField.y - 90,
				addField.y - 3,
				10);
			setRenderSelection(false);
			x0 = addField.x;
			//setRenderBackground(false);
			setRenderTopAndBottom(false);
			update("");
		}

		public void update(String input){
			clearEntries();
			if(input.isEmpty()){
				y0 = addField.y - 30;
				x1 = 0;
				setSelected(null);
				return;
			}
			w = 0;
			Set<String> stored = readStringConfig();
			for(String t : allBlocks){
				if(t.contains(input) && !stored.contains(t)){
					addEntry(new SugEntry(t));
					w = Math.max(w, font.width(t));
				}
			}

			if(children().size() > 0)
				setSelected(children().get(children().size() - 1));
			y0 = Mth.clamp(addField.y - children().size() * 10, addField.y - 90, addField.y - 30);
			setScrollAmount(getMaxScroll());
			width = w + 20;
			height = 100;
			x1 = getScrollbarPosition() + 5;
		}


		@Override
		public boolean keyPressed(int keyCode, int scanCode, int modifier){
			if(children().size() < 1)
				return super.keyPressed(keyCode, scanCode, modifier);
			if(keyCode == GLFW.GLFW_KEY_UP && getSelected() == children().get(0)){
				setSelected(children().get(children().size() - 1));
				ensureVisible(getSelected());
				return true;
			}else if(keyCode == GLFW.GLFW_KEY_DOWN && getSelected() == children().get(children().size() - 1)){
				setSelected(children().get(0));
				ensureVisible(getSelected());
				return true;
			}
			return super.keyPressed(keyCode, scanCode, modifier);
		}

		@Override
		public int getRowLeft(){
			return addField.x;
		}

		@Override
		public int getRowWidth(){
			return w + 20;
		}

		@Override
		protected int getScrollbarPosition(){
			return getRowRight();
		}

		class SugEntry extends ObjectSelectionList.Entry<SugEntry> {
			String block;

			public SugEntry(String block){
				this.block = block;
			}

			@Override
			public void render(PoseStack stack, int index, int top, int left, int width, int height, int mouseX, int mouseY, boolean isMouseOver, float partialTicks){
				if(top < y0 || top + height > y1){
					return;
				}
				if(isMouseOver){
					setSelected(this);
				}
				font.draw(stack, block, left, top, getSelected() == this ? 0xffff00 : 0xffffff);
			}

			@Override
			public boolean mouseClicked(double p_94737_, double p_94738_, int p_94739_){
				addField.setValue(block);
				addField.setFocus(true);
				ConfigXrayScreen.this.setFocused(addField);
				return super.mouseClicked(p_94737_, p_94738_, p_94739_);
			}

			@Override
			public Component getNarration(){
				return TextComponent.EMPTY;
			}
		}
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
			Set<Block> stored = ConfigXrayScreen.readConfig();
			for(Block t : stored){
				addEntry(t);
			}
			Keys.enabledBlocks = stored;  // fix: lost all configs
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

		@Override
		public void render(PoseStack p_93447_, int p_93448_, int p_93449_, float p_93450_){
			y1 = sugList.getTop() - 15;
			super.render(p_93447_, p_93448_, p_93449_, p_93450_);
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
		if(keyCode == GLFW.GLFW_KEY_TAB){
			addField.keyPressed(keyCode, scanCode, modifiers);
			return true;
		}
		setFocused(addField);
		return super.keyPressed(keyCode, scanCode, modifiers);
	}
}
