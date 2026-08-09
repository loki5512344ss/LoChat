package com.loki.lochat.core.filter.filters;

import com.loki.lochat.config.FiltersConfig;
import com.loki.lochat.core.filter.FilterResult;
import com.loki.lochat.utils.text.AhoCorasick;

import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class SwearFilter {
    private final boolean enabled;
    private final boolean replaceMode;
    private final String replacement;
    private AhoCorasick ahoCorasick;

    public SwearFilter(FiltersConfig filters, JavaPlugin plugin) {
        this.enabled = filters.isSwearEnabled();
        this.replaceMode = "replace".equals(filters.getSwearAction());
        this.replacement = filters.getSwearReplacementChar();

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

        if (ahoCorasick.matches(message)) {
            if (replaceMode) {
                return FilterResult.ok(ahoCorasick.replaceAll(message, replacement));
            }
            return FilterResult.blocked("Сообщение содержит запрещенные слова");
        }

        return FilterResult.ok(message);
    }
}
