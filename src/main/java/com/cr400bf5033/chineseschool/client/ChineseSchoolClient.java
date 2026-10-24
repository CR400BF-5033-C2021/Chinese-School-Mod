package com.cr400bf5033.chineseschool.client;

import com.cr400bf5033.chineseschool.ChineseSchoolMod;
import com.cr400bf5033.chineseschool.client.renderer.ChairSeatRenderer;
import com.cr400bf5033.chineseschool.client.screen.DeskScreen;
import com.cr400bf5033.chineseschool.registry.ModEntities;
import com.cr400bf5033.chineseschool.registry.ModMenus;
import net.minecraft.client.Minecraft;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;

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

    /** 注册容器界面（每个 MenuType 对应一个 Screen）。 */
    @SubscribeEvent
    public static void registerScreens(RegisterMenuScreensEvent event) {
        event.register(ModMenus.DESK.get(), DeskScreen::new);
    }

    /** 注册实体渲染器（座位实体本身不可见，用空渲染器）。 */
    @SubscribeEvent
    public static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(ModEntities.CHAIR_SEAT.get(), ChairSeatRenderer::new);
    }

    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent event) {
        ChineseSchoolMod.LOGGER.info("{} 客户端初始化完成（玩家：{}）",
                ChineseSchoolMod.MOD_NAME,
                Minecraft.getInstance().getUser().getName());
    }
}
