package org.xm.search.domain;

import java.util.ArrayList;
import java.util.List;

/**
 * 搜索结果
 *
 * @author XuMing
 */
public class SearchResult {
    private List<Document> documents = new ArrayList<>();
    private String id = "unknown";
    private boolean overload;

    public List<Document> getDocuments() {
        return documents;
    }

    public void setDocuments(List<Document> documents) {
        this.documents = documents;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public boolean isOverload() {
        return overload;
    }

    public void setOverload(boolean overload) {
        this.overload = overload;
    }
}
