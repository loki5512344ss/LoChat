package com.loki.lochat.core.filter;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.LongAdder;

/**
 * Счётчики заблокированных сообщений по каждому фильтру (с момента запуска / сброса).
 */
public final class FilterStatistics {

    private final Map<String, LongAdder> blocked = new ConcurrentHashMap<>();

    public void increment(String filterType) {
        blocked.computeIfAbsent(filterType, k -> new LongAdder()).increment();
    }

    public long getBlocked(String filterType) {
        LongAdder counter = blocked.get(filterType);
        return counter == null ? 0L : counter.sum();
    }

    public long getTotalBlocked() {
        return blocked.values().stream().mapToLong(LongAdder::sum).sum();
    }

    /**
     * Снимок счётчиков, отсортированный по убыванию (заблокировавших больше всего — первыми).
     */
    public Map<String, Long> snapshot() {
        return blocked.entrySet().stream()
                .sorted(Map.Entry.<String, LongAdder>comparingByValue(
                        (a, b) -> Long.compare(b.sum(), a.sum())))
                .collect(LinkedHashMap::new,
                        (m, e) -> m.put(e.getKey(), e.getValue().sum()),
                        LinkedHashMap::putAll);
    }

    public void reset() {
        blocked.clear();
    }
}