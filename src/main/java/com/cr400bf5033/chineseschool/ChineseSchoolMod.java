package com.cr400bf5033.chineseschool;

import com.cr400bf5033.chineseschool.event.DeskInteractionHandler;
import com.cr400bf5033.chineseschool.registry.ModBlockEntities;
import com.cr400bf5033.chineseschool.registry.ModBlocks;
import com.cr400bf5033.chineseschool.registry.ModCreativeModeTabs;
import com.cr400bf5033.chineseschool.registry.ModEntities;
import com.cr400bf5033.chineseschool.registry.ModItems;
import com.cr400bf5033.chineseschool.registry.ModMenus;
import com.mojang.logging.LogUtils;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;
import org.slf4j.Logger;

/**
 * Chinese School Mod 的主入口类。
 *
 * <p>这里只负责把各个注册表（方块、物品、方块实体、容器菜单、创造模式物品栏）挂到模组事件总线上；
 * 具体内容与其他事件监听器应放到各自的类里，保持本类精简。</p>
 */
@Mod(ChineseSchoolMod.MOD_ID)
public final class ChineseSchoolMod {

    /** 模组 ID，必须与 gradle.properties 中的 mod_id 以及 neoforge.mods.toml 保持一致。 */
    public static final String MOD_ID = "chinese_school";

    /** 模组显示名称。 */
    public static final String MOD_NAME = "Chinese School Mod";

    /** 模组日志输出。 */
    public static final Logger LOGGER = LogUtils.getLogger();

    public ChineseSchoolMod(IEventBus modEventBus, ModContainer modContainer) {
        // 注册表挂载
        ModBlocks.BLOCKS.register(modEventBus);
        ModItems.ITEMS.register(modEventBus);
        ModBlockEntities.BLOCK_ENTITIES.register(modEventBus);
        ModEntities.ENTITY_TYPES.register(modEventBus);
        ModMenus.MENUS.register(modEventBus);
        ModCreativeModeTabs.TABS.register(modEventBus);

        // 游戏事件总线：交互处理（潜行手持书右键课桌时绕过原版的方块交互抑制）
        NeoForge.EVENT_BUS.addListener(DeskInteractionHandler::onRightClickBlock);

        LOGGER.info("{} 已加载（Minecraft 26.3 / NeoForge）", MOD_NAME);
    }
}
