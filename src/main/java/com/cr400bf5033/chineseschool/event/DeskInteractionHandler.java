package com.cr400bf5033.chineseschool.event;

import com.cr400bf5033.chineseschool.block.DeskBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.util.TriState;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;

/**
 * 让"潜行 + 手持书右键课桌"能真正触发方块交互。
 *
 * <p>原版在玩家潜行且手上有物品时会跳过方块交互（避免贴着箱子放方块时误开箱子），
 * 所以这里在 {@link PlayerInteractEvent.RightClickBlock} 阶段显式要求引擎仍然调用
 * 课桌的 {@code useItemOn}。只在"课桌还没书 + 手里拿的是书"时才这么做，
 * 不影响潜行对着课桌放方块等正常行为。</p>
 */
public final class DeskInteractionHandler {

    private DeskInteractionHandler() {
    }

    @SubscribeEvent
    public static void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        if (!event.getEntity().isShiftKeyDown()) {
            return;
        }
        Level level = event.getLevel();
        BlockPos pos = event.getPos();
        BlockState state = level.getBlockState(pos);
        if (!(state.getBlock() instanceof DeskBlock)) {
            return;
        }
        if (state.getValue(BlockStateProperties.HAS_BOOK) || !DeskBlock.isBook(event.getItemStack())) {
            return;
        }
        event.setUseBlock(TriState.TRUE);
    }
}
