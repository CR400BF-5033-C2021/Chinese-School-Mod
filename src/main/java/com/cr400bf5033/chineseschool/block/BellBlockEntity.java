package com.cr400bf5033.chineseschool.block;

import com.cr400bf5033.chineseschool.registry.ModBlockEntities;
import com.cr400bf5033.chineseschool.registry.ModSounds;
import com.cr400bf5033.chineseschool.util.BellSchedule;
import java.time.LocalTime;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.protocol.game.ClientboundSoundPacket;
import net.minecraft.network.protocol.game.ClientboundStopSoundPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.Vec3;

/**
 * 电铃的方块实体。
 *
 * <p>按现实系统时间触发：当系统时间的「时:分」命中 {@code config/shangketime.json}
 * 或 {@code config/xiaketime.json} 中的某个时刻时播放对应铃声。声音以电铃为声源，
 * 音量随距离衰减，可听范围由声音事件固定为 16 格。</p>
 */
public class BellBlockEntity extends BlockEntity {

    /** 响铃状态（含音符粒子）持续的 tick 数。 */
    public static final int RING_TICKS = 100;

    /** 一次响铃后多少 tick 内允许"走出范围又回来"的玩家补听到声音（30 秒）。 */
    public static final int SOUND_ACTIVE_TICKS = 600;

    /** 听得见铃声的范围（格）。 */
    public static final double HEAR_RANGE = 16.0;

    private boolean powered;
    private int ringTicks;
    /** 上一次触发的时间键（HH:mm），用于同一分钟只响一次。 */
    private String lastTriggered = "";

    /** 本次响铃还剩多少 tick 可以给新进入范围的玩家补发声音。 */
    private int soundActiveTicks;
    /** 本次响铃是否上课铃。 */
    private boolean playingClass = true;
    /** 本次响铃已经收到过声音的玩家（防止重复播放）。 */
    private final java.util.Set<java.util.UUID> servedPlayers = new java.util.HashSet<>();

