package com.loki.lochat.config;

import org.bukkit.plugin.Plugin;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Конфигурация внешнего вида чата (config/appearance.yml)
 */
public class AppearanceConfig extends BaseConfig {
    private final Map<String, String> emojiCache = new HashMap<>();
    
    public AppearanceConfig(Plugin plugin) {
        super((org.bukkit.plugin.java.JavaPlugin) plugin, "appearance.yml", true); // true = в папке config/
        // init() вызывается отдельно в методе init()
    }
    
    /**
     * Инициализировать конфигурацию (вызывать после создания объекта)
     */
    public void init() {
        super.init(); // ✅ Вызываем init() родителя
        loadEmojiCache();
    }
    
    @Override
    protected void onLoad() {
        loadEmojiCache();
    }
    
    /**
     * Загрузить кэш эмодзи
     */
    private void loadEmojiCache() {
        emojiCache.clear();
        if (getConfig().isConfigurationSection("emojis")) {
            for (String key : getConfig().getConfigurationSection("emojis").getKeys(false)) {
                emojiCache.put(key, getConfig().getString("emojis." + key));
            }
        }
    }
    
    // ========== ПРЕФИКСЫ ==========
    
    private String getPrefixValue(String chatType, String key, String defaultValue) {
        return getConfig().getString("prefixes." + chatType + "." + key, defaultValue);
    }
    
    public String getGlobalEmoji() {
        return getPrefixValue("global", "emoji", "🌍");
    }
    public String getGlobalText() {
        return getPrefixValue("global", "text", "GLOBAL");
    }
    public List<String> getGlobalColors() {
        return getConfig().getStringList("prefixes.global.colors");
    }
    public String getGlobalSeparatorText() {
        return getPrefixValue("global", "separator", " ▶ ");
    }
    public String getGlobalSeparatorColor() {
        return getPrefixValue("global", "separator.color", "#FFD700");
    }
    public String getGlobalMessageColor() {
        return getPrefixValue("global", "message-color", "#FFFFFF");
    }
    
    public String getLocalEmoji() {
        return getPrefixValue("local", "emoji", "");
    }
    public String getLocalText() {
        return getPrefixValue("local", "text", "LOCAL");
    }
    public List<String> getLocalColors() {
        return getConfig().getStringList("prefixes.local.colors");
    }
    public String getLocalSeparatorText() {
        return getPrefixValue("local", "separator", " ▶ ");
    }
    public String getLocalSeparatorColor() {
        return getPrefixValue("local", "separator.color", "#FFFFFF");
    }
    public String getLocalMessageColor() {
        return getPrefixValue("local", "message-color", "#FFFFFF");
    }
    
    // ========== ФОРМАТ СООБЩЕНИЙ ==========
    
    public String getGlobalChatFormat() {
        return getConfig().getString("chat-format.global", "{emoji} {prefix} {separator} {player} : {message}");
    }
    
    public String getLocalChatFormat() {
        return getConfig().getString("chat-format.local", "{emoji} {prefix} {separator} {player} : {message}");
    }
    
    // ========== HOVER ЭФФЕКТЫ ==========
    
    public boolean isHoverEnabled() {
        return getConfig().getBoolean("hover.enabled", true);
    }
    public List<String> getHoverFormat() {
        return getConfig().getStringList("hover.format");
    }
    
    // ========== ЭМОДЗИ ==========
    
    public Map<String, String> getEmojis() {
        return new HashMap<>(emojiCache);
    }
    public String getEmoji(String key) {
        return emojiCache.get(key);
    }
    
    // ========== НАСТРОЙКИ ЧАТА ==========
    
    public int getLocalRadius() {
        return getConfig().getInt("chat.local-radius", 100);
    }
    public int getClearLines() {
        return getConfig().getInt("chat.clear-lines", 100);
    }
    public int getMinMessageLength() {
        return getConfig().getInt("chat.min-message-length", 1);
    }
    public int getMaxMessageLength() {
        return getConfig().getInt("chat.max-message-length", 2000);
    }
    
    private String getChatColor(String type, String defaultColor) {
        return getConfig().getString("chat.colors." + type, defaultColor);
    }
    
    public String getNobodyHeardColor() {
        return getChatColor("nobody-heard", "#FFA726");
    }
    public String getErrorColor() {
        return getChatColor("error", "#FF6B6B");
    }
    public String getSuccessColor() {
        return getChatColor("success", "#6BCB77");
    }
    public String getWarningColor() {
        return getChatColor("warning", "#FFA726");
    }
    public String getInfoColor() {
        return getChatColor("info", "#87CEEB");
    }
    
    // ========== DISCORD ==========
    
    public String getDiscordEmoji(String event) {
        return getConfig().getString("discord.emojis." + event.replace('_', '-'), "");
    }
    public String getDiscordEventTitle(String event) {
        return getConfig().getString("discord.event-titles." + event.replace('_', '-'), "");
    }
    public String getDefaultAvatarUrl() {
        return getConfig().getString("discord.avatar-urls.default", "https://mc-heads.net/avatar/minecraft/64");
    }
    public String getPlayerAvatarUrl() {
        return getConfig().getString("discord.avatar-urls.player", "https://mc-heads.net/avatar/{player}/64");
    }
    public String getDefaultEmbedColor() {
        return getConfig().getString("discord.default-embed-color", "5865F2");
    }
}
