package com.cr400bf5033.chineseschool.registry;

import com.cr400bf5033.chineseschool.ChineseSchoolMod;
import com.cr400bf5033.chineseschool.block.BanpaiBlockEntity;
import com.cr400bf5033.chineseschool.block.BellBlockEntity;
import com.cr400bf5033.chineseschool.block.DeskBlockEntity;
import com.cr400bf5033.chineseschool.block.JiangtaiBlockEntity;
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

    /** 班牌的方块实体（保存并同步班级名称）。 */
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<BanpaiBlockEntity>> BANPAI =
            BLOCK_ENTITIES.register("banpai", () -> new BlockEntityType<>(BanpaiBlockEntity::new, ModBlocks.BANPAI.get()));

    /** 讲台的方块实体（27 格容器，只挂在主块上）。 */
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<JiangtaiBlockEntity>> JIANGTAI =
            BLOCK_ENTITIES.register("jiangtai", () -> new BlockEntityType<>(JiangtaiBlockEntity::new, ModBlocks.JIANGTAI.get()));

    private ModBlockEntities() {
    }
}
