package com.cr400bf5033.chineseschool.registry;

import com.cr400bf5033.chineseschool.ChineseSchoolMod;
import com.cr400bf5033.chineseschool.block.BellBlockEntity;
import com.cr400bf5033.chineseschool.block.DeskBlockEntity;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

/**
 * 方块实体类型的注册表。
 */
public final class ModBlockEntities {

    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES =
            DeferredRegister.create(BuiltInRegistries.BLOCK_ENTITY_TYPE, ChineseSchoolMod.MOD_ID);

    /** 课桌的方块实体。 */
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<DeskBlockEntity>> DESK =
            BLOCK_ENTITIES.register("desk", () -> new BlockEntityType<>(DeskBlockEntity::new, ModBlocks.DESK.get()));

    /** 电铃的方块实体（负责上下课铃声循环）。 */
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<BellBlockEntity>> BELL =
            BLOCK_ENTITIES.register("bell", () -> new BlockEntityType<>(BellBlockEntity::new, ModBlocks.BELL.get()));

    private ModBlockEntities() {
    }
}
