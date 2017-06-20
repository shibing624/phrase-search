package org.xm.search.util;

import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;

/**
 * 时间处理类
 *
 * @author XuMing
 */
public class TimeUtil {
    /**
     * 取时间的中文描述信息
     *
     * @param ms 毫秒（System.currentTimeMillis()）
     * @return 时长描述，如：1天1小时20分钟33秒12毫秒
     */
    public static String getTimeDes(Long ms) {
        //处理参数为NULL的情况
        if (ms == null || ms == 0) {
            return "0毫秒";
        }
        boolean minus = false;
        if (ms < 0) {
            minus = true;
            ms = -ms;
        }
        int ss = 1000;
        int mi = ss * 60;
        int hh = mi * 60;
        int dd = hh * 24;

        long day = ms / dd;
        long hour = (ms - day * dd) / hh;
        long minute = (ms - day * dd - hour * hh) / mi;
        long second = (ms - day * dd - hour * hh - minute * mi) / ss;
        long milliSecond = ms - day * dd - hour * hh - minute * mi - second * ss;

        StringBuilder str = new StringBuilder();
        if (day > 0) {
            str.append(day).append("天,");
        }
        if (hour > 0) {
            str.append(hour).append("小时,");
        }
        if (minute > 0) {
            str.append(minute).append("分钟,");
        }
        if (second > 0) {
            str.append(second).append("秒,");
        }
        if (milliSecond > 0) {
            str.append(milliSecond).append("毫秒,");
        }
        if (str.length() > 0) {
            str.setLength(str.length() - 1);
        }

        if (minus) {
            return "-" + str.toString();
        }

        return str.toString();
    }

    public static String toString(LocalDateTime time, String pattern) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(pattern);
        return time.format(formatter);
    }

    public static String toString(LocalDateTime time) {
        return toString(time, "yyyy-MM-dd HH:mm:ss");
    }

    public static String toString(long time, String pattern) {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(pattern);
        return simpleDateFormat.format(new Date(time));
    }

    public static String toString(long time) {
        return toString(time, "yyyy-MM-dd HH:mm:ss");
    }
}
