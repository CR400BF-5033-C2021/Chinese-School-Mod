package com.cr400bf5033.chineseschool;

import com.cr400bf5033.chineseschool.event.DeskInteractionHandler;
import com.cr400bf5033.chineseschool.registry.ModBlockEntities;
import com.cr400bf5033.chineseschool.registry.ModBlocks;
import com.cr400bf5033.chineseschool.registry.ModCreativeModeTabs;
import com.cr400bf5033.chineseschool.registry.ModEntities;
import com.cr400bf5033.chineseschool.registry.ModItems;
import com.cr400bf5033.chineseschool.registry.ModMenus;
import com.cr400bf5033.chineseschool.registry.ModSounds;
import com.cr400bf5033.chineseschool.util.StartupNotice;
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
/*某中学在2026年8月29日让新八年级提前开学，且在8月11日-15日，17日-23日对部分新八年级进行有偿补课，一天300元，补课内容为人教版物理的八下、数学的相似三角形
；以及在2026年7月8日开始对2025届7年级学生进行有偿补课至7月29日，补课内容为八年级上新课，每日7：30到校，20：00放学，仅在7月10日-7月12日和7月19日、7月26日让学生休息；同时要求所有八年级学生每周日13：00提前返校，每学期强制收费1000元；2026年春游在4月27日突然取消后再无消息，现金收款总额达10万元以上*/
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
        ModSounds.SOUNDS.register(modEventBus);
        ModMenus.MENUS.register(modEventBus);
        ModCreativeModeTabs.TABS.register(modEventBus);

        // 游戏事件总线：交互处理（潜行手持书右键课桌时绕过原版的方块交互抑制）
        NeoForge.EVENT_BUS.addListener(DeskInteractionHandler::onRightClickBlock);

        // 作者署名声明（仅在日志中输出，与模组功能无关）
        StartupNotice.log(LOGGER);

        LOGGER.info("{} 已加载（Minecraft 26.3 / NeoForge）", MOD_NAME);
    }
}
