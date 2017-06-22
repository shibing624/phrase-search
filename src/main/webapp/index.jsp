<%--
  Created by IntelliJ IDEA.
  User: xuming
  Date: 2017/6/22
  Time: 13:40
  To change this template use File | Settings | File Templates.
--%>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%
    boolean highlight = !"false".equals(request.getParameter("highlight"));
    String keywords = request.getParameter("keywords") == null ? "深圳万科" : request.getParameter("keywords");
    if ("true".equals(request.getParameter("refresh"))) {
        String key = "PhraseSearcher";
        application.setAttribute(key, null);
    }
    int topN = 10;
    try {
        topN = Integer.parseInt(request.getParameter("topN"));
    } catch (Exception e) {
    }
%>
<html>
<head>
    <script type="text/javascript" src="jquery/jquery-2.2.1.min.js"></script>
    <script type="text/javascript" src="js/utils.js"></script>
    <script type="text/javascript">
        var contextPath = "<%request.getContextPath();%>";
        var searching = false;
        var lastUrl = "";
        function search() {
            if (searching) {
                return;
            }
            searching = true;
            var keywords = document.getElementById("keywords").value;
            if (keywords.length > 30) {
                keywords = keywords.substr(0, 30);
                document.getElementById("keywords").value = keywords;
            }
            var topN = document.getElementById("topN").value;
            var url = contextPath + "/search.jsp?status=true&type=html&topN=" + topN + "&keywords="
                + encodeURIComponent(keywords) + "&highlight=<%=highlight%>";
            if (lastUrl == url) {
                searching = false;
                return;
            }
            document.getElementById("searchResult").innerHTML = "正在搜索...";
            lastUrl = url;
            $.get(url, function (data) {
                document.getElementById("searchResult").innerHTML = data;
                searching = false;
            }).fail(function (e) {
                Utils.log("取数据出错：" + JSON.stringify(e));
                searching = false;
            })
        }

    </script>
    <title>短语搜索</title>
</head>
<body onload="search();">
<p>
    搜索结果数：<input id="topN" value="<%=topN%>" onkeyup="search();"><br/>
    搜索关键词：<input id="keywords" value="<%=keywords%>" size="80" onkeyup="search();">
</p>

<div id="searchResult"></div>
<br/>
<br/>
<h3><a target="_blank" href="caches.jsp">查看短语搜索缓存命中情况</a></h3>
<h3><a target="_blank" href="search-history.jsp">查看短语搜索历史记录</a></h3>
<h3><a target="_blank" href="search-history.jsp?clear=true">清除短语搜索历史记录</a></h3>
<br/>
</body>
</html>
