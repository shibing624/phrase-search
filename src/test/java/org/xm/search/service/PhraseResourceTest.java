package org.xm.search.service;

import org.junit.Test;
import org.xm.search.Search;
import org.xm.search.domain.Document;

import java.util.Map;

/**
 * @author XuMing
 */
public class PhraseResourceTest {
    @Test
    public void loadPhraseText() throws Exception {
        Map<Integer, Document> text = PhraseResource.loadPhraseText();
        System.out.println("count: " + text.size());
    }

    @Test
    public void loadCustomPhraseText() throws Exception {
        Search.Config.PharseTextPath = "data/test-phrase-text.txt";
        Map<Integer, Document> text = PhraseResource.loadPhraseText();
        System.out.println("Custom Phrase Text count: " + text.size());
    }

}