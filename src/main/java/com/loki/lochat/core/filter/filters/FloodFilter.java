package com.loki.lochat.core.filter.filters;

import com.loki.lochat.config.FiltersConfig;
import com.loki.lochat.core.filter.FilterResult;

import org.bukkit.entity.Player;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class FloodFilter {
    private static final String FLOOD_BLOCK_MESSAGE = "&#CF6679Не флудите!";

    private final int maxMessages;
    private final int timePeriod;
    private final Map<UUID, Deque<Long>> floodTracker = new ConcurrentHashMap<>();

    public FloodFilter(FiltersConfig filters) {
        this.maxMessages = filters.getFloodMaxMessages();
        this.timePeriod = filters.getFloodTimePeriod();
    }

    public FilterResult filter(Player player) {
        if (player.hasPermission("lochat.bypass.flood")) {
            return FilterResult.ok(null);
        }

        long now = System.currentTimeMillis();
        long windowMs = timePeriod * 1000L;

        Deque<Long> timestamps = floodTracker.computeIfAbsent(
            player.getUniqueId(), k -> new ArrayDeque<>());

        // Удаляем старые timestamp'ы
        while (!timestamps.isEmpty() && now - timestamps.peekFirst() > windowMs) {
            timestamps.pollFirst();
        }

        if (timestamps.size() >= maxMessages) {
            return FilterResult.blocked(FLOOD_BLOCK_MESSAGE);
        }

        timestamps.addLast(now);
        return FilterResult.ok(null);
    }

    public void clearPlayer(UUID uuid) {
        floodTracker.remove(uuid);
    }
}
