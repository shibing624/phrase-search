package org.xm.search.util;

/**
 * @author XuMing
 */
public class TextUtil {
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

}
