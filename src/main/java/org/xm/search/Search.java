package org.xm.search;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Search:搜索服务工具包
 *
 * @author XuMing
 */
public class Search {

    /**
     * 日志组件
     */
    private static Logger logger = LogManager.getLogger();

    public static final class Config {
        /**
         * 最大并发数
         */
        public static int SearchMaxConcurrent = 1000;
        /**
         * 高亮前缀
         */
        public static String HighLightPrefix = "<font color='red'>";
        /**
         * 高亮后缀
         */
        public static String HighLightSuffix = "</font>";
        /**
         * 搜索词长度限制
         */
        public static int WordMaxLength = 30;
        /**
         * topN最大长度
         */
        public static int topNMaxLength = 1000;
        /**
         * 启用缓存
         */
        public static boolean enableCache = true;
        /**
         * 缓存大小
         */
        public static int cacheSize = 1000;
        /**
         * 短语文本路径
         */
        public static String PharseTextPath = "/phrase-text.txt";

    }


    public static void main(String[] args) {
        logger.debug("debug");
    }
}
