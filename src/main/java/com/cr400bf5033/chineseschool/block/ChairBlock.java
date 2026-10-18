package com.cr400bf5033.chineseschool.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.redstone.Orientation;
import net.minecraft.world.phys.AABB;
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

    /** 座位实体（隐藏的盔甲架）的名字标记，用于识别与清理。 */
    private static final String SEAT_MARKER = "chinese_school_chair_seat";

    /** 座位高度（方块坐标，0 为方块底面，椅面高 10/16）。 */
    private static final double SEAT_HEIGHT = 0.6;

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
            // 客户端直接转向，避免坐下后视线还停留在原方向
            player.setYRot(yaw);
            player.setYHeadRot(yaw);
            return InteractionResult.SUCCESS;
        }
        if (player.isPassenger()) {
            return InteractionResult.PASS;
        }

        ArmorStand seat = new ArmorStand(level, 0, 0, 0);
        seat.setInvisible(true);
        seat.setNoGravity(true);
        seat.setSilent(true);
        seat.setCustomName(Component.literal(SEAT_MARKER));
        seat.setCustomNameVisible(false);

        // 引擎按「载具坐点 - 乘客挂点」放置乘客，这里先用零坐标算出这个高度差，
        // 再反推座位实体应该放在哪，保证坐下时正好落在椅面上。
        double lift = seat.getPassengerRidingPosition(player).y - player.getVehicleAttachmentPoint(seat).y;
        seat.setPos(pos.getX() + 0.5, pos.getY() + SEAT_HEIGHT - lift, pos.getZ() + 0.5);

        level.addFreshEntity(seat);

        player.startRiding(seat);
        player.setYRot(yaw);
        player.setYHeadRot(yaw);
        return InteractionResult.CONSUME;
    }

    /** 判断某个实体是不是本模组生成的座位。 */
    public static boolean isSeat(Entity entity) {
        return entity instanceof ArmorStand
                && entity.getCustomName() != null
                && SEAT_MARKER.equals(entity.getCustomName().getString());
    }

    @Override
    public BlockState playerWillDestroy(Level level, BlockPos pos, BlockState state, Player player) {
        // 方块被破坏时把座位实体一并清理，避免残留
        if (!level.isClientSide()) {
            for (ArmorStand seat : level.getEntitiesOfClass(ArmorStand.class, new AABB(pos).inflate(1.5), ChairBlock::isSeat)) {
                seat.ejectPassengers();
                seat.discard();
            }
        }
        return super.playerWillDestroy(level, pos, state, player);
    }
}
