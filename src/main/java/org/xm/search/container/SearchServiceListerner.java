package org.xm.search.container;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.xm.search.service.SearchService;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import java.time.LocalDateTime;

/**
 * 搜索服务监听器
 *
 * @author XuMing
 */
public class SearchServiceListerner implements ServletContextListener {
    /**
     * 日志组件
     */
    private static Logger logger = LogManager.getLogger();

    @Override
    public void contextInitialized(ServletContextEvent servletContextEvent) {
        new Thread(() -> SearchService.startup()).start();
        logger.info("phrase search server started at " + LocalDateTime.now());
    }

    @Override
    public void contextDestroyed(ServletContextEvent servletContextEvent) {
        SearchService.destroy();
    }
}
