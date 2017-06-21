package org.xm.search.service;

import org.junit.Test;
import org.xm.search.Search;
import org.xm.search.domain.Document;

import java.util.List;

import static org.junit.Assert.assertEquals;

/**
 * @author XuMing
 */
public class PhraseSearcherTest {
    PhraseSearcher phraseSearcher = new PhraseSearcher();

    @Test
    public void parse() throws Exception {
    }

    private void search(PhraseSearcher searcher, String keyword, int id) {
        List<Document> actualValue = phraseSearcher.search(keyword, 1).getDocuments();
        assertEquals(1, actualValue.size());
        assertEquals(id, actualValue.get(0).getId());
        String expectedName = "<font color='red'>" + keyword + "</font>";
        assertEquals(expectedName, actualValue.get(0).getValue());

        actualValue = phraseSearcher.search(keyword, 1, false).getDocuments();
        expectedName = keyword;
        assertEquals(expectedName, actualValue.get(0).getValue());
    }

    @Test
    public void search1() throws Exception {
        phraseSearcher.index(PhraseResource.loadPhraseText());
        search(phraseSearcher, "深圳俊友", 54945);
        search(phraseSearcher, "腾讯", 70671);
        System.out.println(phraseSearcher.getSearchHistories());
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

        searcher.createIndex(document);
        searcher.saveIndex();

        actualValue = searcher.search("哈利波特魔法屋", 1, false).getDocuments();
        System.out.println(actualValue);
        assertEquals(1, actualValue.size());

    }

    @Test
    public void searchCustomPhrase() throws Exception {
        Search.Config.PharseTextPath = "data/test-phrase-text.txt";
        phraseSearcher.index(PhraseResource.loadPhraseText());
        List<Document> actualValue = phraseSearcher.search("TX", 1, false).getDocuments();
        assertEquals(1, actualValue.size());
        assertEquals(6, actualValue.get(0).getId());
        System.out.println(actualValue);
        assertEquals("腾讯", actualValue.get(0).getValue());
        System.out.println(phraseSearcher.getSearchHistories());
    }

}