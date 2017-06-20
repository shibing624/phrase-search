package org.xm.search.tokenizer;

import com.hankcs.hanlp.HanLP;
import com.hankcs.hanlp.dictionary.py.Pinyin;
import com.hankcs.hanlp.dictionary.py.PinyinDictionary;
import com.hankcs.hanlp.seg.common.Term;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 分词器
 *
 * @author XuMing
 */
public class Tokenizer {
    /**
     * 中文分词
     *
     * @param sentence 待分词文本
     * @return 分词结果列表
     */
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

    public static String getPinyin(String text, String separator, boolean acronymPinyin) {
        List<Pinyin> pinyinList = PinyinDictionary.convertToPinyin(text, true);
        int length = pinyinList.size();
        StringBuilder sb = new StringBuilder(length * (5 + separator.length()));
        int i = 1;
        for (Pinyin pinyin : pinyinList) {
            if (acronymPinyin) {
                sb.append(pinyin.getHead());
            } else {
                if (pinyin == Pinyin.none5) {
                    sb.append(text.charAt(i - 1));
                } else {
                    sb.append(pinyin.getPinyinWithoutTone());
                }
                if (i < length) {
                    sb.append(separator);
                }
                ++i;
            }
        }
        return sb.toString();
    }

}
