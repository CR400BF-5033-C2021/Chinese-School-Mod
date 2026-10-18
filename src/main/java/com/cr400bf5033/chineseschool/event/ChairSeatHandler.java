package com.cr400bf5033.chineseschool.event;

import com.cr400bf5033.chineseschool.block.ChairBlock;
import net.neoforged.neoforge.event.entity.EntityMountEvent;

/**
 * 玩家从椅子上站起来时，把对应的座位实体清理掉，避免世界里的隐藏实体越积越多。
 */
public final class ChairSeatHandler {

    private ChairSeatHandler() {
    }

    public static void onMount(EntityMountEvent event) {
        if (!event.isDismounting()) {
            return;
        }
        if (event.getLevel().isClientSide()) {
            return;
        }
        if (ChairBlock.isSeat(event.getEntityBeingMounted())) {
            event.getEntityBeingMounted().discard();
        }
    }
}
