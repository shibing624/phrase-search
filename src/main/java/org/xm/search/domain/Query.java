package org.xm.search.domain;

import java.util.ArrayList;
import java.util.List;

/**
 * 查询
 *
 * @author XuMing
 */
public class Query {
    private List<String> keywordTerms = new ArrayList<>();
    private boolean hasPinyin;

    public boolean isEmpty() {
        return keywordTerms.isEmpty();
    }

    public boolean containsKeywordTerms(String term) {
        return keywordTerms.contains(term);
    }

    public List<String> getKeywordTerms() {
        return keywordTerms;
    }

    public void addKeywordTerm(String term) {
        if (!containsKeywordTerms(term)) {
            this.keywordTerms.add(term);
        }
    }

    public void addKeywordTerms(List<String> keywordTerms) {
        for (String term : keywordTerms) {
            addKeywordTerm(term);
        }
    }

    public boolean hasPinyin() {
        return hasPinyin;
    }

    public void hasPinyin(boolean hasPinyin) {
        this.hasPinyin = hasPinyin;
    }
}
