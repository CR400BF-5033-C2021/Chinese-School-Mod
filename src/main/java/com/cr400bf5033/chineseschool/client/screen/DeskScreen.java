package com.cr400bf5033.chineseschool.client.screen;

import com.cr400bf5033.chineseschool.ChineseSchoolMod;
import com.cr400bf5033.chineseschool.menu.DeskMenu;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;

/**
 * 课桌容器界面，背景图尺寸 176x166。
 */
public class DeskScreen extends AbstractContainerScreen<DeskMenu> {

    private static final Identifier TEXTURE =
            Identifier.fromNamespaceAndPath(ChineseSchoolMod.MOD_ID, "textures/gui/container/desk.png");

    public DeskScreen(DeskMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
        this.imageWidth = 176;
        this.imageHeight = 166;
    }

    @Override
    public void extractBackground(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        graphics.blit(RenderPipelines.GUI_TEXTURED, TEXTURE, this.leftPos, this.topPos, 0, 0,
                this.imageWidth, this.imageHeight, 256, 256);
    }
}
