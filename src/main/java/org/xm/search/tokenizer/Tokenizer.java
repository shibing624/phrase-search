package org.xm.search.tokenizer;

import com.hankcs.hanlp.HanLP;
import com.hankcs.hanlp.seg.common.Term;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author XuMing
 */
public class Tokenizer {
    public static List<String> segment(String sentence) {
        List<String> results = new ArrayList<>();
        // HanLP
        List<Term> termList = HanLP.segment(sentence);
        results.addAll(termList
                .stream()
                .map(term -> term.word)
                .collect(Collectors.toList())
        );
        return results;
    }
}
