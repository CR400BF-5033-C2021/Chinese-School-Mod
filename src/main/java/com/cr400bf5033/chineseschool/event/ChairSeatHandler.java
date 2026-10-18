package com.cr400bf5033.chineseschool.event;

import com.cr400bf5033.chineseschool.block.ChairBlock;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.entity.Entity;
import net.neoforged.neoforge.event.entity.EntityMountEvent;

/**
 * 玩家从椅子上站起来时，把对应的座位实体清理掉，避免世界里的隐藏实体越积越多。
 *
 * <p>注意不能在事件里直接 {@code discard()}：实体被移除时会先让乘客下车，
 * 而下车又会再次触发本事件，形成无限递归（曾导致 StackOverflowError 崩服）。
 * 所以放到服务器任务队列里延后一 tick 再删。</p>
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

        Entity mounted = event.getEntityBeingMounted();
        if (mounted.isRemoved() || !ChairBlock.isSeat(mounted)) {
            return;
        }

        MinecraftServer server = event.getLevel().getServer();
        if (server != null) {
            server.execute(mounted::discard);
        }
    }
}
