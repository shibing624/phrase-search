package org.xm.search.domain;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * 搜索文档，可排序
 *
 * @author XuMing
 */
public class Document implements Comparable {
    private int id;
    private String value;
    private Set<String> terms = new HashSet<>();
    private int score;

    public Document() {
    }

    public Document(int id, String value) {
        this.id = id;
        this.value = value;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public Set<String> getTerms() {
        return terms;
    }

    public void addTerms(List<String> terms) {
        this.terms.addAll(terms);
    }

    public void setTerms(Set<String> terms) {
        this.terms = terms;
    }

    public int getScore() {
        return score;
    }

    public void setScore(int score) {
        this.score = score;
    }

    public Document clone() {
        Document document = new Document();
        document.id = id;
        document.value = value;
        document.terms = terms;
        document.score = score;
        return document;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Document document = (Document) o;
        return id == document.id;
    }

    @Override
    public int hashCode() {
        return id ^ (id >>> 32);
    }

    @Override
    public String toString() {
        return "Document{" +
                "id=" + id +
                ", value='" + value + '\'' +
                ", terms=" + terms +
                ", score=" + score +
                "}";
    }

    @Override
    public int compareTo(Object o) {
        return Integer.valueOf(id).compareTo(((Document) o).getId());
    }
}
