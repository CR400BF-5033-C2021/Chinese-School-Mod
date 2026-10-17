package com.cr400bf5033.chineseschool.client;

import com.cr400bf5033.chineseschool.ChineseSchoolMod;
import net.minecraft.client.Minecraft;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;

/**
 * 客户端入口。
 *
 * <p>只在客户端加载，服务端不会读取本类中的客户端专用代码。
 * 渲染、按键、界面等客户端内容应放在本类或 {@code client} 包下。</p>
 */
@Mod(value = ChineseSchoolMod.MOD_ID, dist = Dist.CLIENT)
@EventBusSubscriber(modid = ChineseSchoolMod.MOD_ID, value = Dist.CLIENT)
public final class ChineseSchoolClient {

    public ChineseSchoolClient(ModContainer modContainer) {
        // 客户端的注册与初始化入口，例如注册渲染类型、菜单界面、按键绑定等
    }

    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent event) {
        ChineseSchoolMod.LOGGER.info("{} 客户端初始化完成（玩家：{}）",
                ChineseSchoolMod.MOD_NAME,
                Minecraft.getInstance().getUser().getName());
    }
}
