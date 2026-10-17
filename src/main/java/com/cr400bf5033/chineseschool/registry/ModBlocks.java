package com.cr400bf5033.chineseschool.registry;

import com.cr400bf5033.chineseschool.ChineseSchoolMod;
import net.neoforged.neoforge.registries.DeferredRegister;

/**
 * 方块注册表。
 *
 * <p>新增方块时在此定义静态字段，例如：</p>
 * <pre>{@code
 * public static final DeferredBlock<Block> SCHOOL_BELL =
 *         BLOCKS.registerSimpleBlock("school_bell", p -> p.mapColor(MapColor.GOLD));
 * }</pre>
 *
 * <p>如果需要对应的物品形式，请在 {@link ModItems} 中用 {@code registerSimpleBlockItem} 注册。</p>
 */
public final class ModBlocks {

    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(ChineseSchoolMod.MOD_ID);

    private ModBlocks() {
    }
}
