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
 * 课桌容器界面，背景图尺寸 176x166，与 {@link AbstractContainerScreen} 的默认尺寸一致。
 */
public class DeskScreen extends AbstractContainerScreen<DeskMenu> {

    private static final Identifier TEXTURE =
            Identifier.fromNamespaceAndPath(ChineseSchoolMod.MOD_ID, "textures/gui/container/desk.png");

    public DeskScreen(DeskMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
    }

    @Override
    public void extractBackground(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        graphics.blit(RenderPipelines.GUI_TEXTURED, TEXTURE, getLeftPos(), getTopPos(), 0, 0,
                getImageWidth(), getImageHeight(), 256, 256);
    }
}
