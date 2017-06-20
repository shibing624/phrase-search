package org.xm.search.service;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.xm.search.Search;
import org.xm.search.domain.Document;
import org.xm.search.domain.Query;
import org.xm.search.domain.SearchResult;
import org.xm.search.tokenizer.Tokenizer;
import org.xm.search.util.ConcurrentLRUCache;
import org.xm.search.util.TextUtil;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 短语文本搜索器
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
    private Map<String, AtomicInteger> searchHistories = new ConcurrentHashMap<>();
    private Map<String, AtomicInteger> searchCountEveryDay = new ConcurrentHashMap<>();
    private AtomicInteger indexIdGenerator = new AtomicInteger();
    private Map<String, Set<Integer>> INVERTED_INDEX = new ConcurrentHashMap<>();
    private Map<Integer, Integer> INDEX_TO_DOCUMENT = new ConcurrentHashMap<>();
    private Map<Integer, Integer> DOCUMENT_TO_INDEX = new ConcurrentHashMap<>();
    private Map<Integer, Document> DOCUMENT = new ConcurrentHashMap<>();
    private AtomicLong indexTotalCost = new AtomicLong();

    private Set<String> charPinyin = Collections.newSetFromMap(new ConcurrentHashMap<>());
    private int charMaxPinyinLength = 0;

    private static final String PREIX = Search.Config.HighLightPrefix;
    private static final String SUFFIX = Search.Config.HighLightSuffix;

    private static final int SEARCH_WORD_MAX_LENGTH = Search.Config.SearchWordMaxLength;
    private static final int TOPN_MAX_LENGTH = Search.Config.TopNMaxLength;
    private AtomicInteger searchCount = new AtomicInteger();
    private AtomicLong maxSearchTime = new AtomicLong();
    private AtomicLong totalSearchTime = new AtomicLong();
    private long searchServiceStartupTime = System.currentTimeMillis();

    private boolean cacheEnabled = Search.Config.EnableCache;
    private ConcurrentLRUCache<String, SearchResult> cache = null;

    public PhraseSearcher(boolean cacheEnabled) {
        this.cacheEnabled = cacheEnabled;
        if (cacheEnabled) {
            cache = new ConcurrentLRUCache<>(Search.Config.CacheSize);
        }
        logger.info("搜索词长度限制：{}", SEARCH_WORD_MAX_LENGTH);
        logger.info("TopN长度限制：{}", TOPN_MAX_LENGTH);
    }

    public PhraseSearcher() {
        this(true);
    }

    public void clear() {
        if (cacheEnabled) {
            cache.clear();
        }
        INVERTED_INDEX.keySet().forEach(k -> INVERTED_INDEX.get(k).clear());
        INVERTED_INDEX.clear();
        INDEX_TO_DOCUMENT.clear();
        DOCUMENT_TO_INDEX.clear();
        DOCUMENT.clear();
        charPinyin.clear();
    }

    public void saveIndex() {
        long start = System.currentTimeMillis();
//        saveInvertIndex(INVERTED_INDEX);
//        saveIndexIdDocumentIdMapping(INDEX_TO_DOCUMENT);
//        saveDocument(DOCUMENT);
        logger.info("保存索引耗时：{}", System.currentTimeMillis() - start);
    }

    public String getKeyAndHitCount() {
        return cache.getKeyAndHitCount();
    }

    public String getCacheStatus() {
        return cache.getStatus();
    }

    public String getSearchStatus() {
        StringBuilder status = new StringBuilder();
        status.append("系统运行时间：")
                .append(System.currentTimeMillis() - searchServiceStartupTime).append("\n")
                .append("已经搜索次数：").append(searchCount.get()).append("\n")
                .append("搜索最慢组件用时：").append(maxSearchTime.get()).append("\n")
                .append("累计搜索时间：").append(totalSearchTime.get()).append("\n")
                .append("搜索时间占比：").append(totalSearchTime.get() / (float) (System.currentTimeMillis() - searchServiceStartupTime) * 100).append("%\n")
                .append("搜索词长度限制：").append(SEARCH_WORD_MAX_LENGTH).append("\n")
                .append("topN长度限制：").append(TOPN_MAX_LENGTH).append("\n");
        status.append("每日搜索次数统计：\n");
        searchCountEveryDay.keySet().stream()
                .sorted(Comparator.reverseOrder())
                .forEach(day -> status
                        .append("\t").append(day).append(" --> ")
                        .append(searchCountEveryDay.get(day)).append("\n"));
        return status.toString();
    }

    public String getIndexStatus() {
        StringBuilder status = new StringBuilder();
        status.append("索引文档数：").append(DOCUMENT.size()).append("\n")
                .append("索引用时：").append(indexTotalCost.get()).append("\n")
                .append("词数：").append(INVERTED_INDEX.size()).append("\n")
                .append("最长拼音字母数：").append(charMaxPinyinLength).append("\n");
        return status.toString();
    }

    public void index(Map<Integer, Document> documentMap) {
        long start = System.currentTimeMillis();
        documentMap.values().parallelStream().forEach(document -> {
            try {
                indexSingle(document);
            } catch (Exception e) {
                logger.error("索引数据出错", e);
            }
        });
        documentMap.clear();
        indexTotalCost.addAndGet(System.currentTimeMillis() - start);
        logger.info(getIndexStatus());
    }

    private void indexSingle(Document document) {
        deleteOldIndexIfExist(document.getId());
        int indexId = indexIdGenerator.incrementAndGet();
        List<String> terms = Tokenizer.segment(document.getValue());
        document.addTerms(terms);
        for (String term : terms) {
            INVERTED_INDEX.putIfAbsent(term, Collections.newSetFromMap(new ConcurrentHashMap<>()));
            INVERTED_INDEX.get(term).add(indexId);
        }
        INDEX_TO_DOCUMENT.put(indexId, document.getId());
        DOCUMENT_TO_INDEX.put(document.getId(), indexId);
        DOCUMENT.put(document.getId(), document);
    }

    public void createIndex(Document document) {
        long start = System.currentTimeMillis();
        indexSingle(document);
        indexTotalCost.addAndGet(System.currentTimeMillis() - start);
    }

    public void deleteIndex(int documentId) {
        if (deleteOldIndexIfExist(documentId)) {
            logger.debug("文档索引删除成功，ID:{}", documentId);
        } else {
            logger.warn("要删除的文档索引不存在，ID:{}", documentId);
        }
    }

    public void updateIndex(Document document) {
        long start = System.currentTimeMillis();
        indexSingle(document);
        indexTotalCost.addAndGet(System.currentTimeMillis() - start);
    }

    public void highlight(List<Document> documents, String keywords, List<String> keywordTerms) {
        for (Document document : documents) {
            highlight(document, keywords, keywordTerms);
        }
    }

    public void highlight(Document document, String keywords, List<String> keywordTerms) {
        String val = document.getValue();
        if (val.contains(keywords)) {
            document.setValue(val.replace(keywords, PREIX + keywords + SUFFIX));
            return;
        }
        Collections.sort(keywordTerms, (a, b) -> b.length() - a.length());
        String last = "";
        boolean highlight = false;
        for (String keywordTerm : keywordTerms) {
            if (StringUtils.isNotBlank(last) && last.contains(keywordTerm)) {
                continue;
            }
            int index = val.indexOf(keywordTerm);
            if (index > -1) {
                highlight = true;
                val = val.replace(keywordTerm, PREIX + keywordTerm + SUFFIX);
                if (StringUtils.isBlank(last)) {
                    last = keywordTerm;
                }
            }
        }
        if (highlight) {
            document.setValue(val);
        }
    }

    private boolean deleteOldIndexIfExist(int documentId) {
        Integer indexId = DOCUMENT_TO_INDEX.get(documentId);
        if (indexId != null) {
            INDEX_TO_DOCUMENT.remove(indexId);
            DOCUMENT_TO_INDEX.remove(documentId);
            DOCUMENT.remove(documentId);
            logger.debug("删除文档索引，ID:{}", documentId);
            return true;
        }
        return false;
    }

    public void clearSearchHistories() {
        searchHistories.clear();
    }

    public String getSearchHistories() {
        StringBuilder sb = new StringBuilder();
        AtomicInteger i = new AtomicInteger();
        searchHistories.entrySet().stream()
                .sorted((a, b) -> {
                    int c = b.getValue().get() - a.getValue().get();
                    if (c == 0) {
                        c = a.getKey().compareTo(b.getKey());
                    }
                    return c;
                })
                .forEach(e ->
                        sb.append(i.incrementAndGet())
                                .append("\t")
                                .append(e.getKey())
                                .append("\t")
                                .append(e.getValue().get())
                                .append("\n"));
        return sb.toString();
    }

    public Query parse(String keywords) {
        Query query = new Query();
        if (StringUtils.isBlank(keywords)) {
            return query;
        }
        keywords = keywords.trim();
        boolean isAllUpperCase = false;
        if (TextUtil.isAllNonChinese(keywords)) {
            isAllUpperCase = StringUtils.isAllUpperCase(keywords);
        }
        keywords = keywords.toLowerCase();
        if (keywords.length() > SEARCH_WORD_MAX_LENGTH) {
            String temp = keywords;
            keywords = keywords.substring(0, SEARCH_WORD_MAX_LENGTH);
            logger.warn("搜索词长度大于：{}，将搜索词：{} 截短为：{}", SEARCH_WORD_MAX_LENGTH, temp, keywords);
        }
        if (TextUtil.isAllNonChinese(keywords) && (keywords.contains(" ") || keywords.contains("'"))) {
            List<String> terms = new ArrayList<>();
            if (keywords.contains("'")) {
                for (String term : keywords.split("'")) {
                    terms.add(term);
                }
            } else {
                for (String term : keywords.split("\\s+")) {
                    terms.add(term);
                }
            }
//            query.addKeywordTerms();
        }
        return query;
    }
}
