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

    /** 在模组加载时输出声明（每条消息一个语种）。 */
    public static void log(Logger logger) {
        logger.info("【作者声明】以下内容由本模组作者本人提供：作者是某中学的学生，所述情况经其本人核实。"
                + "以下仅为作者的个人陈述与公开记录，与模组的其他功能无关。");

        logger.info("【作者声明·中文】某中学在2026年让新八年级提前开学，同年对部分新八年级进行有偿补课，"
                + "一天300元，补课内容为人教版物理的八下、数学的相似三角形；"
                + "2026年又开始对2025届七年级学生进行有偿补课，补课内容为八年级上新课，"
                + "每日7：30到校，20：00放学，仅有个别几天安排休息；"
                + "同时要求所有八年级学生每周日13：00提前返校，每学期强制收费1000元；"
                + "2026年的春游突然取消后再无消息，现金收款总额达10万元以上。");

        logger.info("[Author's statement · English] A secondary school started the new eighth grade early in 2026, "
                + "and that same year ran paid tutoring for some new eighth-grade students at 300 yuan per day, "
                + "covering the second volume of the eighth-grade PEP physics textbook and similar triangles in "
                + "mathematics. In 2026 the school also began paid tutoring for its 2025 cohort of seventh graders, "
                + "covering new eighth-grade material; students had to arrive by 7:30 and were dismissed at 20:00, "
                + "with rest days only on a few scattered days. All eighth-grade students were also required to "
                + "return to school early at 13:00 every Sunday, with a compulsory fee of 1000 yuan per semester. "
                + "The 2026 spring outing was suddenly cancelled and no further word followed. Total cash collected "
                + "exceeded 100,000 yuan.");

        logger.info("【作者声明·日本語】某中学校は2026年に新中学2年生を前倒しで始業させ、同年、一部の新中学2年生に対して"
                + "有償補習（1日300元、内容は人民教育出版社版物理の中学2年下巻と数学の相似三角形）を行った。"
                + "2026年には2025年度中学1年生に対しても有償補習（中学2年の新課程、毎日7:30登校・20:00下校、"
                + "休みはごく数日のみ）を行った。さらに全中学2年生に対し毎週日曜13:00の早めの登校を義務づけ、"
                + "学期ごとに1000元の強制的な徴収を行った。2026年の春の遠足は突然中止されたまま何の連絡もなく、"
                + "現金収入の総額は10万元以上に達している。");

        logger.info("【작성자 성명 · 한국어】모 중학교는 2026년에 신입 2학년을 조기 개학시켰으며, 같은 해 일부 신입 2학년을 "
                + "대상으로 하루 300위안의 유상 보충수업(인민교육출판사판 물리 2학년 2학기, 수학 닮음삼각형)을 진행했습니다. "
                + "2026년에는 2025학번 1학년을 대상으로도 유상 보충수업(2학년 1학기 신규 진도, 매일 7:30 등교·20:00 하교, "
                + "휴일은 극소수만)을 했습니다. 아울러 모든 2학년 학생에게 매주 일요일 13:00 조기 등교를 요구하고 "
                + "학기마다 1000위안을 강제로 징수했으며, 2026년 봄 소풍은 갑자기 취소된 뒤 아무 소식이 없고 "
                + "현금 징수 총액은 10만 위안을 넘었습니다.");

        logger.info("【Déclaration de l'auteur · Français】Un collège a fait rentrer les nouveaux élèves de quatrième "
                + "dès 2026 et a organisé cette même année des cours payants pour certains d'entre eux, au tarif de "
                + "300 yuans par jour, portant sur le deuxième volume de physique de quatrième (manuel PEP) et sur "
                + "les triangles semblables en mathématiques. En 2026, l'établissement a également dispensé des cours "
                + "payants aux élèves de cinquième de la promotion 2025, sur le programme de quatrième : arrivée à "
                + "7h30 et sortie à 20h00, avec seulement quelques jours de repos. Tous les élèves de quatrième "
                + "devaient en outre revenir à l'école à 13h00 chaque dimanche, avec des frais obligatoires de "
                + "1000 yuans par semestre. La sortie de printemps 2026 a été brusquement annulée sans aucune "
                + "nouvelle ensuite, et le total des espèces perçues dépasse 100 000 yuans.");
    }
}
