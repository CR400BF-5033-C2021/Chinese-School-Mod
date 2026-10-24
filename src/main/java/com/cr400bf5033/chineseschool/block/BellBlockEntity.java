package com.cr400bf5033.chineseschool.block;

import com.cr400bf5033.chineseschool.registry.ModBlockEntities;
import com.cr400bf5033.chineseschool.registry.ModSounds;
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
 * 电铃的方块实体，负责上下课铃声的循环计时。
 *
 * <p>放下 5 秒后开始上课铃，之后 40 分钟 → 下课铃 → 10 分钟 → 上课铃，如此循环。
 * 正在响的时候会在电铃周围冒出音符粒子。红石充能时完全静音，取消充能后重置循环。</p>
 */
public class BellBlockEntity extends BlockEntity {

    /** 放下（或取消充能）后到第一声铃的延迟：5 秒。 */
    public static final int START_DELAY = 5 * 20;

    /** 一节课的时长：40 分钟（现实时间）。 */
    public static final int CLASS_TICKS = 40 * 60 * 20;

    /** 课间时长：10 分钟。 */
    public static final int BREAK_TICKS = 10 * 60 * 20;

    /** 响铃状态（含音符粒子）持续的 tick 数。 */
    public static final int RING_TICKS = 100;

    private boolean powered;
    private boolean initialized;
    private boolean nextIsClass = true;
    private int ticksUntilNext = START_DELAY;
    private int ringTicks;

    public BellBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.BELL.get(), pos, state);
    }

    /** 红石信号变化时的处理。 */
    public void onPowerChanged(boolean powered) {
        this.powered = powered;
        this.ringTicks = 0;
        if (powered) {
            // 充能：立刻静音
            this.stopSounds();
        } else {
            // 取消充能：重置循环，5 秒后重新从上课铃开始
            this.nextIsClass = true;
            this.ticksUntilNext = START_DELAY;
        }
        this.setChanged();
    }

    /** 每个服务端 tick 调用一次。 */
    public static void serverTick(Level level, BlockPos pos, BellBlockEntity bell) {
        if (!(level instanceof ServerLevel serverLevel)) {
            return;
        }
        if (!bell.initialized) {
            bell.initialized = true;
            bell.ticksUntilNext = START_DELAY;
            bell.nextIsClass = true;
        }
        if (bell.powered) {
            return;
        }

        // 响铃中：冒音符粒子，结束后把状态复位
        if (bell.ringTicks > 0) {
            bell.ringTicks--;
            if (bell.ringTicks % 4 == 0) {
                net.minecraft.util.RandomSource random = serverLevel.getRandom();
                serverLevel.sendParticles(ParticleTypes.NOTE,
                        pos.getX() + 0.5 + (random.nextDouble() - 0.5) * 0.8,
                        pos.getY() + 0.55 + (random.nextDouble() - 0.5) * 0.5,
                        pos.getZ() + 0.5 + (random.nextDouble() - 0.5) * 0.8,
                        1, 0.0, 0.15, 0.0, 0.0);
            }
            if (bell.ringTicks == 0) {
                bell.setRinging(serverLevel, pos, 0);
            }
        }

        if (bell.ticksUntilNext > 0) {
            bell.ticksUntilNext--;
            if (bell.ticksUntilNext % 200 == 0) {
                bell.setChanged();
            }
        }

        if (bell.ticksUntilNext <= 0) {
            boolean isClass = bell.nextIsClass;
            SoundEvent sound = (isClass ? ModSounds.SHANGKE : ModSounds.XIAKE).get();
            serverLevel.playSound(null, pos, sound, SoundSource.RECORDS, 4.0F, 1.0F);
            bell.setRinging(serverLevel, pos, isClass ? 1 : 2);
            bell.ringTicks = RING_TICKS;
            bell.ticksUntilNext = isClass ? CLASS_TICKS : BREAK_TICKS;
            bell.nextIsClass = !isClass;
            bell.setChanged();
        }
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
        output.putBoolean("Initialized", this.initialized);
        output.putBoolean("NextIsClass", this.nextIsClass);
        output.putInt("TicksUntilNext", this.ticksUntilNext);
        output.putInt("RingTicks", this.ringTicks);
    }

    @Override
    public void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        this.powered = input.getBooleanOr("Powered", false);
        this.initialized = input.getBooleanOr("Initialized", false);
        this.nextIsClass = input.getBooleanOr("NextIsClass", true);
        this.ticksUntilNext = input.getIntOr("TicksUntilNext", START_DELAY);
        this.ringTicks = input.getIntOr("RingTicks", 0);
    }
}
