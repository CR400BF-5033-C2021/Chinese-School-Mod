package com.cr400bf5033.chineseschool.registry;

import com.cr400bf5033.chineseschool.ChineseSchoolMod;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

/**
 * 声音事件注册表。
 *
 * <p>实际的 .ogg 文件由运行期资源包提供（见 {@code BellSoundPack}），来源是游戏实例
 * config 目录里的 {@code shangke.ogg} 与 {@code xiake.ogg}。</p>
 */
public final class ModSounds {

    public static final DeferredRegister<SoundEvent> SOUNDS =
            DeferredRegister.create(Registries.SOUND_EVENT, ChineseSchoolMod.MOD_ID);

    /** 上课铃。 */
    public static final DeferredHolder<SoundEvent, SoundEvent> SHANGKE = register("shangke");

    /** 下课铃。 */
    public static final DeferredHolder<SoundEvent, SoundEvent> XIAKE = register("xiake");

    private static DeferredHolder<SoundEvent, SoundEvent> register(String name) {
        // 固定 16 格范围的原版式音效事件
        return SOUNDS.register(name, () -> SoundEvent.createFixedRangeEvent(
                Identifier.fromNamespaceAndPath(ChineseSchoolMod.MOD_ID, name), 16.0F));
    }

    private ModSounds() {
    }
}
