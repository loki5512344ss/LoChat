package com.loki.lochat.config;

import org.bukkit.plugin.java.JavaPlugin;

/**
 * Конфигурация мутов (config/mute.yml)
 */
public class MuteConfig extends BaseConfig {

    public MuteConfig(JavaPlugin plugin) {
        super(plugin, "mute.yml", true);
    }

    // ── Основные настройки ──────────────────────────────────────────────────────
    
    public boolean isEnabled() {
        return getConfig().getBoolean("settings.auto-unmute", true);
    }

    public String getDefaultDuration() {
        return getConfig().getString("settings.default-duration", "7d");
    }

    public String getDefaultReason() {
        return getConfig().getString("settings.default-reason", "No reason specified");
    }

    public int getMaxHistoryPerPlayer() {
        return getConfig().getInt("settings.max-history-entries", 50);
    }

    // ── Права на длительность ───────────────────────────────────────────────────
    
    public boolean isPermissionBasedDuration() {
        return true;
    }

    // ── Уведомления ─────────────────────────────────────────────────────────────
    
    public boolean isNotifyOnMute() {
        return getConfig().getBoolean("notifications.public-mute", true);
    }

    public boolean isNotifyOnUnmute() {
        return getConfig().getBoolean("notifications.public-unmute", true);
    }

    public boolean isNotifyOnExpire() {
        return true;
    }

    // ── Тихий мут ───────────────────────────────────────────────────────────────
    
    public boolean isSilentMuteEnabled() {
        return getConfig().getBoolean("notifications.silent-mute.enabled", true);
    }

    public boolean isSilentMuteLogToConsole() {
        return true;
    }
}
