package com.cr400bf5033.chineseschool.registry;

import com.cr400bf5033.chineseschool.ChineseSchoolMod;
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
 * <p>方块物品用 {@code ITEMS.registerSimpleBlockItem(name, block)} 注册。</p>
 */
public final class ModItems {

    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(ChineseSchoolMod.MOD_ID);

    private ModItems() {
    }
}
