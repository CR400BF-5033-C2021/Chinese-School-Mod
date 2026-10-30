package com.cr400bf5033.chineseschool.util;

import com.cr400bf5033.chineseschool.ChineseSchoolMod;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import net.neoforged.fml.loading.FMLPaths;

/**
 * 上下课铃声的时间表。
 *
 * <p>从游戏实例的 config 目录读取 {@code shangketime.json}（上课铃时间）与
 * {@code xiaketime.json}（下课铃时间），内容为 24 小时制的时刻列表，例如：</p>
 *
 * <pre>
 * [
 *   "07:00",
 *   "13:30"
 * ]
 * </pre>
 *
 * <p>只比较时与分，不关心年月日；文件改完无需重启，会在 1 秒内自动重新读取。</p>
 */
public final class BellSchedule {

    private static final String CLASS_FILE = "shangketime.json";
    private static final String BREAK_FILE = "xiaketime.json";
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
    private static LocalTime[] classTimes = new LocalTime[0];
    private static LocalTime[] breakTimes = new LocalTime[0];

    private BellSchedule() {
    }

    /** 现在这一刻是不是配置的上课铃时间。 */
    public static boolean isClassTime(LocalTime now) {
        ensureLoaded();
        return contains(classTimes, now);
    }

    /** 现在这一刻是不是配置的下课铃时间。 */
    public static boolean isBreakTime(LocalTime now) {
        ensureLoaded();
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

    private static void ensureLoaded() {
        long now = System.currentTimeMillis();
        if (lastLoadMs >= 0 && now - lastLoadMs < RELOAD_INTERVAL_MS) {
            return;
        }
        lastLoadMs = now;
        classTimes = load(CLASS_FILE, DEFAULT_CLASS);
        breakTimes = load(BREAK_FILE, DEFAULT_BREAK);
    }

    private static LocalTime[] load(String fileName, String defaultContent) {
        Path path = FMLPaths.CONFIGDIR.get().resolve(fileName);
        try {
            if (!Files.isRegularFile(path)) {
                Files.createDirectories(path.getParent());
                Files.writeString(path, defaultContent, StandardCharsets.UTF_8);
                ChineseSchoolMod.LOGGER.info("已生成默认的铃声时间表：{}", path);
                return new LocalTime[0];
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
