package com.loki.lochat.core.filter;

import com.loki.lochat.LoChat;
import com.loki.lochat.config.FiltersConfig;
import com.loki.lochat.core.filter.filters.CharacterFilter;
import com.loki.lochat.core.filter.filters.FloodFilter;
import com.loki.lochat.core.filter.filters.SpamFilter;
import com.loki.lochat.core.filter.filters.SwearFilter;
import com.loki.lochat.core.filter.filters.UrlFilter;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.UUID;

/**
 * Главный фильтр сообщений - координирует работу всех фильтров
 */
public class AdvancedMessageFilter {

    private final FiltersConfig filtersConfig;
    private final CapsFilter capsFilter;
    private final SwearFilter swearFilter;
    private final FloodFilter floodFilter;
    private final SpamFilter spamFilter;
    private final UrlFilter urlFilter;
    private final CharacterFilter characterFilter;

    public AdvancedMessageFilter(FileConfiguration config, JavaPlugin plugin) {
        this.filtersConfig = resolveFiltersConfig(plugin);

        int maxCapsPercent = filtersConfig.getCapsMaxPercent();
        int minLength = filtersConfig.getCapsMinLength();
        boolean autoLower = filtersConfig.isCapsAutoLowercase();
        boolean blockCaps = filtersConfig.isCapsBlock();
        this.capsFilter = new CapsFilter(maxCapsPercent, minLength, autoLower, blockCaps);

        this.swearFilter = new SwearFilter(filtersConfig, plugin);
        this.floodFilter = new FloodFilter(filtersConfig);
        this.spamFilter = new SpamFilter(filtersConfig);
        this.urlFilter = new UrlFilter(filtersConfig);
        this.characterFilter = new CharacterFilter(filtersConfig);
    }

    private static FiltersConfig resolveFiltersConfig(JavaPlugin plugin) {
        if (plugin instanceof LoChat loChat) {
            return loChat.getConfigManager().getFiltersConfig();
        }
        if (plugin != null) {
            FiltersConfig filtersConfig = new FiltersConfig(plugin);
            filtersConfig.init();
            return filtersConfig;
        }
        throw new IllegalArgumentException("AdvancedMessageFilter requires a FiltersConfig");
    }

    /**
     * Проверяет, может ли игрок обойти фильтр
     */
    private boolean canBypassFilter(Player player, String filterType) {
        return player.hasPermission("lochat.bypass." + filterType) 
            || player.hasPermission("lochat.bypass.filter");
    }
    
    /**
     * Главный метод фильтрации
     */
    public FilterResult filterMessage(Player player, String message) {
        
        // 1. Капс
        if (filtersConfig.isCapsEnabled()) {
            String filtered = capsFilter.filter(player, message);
            if (filtered == null) {
                return FilterResult.blocked("&#CF6679Не кричите в чате");
            }
            message = filtered;
        }

        // 2. Мат
        if (filtersConfig.isSwearEnabled() && !canBypassFilter(player, "swear")) {
            FilterResult swear = swearFilter.filter(player, message);
            if (!swear.allowed()) {
                return swear;
            }
            message = swear.filteredMessage();
        }

        // 3. URL фильтры
        if (filtersConfig.isAdvertisingEnabled()) {
            FilterResult r = urlFilter.filterUrls(player, message);
            if (!r.allowed()) {
                return r;
            }
            message = r.filteredMessage();

            r = urlFilter.filterHiddenUrls(player, message);
            if (!r.allowed()) {
                return r;
            }
        }

        // 4. IP
        if (filtersConfig.isIpEnabled()) {
            FilterResult r = urlFilter.filterIPs(player, message);
            if (!r.allowed()) {
                return r;
            }
            message = r.filteredMessage();
        }

        // 5. Повторяющиеся символы
        if (filtersConfig.isRepeatEnabled()) {
            message = characterFilter.filter(message);
        }

        // 6. Anti-flood
        if (filtersConfig.isFloodEnabled() && !canBypassFilter(player, "flood")) {
            FilterResult r = floodFilter.filter(player);
            if (!r.allowed()) {
                return r;
            }
        }

        // 7. Anti-spam
        if (filtersConfig.isSpamEnabled() && !canBypassFilter(player, "spam")) {
            FilterResult r = spamFilter.filter(player, message);
            if (!r.allowed()) {
                return r;
            }
        }

        return FilterResult.ok(message);
    }

    /**
     * Очистка данных игрока при выходе
     */
    public void clearPlayer(UUID uuid) {
        floodFilter.clearPlayer(uuid);
        spamFilter.clearPlayer(uuid);
    }
}
