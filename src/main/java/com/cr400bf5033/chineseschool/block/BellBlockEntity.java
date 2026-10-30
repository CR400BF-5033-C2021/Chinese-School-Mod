package com.cr400bf5033.chineseschool.block;

import com.cr400bf5033.chineseschool.registry.ModBlockEntities;
import com.cr400bf5033.chineseschool.registry.ModSounds;
import com.cr400bf5033.chineseschool.util.BellSchedule;
import java.time.LocalTime;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
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

/**
 * 电铃的方块实体。
 *
 * <p>按现实系统时间触发：当系统时间的「时:分」命中 {@code config/shangketime.json}
 * 或 {@code config/xiaketime.json} 中的某个时刻时，用和原版唱片机一样的方式
 * （{@code level.playSound} 定点播放）放一次铃声。</p>
 */
public class BellBlockEntity extends BlockEntity {

    /** 响铃状态（含音符粒子）持续的 tick 数。 */
    public static final int RING_TICKS = 100;

    private boolean powered;
    private int ringTicks;
    /** 上一次触发的时间键（HH:mm），用于同一分钟只响一次。 */
    private String lastTriggered = "";

    public BellBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.BELL.get(), pos, state);
    }

    /** 红石信号变化：充能时立刻静音。 */
    public void onPowerChanged(boolean powered) {
        this.powered = powered;
        this.ringTicks = 0;
        if (powered) {
            this.stopSounds();
            if (this.level != null) {
                BlockState state = this.level.getBlockState(this.worldPosition);
                if (state.hasProperty(BellBlock.RINGING)) {
                    this.level.setBlock(this.worldPosition, state.setValue(BellBlock.RINGING, 0), Block.UPDATE_ALL);
                }
            }
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

        // 每秒检查一次系统时间
        if (serverLevel.getGameTime() % 20L != 0L) {
            return;
        }

        LocalTime now = LocalTime.now();
        String key = String.format("%02d:%02d", now.getHour(), now.getMinute());
        if (key.equals(bell.lastTriggered)) {
            return;
        }

        boolean classTime = BellSchedule.isClassTime(serverLevel, now);
        boolean breakTime = BellSchedule.isBreakTime(serverLevel, now);
        if (!classTime && !breakTime) {
            return;
        }

        bell.lastTriggered = key;
        boolean isClass = classTime;
        // 和原版唱片机一样：以方块位置为声源原地播放一次。
        // 音量 4.0 → 可变范围事件的广播半径 64 格，客户端那条实例会一直播下去，
        // 所以玩家走出再回来听到的是当前进度，而不是从头开始。
        SoundEvent sound = (isClass ? ModSounds.SHANGKE : ModSounds.XIAKE).get();
        serverLevel.playSound(null, pos, sound, SoundSource.RECORDS, 4.0F, 1.0F);
        bell.setRinging(serverLevel, pos, isClass ? 1 : 2);
        bell.ringTicks = RING_TICKS;
        bell.setChanged();
    }

    private void setRinging(ServerLevel level, BlockPos pos, int value) {
        BlockState state = this.getBlockState();
        if (state.hasProperty(BellBlock.RINGING) && state.getValue(BellBlock.RINGING) != value) {
            level.setBlock(pos, state.setValue(BellBlock.RINGING, value), Block.UPDATE_ALL);
        }
    }

    /** 强制切断正在播放的铃声。 */
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
