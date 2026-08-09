package com.loki.lochat.core.filter.filters;

import com.loki.lochat.config.FiltersConfig;
import com.loki.lochat.core.filter.FilterResult;
import com.loki.lochat.utils.text.AhoCorasick;
import com.loki.lochat.utils.text.SwearNormalizer;

import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;

public class SwearFilter {
    private final boolean enabled;
    private final boolean replaceMode;
    private AhoCorasick ahoCorasick;

    public SwearFilter(FiltersConfig filters, JavaPlugin plugin) {
        this.enabled = filters.isSwearEnabled();
        this.replaceMode = "replace".equals(filters.getSwearAction());

        if (enabled) {
            loadWordList(plugin);
        }
    }

    private void loadWordList(JavaPlugin plugin) {
        List<String> words = new ArrayList<>();
        try (InputStream is = plugin.getResource("badwords.txt");
             BufferedReader reader = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {
            reader.lines()
                .map(String::trim)
                .filter(line -> !line.isEmpty() && !line.startsWith("#"))
                .forEach(words::add);
        } catch (Exception e) {
            plugin.getLogger().warning("Failed to load badwords.txt: " + e.getMessage());
        }
        this.ahoCorasick = new AhoCorasick(words);
    }

    public FilterResult filter(Player player, String message) {
        if (!enabled || player.hasPermission("lochat.bypass.swear")) {
            return FilterResult.ok(message);
        }

        String normalized = SwearNormalizer.normalizeForSearch(message);
        if (ahoCorasick.matches(normalized)) {
            if (replaceMode) {
                return FilterResult.ok(maskMatches(message, normalized));
            }
            return FilterResult.blocked("Сообщение содержит запрещенные слова");
        }

        return FilterResult.ok(message);
    }

    /**
     * Маскирует в оригинальном сообщении каждое найденное (возможно, обфусцированное) слово.
     */
    private String maskMatches(String original, String normalized) {
        String result = original;
        for (String word : ahoCorasick.findMatches(normalized)) {
            Matcher matcher = SwearNormalizer.wordPattern(word).matcher(result);
            result = matcher.replaceAll(maskFor(word.length()));
        }
        return result;
    }

    private static String maskFor(int wordLength) {
        return "*".repeat(Math.max(1, wordLength));
    }
}
