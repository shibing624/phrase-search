<%@ page import="org.apache.commons.lang3.StringUtils" %>
<%@ page import="org.xm.search.service.PhraseSearcher" %>
<%@ page import="org.xm.search.service.SearchService" %>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%
    PhraseSearcher searcher = SearchService.getPhraseSearcher();
    if (searcher == null) {
        out.println("search service unavailable");
        return;
    }
%>
<html>
<head>
    <title>短语搜索缓存命中情况</title>
    <!-- jquery -->
    <script type="text/javascript" src="jquery/jquery-2.2.1.min.js"></script>
    <script type="text/javascript" src="js/utils.js"></script>
    <script type="text/javascript">
        var contextPath = '<%=request.getContextPath()%>';
    </script>
</head>
<body>
<%
    String keyAndHitCount = searcher.getKeyAndHitCount();
    if (StringUtils.isNotBlank(keyAndHitCount)) {
        String[] lines = keyAndHitCount.split("\\n");
%>
<p>
<h3>短语搜索缓存命中情况(<%=lines.length%>)</h3>
<table border="1">
    <tr>
        <th>序号</th>
        <th>搜索关键词</th>
        <th>缓存命中次数</th>
    </tr>
    <%
        for (String line : lines) {
            String[] attr = line.split("\\t");
    %>
    <tr>
        <td><%=attr[0]%>
        </td>
        <td><a target="_blank" href="index.jsp?topN=5&keywords=<%=attr[1].split("_")[0]%>"><%=attr[1].split("_")[0]%>
        </a></td>
        <td><%=attr[2]%>
        </td>
    </tr>
    <%
        }
    %>
</table>
</p>
<%
    } else {
        out.print("还没有缓存命中记录");
    }
%>
</body>
</html>
