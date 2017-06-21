package org.xm.search.service;

import org.junit.Test;
import org.xm.search.Search;

/**
 * 自定义短语搜索测试
 *
 * @author XuMing
 */
public class SaveSearcherIndexTest {

    @Test
    public void saveIndex() throws Exception {
        PhraseSearcher phraseSearcher = new PhraseSearcher();
        Search.Config.PharseTextPath = "data/test-phrase-text.txt";
        phraseSearcher.index(PhraseResource.loadPhraseText());
        phraseSearcher.saveIndex();
    }

}
