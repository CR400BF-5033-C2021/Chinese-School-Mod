package com.cr400bf5033.chineseschool.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ScheduledTickAccess;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.util.RandomSource;
import net.minecraft.world.phys.BlockHitResult;

/**
 * 电铃。
 *
 * <p>挂在墙上的音箱式电铃，右键开关学校的上下课铃声循环：
 * 上课铃 → 40 分钟 → 下课铃 → 10 分钟 → 上课铃 ……
 * 关闭时会立刻切断正在播放的铃声。</p>
 */
public class BellBlock extends HorizontalDirectionalBlock implements EntityBlock {

    public BellBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.NORTH));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING);
    }

    /** 只能贴在方块的侧面，铃面朝外。 */
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        Direction clicked = context.getClickedFace();
        if (!clicked.getAxis().isHorizontal()) {
            return null;
        }
        return this.defaultBlockState().setValue(FACING, clicked);
    }

    @Override
    protected boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        Direction back = state.getValue(FACING).getOpposite();
        BlockPos supportPos = pos.relative(back);
        return level.getBlockState(supportPos).isFaceSturdy(level, supportPos, state.getValue(FACING));
    }

    @Override
    protected BlockState updateShape(BlockState state, LevelReader level, ScheduledTickAccess ticks, BlockPos pos,
                                     Direction direction, BlockPos neighborPos, BlockState neighborState, RandomSource random) {
        return this.canSurvive(state, level, pos) ? state : net.minecraft.world.level.block.Blocks.AIR.defaultBlockState();
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new BellBlockEntity(pos, state);
    }

    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        if (level.isClientSide()) {
            return null;
        }
        return (lvl, pos, st, be) -> {
            if (be instanceof BellBlockEntity bell) {
                BellBlockEntity.serverTick(lvl, pos, bell);
            }
        };
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        if (level.isClientSide()) {
            return InteractionResult.SUCCESS;
        }
        if (level.getBlockEntity(pos) instanceof BellBlockEntity bell) {
            bell.toggle();
            return InteractionResult.CONSUME;
        }
        return InteractionResult.PASS;
    }
}
