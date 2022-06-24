package com.voila.forge.mixin;

import com.mojang.blaze3d.systems.*;
import com.mojang.blaze3d.vertex.*;
import com.mojang.math.*;
import com.voila.forge.*;
import net.minecraft.client.gui.*;
import net.minecraft.client.gui.components.*;
import net.minecraft.client.gui.components.events.*;
import net.minecraft.client.gui.screens.*;
import net.minecraft.client.gui.screens.inventory.tooltip.*;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.entity.*;
import net.minecraft.world.item.*;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.*;

import java.util.*;

/** Tooltip overflow fix */
@Mixin(Screen.class)
public abstract class MixinScreen extends AbstractContainerEventHandler implements Widget {
	@Shadow public int width;
	@Shadow public int height;
	@Shadow protected ItemRenderer itemRenderer;
	@Shadow private ItemStack tooltipStack;
	@Shadow private Font tooltipFont;
	@Shadow protected Font font;

	@Shadow public abstract List<? extends GuiEventListener> children();

	@Shadow public abstract void render(PoseStack p_96562_, int p_96563_, int p_96564_, float p_96565_);

	@Rewrite
	@Inject(method = "renderTooltipInternal", at = @At("HEAD"), cancellable = true)
	private void renderTooltip(PoseStack stack, List<ClientTooltipComponent> componentList, int x, int y, CallbackInfo info){
		info.cancel();

		if(!componentList.isEmpty()){
			net.minecraftforge.client.event.RenderTooltipEvent.Pre preEvent = net.minecraftforge.client.ForgeHooksClient.onRenderTooltipPre(this.tooltipStack, stack, x, y, width, height, componentList, this.tooltipFont, this.font);
			if(preEvent.isCanceled()) return;

			int maxWidth = 0;
			int allHeight = componentList.size() == 1 ? -2 : 0;

			for(ClientTooltipComponent component : componentList){
				int w = component.getWidth(preEvent.getFont());
				if(w > maxWidth){
					maxWidth = w;
				}

				allHeight += component.getHeight();
			}

			int left = preEvent.getX() + 12;
			int top = preEvent.getY() - 12;

			//右边超出屏幕时跳到左侧
			if(left + maxWidth /*right*/ > this.width){
				left -= (28 + maxWidth);
			}

			if(top + allHeight /*bottom*/ + 6 > this.height){
				top = this.height - allHeight - 6;
			}

			if(allHeight > this.height){
				top = -((allHeight - this.height) / 2);
			}

			stack.pushPose();
			float offset = this.itemRenderer.blitOffset;
			this.itemRenderer.blitOffset = 400.0F;
			Tesselator tesselator = Tesselator.getInstance();
			BufferBuilder bufferbuilder = tesselator.getBuilder();
			RenderSystem.setShader(GameRenderer::getPositionColorShader);
			bufferbuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
			Matrix4f matrix4f = stack.last().pose();
			net.minecraftforge.client.event.RenderTooltipEvent.Color colorEvent = net.minecraftforge.client.ForgeHooksClient.onRenderTooltipColor(this.tooltipStack, stack, left, top, preEvent.getFont(), componentList);
			fillGradient(matrix4f, bufferbuilder, left - 3, top - 4, left + maxWidth + 3, top - 3, 400, colorEvent.getBackgroundStart(), colorEvent.getBackgroundStart());
			fillGradient(matrix4f, bufferbuilder, left - 3, top + allHeight + 3, left + maxWidth + 3, top + allHeight + 4, 400, colorEvent.getBackgroundEnd(), colorEvent.getBackgroundEnd());
			fillGradient(matrix4f, bufferbuilder, left - 3, top - 3, left + maxWidth + 3, top + allHeight + 3, 400, colorEvent.getBackgroundStart(), colorEvent.getBackgroundEnd());
			fillGradient(matrix4f, bufferbuilder, left - 4, top - 3, left - 3, top + allHeight + 3, 400, colorEvent.getBackgroundStart(), colorEvent.getBackgroundEnd());
			fillGradient(matrix4f, bufferbuilder, left + maxWidth + 3, top - 3, left + maxWidth + 4, top + allHeight + 3, 400, colorEvent.getBackgroundStart(), colorEvent.getBackgroundEnd());
			fillGradient(matrix4f, bufferbuilder, left - 3, top - 3 + 1, left - 3 + 1, top + allHeight + 3 - 1, 400, colorEvent.getBorderStart(), colorEvent.getBorderEnd());
			fillGradient(matrix4f, bufferbuilder, left + maxWidth + 2, top - 3 + 1, left + maxWidth + 3, top + allHeight + 3 - 1, 400, colorEvent.getBorderStart(), colorEvent.getBorderEnd());
			fillGradient(matrix4f, bufferbuilder, left - 3, top - 3, left + maxWidth + 3, top - 3 + 1, 400, colorEvent.getBorderStart(), colorEvent.getBorderStart());
			fillGradient(matrix4f, bufferbuilder, left - 3, top + allHeight + 2, left + maxWidth + 3, top + allHeight + 3, 400, colorEvent.getBorderEnd(), colorEvent.getBorderEnd());
			RenderSystem.enableDepthTest();
			RenderSystem.disableTexture();
			RenderSystem.enableBlend();
			RenderSystem.defaultBlendFunc();
			BufferUploader.drawWithShader(bufferbuilder.end());
			RenderSystem.disableBlend();
			RenderSystem.enableTexture();
			MultiBufferSource.BufferSource multibuffersource$buffersource = MultiBufferSource.immediate(Tesselator.getInstance().getBuilder());
			stack.translate(0.0D, 0.0D, 400.0D);

			int line = top;
			for(int i = 0; i < componentList.size(); ++i){
				ClientTooltipComponent component = componentList.get(i);
				component.renderText(preEvent.getFont(), left, line, matrix4f, multibuffersource$buffersource);
				line += component.getHeight() + (i == 0 ? 2 : 0);
			}

			multibuffersource$buffersource.endBatch();
			stack.popPose();

			line = top;
			for(int i = 0; i < componentList.size(); ++i){
				ClientTooltipComponent component = componentList.get(i);
				component.renderImage(preEvent.getFont(), left, line, stack, this.itemRenderer, 400);
				line += component.getHeight() + (i == 0 ? 2 : 0);
			}

			this.itemRenderer.blitOffset = offset;
		}
	}

}
