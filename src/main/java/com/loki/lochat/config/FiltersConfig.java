package com.loki.lochat.config;

import org.bukkit.plugin.java.JavaPlugin;

import java.util.List;

/**
 * Конфигурация фильтров (config/filters.yml)
 */
public class FiltersConfig extends BaseConfig {

    public FiltersConfig(JavaPlugin plugin) {
        super(plugin, "filters.yml", true);
    }

    // ── Капс ────────────────────────────────────────────────────────────────────
    
    public boolean isCapsEnabled() {
        return getConfig().getBoolean("caps.enabled", true);
    }

    public int getCapsMaxPercent() {
        return getConfig().getInt("caps.max-caps-percent", 70);
    }

    public int getCapsMinLength() {
        return getConfig().getInt("caps.min-length", 5);
    }

    public boolean isCapsAutoLowercase() {
        return true;
    }

    public boolean isCapsBlock() {
        return false;
    }

    // ── Мат ─────────────────────────────────────────────────────────────────────
    
    public boolean isSwearEnabled() {
        return getConfig().getBoolean("swear.enabled", true);
    }

    public String getSwearAction() {
        return getConfig().getBoolean("swear.replace", true) ? "replace" : "block";
    }

    public String getSwearReplacementChar() {
        return getConfig().getString("swear.replacement", "*");
    }

    public boolean isSwearCheckFragments() {
        return getConfig().getBoolean("swear.check-fragments", true);
    }

    public boolean isSwearIgnoreCase() {
        return getConfig().getBoolean("swear.ignore-case", true);
    }

    public boolean isSwearUseExternalFile() {
        return true;
    }

    public List<String> getSwearWords() {
        return getConfig().getStringList("swear.words");
    }

    public String getSwearBlockMessage() {
        return getConfig().getString("swear.block-message", "&#CF6679Мат запрещён");
    }

    // ── Реклама ─────────────────────────────────────────────────────────────────
    
    public boolean isAdvertisingEnabled() {
        return getConfig().getBoolean("hidden-urls.enabled", true);
    }

    public boolean isBlockHiddenUrls() {
        return getConfig().getBoolean("hidden-urls.enabled", true);
    }

    public boolean isBlockDomains() {
        return true;
    }

    public List<String> getWhitelistedDomains() {
        return List.of();
    }

    public List<String> getBlacklistedDomains() {
        return List.of();
    }

    public String getAdvertisingBlockMessage() {
        return "&#CF6679Реклама запрещена!";
    }

    // ── IP адреса ───────────────────────────────────────────────────────────────
    
    public boolean isIpEnabled() {
        return getConfig().getBoolean("ip.enabled", true);
    }

    public boolean isIpBlock() {
        return false;
    }

    public String getIpReplacement() {
        return getConfig().getString("ip.replacement", "[IP скрыт]");
    }

    public String getIpBlockMessage() {
        return getConfig().getString("ip.blocked-message", "&#CF6679IP адреса запрещены!");
    }

    // ── Повторяющиеся символы ───────────────────────────────────────────────────
    
    public boolean isRepeatEnabled() {
        return getConfig().getBoolean("spam.enabled", true);
    }

    public int getRepeatMaxRepeats() {
        return getConfig().getInt("spam.max-repeating-chars", 3);
    }

    // ── Флуд ────────────────────────────────────────────────────────────────────
    
    public boolean isFloodEnabled() {
        return getConfig().getBoolean("flood.enabled", true);
    }

    public int getFloodMaxMessages() {
        return getConfig().getInt("flood.max-same-messages", 3);
    }

    public int getFloodTimePeriod() {
        return 10;
    }

    public String getFloodBlockMessage() {
        return getConfig().getString("flood.block-message", "&#CF6679Не флудите!");
    }

    // ── Спам ────────────────────────────────────────────────────────────────────
    
    public boolean isSpamEnabled() {
        return getConfig().getBoolean("spam.enabled", true);
    }

    public int getSpamMaxSimilarMessages() {
        return 3;
    }

    public int getSpamSimilarityThreshold() {
        return 80;
    }

    public String getSpamBlockMessage() {
        return getConfig().getString("spam.block-message", "&#CF6679Не отправляйте одинаковые сообщения");
    }
}
