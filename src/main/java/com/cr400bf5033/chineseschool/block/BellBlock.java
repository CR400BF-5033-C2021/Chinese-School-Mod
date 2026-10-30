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
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.util.RandomSource;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * 电铃。
 *
 * <p>挂在墙上的音箱式电铃。按现实系统时间触发：系统时间的「时:分」命中
 * {@code config/shangketime.json} 或 {@code config/xiaketime.json} 中的时刻时播放对应铃声。</p>
 *
 * <p>用红石控制：未充能时正常按时间响铃；充能时进入静音状态
 * （编号 {@link #RINGING} 归零，并切断正在播放的声音）。</p>
 */
public class BellBlock extends HorizontalDirectionalBlock implements EntityBlock {

    /** 当前在响哪种铃：0 = 不响，1 = 上课铃，2 = 下课铃。 */
    public static final IntegerProperty RINGING = IntegerProperty.create("ringing", 0, 2);

    /** 是否被红石充能。 */
    public static final BooleanProperty POWERED = BlockStateProperties.POWERED;

    public BellBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any()
                .setValue(FACING, Direction.NORTH)
                .setValue(RINGING, 0)
                .setValue(POWERED, Boolean.FALSE));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, RINGING, POWERED);
    }

    /** 只能贴在方块的侧面，铃面朝外。 */
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        Direction clicked = context.getClickedFace();
        if (!clicked.getAxis().isHorizontal()) {
            return null;
        }
        return this.defaultBlockState()
                .setValue(FACING, clicked)
                .setValue(POWERED, context.getLevel().hasNeighborSignal(context.getClickedPos()));
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

    /** 与 bbmodel 一致的实际体积（1/16 格单位，朝向为北时）。 */
    private static final VoxelShape NORTH_SHAPE = Shapes.or(
            Block.box(2.0, 3.0, 10.0, 14.0, 15.0, 15.0),
            Block.box(6.0, 15.0, 12.0, 10.0, 16.0, 14.0));

    /** 碰撞箱/选中箱按模型真实体积计算，并随朝向旋转。 */
    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return switch (state.getValue(FACING)) {
            case SOUTH -> rotateShape(180);
            case EAST -> rotateShape(90);
            case WEST -> rotateShape(270);
            default -> NORTH_SHAPE;
        };
    }

    private static VoxelShape rotateShape(int degrees) {
        VoxelShape shape = Shapes.empty();
        for (AABB box : NORTH_SHAPE.toAabbs()) {
            double x1 = box.minX * 16.0;
            double x2 = box.maxX * 16.0;
            double y1 = box.minY * 16.0;
            double y2 = box.maxY * 16.0;
            double z1 = box.minZ * 16.0;
            double z2 = box.maxZ * 16.0;
            VoxelShape rotated = switch (degrees) {
                case 180 -> Block.box(16 - x2, y1, 16 - z2, 16 - x1, y2, 16 - z1);
                case 90 -> Block.box(16 - z2, y1, x1, 16 - z1, y2, x2);
                case 270 -> Block.box(z1, y1, 16 - x2, z2, y2, 16 - x1);
                default -> Block.box(x1, y1, z1, x2, y2, z2);
            };
            shape = Shapes.or(shape, rotated);
        }
        return shape;
    }

    /** 红石信号变化：充能静音，取消充能后重置循环。 */
    @Override
    protected void neighborChanged(BlockState state, Level level, BlockPos pos, Block block,
                                   net.minecraft.world.level.redstone.Orientation orientation, boolean movedByPiston) {
        super.neighborChanged(state, level, pos, block, orientation, movedByPiston);
        if (level.isClientSide()) {
            return;
        }
        boolean powered = level.hasNeighborSignal(pos);
        if (powered != state.getValue(POWERED)) {
            level.setBlock(pos, state.setValue(POWERED, powered).setValue(RINGING, 0), Block.UPDATE_ALL);
            if (level.getBlockEntity(pos) instanceof BellBlockEntity bell) {
                bell.onPowerChanged(powered);
            }
        }
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

    /** 电铃不再用右键开关，右键保持原版行为。 */
    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        return InteractionResult.PASS;
    }
}
