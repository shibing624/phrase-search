package org.xm.search.service;

import org.junit.Test;
import org.xm.search.Search;
import org.xm.search.domain.Document;

import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;

/**
 * @author XuMing
 */
public class CustomPhraseSearchTest {
    @Test
    public void searchCustomPhrase() throws Exception {
        PhraseSearcher phraseSearcher = new PhraseSearcher();
        Search.Config.PharseTextPath = "data/test-phrase-text.txt";
        phraseSearcher.index(PhraseResource.loadPhraseText());
        List<Document> actualValue = phraseSearcher.search("TX", 1, false).getDocuments();
        assertEquals(1, actualValue.size());
        assertEquals(6, actualValue.get(0).getId());
        System.out.println(actualValue);
        assertEquals("腾讯", actualValue.get(0).getValue());
        System.out.println(phraseSearcher.getSearchHistories());
    }

    @Test
    public void loadCustomPhraseText() throws Exception {
        Search.Config.PharseTextPath = "data/test-phrase-text.txt";
        Map<Integer, Document> text = PhraseResource.loadPhraseText();
        System.out.println("Custom Phrase Text count: " + text.size());
    }
}
