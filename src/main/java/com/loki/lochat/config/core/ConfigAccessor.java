package com.loki.lochat.config.core;

import com.loki.lochat.utils.format.ChatFormatter;

import org.bukkit.configuration.file.FileConfiguration;

import java.util.List;

/**
 * Предоставляет доступ к значениям конфигурации
 * Инкапсулирует все геттеры для работы с config.yml
 */
public class ConfigAccessor {

    private FileConfiguration config;

    public ConfigAccessor(FileConfiguration config) {
        this.config = config;
    }

    public void updateConfig(FileConfiguration config) {
        this.config = config;
    }

    // ========== Базовые методы доступа ==========

    public String getString(String path, String defaultValue) {
        return config.getString(path, defaultValue);
    }

    public String getString(String path) {
        return config.getString(path);
    }

    public int getInt(String path, int defaultValue) {
        return config.getInt(path, defaultValue);
    }

    public boolean getBoolean(String path, boolean defaultValue) {
        return config.getBoolean(path, defaultValue);
    }

    public List<String> getStringList(String path) {
        return config.getStringList(path);
    }

    // ========== Настройки чата ==========

    public boolean isGlobalEnabled() {
        return config.getBoolean("chat.global.enabled", true);
    }

    public int getGlobalCooldown() {
        return config.getInt("chat.global.cooldown", 3);
    }

    public boolean isLocalEnabled() {
        return config.getBoolean("chat.local.enabled", true);
    }

    public int getLocalCooldown() {
        return config.getInt("chat.local.cooldown", 1);
    }

    // ========== PM ==========

    public boolean isPmEnabled() {
        return config.getBoolean("chat.pm.enabled", true);
    }

    public boolean isPmLogEnabled() {
        return config.getBoolean("chat.pm.log", false);
    }

    // ========== Mentions ==========

    public boolean isMentionsEnabled() {
        return config.getBoolean("mentions.enabled", true);
    }

    public String getMentionHighlight() {
        return ChatFormatter.convertAllColors(config.getString("mentions.highlight", "&#FFFF00@{player}"));
    }

    public String getSelfMentionHighlight() {
        return ChatFormatter.convertAllColors(config.getString("mentions.self-highlight", "&#FFD700{player}"));
    }

    // ========== Clear chat ==========

    public boolean isClearChatMessageEnabled() {
        return config.getBoolean("clearchat.message.enabled", true);
    }

    public String getClearChatMessage() {
        return config.getString("clearchat.message.text", "&#04CADFЧат очищен администратором {player}");
    }

    // ========== Auto messages ==========

    public boolean isAutoMessagesEnabled() {
        return config.getBoolean("automessages.enabled", true);
    }

    public int getAutoMessagesInterval() {
        return config.getInt("automessages.interval", 300);
    }

    public boolean isAutoMessagesRandom() {
        return config.getBoolean("automessages.random", false);
    }

    // ========== Formats ==========

    public String getAnnouncementFormat() {
        return ChatFormatter.convertAllColors(config.getString("formats.announcement",
            "&#FFD700[ОБЪЯВЛЕНИЕ] <message>"));
    }

    // ========== Announcements ==========

    public boolean isAnnouncementTitleEnabled() {
        return config.getBoolean("announcements.show-title", true);
    }

    public String getAnnouncementTitleHeader() {
        return ChatFormatter.convertAllColors(config.getString("announcements.title-header",
            "&#FFD700ОБЪЯВЛЕНИЕ"));
    }

    public boolean isAnnouncementActionBarEnabled() {
        return config.getBoolean("announcements.show-actionbar", true);
    }

    public int getAnnouncementTitleDuration() {
        return config.getInt("announcements.title-duration", 3);
    }

    // ========== Chat Translation ==========

    public boolean isTranslationEnabled() {
        return config.getBoolean("chat.translation.enabled", false);
    }

    public String getTranslationEndpoint() {
        return config.getString("chat.translation.endpoint", "https://libretranslate.com");
    }

    public String getTranslationApiKey() {
        return config.getString("chat.translation.api-key", "");
    }

    /** "libretranslate" (needs endpoint and usually a key) or "google" (free, no key). */
    public String getTranslationProvider() {
        return config.getString("chat.translation.provider", "libretranslate");
    }

    /** "hover" shows the translation when hovering the message, "actionbar" shows it above the hotbar. */
    public String getTranslationMode() {
        return config.getString("chat.translation.mode", "hover");
    }

    public int getTranslationTimeoutMs() {
        return config.getInt("chat.translation.timeout-ms", 3000);
    }

    public java.util.List<String> getTranslationHoverFormat() {
        java.util.List<String> lines = config.getStringList("chat.translation.hover-format");
        return lines.isEmpty() ? java.util.List.of("&7Translation:", "&f{translation}") : lines;
    }

    public String getTranslationBypassPermission() {
        return config.getString("chat.translation.bypass-permission", "lochat.translate.bypass");
    }

    // ========== LoPreff integration ==========

    public boolean isLopreffEnabled() {
        return config.getBoolean("lopreff.enabled", true);
    }

    public boolean useGradientName() {
        return config.getBoolean("lopreff.use-gradient-name", true);
    }

    public boolean useCustomPrefix() {
        return config.getBoolean("lopreff.use-custom-prefix", true);
    }

    // ========== Кастомные сообщения ==========

    public boolean isJoinMessageEnabled() {
        return config.getBoolean("messages.join.enabled", true);
    }

    public String getJoinMessageFormat() {
        return config.getString("messages.join.format", "&#9878C9✦ &#B798A8{player} &#7858E9зашел на сервер");
    }

    public boolean isQuitMessageEnabled() {
        return config.getBoolean("messages.quit.enabled", true);
    }

    public String getQuitMessageFormat() {
        return config.getString("messages.quit.format", "&#9878C9✦ &#B798A8{player} &#7858E9вышел с сервера");
    }

    public boolean isDeathMessageEnabled() {
        return config.getBoolean("messages.death.enabled", true);
    }

    public String getDeathPlayerKillFormat() {
        return config.getString("messages.death.player_kill", "&#CF6679☠ &#B798A8{player} &#7858E9был убит"
                + "игроком &#B798A8{killer} &#9878C9({weapon})");
    }

    public String getDeathMobKillFormat() {
        return config.getString("messages.death.mob_kill", "&#CF6679☠ &#B798A8{player} &#7858E9был убит мобом &#B798A8{killer}");
    }

    public String getDeathDefaultFormat() {
        return config.getString("messages.death.default", "&#CF6679☠ &#B798A8{player} &#7858E9{death_message}");
    }
}
