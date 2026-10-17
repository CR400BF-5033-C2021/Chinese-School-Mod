package com.cr400bf5033.chineseschool.registry;

import com.cr400bf5033.chineseschool.ChineseSchoolMod;
import net.minecraft.world.item.BlockItem;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

/**
 * 物品注册表。
 *
 * <p>新增物品时在此定义静态字段，例如：</p>
 * <pre>{@code
 * public static final DeferredItem<Item> NOTEBOOK =
 *         ITEMS.registerSimpleItem("notebook", p -> p.stacksTo(16));
 * }</pre>
 *
 * <p>方块对应的物品形式用 {@code ITEMS.registerItem(name, p -> new BlockItem(block, p))} 注册。</p>
 */
public final class ModItems {

    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(ChineseSchoolMod.MOD_ID);

    /** 课桌的物品形式。 */
    public static final DeferredItem<BlockItem> DESK = ITEMS.registerItem("desk",
            properties -> new BlockItem(ModBlocks.DESK.get(), properties));

    private ModItems() {
    }
}
