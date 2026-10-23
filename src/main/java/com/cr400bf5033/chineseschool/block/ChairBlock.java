package com.cr400bf5033.chineseschool.block;

import com.cr400bf5033.chineseschool.entity.ChairSeatEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.redstone.Orientation;
import net.minecraft.world.phys.BlockHitResult;

/**
 * 椅子。
 *
 * <p>四个朝向，放置时朝向玩家看的方向；空手右键可以坐下，坐下后视角朝向椅背的反方向。
 * 当正下方是课桌时自动切换为倒置状态（相当于把椅子倒扣在桌子上），此时不能坐。</p>
 */
public class ChairBlock extends HorizontalDirectionalBlock {

    /** 是否处于"倒扣在课桌上"的状态。 */
    public static final BooleanProperty INVERTED = BooleanProperty.create("inverted");

    /** 椅面高度（方块坐标，椅面在模型里高 10/16）。 */
    public static final double SEAT_HEIGHT = 10.0 / 16.0;

    public ChairBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any()
                .setValue(FACING, Direction.NORTH)
                .setValue(INVERTED, Boolean.FALSE));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, INVERTED);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        BlockPos pos = context.getClickedPos();
        boolean inverted = context.getLevel().getBlockState(pos.below()).getBlock() instanceof DeskBlock;
        return this.defaultBlockState()
                .setValue(FACING, context.getHorizontalDirection())
                .setValue(INVERTED, inverted);
    }

    /** 下方方块变化时重新判断是否倒置。 */
    @Override
    protected void neighborChanged(BlockState state, Level level, BlockPos pos, Block block, Orientation orientation, boolean movedByPiston) {
        super.neighborChanged(state, level, pos, block, orientation, movedByPiston);
        if (level.isClientSide()) {
            return;
        }
        boolean inverted = level.getBlockState(pos.below()).getBlock() instanceof DeskBlock;
        if (state.getValue(INVERTED) != inverted) {
            level.setBlock(pos, state.setValue(INVERTED, inverted), Block.UPDATE_ALL);
        }
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        if (state.getValue(INVERTED)) {
            return InteractionResult.PASS;
        }

        float yaw = state.getValue(FACING).toYRot();
        if (level.isClientSide()) {
            // 客户端立刻转向，避免坐下瞬间视角还停在原方向
            player.setYRot(yaw);
            player.setYHeadRot(yaw);
            return InteractionResult.SUCCESS;
        }
        if (player.isPassenger()) {
            return InteractionResult.PASS;
        }

        return ChairSeatEntity.sit(player, pos, SEAT_HEIGHT, state.getValue(FACING))
                ? InteractionResult.CONSUME
                : InteractionResult.SUCCESS;
    }
}
