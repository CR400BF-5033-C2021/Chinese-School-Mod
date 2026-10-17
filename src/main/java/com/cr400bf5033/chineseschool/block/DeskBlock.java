package com.cr400bf5033.chineseschool.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.phys.BlockHitResult;

/**
 * 课桌。
 *
 * <p>拥有东、南、西、北四个朝向（{@link #FACING}），放置时正面朝向玩家；
 * 右键可以打开 9 格容器的界面。</p>
 */
public class DeskBlock extends HorizontalDirectionalBlock implements EntityBlock {

    /** 朝向属性，只允许水平四个方向。 */
    public static final DirectionProperty FACING = HorizontalDirectionalBlock.FACING;

    public DeskBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.NORTH));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING);
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new DeskBlockEntity(pos, state);
    }

    @Override
    public InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        if (level.isClientSide()) {
            return InteractionResult.SUCCESS;
        }
        if (level.getBlockEntity(pos) instanceof DeskBlockEntity desk) {
            player.openMenu(desk, pos);
        }
        return InteractionResult.CONSUME;
    }

    @Override
    public BlockState playerWillDestroy(Level level, BlockPos pos, BlockState state, Player player) {
        // 方块被破坏时把容器里的物品掉出来，避免物品丢失
        if (!level.isClientSide() && level.getBlockEntity(pos) instanceof DeskBlockEntity desk) {
            desk.dropContents();
        }
        return super.playerWillDestroy(level, pos, state, player);
    }
}
