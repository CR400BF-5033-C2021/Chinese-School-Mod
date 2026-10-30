package com.cr400bf5033.chineseschool.registry;

import com.cr400bf5033.chineseschool.ChineseSchoolMod;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

/**
 * 声音事件注册表。
 *
 * <p>铃声被切成每秒一段（见 {@code tools/split_ogg.py}），每段一个声音事件，
 * 播放时按秒推进，这样玩家走出范围再回来可以从当前秒继续听。</p>
 */
public final class ModSounds {

    public static final DeferredRegister<SoundEvent> SOUNDS =
            DeferredRegister.create(Registries.SOUND_EVENT, ChineseSchoolMod.MOD_ID);

    /** 上课铃切出来的段数。 */
    public static final int SHANGKE_CHUNKS = 8;

    /** 下课铃切出来的段数。 */
    public static final int XIAKE_CHUNKS = 16;

    /** 上课铃的逐秒音频段。 */
    public static final List<DeferredHolder<SoundEvent, SoundEvent>> SHANGKE =
            registerChunks("shangke", SHANGKE_CHUNKS);

    /** 下课铃的逐秒音频段。 */
    public static final List<DeferredHolder<SoundEvent, SoundEvent>> XIAKE =
            registerChunks("xiake", XIAKE_CHUNKS);

    private static List<DeferredHolder<SoundEvent, SoundEvent>> registerChunks(String prefix, int count) {
        List<DeferredHolder<SoundEvent, SoundEvent>> chunks = new ArrayList<>(count);
        for (int i = 0; i < count; i++) {
            String name = String.format("%s_%02d", prefix, i);
            // 固定 16 格范围的原版式声音事件
            chunks.add(SOUNDS.register(name, () -> SoundEvent.createFixedRangeEvent(
                    Identifier.fromNamespaceAndPath(ChineseSchoolMod.MOD_ID, name), 16.0F)));
        }
        return List.copyOf(chunks);
    }

    private ModSounds() {
    }
}
