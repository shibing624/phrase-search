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
    private String flag = "unknown";
    private boolean overload;

    public List<Document> getDocuments() {
        return documents;
    }

    public void setDocuments(List<Document> documents) {
        this.documents = documents;
    }

    public String getFlag() {
        return flag;
    }

    public void setFlag(String flag) {
        this.flag = flag;
    }

    public boolean isOverload() {
        return overload;
    }

    public void setOverload(boolean overload) {
        this.overload = overload;
    }
}
