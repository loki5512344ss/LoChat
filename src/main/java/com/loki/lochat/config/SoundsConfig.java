package com.loki.lochat.config;

import org.bukkit.plugin.java.JavaPlugin;

/**
 * Конфигурация звуков (config/sounds.yml)
 */
public class SoundsConfig extends BaseConfig {

    public SoundsConfig(JavaPlugin plugin) {
        super(plugin, "sounds.yml", true);
    }

    // ── Звуки упоминаний ────────────────────────────────────────────────────────
    
    public boolean isMentionSoundEnabled() {
        return getConfig().getBoolean("messages.mention.enabled", true);
    }

    public String getMentionSound() {
        return getConfig().getString("messages.mention.sound", "BLOCK_NOTE_BLOCK_PLING");
    }

    public float getMentionVolume() {
        return (float) getConfig().getDouble("messages.mention.volume", 1.0);
    }

    public float getMentionPitch() {
        return (float) getConfig().getDouble("messages.mention.pitch", 1.0);
    }

    // ── Звуки личных сообщений ──────────────────────────────────────────────────
    
    public boolean isPmSoundEnabled() {
        return getConfig().getBoolean("messages.private-message.enabled", true);
    }

    public String getPmSound() {
        return getConfig().getString("messages.private-message.sound", "ENTITY_EXPERIENCE_ORB_PICKUP");
    }

    public float getPmVolume() {
        return (float) getConfig().getDouble("messages.private-message.volume", 1.0);
    }

    public float getPmPitch() {
        return (float) getConfig().getDouble("messages.private-message.pitch", 1.0);
    }

    // ── Звуки команд ────────────────────────────────────────────────────────────
    
    public boolean isCommandSoundEnabled() {
        return getConfig().getBoolean("commands.success.enabled", false);
    }

    public String getCommandSound() {
        return getConfig().getString("commands.success.sound", "UI_BUTTON_CLICK");
    }

    public float getCommandVolume() {
        return (float) getConfig().getDouble("commands.success.volume", 0.5);
    }

    public float getCommandPitch() {
        return (float) getConfig().getDouble("commands.success.pitch", 1.0);
    }
}
