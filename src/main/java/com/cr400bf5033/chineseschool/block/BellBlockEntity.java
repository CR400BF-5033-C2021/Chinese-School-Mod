package com.cr400bf5033.chineseschool.block;

import com.cr400bf5033.chineseschool.registry.ModBlockEntities;
import com.cr400bf5033.chineseschool.registry.ModSounds;
import net.minecraft.core.BlockPos;
import net.minecraft.network.protocol.game.ClientboundStopSoundPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

/**
 * 电铃的方块实体，负责上下课铃声的循环计时。
 *
 * <p>计时规则：上课铃响过之后等 40 分钟播下课铃，下课铃响过之后等 10 分钟播上课铃。
 * 右键开关；关闭时立刻切断正在播放的声音。</p>
 */
public class BellBlockEntity extends BlockEntity {

    /** 一节课 40 分钟（现实时间）。 */
    public static final int CLASS_TICKS = 40 * 60 * 20;

    /** 课间 10 分钟。 */
    public static final int BREAK_TICKS = 10 * 60 * 20;

    private boolean enabled;
    /** 下一次该响的是不是上课铃。 */
    private boolean nextIsClass = true;
    private int ticksRemaining;

    public BellBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.BELL.get(), pos, state);
    }

    public boolean isEnabled() {
        return this.enabled;
    }

    /** 右键开关：打开时立刻打一次上课铃，关闭时切断正在播放的声音。 */
    public void toggle() {
        this.enabled = !this.enabled;
        if (this.enabled) {
            this.nextIsClass = true;
            this.ticksRemaining = 0;
            this.setChanged();
        } else {
            this.ticksRemaining = 0;
            this.stopSounds();
            this.setChanged();
        }
    }

    /** 每个服务端 tick 调用一次。 */
    public static void serverTick(Level level, BlockPos pos, BellBlockEntity bell) {
        if (level.isClientSide() || !bell.enabled) {
            return;
        }
        if (bell.ticksRemaining > 0) {
            bell.ticksRemaining--;
            if (bell.ticksRemaining % 200 == 0) {
                bell.setChanged();
            }
            return;
        }
        if (level instanceof ServerLevel serverLevel) {
            SoundEvent sound = (bell.nextIsClass ? ModSounds.SHANGKE : ModSounds.XIAKE).get();
            serverLevel.playSound(null, pos, sound, SoundSource.BLOCKS, 4.0F, 1.0F);
            bell.ticksRemaining = bell.nextIsClass ? CLASS_TICKS : BREAK_TICKS;
            bell.nextIsClass = !bell.nextIsClass;
            bell.setChanged();
        }
    }

    /** 强制切断两种铃声。 */
    private void stopSounds() {
        if (!(this.level instanceof ServerLevel serverLevel)) {
            return;
        }
        for (ServerPlayer player : serverLevel.players()) {
            player.connection.send(new ClientboundStopSoundPacket(ModSounds.SHANGKE.get().location(), SoundSource.BLOCKS));
            player.connection.send(new ClientboundStopSoundPacket(ModSounds.XIAKE.get().location(), SoundSource.BLOCKS));
        }
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        output.putBoolean("Enabled", this.enabled);
        output.putBoolean("NextIsClass", this.nextIsClass);
        output.putInt("TicksRemaining", this.ticksRemaining);
    }

    @Override
    public void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        this.enabled = input.getBooleanOr("Enabled", false);
        this.nextIsClass = input.getBooleanOr("NextIsClass", true);
        this.ticksRemaining = input.getIntOr("TicksRemaining", 0);
    }
}
