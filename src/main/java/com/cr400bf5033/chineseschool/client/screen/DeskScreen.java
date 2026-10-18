package com.cr400bf5033.chineseschool.client.screen;

import com.cr400bf5033.chineseschool.menu.DeskMenu;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;

/**
 * 课桌容器界面：5 个槽位，直接复用漏斗的界面贴图（176x133）。
 */
public class DeskScreen extends AbstractContainerScreen<DeskMenu> {

    private static final Identifier TEXTURE =
            Identifier.fromNamespaceAndPath("minecraft", "textures/gui/container/hopper.png");

    public DeskScreen(DeskMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title, 176, 133);
    }

    @Override
    public void extractBackground(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        // 先画原版的半透明模糊背景，再画容器贴图
        super.extractBackground(graphics, mouseX, mouseY, partialTick);
        graphics.blit(RenderPipelines.GUI_TEXTURED, TEXTURE, getLeftPos(), getTopPos(), 0, 0,
                getImageWidth(), getImageHeight(), 256, 256);
    }
}