    public BellBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.BELL.get(), pos, state);
    }

    /** 红石信号变化：充能时立刻静音。 */
    public void onPowerChanged(boolean powered) {
        this.powered = powered;
        this.ringTicks = 0;
        this.soundActiveTicks = 0;
        this.servedPlayers.clear();
        if (powered) {
            this.stopSounds();
        }
        this.setChanged();
    }

    /** 每个服务端 tick 调用一次。 */
    public static void serverTick(Level level, BlockPos pos, BellBlockEntity bell) {
        if (!(level instanceof ServerLevel serverLevel)) {
            return;
        }

        // 响铃中：冒音符粒子，结束后把状态复位
        if (bell.ringTicks > 0) {
            bell.ringTicks--;
            if (bell.ringTicks % 4 == 0) {
                serverLevel.sendParticles(ParticleTypes.NOTE,
                        pos.getX() + 0.5 + (serverLevel.getRandom().nextDouble() - 0.5) * 0.8,
                        pos.getY() + 0.55 + (serverLevel.getRandom().nextDouble() - 0.5) * 0.5,
                        pos.getZ() + 0.5 + (serverLevel.getRandom().nextDouble() - 0.5) * 0.8,
                        1, 0.0, 0.15, 0.0, 0.0);
            }
            if (bell.ringTicks == 0) {
                bell.setRinging(serverLevel, pos, 0);
            }
        }

        // 红石充能时完全不响
        if (bell.powered) {
            return;
        }

        // 响铃有效期内的补发：走出范围又走回来的玩家能重新听到
        if (bell.soundActiveTicks > 0) {
            bell.soundActiveTicks--;
            if (bell.soundActiveTicks % 4 == 0) {
                resendToEnteringPlayers(serverLevel, pos, bell);
            }
            if (bell.soundActiveTicks == 0) {
                bell.servedPlayers.clear();
            }
        }

        // 每秒检查一次系统时间
        if (serverLevel.getGameTime() % 20L != 0L) {
            return;
        }

        LocalTime now = LocalTime.now();
        String key = String.format("%02d:%02d", now.getHour(), now.getMinute());
        if (key.equals(bell.lastTriggered)) {
            return;
        }

        boolean classTime = BellSchedule.isClassTime(now);
        boolean breakTime = BellSchedule.isBreakTime(now);
        if (!classTime && !breakTime) {
            return;
        }

        bell.lastTriggered = key;
        boolean isClass = classTime;
        bell.playingClass = isClass;
        // 以电铃为声源的定点播放：音量随距离衰减，可听范围由声音事件固定为 16 格
        playToRange(serverLevel, pos, bell);
        bell.setRinging(serverLevel, pos, isClass ? 1 : 2);
        bell.ringTicks = RING_TICKS;
        bell.setChanged();
    }

    /** 开始一次响铃：给当前 16 格内的玩家发声音，并开启补发窗口。 */
    private static void playToRange(ServerLevel level, BlockPos pos, BellBlockEntity bell) {
        bell.servedPlayers.clear();
        bell.soundActiveTicks = SOUND_ACTIVE_TICKS;
        Vec3 center = Vec3.atCenterOf(pos);
        double rangeSqr = HEAR_RANGE * HEAR_RANGE;
        for (ServerPlayer player : level.players()) {
            if (player.position().distanceToSqr(center) <= rangeSqr) {
                sendSound(player, level, pos, bell.playingClass);
                bell.servedPlayers.add(player.getUUID());
            }
        }
    }

    /** 响铃有效期内，给刚走进范围的玩家补发声音。 */
    private static void resendToEnteringPlayers(ServerLevel level, BlockPos pos, BellBlockEntity bell) {
        Vec3 center = Vec3.atCenterOf(pos);
        double rangeSqr = HEAR_RANGE * HEAR_RANGE;
        for (ServerPlayer player : level.players()) {
            if (bell.servedPlayers.contains(player.getUUID())) {
                continue;
            }
            if (player.position().distanceToSqr(center) <= rangeSqr) {
                sendSound(player, level, pos, bell.playingClass);
                bell.servedPlayers.add(player.getUUID());
            }
        }
    }

    /** 以电铃位置为声源给单个玩家播放铃声。 */
    private static void sendSound(ServerPlayer player, ServerLevel level, BlockPos pos, boolean isClass) {
        SoundEvent sound = (isClass ? ModSounds.SHANGKE : ModSounds.XIAKE).get();
        player.connection.send(new ClientboundSoundPacket(Holder.direct(sound), SoundSource.RECORDS,
                pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5,
                1.0F, 1.0F, level.getRandom().nextLong()));
    }

    private void setRinging(ServerLevel level, BlockPos pos, int value) {
        BlockState state = this.getBlockState();
        if (state.hasProperty(BellBlock.RINGING) && state.getValue(BellBlock.RINGING) != value) {
            level.setBlock(pos, state.setValue(BellBlock.RINGING, value), Block.UPDATE_ALL);
        }
    }

    /** 强制切断两种铃声。 */
    private void stopSounds() {
        if (!(this.level instanceof ServerLevel serverLevel)) {
            return;
        }
        for (ServerPlayer player : serverLevel.players()) {
            player.connection.send(new ClientboundStopSoundPacket(ModSounds.SHANGKE.get().location(), SoundSource.RECORDS));
            player.connection.send(new ClientboundStopSoundPacket(ModSounds.XIAKE.get().location(), SoundSource.RECORDS));
        }
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        output.putBoolean("Powered", this.powered);
        output.putInt("RingTicks", this.ringTicks);
        output.putString("LastTriggered", this.lastTriggered);
    }

    @Override
    public void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        this.powered = input.getBooleanOr("Powered", false);
        this.ringTicks = input.getIntOr("RingTicks", 0);
        this.lastTriggered = input.getStringOr("LastTriggered", "");
    }
}
