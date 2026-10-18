package com.cr400bf5033.chineseschool.registry;

import com.cr400bf5033.chineseschool.ChineseSchoolMod;
import com.cr400bf5033.chineseschool.block.ChairBlock;
import com.cr400bf5033.chineseschool.block.DeskBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.neoforged.neoforge.registries.DeferredBlock;
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
 * <p>需要物品形式时，在 {@link ModItems} 中注册对应的 {@code BlockItem}。</p>
 */
public final class ModBlocks {

    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(ChineseSchoolMod.MOD_ID);

    /** 课桌：可朝向、可存放 9 格物品。 */
    public static final DeferredBlock<DeskBlock> DESK = BLOCKS.registerBlock("desk", DeskBlock::new,
            properties -> properties
                    .mapColor(MapColor.WOOD)
                    .strength(2.0F, 3.0F)
                    .sound(SoundType.WOOD)
                    // 模型不是完整方块，必须关闭遮挡剔除，否则会剔掉相邻方块的面（表现为桌下空洞）
                    .noOcclusion());

    /** 椅子：可朝向、可坐；正下方是课桌时会自动倒扣在桌上。 */
    public static final DeferredBlock<ChairBlock> CHAIR = BLOCKS.registerBlock("chair", ChairBlock::new,
            properties -> properties
                    .mapColor(MapColor.WOOD)
                    .strength(2.0F, 3.0F)
                    .sound(SoundType.WOOD)
                    .noOcclusion());

    private ModBlocks() {
    }
}
