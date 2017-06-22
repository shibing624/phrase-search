# phrase-search
短语搜索，支持公司名称、地址名称等短语的搜索，支持自定义排序、拼音处理，内置jetty提供web接口。java编写。


## 使用方法

1. git clone https://github.com/shibing624/phrase-search
2. cd phrase-search

       unix类操作系统执行：
            chmod +x startup.sh & ./startup.sh
            
       windows类操作系统执行：
            ./startup.bat
            
3. 打开浏览器访问: http://localhost:8080/index.jsp
4. JSON格式的API接口: http://localhost:8080/search-api.jsp?kw=%E6%B7%B1%E5%9C%B3%E8%85%BE%E8%AE%AF&topN=10&highlight=true

    
