package com.cr400bf5033.chineseschool.block;

import com.cr400bf5033.chineseschool.registry.ModBlockEntities;
import com.cr400bf5033.chineseschool.registry.ModSounds;
import com.cr400bf5033.chineseschool.util.BellSchedule;
import java.time.LocalTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
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
import net.neoforged.neoforge.registries.DeferredHolder;

/**
 * 电铃的方块实体。
 *
 * <p>按现实系统时间触发：当系统时间的「时:分」命中 {@code config/shangketime.json}
 * 或 {@code config/xiaketime.json} 中的某个时刻时播放对应铃声。</p>
 *
 * <p>铃声按秒切成多段逐秒播放，因此玩家走出 16 格再回来时会从"当前第几秒"继续，
 * 而不是从头重播。</p>
 */
public class BellBlockEntity extends BlockEntity {

    /** 每一段的时长（tick）。 */
    public static final int SEGMENT_TICKS = 20;

    /** 听得见铃声的范围（格）。 */
    public static final double HEAR_RANGE = 16.0;

    private boolean powered;
    /** 上一次触发的时间键（HH:mm），用于同一分钟只响一次。 */
    private String lastTriggered = "";

    /** 当前播放到第几段，-1 表示没在播放。 */
    private int currentSegment = -1;
    /** 当前段已经播放了多少 tick。 */
    private int segmentTicks;
    /** 这次响的是不是上课铃。 */
    private boolean playingClass = true;
    /** 已经收到过当前段声音的玩家，避免同一段重复播放。 */
    private final Set<UUID> servedPlayers = new HashSet<>();

    public BellBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.BELL.get(), pos, state);
    }

    /** 红石信号变化：充能时立刻静音并停止播放。 */
    public void onPowerChanged(boolean powered) {
        this.powered = powered;
        this.currentSegment = -1;
        this.segmentTicks = 0;
        this.servedPlayers.clear();
        if (powered) {
            this.stopSounds();
            if (this.level != null && this.level.getBlockState(this.worldPosition).hasProperty(BellBlock.RINGING)) {
                this.level.setBlock(this.worldPosition,
                        this.level.getBlockState(this.worldPosition).setValue(BellBlock.RINGING, 0), Block.UPDATE_ALL);
            }
        }
        this.setChanged();
    }

    /** 每个服务端 tick 调用一次。 */
    public static void serverTick(Level level, BlockPos pos, BellBlockEntity bell) {
        if (!(level instanceof ServerLevel serverLevel)) {
            return;
        }

        // 正在播放：逐秒推进，给范围内还没听到当前段的玩家发声音
        if (bell.currentSegment >= 0) {
            bell.segmentTicks++;
            if (bell.segmentTicks >= SEGMENT_TICKS) {
                bell.segmentTicks = 0;
                bell.currentSegment++;
                bell.servedPlayers.clear();
            }
            int total = bell.chunkCount();
            if (bell.currentSegment >= total) {
                bell.stopPlayback(serverLevel, pos);
                return;
            }
            if (bell.currentSegment >= 0) {
                sendCurrentSegment(serverLevel, pos, bell);
                spawnNoteParticles(serverLevel, pos);
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

        boolean classTime = BellSchedule.isClassTime(now);
        boolean breakTime = BellSchedule.isBreakTime(now);
        if (!classTime && !breakTime) {
            return;
        }

        bell.lastTriggered = key;
        boolean isClass = classTime;
        bell.playingClass = isClass;
        bell.currentSegment = 0;
        bell.segmentTicks = 0;
        bell.servedPlayers.clear();
        bell.setRinging(serverLevel, pos, isClass ? 1 : 2);
        sendCurrentSegment(serverLevel, pos, bell);
        bell.setChanged();
    }

    private int chunkCount() {
        return (this.playingClass ? ModSounds.SHANGKE : ModSounds.XIAKE).size();
    }

    private void stopPlayback(ServerLevel level, BlockPos pos) {
        this.currentSegment = -1;
        this.segmentTicks = 0;
        this.servedPlayers.clear();
        this.setRinging(level, pos, 0);
        this.setChanged();
    }

    /** 把"当前这一段"发给范围内还没收到的玩家。 */
    private static void sendCurrentSegment(ServerLevel level, BlockPos pos, BellBlockEntity bell) {
        List<DeferredHolder<SoundEvent, SoundEvent>> chunks =
                bell.playingClass ? ModSounds.SHANGKE : ModSounds.XIAKE;
        if (bell.currentSegment < 0 || bell.currentSegment >= chunks.size()) {
            return;
        }
        SoundEvent sound = chunks.get(bell.currentSegment).get();
        Holder<SoundEvent> holder = Holder.direct(sound);
        Vec3 center = Vec3.atCenterOf(pos);
        double rangeSqr = HEAR_RANGE * HEAR_RANGE;
        for (ServerPlayer player : level.players()) {
            if (player.position().distanceToSqr(center) > rangeSqr) {
                continue;
            }
            if (!bell.servedPlayers.add(player.getUUID())) {
                continue;
            }
            player.connection.send(new ClientboundSoundPacket(holder, SoundSource.RECORDS,
                    pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5,
                    1.0F, 1.0F, level.getRandom().nextLong()));
        }
    }

    private static void spawnNoteParticles(ServerLevel level, BlockPos pos) {
        if (level.getGameTime() % 4L != 0L) {
            return;
        }
        level.sendParticles(ParticleTypes.NOTE,
                pos.getX() + 0.5 + (level.getRandom().nextDouble() - 0.5) * 0.8,
                pos.getY() + 0.55 + (level.getRandom().nextDouble() - 0.5) * 0.5,
                pos.getZ() + 0.5 + (level.getRandom().nextDouble() - 0.5) * 0.8,
                1, 0.0, 0.15, 0.0, 0.0);
    }

    private void setRinging(ServerLevel level, BlockPos pos, int value) {
        BlockState state = this.getBlockState();
        if (state.hasProperty(BellBlock.RINGING) && state.getValue(BellBlock.RINGING) != value) {
            level.setBlock(pos, state.setValue(BellBlock.RINGING, value), Block.UPDATE_ALL);
        }
    }

    /** 强制切断正在播放的铃声（逐段停止）。 */
    private void stopSounds() {
        if (!(this.level instanceof ServerLevel serverLevel)) {
            return;
        }
        for (ServerPlayer player : serverLevel.players()) {
            for (DeferredHolder<SoundEvent, SoundEvent> chunk : ModSounds.SHANGKE) {
                player.connection.send(new ClientboundStopSoundPacket(chunk.get().location(), SoundSource.RECORDS));
            }
            for (DeferredHolder<SoundEvent, SoundEvent> chunk : ModSounds.XIAKE) {
                player.connection.send(new ClientboundStopSoundPacket(chunk.get().location(), SoundSource.RECORDS));
            }
        }
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        output.putBoolean("Powered", this.powered);
        output.putBoolean("PlayingClass", this.playingClass);
        output.putInt("CurrentSegment", this.currentSegment);
        output.putInt("SegmentTicks", this.segmentTicks);
        output.putString("LastTriggered", this.lastTriggered);
    }

    @Override
    public void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        this.powered = input.getBooleanOr("Powered", false);
        this.playingClass = input.getBooleanOr("PlayingClass", true);
        this.currentSegment = input.getIntOr("CurrentSegment", -1);
        this.segmentTicks = input.getIntOr("SegmentTicks", 0);
        this.lastTriggered = input.getStringOr("LastTriggered", "");
    }
}
