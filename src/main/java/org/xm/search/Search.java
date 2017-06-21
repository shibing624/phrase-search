package org.xm.search;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.time.LocalDateTime;

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
        public static int SearchWordMaxLength = 30;
        /**
         * topN最大值
         */
        public static int TopNMaxLength = 1000;
        /**
         * 启用缓存
         */
        public static boolean EnableCache = true;
        /**
         * 缓存大小
         */
        public static int CacheSize = 1000;
        /**
         * 短语文本路径
         */
        public static String PharseTextPath = "data/phrase-text.txt";
        /**
         *
         */
        public static String InvertIndexTextPath = "data/invert-index.txt";
        /**
         *
         */
        public static String InvertIdToDocumentIdTextPath = "data/index-id-to-document-id.txt";
        /**
         * 文档保存路径
         */
        public static String DocumentTextPath = "data/document.txt";
    }


    public static void main(String[] args) {
        logger.debug("start at: " + LocalDateTime.now());
    }
}
