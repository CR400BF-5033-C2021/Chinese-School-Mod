package com.cr400bf5033.chineseschool;

import com.cr400bf5033.chineseschool.registry.ModBlocks;
import com.cr400bf5033.chineseschool.registry.ModCreativeModeTabs;
import com.cr400bf5033.chineseschool.registry.ModItems;
import com.mojang.logging.LogUtils;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import org.slf4j.Logger;

/**
 * Chinese School Mod 的主入口类。
 *
 * <p>这里只负责把各个注册表（方块、物品、创造模式物品栏等）挂到模组事件总线上；
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
        ModCreativeModeTabs.TABS.register(modEventBus);

        LOGGER.info("{} 已加载（Minecraft 26.3 / NeoForge）", MOD_NAME);
    }
}
