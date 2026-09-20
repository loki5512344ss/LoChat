package com.loki.lochat.integrations.discord;

import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;

/**
 * Конфигурация Discord интеграции
 */
public class DiscordConfig {
    private final JavaPlugin plugin;
    private YamlConfiguration config;
    
    public DiscordConfig(JavaPlugin plugin) {
        this.plugin = plugin;
        load();
    }
    
    private void load() {
        File configFolder = new File(plugin.getDataFolder(), "config");
        if (!configFolder.exists()) {
            configFolder.mkdirs();
        }
        
        File configFile = new File(configFolder, "discord.yml");
        if (!configFile.exists()) {
            try (java.io.InputStream in = plugin.getResource("config/discord.yml")) {
                if (in != null) {
                    java.nio.file.Files.copy(in, configFile.toPath());
                }
            } catch (Exception e) {
                plugin.getLogger().warning("Failed to copy config/discord.yml: " + e.getMessage());
            }
        }
        
        config = YamlConfiguration.loadConfiguration(configFile);
    }
    
    public boolean isEnabled() {
        return config.getBoolean("enabled", false);
    }
    
    public String getWebhookUrl() {
        return config.getString("webhook.url", "");
    }
    
    public String getUsername() {
        return config.getString("webhook.username", "Minecraft Server");
    }
    
    public String getAvatarUrl() {
        return config.getString("webhook.avatar-url", "");
    }
    
    public int getTimeout() {
        return config.getInt("performance.timeout", 10);
    }
    
    public int getRetryAttempts() {
        return config.getInt("performance.retry-attempts", 3);
    }
    
    public long getRetryDelay() {
        return config.getLong("performance.retry-delay", 1000);
    }
    
    public boolean isAsync() {
        return config.getBoolean("performance.async", true);
    }

    public double getMaxRequestsPerSecond() {
        return config.getDouble("performance.max-requests-per-second", 5.0);
    }

    public int getRateLimitBurst() {
        return config.getInt("performance.rate-limit-burst", 10);
    }
    
    public boolean isChatEnabled() {
        return config.getBoolean("chat.enabled", true);
    }
    
    public boolean isGlobalOnly() {
        return config.getBoolean("chat.global-only", true);
    }
    
    public boolean useEmbed() {
        return config.getBoolean("chat.use-embed", true);
    }
    
    public String getChatFormat() {
        return config.getString("chat.format", "**{player}**: {message}");
    }
    
    public String getEmbedColor() {
        return config.getString("chat.embed-color", "5865F2");
    }
    
    public boolean isEventEnabled(String event) {
        return config.getBoolean("events.enabled", true) && 
               config.getBoolean("events." + event + ".enabled", true);
    }
    
    public String getEventTitle(String event) {
        return config.getString("events." + event + ".title", "");
    }
    
    public String getEventDescription(String event) {
        return config.getString("events." + event + ".description", "");
    }
    
    public String getEventColor(String event) {
        return config.getString("events." + event + ".color", "5865F2");
    }
    
    public String getEventThumbnail(String event) {
        return config.getString("events." + event + ".thumbnail", "");
    }
    
    public int getMinMessageLength() {
        return config.getInt("filter.min-message-length", 1);
    }
    
    public int getMaxMessageLength() {
        return config.getInt("filter.max-message-length", 2000);
    }
    
    public boolean sanitizeMentions() {
        return config.getBoolean("filter.sanitize-mentions", true);
    }
}
