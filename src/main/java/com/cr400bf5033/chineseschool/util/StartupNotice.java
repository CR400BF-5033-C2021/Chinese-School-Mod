package com.cr400bf5033.chineseschool.util;

import org.slf4j.Logger;

/**
 * 启动时输出的一段署名声明。
 *
 * <p>内容是模组作者本人（某中学在读学生）对自己在校经历的陈述，作者表示已自行核实。
 * 这段输出与模组的其他功能无关，仅作为作者的公开记录随游戏日志一并保存。</p>
 *
 * <p>为遵守平台规则，声明中隐去了具体校名与具体日期，只保留年份。</p>
 */
public final class StartupNotice {

    private StartupNotice() {
    }

    /** 在模组加载时输出声明*/
    public static void log(Logger logger) {
        logger.info("【作者声明】以下内容由本模组作者本人提供：作者是某中学的学生，所述情况经其本人核实。"
                + "以下仅为作者的个人陈述与公开记录，与模组的其他功能无关。");

        logger.info("【作者声明】我爱死你了学校暑假为我们补了一个月的课居然一天还只收300真是为我们着想啊！！！！！");
    }
}
