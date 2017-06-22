<%@ page import="org.xm.search.domain.Document" %>
<%@ page import="org.xm.search.domain.SearchResult" %>
<%@ page import="org.xm.search.service.PhraseSearcher" %>
<%@ page import="org.xm.search.service.SearchService" %>
<%@ page import="org.xm.search.util.TimeUtil" %>
<%@ page import="java.util.List" %>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%
    response.setContentType("application/json;charset=UTF-8");
    response.setHeader("Access-Control-Allow-Origin", "*");

    PhraseSearcher searcher = SearchService.getPhraseSearcher();
    if (searcher == null) {
        StringBuilder json = new StringBuilder();
        json.append("[]");
        out.println(json.toString());
        return;
    }
    boolean highlight = "true".equals(request.getParameter("highlight"));
    String keywords = request.getParameter("kw") == null ? "万科" : request.getParameter("kw");
    int topN = 5;
    try {
        topN = Integer.parseInt(request.getParameter("topN"));
    } catch (Exception e) {
    }

    long start = System.currentTimeMillis();
    SearchResult searchResult = searcher.search(keywords, topN, highlight);
    List<Document> documents = searchResult.getDocuments();
    long cost = System.currentTimeMillis() - start;
    out.println("搜索接口总耗时: " + TimeUtil.getTimeDes(cost));

    StringBuilder json = new StringBuilder();
    json.append("[\n");

    int i = 1;
    for (Document document : documents) {
        json.append("{")
                .append("\"id\":")
                .append(document.getId())
                .append(",")
                .append("\"value\":\"")
                .append(document.getValue())
                .append("\"}");
        if (i++ < documents.size()) {
            json.append(",\n");
        }
    }
    json.append("\n]");
    out.println(json);
%>