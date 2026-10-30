package com.cr400bf5033.chineseschool.util;

import com.cr400bf5033.chineseschool.ChineseSchoolMod;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.storage.LevelResource;
import net.neoforged.fml.loading.FMLPaths;

/**
 * 上下课铃声的时间表。
 *
 * <p>每张存档都有自己的时间表：文件位于
 * {@code <存档目录>/chinese_school/shangketime.json}（上课铃）与
 * {@code <存档目录>/chinese_school/xiaketime.json}（下课铃），
 * 内容为 24 小时制的时刻列表，例如：</p>
 *
 * <pre>
 * [
 *   "07:00",
 *   "13:30"
 * ]
 * </pre>
 *
 * <p>只比较时与分，不关心年月日；文件改完无需重启，会在 1 秒内自动重新读取。
 * 如果存档里还没有这两个文件，会先尝试从旧的 {@code <游戏实例>/config/} 迁移，
 * 没有旧文件则写入示例内容。</p>
 */
public final class BellSchedule {

    private static final String CLASS_FILE = "shangketime.json";
    private static final String BREAK_FILE = "xiaketime.json";
    /** 存档内存放时间表的子目录。 */
    private static final String DIR_NAME = "chinese_school";
    private static final long RELOAD_INTERVAL_MS = 1000L;

    private static final String DEFAULT_CLASS = """
            [
              "08:00",
              "13:30"
            ]
            """;
    private static final String DEFAULT_BREAK = """
            [
              "11:40",
              "17:10"
            ]
            """;

    private static long lastLoadMs = -1L;
    private static String loadedDir = "";
    private static LocalTime[] classTimes = new LocalTime[0];
    private static LocalTime[] breakTimes = new LocalTime[0];

    private BellSchedule() {
    }

    /** 现在这一刻是不是配置的上课铃时间。 */
    public static boolean isClassTime(ServerLevel level, LocalTime now) {
        ensureLoaded(level);
        return contains(classTimes, now);
    }

    /** 现在这一刻是不是配置的下课铃时间。 */
    public static boolean isBreakTime(ServerLevel level, LocalTime now) {
        ensureLoaded(level);
        return contains(breakTimes, now);
    }

    private static boolean contains(LocalTime[] times, LocalTime now) {
        for (LocalTime time : times) {
            if (time.getHour() == now.getHour() && time.getMinute() == now.getMinute()) {
                return true;
            }
        }
        return false;
    }

    /** 存档内的时间表目录。 */
    private static Path scheduleDir(ServerLevel level) {
        return level.getServer().getWorldPath(LevelResource.ROOT).resolve(DIR_NAME);
    }

    private static void ensureLoaded(ServerLevel level) {
        Path dir = scheduleDir(level);
        long now = System.currentTimeMillis();
        // 换了存档或者缓存过期就重新读取
        if (dir.toString().equals(loadedDir) && lastLoadMs >= 0 && now - lastLoadMs < RELOAD_INTERVAL_MS) {
            return;
        }
        loadedDir = dir.toString();
        lastLoadMs = now;
        classTimes = load(dir, CLASS_FILE, DEFAULT_CLASS);
        breakTimes = load(dir, BREAK_FILE, DEFAULT_BREAK);
    }

    private static LocalTime[] load(Path dir, String fileName, String defaultContent) {
        Path path = dir.resolve(fileName);
        try {
            if (!Files.isRegularFile(path)) {
                Files.createDirectories(dir);
                Path legacy = FMLPaths.CONFIGDIR.get().resolve(fileName);
                if (Files.isRegularFile(legacy)) {
                    // 从旧的全游戏 config 目录迁移一次
                    Files.copy(legacy, path, StandardCopyOption.REPLACE_EXISTING);
                    ChineseSchoolMod.LOGGER.info("已把 {} 迁移到存档目录：{}", fileName, path);
                } else {
                    Files.writeString(path, defaultContent, StandardCharsets.UTF_8);
                    ChineseSchoolMod.LOGGER.info("已为当前存档生成默认铃声时间表：{}", path);
                }
            }
            String text = Files.readString(path, StandardCharsets.UTF_8);
            List<LocalTime> times = new ArrayList<>();
            collectTimes(com.google.gson.JsonParser.parseString(text), times);
            return times.toArray(new LocalTime[0]);
        } catch (Exception e) {
            ChineseSchoolMod.LOGGER.warn("读取 {} 失败，本次按空时间表处理：{}", fileName, e.toString());
            return new LocalTime[0];
        }
    }

    /** 兼容数组、单个字符串以及 {"times": [...]} 几种写法。 */
    private static void collectTimes(com.google.gson.JsonElement element, List<LocalTime> out) {
        if (element == null) {
            return;
        }
        if (element.isJsonArray()) {
            for (com.google.gson.JsonElement child : element.getAsJsonArray()) {
                collectTimes(child, out);
            }
            return;
        }
        if (element.isJsonObject()) {
            com.google.gson.JsonObject object = element.getAsJsonObject();
            if (object.has("times")) {
                collectTimes(object.get("times"), out);
            }
            return;
        }
        if (element.isJsonPrimitive()) {
            parseTime(element.getAsString()).ifPresent(out::add);
        }
    }

    private static Optional<LocalTime> parseTime(String raw) {
        // 兼容中文冒号与多余空格
        String text = raw.trim().replace('：', ':');
        String[] parts = text.split(":");
        if (parts.length < 2) {
            return Optional.empty();
        }
        try {
            int hour = Integer.parseInt(parts[0].trim());
            int minute = Integer.parseInt(parts[1].trim());
            if (hour < 0 || hour > 23 || minute < 0 || minute > 59) {
                return Optional.empty();
            }
            return Optional.of(LocalTime.of(hour, minute));
        } catch (NumberFormatException e) {
            return Optional.empty();
        }
    }
}
