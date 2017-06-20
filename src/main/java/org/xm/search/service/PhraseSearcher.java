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
import org.xm.search.util.TimeUtil;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

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
    private AtomicLong currentProcessSearchCount = new AtomicLong();
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
                .append("搜索时间占比：").append(totalSearchTime.get() / (float) (System.currentTimeMillis() -
                searchServiceStartupTime) * 100).append("%\n")
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

    /**
     * 根据关键词取Query解析结果
     *
     * @param keywords 关键词
     * @return Query
     */
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
            query.addKeywordTerms(terms);
            return query;
        }
        if (!query.hasPinyin() || isAllUpperCase) {
            List<String> keywordTerms = Tokenizer.segment(keywords);
            query.addKeywordTerms(keywordTerms);
        }
        return query;
    }

    /**
     * 搜索核心方法
     *
     * @param keywords  关键词
     * @param topN      搜索词前N个
     * @param highlight 是否高亮
     * @return SearchResult
     */
    public SearchResult search(String keywords, int topN, boolean highlight) {
        if (searchHistories.size() > 1000) {
            searchHistories.clear();
        }
        searchHistories.putIfAbsent(keywords, new AtomicInteger());
        searchHistories.get(keywords).incrementAndGet();

        String key = TimeUtil.toString(System.currentTimeMillis(), "yyyyMMdd");
        searchCountEveryDay.putIfAbsent(key, new AtomicInteger());
        searchCountEveryDay.get(key).incrementAndGet();

        String identity = searchCount.incrementAndGet() + "-" + SEARCH_MAX_CONCURRENT;
        // 控制并发请求数量
        if (currentProcessSearchCount.incrementAndGet() > SEARCH_MAX_CONCURRENT) {
            SearchResult searchResult = new SearchResult();
            searchResult.setOverload(true);
            logger.info("并发降级，当前并发请求数量：{} 超过系统预设能承受的负载：{} {}",
                    currentProcessSearchCount.get(), SEARCH_MAX_CONCURRENT, identity);
            currentProcessSearchCount.decrementAndGet();
            return searchResult;
        }
        // 缓存
        String cacheKey = keywords + "_" + topN + "_" + highlight;
        if (cacheEnabled) {
            SearchResult result = cache.get(cacheKey);
            if (result != null) {
                logger.info("搜索命中缓存：{}，topN：{}，highlight：{} {}", keywords, topN, highlight, identity);
                currentProcessSearchCount.decrementAndGet();
                return result;
            }
        }
        SearchResult searchResult = new SearchResult();
        searchResult.setId(identity);
        if (topN > TOPN_MAX_LENGTH) {
            logger.warn("topN：{} 大于 {}，限制为{} {}", topN, TOPN_MAX_LENGTH, TOPN_MAX_LENGTH, identity);
            topN = TOPN_MAX_LENGTH;
        }
        logger.info("搜索关键词：{}，topN：{}，highlight：{} {}", keywords, topN, highlight, identity);
        long start = System.currentTimeMillis();
        Query query = parse(keywords);
        long cost = System.currentTimeMillis() - start;
        totalSearchTime.addAndGet(cost);
        if (maxSearchTime.get() < cost) {
            maxSearchTime.set(cost);
        }
        logger.info("{} 查询解析耗时：{} {}", cost, TimeUtil.getTimeDes(cost), identity);
        if (query.isEmpty()) {
            currentProcessSearchCount.decrementAndGet();
            return searchResult;
        }
        logger.info("查询结构：{} {}", query.getKeywordTerms(), identity);

        start = System.currentTimeMillis();
        Map<Integer, AtomicInteger> hits = new ConcurrentHashMap<>();
        // 收集并初始化文档分数
        query.getKeywordTerms().parallelStream().forEach(keywordTerm -> {
            Set<Integer> indexIds = INVERTED_INDEX.get(keywordTerm);
            if (indexIds != null) {
                Set<Integer> deletedIndexIds = new HashSet<>();
                for (int indexId : indexIds) {
                    Integer documentId = INDEX_TO_DOCUMENT.get(indexId);
                    if (documentId == null) {
                        deletedIndexIds.add(indexId);
                        continue;
                    }
                    Document document = DOCUMENT.get(documentId);
                    if (document != null) {
                        hits.putIfAbsent(documentId, new AtomicInteger());
                        hits.get(documentId).addAndGet(keywordTerm.length());
                    } else {
                        logger.error("没有ID是：{} 的文档 {}", documentId, identity);
                    }
                }
                indexIds.removeAll(deletedIndexIds);
            }
        });
        // 限制文档数
        int limitedDocCount = topN * 10 < 1000 ? 1000 : topN * 10;
        Map<Integer, AtomicInteger> limitedDocs = new ConcurrentHashMap<>();
        hits.entrySet().parallelStream()
                .sorted((a, b) -> b.getValue().intValue() - a.getValue().intValue())
                .limit(limitedDocCount)
                .forEach(i -> limitedDocs.put(i.getKey(), i.getValue()));
        cost = System.currentTimeMillis() - start;
        totalSearchTime.addAndGet(cost);
        if (maxSearchTime.get() < cost) {
            maxSearchTime.set(cost);
        }
        logger.info("{} 搜索耗时：{} {}", cost, TimeUtil.getTimeDes(cost), identity);
        logger.info("搜索到的结果文档数：{}，总的文档数：{}，搜索结果占总文档的比例：{} %，限制后的搜索结果数：{}，"
                        + "限制后的搜索结果占总文档的比例：{} % {}",
                hits.size(), DOCUMENT.size(), hits.size() / (float) DOCUMENT.size() * 100, limitedDocs.size(),
                limitedDocs.size() / (float) DOCUMENT.size() * 100, identity);
        start = System.currentTimeMillis();
        String finalKeywords = keywords.trim().toLowerCase();
        // 文档得分
        Map<Integer, Integer> scores = new ConcurrentHashMap<>();
        limitedDocs.entrySet().parallelStream().forEach(i -> {
            int documentId = i.getKey();
            int score = i.getValue().get();
            Document doc = DOCUMENT.get(documentId);
            String value = doc.getValue();
            if (finalKeywords.equals(value.trim().toLowerCase())
                    || TextUtil.normalize(finalKeywords).equals(TextUtil.normalize(value))) {
                score += value.length();
            }
            if (TextUtil.isAllNonChinese(keywords) && !query.hasPinyin()) {
                int param = finalKeywords.length() - value.length();
                if (param != 0) {
                    score += param;
                }
            }
            if (TextUtil.isAllNonChinese(keywords)) {
                String chineseValue = TextUtil.extractChinese(value);
                String acronymPinyin = Tokenizer.getPinyin(chineseValue, "", true);
                String fullPinyin = Tokenizer.getPinyin(chineseValue, "", false);
                if (finalKeywords.equals(acronymPinyin)) {
                    score += finalKeywords.length();
                }
                if (finalKeywords.equals(fullPinyin)) {
                    score += finalKeywords.length();
                }
            }
            scores.put(documentId, score);
        });

        cost = System.currentTimeMillis() - start;
        totalSearchTime.addAndGet(cost);
        if (maxSearchTime.get() < cost) {
            maxSearchTime.set(cost);
        }
        logger.info("{} 评分耗时：{} {}", cost, TimeUtil.getTimeDes(cost), identity);
        start = System.currentTimeMillis();

        // 排序并限制文档数
        List<Document> result = scores.entrySet().parallelStream()
                .map(i -> {
                    Document document = DOCUMENT.get(i.getKey()).clone();
                    document.setScore(i.getValue().intValue());
                    return document;
                })
                .sorted((a, b) -> {
                    int temp = b.getScore() - a.getScore();
                    if (temp == 0) {
                        temp = Long.valueOf(a.getId()).compareTo(Long.valueOf(b.getId()));
                    }
                    return temp;
                })
                .limit(topN).collect(Collectors.toList());
        cost = System.currentTimeMillis() - start;
        totalSearchTime.addAndGet(cost);
        if (maxSearchTime.get() < cost) {
            maxSearchTime.set(cost);
        }
        logger.info("{} 排序耗时：{} {}", cost, TimeUtil.getTimeDes(cost), identity);
        if (highlight && !TextUtil.isAllNonChinese(keywords)) {
            // 高亮
            start = System.currentTimeMillis();
            highlight(result, keywords, query.getKeywordTerms());
            cost = System.currentTimeMillis() - start;
            totalSearchTime.addAndGet(cost);
            if (maxSearchTime.get() < cost) {
                maxSearchTime.set(cost);
            }
            logger.info("{} 高亮耗时：{} {}", TimeUtil.getTimeDes(cost), identity);
        }
        searchResult.setDocuments(result);
        currentProcessSearchCount.decrementAndGet();
        if (cacheEnabled) {
            cache.put(cacheKey, searchResult);
        }
        return searchResult;
    }

    public static void main(String[] args) {
        PhraseSearcher searcher = new PhraseSearcher();
        searcher.index(PhraseResource.loadPhraseText());

        AtomicInteger i = new AtomicInteger();
        searcher.search("阿里", 500, true)
                .getDocuments()
                .forEach(doc -> System.out.println(i.incrementAndGet() + ". " + doc.getValue() + " " + " (" + doc.getScore() + ")"));
    }
}
