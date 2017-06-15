package org.xm.search.service;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.xm.search.Search;
import org.xm.search.domain.Document;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 短语文本搜索
 *
 * @author XuMing
 */
public class PhraseSearcher {
    /**
     * 日志组件
     */
    private static Logger logger = LogManager.getLogger();
    private AtomicLong processSearchCount = new AtomicLong();
    private static final int SEARCH_MAX_CONCURRENT = Search.Config.SearchMaxConcurrent;
    private Map<String,AtomicInteger> searchHistories = new ConcurrentHashMap<>();
    private Map<String,AtomicInteger> searchCountEveryDay = new ConcurrentHashMap<>();
    private AtomicInteger indexId = new AtomicInteger();
    private Map<String,Set<Integer>> invertedIndex = new ConcurrentHashMap<>();
    private Map<Integer,Integer> indexToDocument = new ConcurrentHashMap<>();
    private Map<Integer,Integer> documentToIndex = new ConcurrentHashMap<>();
    private Map<Integer,Document> documentMap  = new ConcurrentHashMap<>();

}
