package com.cr400bf5033.chineseschool.client;

import com.cr400bf5033.chineseschool.ChineseSchoolMod;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import net.minecraft.SharedConstants;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.repository.FolderRepositorySource;
import net.minecraft.server.packs.repository.PackSource;
import net.minecraft.world.level.validation.DirectoryValidator;
import net.neoforged.fml.loading.FMLPaths;
import net.neoforged.neoforge.event.AddPackFindersEvent;

/**
 * 从游戏实例的 config 目录读取上下课铃声，生成一个运行期资源包并挂载。
 *
 * <p>用户把 {@code shangke.ogg}（上课铃）与 {@code xiake.ogg}（下课铃）放进
 * {@code <游戏实例>/config/} 即可，启动时会被复制进
 * {@code config/chinese_school_bell_packs/chinese_school_bell/} 并作为内置资源包加载。</p>
 */
public final class BellSoundPack {

    /** 存放资源包的父目录：FolderRepositorySource 会把里面每个子文件夹当成一个资源包。 */
    private static final String PACKS_DIR = "chinese_school_bell_packs";

    /** 资源包文件夹名。 */
    private static final String PACK_NAME = "chinese_school_bell";

    private BellSoundPack() {
    }

    public static void onAddPackFinders(AddPackFindersEvent event) {
        if (event.getPackType() != PackType.CLIENT_RESOURCES) {
            return;
        }
        Path packsRoot = buildPack();
        if (packsRoot == null) {
            return;
        }
        // 注意：FolderRepositorySource 的参数是「装着若干资源包的目录」，不是资源包本身
        event.addRepositorySource(consumer -> new FolderRepositorySource(
                packsRoot, PackType.CLIENT_RESOURCES, PackSource.BUILT_IN, new DirectoryValidator(path -> false)
        ).loadPacks(consumer));
    }

    /** 生成资源包目录；失败时返回 null（不影响游戏启动）。 */
    private static Path buildPack() {
        try {
            Path configDir = FMLPaths.CONFIGDIR.get();
            Path packsRoot = configDir.resolve(PACKS_DIR);
            Path packDir = packsRoot.resolve(PACK_NAME);
            Path soundsDir = packDir.resolve("assets").resolve(ChineseSchoolMod.MOD_ID).resolve("sounds");
            Files.createDirectories(soundsDir);

            copyIfPresent(configDir.resolve("shangke.ogg"), soundsDir.resolve("shangke.ogg"));
            copyIfPresent(configDir.resolve("xiake.ogg"), soundsDir.resolve("xiake.ogg"));

            Files.writeString(packDir.resolve("assets").resolve(ChineseSchoolMod.MOD_ID).resolve("sounds.json"),
                    soundsJson(), StandardCharsets.UTF_8);
            Files.writeString(packDir.resolve("pack.mcmeta"), packMeta(), StandardCharsets.UTF_8);
            ChineseSchoolMod.LOGGER.info("电铃铃声资源包已生成：{}", packDir);
            return packsRoot;
        } catch (Exception e) {
            ChineseSchoolMod.LOGGER.warn("生成电铃资源包失败，铃声将不可用：{}", e.toString());
            return null;
        }
    }

    private static void copyIfPresent(Path from, Path to) throws IOException {
        if (Files.isRegularFile(from)) {
            Files.copy(from, to, StandardCopyOption.REPLACE_EXISTING);
        }
    }

    private static String soundsJson() {
        return """
                {
                  "shangke": { "sounds": [ { "name": "chinese_school:shangke", "stream": true } ] },
                  "xiake": { "sounds": [ { "name": "chinese_school:xiake", "stream": true } ] }
                }
                """;
    }

    private static String packMeta() {
        int format = SharedConstants.RESOURCE_PACK_FORMAT_MAJOR;
        return """
                {
                  "pack": {
                    "pack_format": %d,
                    "description": "Chinese School Mod bell sounds"
                  }
                }
                """.formatted(format);
    }
}
