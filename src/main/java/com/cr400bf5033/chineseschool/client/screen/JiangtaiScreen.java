package com.cr400bf5033.chineseschool.client.screen;

import com.cr400bf5033.chineseschool.menu.JiangtaiMenu;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;

/**
 * 讲台容器界面：27 格（3 行），复用箱子的界面贴图。
 *
 * <p>箱子贴图（generic_54）里上半部分是容器区、下半部分是背包区，
 * 原版是分两段贴的，这里照做，否则槽位会错位。</p>
 */
public class JiangtaiScreen extends AbstractContainerScreen<JiangtaiMenu> {

    private static final Identifier TEXTURE =
            Identifier.fromNamespaceAndPath("minecraft", "textures/gui/container/generic_54.png");

    /** 容器区高度：3 行槽位 + 上边框。 */
    private static final int CONTAINER_HEIGHT = 3 * 18 + 17;

    /** 背包区在贴图中的起始 V 坐标。 */
    private static final int INVENTORY_V = 126;

    /** 背包区高度。 */
    private static final int INVENTORY_HEIGHT = 96;

    public JiangtaiScreen(JiangtaiMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title, 176, 168);
    }

    @Override
    public void extractBackground(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        // 先画原版的半透明模糊背景
        super.extractBackground(graphics, mouseX, mouseY, partialTick);
        // 容器区
        graphics.blit(RenderPipelines.GUI_TEXTURED, TEXTURE, this.leftPos, this.topPos, 0.0F, 0.0F,
                this.imageWidth, CONTAINER_HEIGHT, 256, 256);
        // 背包区
        graphics.blit(RenderPipelines.GUI_TEXTURED, TEXTURE, this.leftPos, this.topPos + CONTAINER_HEIGHT,
                0.0F, (float) INVENTORY_V, this.imageWidth, INVENTORY_HEIGHT, 256, 256);
    }
}
