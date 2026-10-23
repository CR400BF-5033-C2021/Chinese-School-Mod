package com.cr400bf5033.chineseschool.entity;

import com.cr400bf5033.chineseschool.registry.ModEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

/**
 * 椅子座位实体。
 *
 * <p>参考 MrCrayfish's Furniture Mod 的做法：用一个尺寸为 0 的自定义实体当座位，
 * 玩家骑在它上面。因为它不是生物，没有额外的骑乘高度偏移，实体自身的位置就是
 * 玩家坐下后的位置，所以不会出现"坐在空中"的问题。</p>
 *
 * <p>座位只在有人坐的时候存在：每 tick 检查一次，没人骑、或者脚下方块没了就自行消失。</p>
 */
public class ChairSeatEntity extends Entity {

    public ChairSeatEntity(EntityType<?> type, Level level) {
        super(type, level);
    }

    private ChairSeatEntity(Level level, BlockPos pos, double seatHeight, float yaw) {
        this(ModEntities.CHAIR_SEAT.get(), level);
        this.setPos(Vec3.atBottomCenterOf(pos).add(0, seatHeight, 0));
        this.setRot(yaw, 0);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
    }

    @Override
    protected void readAdditionalSaveData(ValueInput input) {
    }

    @Override
    protected void addAdditionalSaveData(ValueOutput output) {
    }

    @Override
    public void tick() {
        super.tick();
        if (this.level().isClientSide()) {
            return;
        }
        BlockPos pos = this.blockPosition();
        if (this.getPassengers().isEmpty() || this.level().isEmptyBlock(pos)) {
            this.discard();
        }
    }

    @Override
    public boolean hurtServer(ServerLevel level, DamageSource source, float damage) {
        return false;
    }

    /** 坐上去时让玩家朝向椅子的正面。 */
    @Override
    protected void addPassenger(Entity passenger) {
        super.addPassenger(passenger);
        passenger.setYRot(this.getYRot());
        passenger.setYHeadRot(this.getYRot());
    }

    /**
     * 在指定方块上放一个座位并让玩家坐下。
     *
     * @param player     要坐下的玩家
     * @param pos        椅子的方块坐标
     * @param seatHeight 椅面相对方块底面的高度
     * @param facing     椅子朝向（决定玩家坐下后的朝向），为空则沿用玩家当前朝向
     * @return 是否成功坐下
     */
    public static boolean sit(Player player, BlockPos pos, double seatHeight, Direction facing) {
        Level level = player.level();
        if (level.isClientSide() || !availableAt(level, pos)) {
            return false;
        }
        float yaw = facing != null ? facing.toYRot() : player.getYRot();        ChairSeatEntity seat = new ChairSeatEntity(level, pos, seatHeight, yaw);
        level.addFreshEntity(seat);
        return player.startRiding(seat);
    }

    /** 该方块位置是否还能放座位（同一位置只允许一个座位）。 */
    public static boolean availableAt(Level level, BlockPos pos) {
        return level.getEntitiesOfClass(ChairSeatEntity.class, new AABB(pos)).isEmpty();
    }
}
