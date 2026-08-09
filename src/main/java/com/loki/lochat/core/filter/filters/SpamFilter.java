package com.loki.lochat.core.filter.filters;

import com.loki.lochat.config.FiltersConfig;
import com.loki.lochat.core.filter.FilterResult;

import org.bukkit.entity.Player;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class SpamFilter {
    private final int maxSimilar;
    private final int threshold;
    private final String blockMessage;
    private final Map<UUID, Deque<String>> spamTracker = new ConcurrentHashMap<>();

    public SpamFilter(FiltersConfig filters) {
        this(filters.getSpamMaxSimilarMessages(), filters.getSpamSimilarityThreshold(),
                filters.getSpamBlockMessage());
    }

    public SpamFilter(int maxSimilar, int threshold, String blockMessage) {
        this.maxSimilar = maxSimilar;
        this.threshold = threshold;
        this.blockMessage = blockMessage;
    }

    public FilterResult filter(Player player, String message) {
        if (player.hasPermission("lochat.bypass.spam")) {
            return FilterResult.ok(message);
        }

        Deque<String> history = spamTracker.computeIfAbsent(
            player.getUniqueId(), k -> new ArrayDeque<>());

        // Считаем похожие сообщения
        long similarCount = history.stream()
            .filter(prev -> similarity(prev, message) >= threshold)
            .count();

        if (similarCount >= maxSimilar) {
            return FilterResult.blocked(blockMessage);
        }

        // Добавляем в историю (храним последние 10)
        history.addLast(message);
        if (history.size() > 10) {
            history.pollFirst();
        }

        return FilterResult.ok(null);
    }

    private int similarity(String a, String b) {
        if (a.equals(b)) {
            return 100;
        }
        int maxLen = Math.max(a.length(), b.length());
        if (maxLen == 0) {
            return 100;
        }
        
        int dist = levenshtein(
            a.toLowerCase().substring(0, Math.min(a.length(), 50)),
            b.toLowerCase().substring(0, Math.min(b.length(), 50)));
        
        return 100 - (dist * 100 / maxLen);
    }

    private int levenshtein(String a, String b) {
        int[] dp = new int[b.length() + 1];
        for (int j = 0; j <= b.length(); j++) {
            dp[j] = j;
        }
        
        for (int i = 1; i <= a.length(); i++) {
            int prev = dp[0];
            dp[0] = i;
            for (int j = 1; j <= b.length(); j++) {
                int temp = dp[j];
                dp[j] = a.charAt(i - 1) == b.charAt(j - 1)
                    ? prev
                    : 1 + Math.min(prev, Math.min(dp[j], dp[j - 1]));
                prev = temp;
            }
        }
        
        return dp[b.length()];
    }

    public void clearPlayer(UUID uuid) {
        spamTracker.remove(uuid);
    }
}
