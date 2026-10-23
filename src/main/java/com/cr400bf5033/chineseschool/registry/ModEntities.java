package com.cr400bf5033.chineseschool.registry;

import com.cr400bf5033.chineseschool.ChineseSchoolMod;
import com.cr400bf5033.chineseschool.entity.ChairSeatEntity;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

/**
 * 实体类型注册表。
 */
public final class ModEntities {

    public static final DeferredRegister<EntityType<?>> ENTITY_TYPES =
            DeferredRegister.create(Registries.ENTITY_TYPE, ChineseSchoolMod.MOD_ID);

    /** 椅子座位实体：零尺寸，不可见，只在有人坐时存在。 */
    public static final DeferredHolder<EntityType<?>, EntityType<ChairSeatEntity>> CHAIR_SEAT =
            ENTITY_TYPES.register("chair_seat", () -> EntityType.Builder
                    .of(ChairSeatEntity::new, MobCategory.MISC)
                    .sized(0.0F, 0.0F)
                    .clientTrackingRange(10)
                    .build(ResourceKey.create(Registries.ENTITY_TYPE,
                            Identifier.fromNamespaceAndPath(ChineseSchoolMod.MOD_ID, "chair_seat"))));

    private ModEntities() {
    }
}
