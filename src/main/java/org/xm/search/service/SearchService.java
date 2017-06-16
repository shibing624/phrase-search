package org.xm.search.service;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.time.LocalDateTime;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

/**
 * @author XuMing
 */
public class SearchService {
    /**
     * 日志组件
     */
    private static Logger logger = LogManager.getLogger();

    private static PhraseSearcher phraseSearcher = null;
    private static CountDownLatch countDownLatch = new CountDownLatch(1);
    private static volatile boolean ready = false;
    private static final ScheduledExecutorService SCHEDULED_EXECUTOR_SERVICE = Executors.newScheduledThreadPool(1);

    private static void startupUpdateIndexScheduler() {
        LocalDateTime currentTime = LocalDateTime.now();
        // 每天调度时间差
        LocalDateTime period = currentTime.plusDays(currentTime.isBefore(currentTime.withHour(3).withMinute(30).withSecond(0)) ? 0 : 1).withHour(3).withMinute(30).withSecond(0);
        logger.info("现在时间：{}", currentTime);
    }
    public static void startup() {

    }

    public static void destroy() {

    }
}
