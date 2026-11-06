package com.cr400bf5033.chineseschool.block;

import com.mojang.serialization.Codec;
import java.util.ArrayList;
import java.util.List;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
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
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.util.RandomSource;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * 讲台（多方块结构）。
 *
 * <p>由两块方块组成：玩家放置时点击的那一格是主块（左半，持有 27 格容器），
 * 另一块自动生成在玩家的右手边。朝向由玩家放置时的朝向决定。</p>
 */
public class JiangtaiBlock extends HorizontalDirectionalBlock implements EntityBlock {

    /** 主块 / 副块。 */
    public enum Part implements StringRepresentable {
        MAIN("main"),
        EXTRA("extra");

        public static final Codec<Part> CODEC = StringRepresentable.fromEnum(Part::values);
        private final String name;

        Part(String name) {
            this.name = name;
        }

        @Override
        public String getSerializedName() {
            return this.name;
        }
    }

    public static final EnumProperty<Part> PART = EnumProperty.create("part", Part.class);

    /** 单个半块的碰撞箱：整格宽、1.5 格高。 */
    private static final VoxelShape SHAPE = Shapes.box(0.0, 0.0, 0.0, 1.0, 1.5, 1.0);

    public JiangtaiBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any()
                .setValue(FACING, Direction.NORTH)
                .setValue(PART, Part.MAIN));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, PART);
    }

    /** 正面朝向玩家；副块放在玩家的右手边（即 FACING 的逆时针方向）。 */
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        Direction facing = context.getHorizontalDirection().getOpposite();
        BlockPos pos = context.getClickedPos();
        BlockPos extraPos = pos.relative(facing.getCounterClockWise());
        Level level = context.getLevel();
        if (!level.getBlockState(extraPos).canBeReplaced()) {
            return null;
        }
        return this.defaultBlockState().setValue(FACING, facing).setValue(PART, Part.MAIN);
    }

    @Override
    protected void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean movedByPiston) {
        super.onPlace(state, level, pos, oldState, movedByPiston);
        if (level.isClientSide() || state.getValue(PART) != Part.MAIN || oldState.getBlock() == this) {
            return;
        }
        BlockPos extraPos = partnerPos(pos, state);
        if (level.getBlockState(extraPos).canBeReplaced()) {
            level.setBlock(extraPos, state.setValue(PART, Part.EXTRA), Block.UPDATE_ALL);
        }
    }

    /** 另一半的位置。 */
    public static BlockPos partnerPos(BlockPos pos, BlockState state) {
        Direction facing = state.getValue(FACING);
        return state.getValue(PART) == Part.MAIN
                ? pos.relative(facing.getCounterClockWise())
                : pos.relative(facing.getClockWise());
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        // 只有主块持有容器
        return state.getValue(PART) == Part.MAIN ? new JiangtaiBlockEntity(pos, state) : null;
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    /** 另一半没了就跟着消失，避免留下半个讲台。 */
    @Override
    protected BlockState updateShape(BlockState state, LevelReader level, ScheduledTickAccess ticks, BlockPos pos,
                                     Direction direction, BlockPos neighborPos, BlockState neighborState, RandomSource random) {
        if (direction == state.getValue(FACING).getClockWise() || direction == state.getValue(FACING).getCounterClockWise()) {
            BlockPos partner = partnerPos(pos, state);
            BlockState partnerState = level.getBlockState(partner);
            if (!(partnerState.getBlock() instanceof JiangtaiBlock)
                    || partnerState.getValue(PART) == state.getValue(PART)) {
                return Blocks.AIR.defaultBlockState();
            }
        }
        return state;
    }

    /** 右键打开容器（点哪一半都打开主块的那份容器）。 */
    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        if (level.isClientSide()) {
            return InteractionResult.SUCCESS;
        }
        BlockPos mainPos = state.getValue(PART) == Part.MAIN ? pos : partnerPos(pos, state);
        if (level.getBlockEntity(mainPos) instanceof JiangtaiBlockEntity jiangtai) {
            player.openMenu(jiangtai, mainPos);
            return InteractionResult.CONSUME;
        }
        return InteractionResult.PASS;
    }

    /** 玩家破坏任意一半时：掉落容器内容，并静默移除另一半（只掉一次方块）。 */
    @Override
    public BlockState playerWillDestroy(Level level, BlockPos pos, BlockState state, Player player) {
        if (!level.isClientSide()) {
            BlockPos partnerPos = partnerPos(pos, state);
            BlockPos mainPos = state.getValue(PART) == Part.MAIN ? pos : partnerPos;
            if (level.getBlockEntity(mainPos) instanceof JiangtaiBlockEntity jiangtai) {
                jiangtai.dropContents();
            }
            if (level.getBlockState(partnerPos).getBlock() instanceof JiangtaiBlock) {
                level.removeBlock(partnerPos, false);
            }
        }
        return super.playerWillDestroy(level, pos, state, player);
    }

    /** 收到红石等外部信号时也保持结构完整（被活塞等拆走则另一半自行消失）。 */
    @Override
    protected void neighborChanged(BlockState state, Level level, BlockPos pos, Block block,
                                   net.minecraft.world.level.redstone.Orientation orientation, boolean movedByPiston) {
        super.neighborChanged(state, level, pos, block, orientation, movedByPiston);
        if (level.isClientSide()) {
            return;
        }
        BlockPos partnerPos = partnerPos(pos, state);
        if (!(level.getBlockState(partnerPos).getBlock() instanceof JiangtaiBlock)) {
            level.removeBlock(pos, false);
        }
    }
}
