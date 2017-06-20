package org.xm.search.util;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * @author XuMing
 */
public class TextUtil {
    /**
     * 日志组件
     */
    private static Logger logger = LogManager.getLogger();

    public static boolean isAllNonChinese(String text) {
        for (char c : text.toCharArray()) {
            if (isChinese(c)) {
                return false;
            }
        }
        return true;
    }

    public static boolean isAllChinese(String text) {
        boolean chinese = true;
        for (char c : text.toCharArray()) {
            if (!isChinese(c)) {
                chinese = false;
            }
        }
        return chinese;
    }

    public static boolean isChinese(char c) {
        return c >= '\u4e00' && c <= '\u9fa5';
    }

    public static boolean isEnglish(char c) {
        if (c > 'z' && c < 'Ａ') {
            return false;
        }
        if (c < 'A') {
            return false;
        }
        if (c > 'Z' && c < 'a') {
            return false;
        }
        if (c > 'Ｚ' && c < 'ａ') {
            return false;
        }
        if (c > 'ｚ') {
            return false;
        }
        return true;
    }

    public static boolean isEnglish(String text) {
        boolean english = true;
        for (char c : text.toCharArray()) {
            if (!isEnglish(c)) {
                english = false;
            }
        }
        return english;
    }

    public static boolean isNumber(char c) {
        //大部分字符在这个范围
        if (c > '9' && c < '０') {
            return false;
        }
        if (c < '0') {
            return false;
        }
        if (c > '９') {
            return false;
        }
        return true;
    }

    public static String normalize(String text) {
        StringBuilder result = new StringBuilder();
        for (char c : text.toCharArray()) {
            if (isChinese(c) || isEnglish(c) || isNumber(c)) {
                result.append(c);
            }
        }
        if (text.length() != result.length()) {
            logger.debug("规范化处理，仅仅保留中英文字符和数字，规范化之前：{}，规范化之后：{}", text, result.toString());
        }
        return result.toString().toLowerCase();
    }

    public static String extractChinese(String text) {
        StringBuilder result = new StringBuilder();
        for (char c : text.toCharArray()) {
            if (isChinese(c)) {
                result.append(c);
            }
        }
        return result.toString().toLowerCase();
    }
}
