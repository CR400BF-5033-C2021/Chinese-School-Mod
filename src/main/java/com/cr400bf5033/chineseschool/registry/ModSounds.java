package com.cr400bf5033.chineseschool.registry;

import com.cr400bf5033.chineseschool.ChineseSchoolMod;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

/**
 * 声音事件注册表。
 */
public final class ModSounds {

    public static final DeferredRegister<SoundEvent> SOUNDS =
            DeferredRegister.create(Registries.SOUND_EVENT, ChineseSchoolMod.MOD_ID);

    /** 上课铃。 */
    public static final DeferredHolder<SoundEvent, SoundEvent> SHANGKE = register("shangke");

    /** 下课铃。 */
    public static final DeferredHolder<SoundEvent, SoundEvent> XIAKE = register("xiake");

    private static DeferredHolder<SoundEvent, SoundEvent> register(String name) {
        // 可变范围事件：广播半径 = 16 × 播放音量（见 SoundEvent#getRange）。
        // 和原版唱片机一致，靠客户端那条持续存在的声音实例实现"走出去再回来接着听"。
        return SOUNDS.register(name, () -> SoundEvent.createVariableRangeEvent(
                Identifier.fromNamespaceAndPath(ChineseSchoolMod.MOD_ID, name)));
    }

    private ModSounds() {
    }
}
