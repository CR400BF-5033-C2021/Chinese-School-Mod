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
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.util.RandomSource;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * 班牌：挂在墙上的班级牌。
 *
 * <p>只能贴在方块侧面，右键打开编辑界面输入班级名称（显示在牌面中央）。</p>
 */
public class BanpaiBlock extends HorizontalDirectionalBlock implements EntityBlock {

    /** 牌子的碰撞箱：宽 10px、高 14px、厚 2px（与模型一致）。 */
    private static final VoxelShape SHAPE = Shapes.box(3.0 / 16, 1.0 / 16, 7.0 / 16,
            13.0 / 16, 15.0 / 16, 9.0 / 16);

    public BanpaiBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.NORTH));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING);
    }

    /** 只能贴在方块侧面：FACING 指向背离墙的方向。 */
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        Direction clicked = context.getClickedFace();
        if (!clicked.getAxis().isHorizontal()) {
            return null;
        }
        BlockPos pos = context.getClickedPos();
        if (!context.getLevel().getBlockState(pos.relative(clicked.getOpposite())).isFaceSturdy(
                context.getLevel(), pos.relative(clicked.getOpposite()), clicked)) {
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
        return this.canSurvive(state, level, pos) ? state : Blocks.AIR.defaultBlockState();
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new BanpaiBlockEntity(pos, state);
    }

    /** 右键打开编辑界面（实际的界面在客户端事件里处理）。 */
    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        if (level.isClientSide()) {
            com.cr400bf5033.chineseschool.client.BanpaiClientHooks.openEditor((BanpaiBlockEntity) level.getBlockEntity(pos));
            return InteractionResult.SUCCESS;
        }
        return InteractionResult.CONSUME;
    }
}
