package com.loki.lochat.core.filter;

import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FilterStatisticsTest {

    @Test
    void increment_countsPerFilter() {
        FilterStatistics stats = new FilterStatistics();
        stats.increment("swear");
        stats.increment("swear");
        stats.increment("flood");

        assertEquals(2, stats.getBlocked("swear"));
        assertEquals(1, stats.getBlocked("flood"));
        assertEquals(0, stats.getBlocked("caps"));
        assertEquals(3, stats.getTotalBlocked());
    }

    @Test
    void snapshot_sortedDescending() {
        FilterStatistics stats = new FilterStatistics();
        stats.increment("flood");
        stats.increment("swear");
        stats.increment("swear");
        stats.increment("spam");
        stats.increment("spam");
        stats.increment("spam");

        Map<String, Long> snapshot = stats.snapshot();
        assertEquals(3, snapshot.get("spam"));
        assertEquals(2, snapshot.get("swear"));
        assertEquals(1, snapshot.get("flood"));

        // Первый элемент — фильтр с наибольшим количеством блокировок
        String first = snapshot.keySet().iterator().next();
        assertEquals("spam", first);
    }

    @Test
    void reset_clearsCounters() {
        FilterStatistics stats = new FilterStatistics();
        stats.increment("swear");
        stats.reset();
        assertEquals(0, stats.getTotalBlocked());
        assertTrue(stats.snapshot().isEmpty());
    }
}