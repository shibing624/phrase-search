package org.xm.search.service;

import org.junit.Test;
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

}