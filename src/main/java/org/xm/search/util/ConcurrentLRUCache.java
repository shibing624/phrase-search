package org.xm.search.util;

import java.util.Collections;
import java.util.Comparator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 缓存淘汰算法
 * LRU（Least recently used，最近最少使用）算法根据数据的历史访问记录来进行淘汰数据，其核心思想是“如果数据最近被访问过，
 * 那么将来被访问的几率也更高”。
 *
 * @author XuMing
 */
public class ConcurrentLRUCache<K, V> {
    private static class CacheItem<V> {
        private V value;
        private AtomicInteger count;

        public CacheItem(V value, AtomicInteger count) {
            this.value = value;
            this.count = count;
        }

        public void hit() {
            this.count.incrementAndGet();
        }

        public V getValue() {
            return value;
        }

        public int getCount() {
            return count.get();
        }
    }

    private int maxCacheSize;
    private Map<K, CacheItem<V>> cache = new ConcurrentHashMap<>();
    private AtomicLong totalRemoveCount = new AtomicLong();
    private AtomicLong hitCount = new AtomicLong();
    private AtomicLong notHitCount = new AtomicLong();

    public int getMaxCacheSize() {
        return maxCacheSize;
    }

    public Map<K, CacheItem<V>> getCache() {
        return Collections.unmodifiableMap(cache);
    }

    public int getActualCacheSize() {
        return cache.size();
    }

    public AtomicLong getTotalRemoveCount() {
        return totalRemoveCount;
    }

    public AtomicLong getHitCount() {
        return hitCount;
    }

    public AtomicLong getNotHitCount() {
        return notHitCount;
    }

    public ConcurrentLRUCache(int maxCacheSize) {
        cache = new ConcurrentHashMap<K, CacheItem<V>>(maxCacheSize, 1, 10);
        this.maxCacheSize = maxCacheSize;
    }

    public void clear() {
        cache.clear();
        totalRemoveCount.set(0);
        hitCount.set(0);
        notHitCount.set(0);
    }

    public String getStatus() {
        StringBuilder status = new StringBuilder();
        long total = hitCount.get() + notHitCount.get();
        status.append("最大缓存数：").append(maxCacheSize).append("\n")
                .append("当前缓存数量：").append(getActualCacheSize()).append("\n")
                .append("删除缓存次数：").append(totalRemoveCount.get()).append("\n")
                .append("命中缓存次数：").append(hitCount.get()).append("\n")
                .append("未命中缓存次数：").append(notHitCount.get()).append("\n")
                .append("缓存命中比例:").append(total == 0 ? 0 : hitCount.get() / (float) total * 100).append(" %\n");
        return status.toString();
    }

    public String getKeyAndHitCount() {
        StringBuilder status = new StringBuilder();
        AtomicInteger i = new AtomicInteger();
        cache.entrySet().stream()
                .sorted((a, b) -> b.getValue().getCount() - a.getValue().getCount())
                .forEach(e -> status
                        .append(i.incrementAndGet()).append("\t")
                        .append(e.getKey()).append("\t")
                        .append(e.getValue().getCount()).append("\n"));
        return status.toString();
    }

    public void put(K key, V value) {
        if (cache.size() >= maxCacheSize) {
            int temp = (int) (maxCacheSize * 0.1);
            if (temp < 1) {
                temp = 1;
            }
            totalRemoveCount.addAndGet(temp);
            cache.entrySet().stream()
                    .sorted(Comparator.comparingInt(a -> a.getValue().getCount()))
                    .limit(temp)
                    .forEach(e -> cache.remove(e.getKey()));
            return;
        }
        cache.put(key, new CacheItem<>(value, new AtomicInteger()));
    }

    public V get(K key) {
        CacheItem<V> item = cache.get(key);
        if (item != null) {
            item.hit();
            hitCount.incrementAndGet();
            return item.getValue();
        }
        notHitCount.incrementAndGet();
        return null;
    }

    public static void main(String[] args) {
        ConcurrentLRUCache<Integer, Integer> cache = new ConcurrentLRUCache<>(4);
        for (int i = 0; i < 9; i++) {
            cache.put(i, i);
            for (int j = 0; j < i + 2; j++) {
                cache.get(i);
            }
        }
        System.out.println(cache.getStatus());
        System.out.println(cache.getKeyAndHitCount());
    }
}
