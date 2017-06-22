package org.xm.search.service;

import org.junit.Test;
import org.xm.search.domain.Document;

import java.util.List;

import static org.junit.Assert.assertEquals;

/**
 * @author XuMing
 */
public class PhraseSearcherTest {

    @Test
    public void search1() throws Exception {
        PhraseSearcher searcher = new PhraseSearcher();
        searcher.index(PhraseResource.loadPhraseText());
        String keyword = "腾讯";
        int id = 70671; // expected
        List<Document> actualValue = searcher.search(keyword, 1).getDocuments();
        assertEquals(1, actualValue.size());
        assertEquals(id, actualValue.get(0).getId());
        String expectedName = "<font color='red'>" + keyword + "</font>";
        assertEquals(expectedName, actualValue.get(0).getValue());

        actualValue = searcher.search(keyword, 1, false).getDocuments();
        expectedName = keyword;
        assertEquals(expectedName, actualValue.get(0).getValue());
        System.out.println(searcher.getSearchHistories());
        System.out.println(searcher.getCacheStatus());
        System.out.println(searcher.getSearchStatus());
        System.out.println(searcher.getIndexStatus());
        System.out.println(searcher.getKeyAndHitCount());
    }

    @Test
    public void search2() throws Exception {
        // 不启用搜索结果缓存功能
        PhraseSearcher searcher = new PhraseSearcher(false);
        List<Document> actualValue = searcher.search("哈利波特魔法屋", 1, false).getDocuments();
        assertEquals(0, actualValue.size());

        Document document = new Document();
        document.setId(-100);
        document.setValue("哈利波特魔法屋");
        // 创建索引
        searcher.createIndex(document);
        searcher.saveIndex();

        actualValue = searcher.search("哈利波特魔法屋", 1, false).getDocuments();
        System.out.println(actualValue);
        assertEquals(1, actualValue.size());

        // 删除索引
        searcher.deleteIndex(document.getId());
        actualValue = searcher.search("哈利波特魔法屋", 1, false).getDocuments();
        assertEquals(0, actualValue.size());

    }

    /**
     * 单独执行
     *
     * @throws Exception
     */
    @Test
    public void search3() throws Exception {
        // 不启用搜索结果缓存功能
        PhraseSearcher searcher = new PhraseSearcher(false);
        searcher.index(PhraseResource.loadPhraseText());
        List<Document> actualValue = searcher.search("a d 视觉", 5, false).getDocuments();
        System.out.println(actualValue);

        actualValue = searcher.search("艾的视觉", 5, false).getDocuments();
        System.out.println(actualValue);

        actualValue = searcher.search("艾的", 5, false).getDocuments();
        System.out.println(actualValue);

        // 更新索引后再搜索，必须关闭缓存功能，不然会命中缓存
        Document document = new Document(-1, "艾的");
        searcher.updateIndex(document);
        actualValue = searcher.search("艾的", 5, false).getDocuments();
        System.out.println(actualValue);

    }

}