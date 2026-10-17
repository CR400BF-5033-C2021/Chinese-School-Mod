package com.cr400bf5033.chineseschool.registry;

import com.cr400bf5033.chineseschool.ChineseSchoolMod;
import com.cr400bf5033.chineseschool.menu.DeskMenu;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.inventory.MenuType;
import net.neoforged.neoforge.common.extensions.IMenuTypeExtension;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

/**
 * 容器菜单类型的注册表。
 */
public final class ModMenus {

    public static final DeferredRegister<MenuType<?>> MENUS =
            DeferredRegister.create(Registries.MENU, ChineseSchoolMod.MOD_ID);

    /** 课桌容器菜单。 */
    public static final DeferredHolder<MenuType<?>, MenuType<DeskMenu>> DESK =
            MENUS.register("desk", () -> IMenuTypeExtension.create(DeskMenu::new));

    private ModMenus() {
    }
}
