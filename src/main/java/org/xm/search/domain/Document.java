package org.xm.search.domain;

import java.util.HashSet;
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
    public Document(){}
    public Document(int id,String value){

    }
    @Override
    public int compareTo(Object o) {
        return 0;
    }
}
