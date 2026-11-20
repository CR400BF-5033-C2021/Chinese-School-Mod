package com.cr400bf5033.chineseschool.client.screen;

import com.cr400bf5033.chineseschool.block.BanpaiBlockEntity;
import com.cr400bf5033.chineseschool.network.BanpaiTextPayload;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.client.Minecraft;
import net.minecraft.network.protocol.common.ServerboundCustomPayloadPacket;

/**
 * 班牌文字编辑界面：输入班级名称并提交给服务端。
 */
public class BanpaiEditScreen extends Screen {

    private final BanpaiBlockEntity banpai;
    private EditBox input;

    public BanpaiEditScreen(BanpaiBlockEntity banpai) {
        super(Component.translatable("container.chinese_school.banpai"));
        this.banpai = banpai;
    }

    @Override
    protected void init() {
        int cx = this.width / 2;
        int cy = this.height / 2;
        this.input = new EditBox(this.font, cx - 90, cy - 20, 180, 20,
                Component.translatable("container.chinese_school.banpai"));
        this.input.setMaxLength(BanpaiBlockEntity.MAX_LENGTH);
        this.input.setValue(this.banpai.getText());
        this.addRenderableWidget(this.input);
        this.addRenderableWidget(Button.builder(Component.translatable("gui.done"), b -> this.saveAndClose())
                .bounds(cx - 60, cy + 20, 120, 20).build());
    }

    private void saveAndClose() {
        Minecraft.getInstance().getConnection()
                .send(new ServerboundCustomPayloadPacket(new BanpaiTextPayload(this.banpai.getBlockPos(), this.input.getValue())));
        this.onClose();
    }

    @Override
    public void onClose() {
        super.onClose();
    }

}
