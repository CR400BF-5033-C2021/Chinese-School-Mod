package com.cr400bf5033.chineseschool.client;

import com.cr400bf5033.chineseschool.block.BanpaiBlockEntity;
import com.cr400bf5033.chineseschool.client.screen.BanpaiEditScreen;
import net.minecraft.client.Minecraft;

/**
 * 客户端侧的班牌辅助方法（服务端不会加载本类）。
 */
public final class BanpaiClientHooks {

    private BanpaiClientHooks() {
    }

    /** 打开班牌编辑界面。 */
    public static void openEditor(BanpaiBlockEntity banpai) {
        Minecraft.getInstance().setScreenAndShow(new BanpaiEditScreen(banpai));
    }
}
