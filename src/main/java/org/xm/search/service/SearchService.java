package org.xm.search.service;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * 短语搜索服务
 *
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
        logger.info("明天全量重建索引调度时间：{}", period);
        // 延迟执行时间
        long delay = ChronoUnit.SECONDS.between(currentTime, period);
        logger.info("延迟执行时间：{}", delay * 1000);
        SCHEDULED_EXECUTOR_SERVICE.scheduleAtFixedRate(() -> {
            long start = System.currentTimeMillis();
            logger.info("开始全量重建索引");
            phraseSearcher.clear();
            phraseSearcher.index(PhraseResource.loadPhraseText());
            logger.info("重建索引完成，用时：{}毫秒", System.currentTimeMillis() - start);
        }, delay, 24 * 60 * 60, TimeUnit.SECONDS);
    }

    public static PhraseSearcher getPhraseSearcher() {
        waitIfNotReady();
        return phraseSearcher;
    }

    public static boolean waitIfNotReady() {
        if (ready) {
            return true;
        }
        try {
            logger.info("等待搜索服务启动。。。");
            countDownLatch.await();
        } catch (InterruptedException e) {
            logger.error("搜索服务启动异常", e);
        } catch (Exception e) {
            logger.error("短语搜索服务异常", e);
        }
        return true;
    }

    public static void startup() {
        startupPhraseSearcher();
        ready = true;
        logger.info("开始搜索索引重建");
        startupUpdateIndexScheduler();
        logger.info("完成搜索索引重建");
        saveIndex();
    }

    private static void startupPhraseSearcher() {
        logger.info("开始启动短语搜索。。。");
        phraseSearcher = new PhraseSearcher();
        phraseSearcher.index(PhraseResource.loadPhraseText());
        logger.info("短语搜索服务启动完成");
        countDownLatch.countDown();
    }

    private static void saveIndex() {
        logger.info("开始保存索引");
        long start = System.currentTimeMillis();
        phraseSearcher.saveIndex();
        logger.info("索引保存完毕，用时：{}毫秒", System.currentTimeMillis() - start);
    }

    public static void destroy() {
        if (phraseSearcher != null) {
            logger.info("关闭短语搜索");
            phraseSearcher.clear();
        }
        List<Runnable> runnableList = SCHEDULED_EXECUTOR_SERVICE.shutdownNow();
        logger.info("停止任务调度：{}", runnableList.toString());
    }
}
