<%@ page import="org.xm.search.domain.Document" %>
<%@ page import="org.xm.search.domain.SearchResult" %>
<%@ page import="org.xm.search.service.PhraseSearcher" %>
<%@ page import="org.xm.search.service.SearchService" %>
<%@ page import="org.xm.search.util.TimeUtil" %>
<%@ page import="java.net.URLEncoder" %>
<%@ page import="java.util.List" %>
<%--
  Created by IntelliJ IDEA.
  User: xuming
  Date: 2017/6/22
  Time: 14:17
  To change this template use File | Settings | File Templates.
--%>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%
    boolean status = "true".equals(request.getParameter("status"));
    PhraseSearcher searcher = SearchService.getPhraseSearcher();
    if (searcher == null) {
        out.println("search service unavailable");
        return;
    }
    boolean highlight = "true".equals(request.getParameter("highlight"));
    String keywords = request.getParameter("keywords") == null ? "深圳万科" : request.getParameter("keywords");
    String id = request.getParameter("id");
    if ("true".equals(request.getParameter("explain")) && id != null) {
//        String explain = searcher.explain(keywords, Integer.parseInt(id)).replace("\n", "<br/>").replace("\t", "&nbsp&nbsp&nbsp&nbsp");
//        out.println(explain);
        return;
    }
    int topN = 100;
    try {
        topN = Integer.parseInt(request.getParameter("topN"));
    } catch (Exception e) {
    }
    if ("html".equals(request.getParameter("type"))) {
        long start = System.currentTimeMillis();
        SearchResult searchResult = searcher.search(keywords, topN, highlight);
        List<Document> documents = searchResult.getDocuments();
        long cost = System.currentTimeMillis() - start;
        out.println("搜索接口总耗时: " + TimeUtil.getTimeDes(cost));
        StringBuilder html = new StringBuilder();
        html.append("搜索耗时: ")
                .append(TimeUtil.getTimeDes(cost))
                .append("<br/>")
                .append("结果条数: ")
                .append(documents.size())
                .append("<br/>");
        html.append("<table border=\"1\">")
                .append("<tr><th>序号</th><th>ID</th><th>短文本</th><th>搜索评分</th></tr>\n");
        int i = 1;
        for (Document document : documents) {
            html.append("<tr>")
                    .append("<td>")
                    .append(i++)
                    .append("</td>")
                    .append("<td>")
                    .append(document.getId())
                    .append("</td>")
                    .append("<td>")
                    .append(document.getValue())
                    .append("</td>")
                    .append("<td>")
                    .append("<a target=\"_blank\" href=\"search.jsp?explain=true&id=")
                    .append(document.getId())
                    .append("&keywords=")
                    .append(URLEncoder.encode(keywords, "utf-8"))
                    .append("\">")
                    .append(document.getScore())
                    .append("</a>")
                    .append("</td>")
                    .append("</tr>");
        }
        html.append("</table>");
        if (status) {
            String indexStatus = searcher.getIndexStatus();
            if (indexStatus != null) {
                html.append("<br/><font color=\"red\">索引状态</font><br/>").append(indexStatus.replace("\n", "<br/>").replace("\t", "&nbsp&nbsp&nbsp&nbsp"));
            }
            String searchStatus = searcher.getSearchStatus();
            if (searchStatus != null) {
                html.append("<br/><font color=\"red\">搜索状态</font><br/>").append(searchStatus.replace("\n", "<br/>").replace("\t", "&nbsp&nbsp&nbsp&nbsp"));
            }
            String cacheStatus = searcher.getCacheStatus();
            if (cacheStatus != null) {
                html.append("<br/><font color=\"red\">缓存状态</font><br/>").append(cacheStatus.replace("\n", "<br/>").replace("\t", "&nbsp&nbsp&nbsp&nbsp"));
            }
        }
        out.println(html);
    } else {
        response.setContentType("application/json;charset=UTF-8");
        response.setHeader("Access-Control-Allow-Origin", "*");
        long start = System.currentTimeMillis();
        SearchResult searchResult = searcher.search(keywords, topN, highlight);
        List<Document> documents = searchResult.getDocuments();
        long cost = System.currentTimeMillis() - start;
        out.println("搜索接口总耗时: {} " + TimeUtil.getTimeDes(cost));
        StringBuilder json = new StringBuilder();
        json.append("{\n")
                .append("\"cost\":\"")
                .append(TimeUtil.getTimeDes(cost))
                .append("\",\n")
                .append(",\n")
                .append("\"size\":")
                .append(documents.size());

        if (searchResult.isOverload()) {
            json.append(",\n")
                    .append("\"message\":\"")
                    .append("search service overload")
                    .append("\",\n");
        }

        if (status) {
            String indexStatus = searcher.getIndexStatus();
            if (indexStatus != null) {
                json.append(",\n\"indexStatus\":\n\"");
                json.append(indexStatus.replace("\n", "; "))
                        .append("\"");
            }
            String searchStatus = searcher.getSearchStatus();
            if (searchStatus != null) {
                json.append(",\n\"searchStatus\":\n\"");
                json.append(searchStatus.replace("\n", "; "));
            }
        }
        json.append("\",\n\"result\":\n[");
        for (Document document : documents) {
            json.append("{")
                    .append("\"id\":")
                    .append(document.getId())
                    .append(",")
                    .append("\"value\":\"")
                    .append(document.getValue())
                    .append("\",")
                    .append("\"score\":")
                    .append(document.getScore())
                    .append("},\n");
        }
        if (documents.size() > 0) {
            json.setLength(json.length() - 2);
        }
        json.append("]\n}\n");
        out.println(json);
    }
%>