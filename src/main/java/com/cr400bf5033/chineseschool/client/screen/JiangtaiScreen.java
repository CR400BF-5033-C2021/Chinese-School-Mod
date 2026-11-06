package com.cr400bf5033.chineseschool.client.screen;

import com.cr400bf5033.chineseschool.menu.JiangtaiMenu;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;

/**
 * 讲台容器界面：27 格（3 行），直接复用箱子的界面贴图。
 */
public class JiangtaiScreen extends AbstractContainerScreen<JiangtaiMenu> {

    private static final Identifier TEXTURE =
            Identifier.fromNamespaceAndPath("minecraft", "textures/gui/container/generic_54.png");

    public JiangtaiScreen(JiangtaiMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title, 176, 168);
    }

    @Override
    public void extractBackground(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        // 先画原版的半透明模糊背景，再画容器贴图（generic_54 的顶部 168 像素正好是 3 行箱子）
        super.extractBackground(graphics, mouseX, mouseY, partialTick);
        graphics.blit(RenderPipelines.GUI_TEXTURED, TEXTURE, this.leftPos, this.topPos, 0, 0,
                this.imageWidth, this.imageHeight, 256, 256);
    }
}
