package com.loki.lochat.gradient.config;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.List;

/**
 * Конфигурация градиентного модуля
 */
public class GradientConfig {

    private final JavaPlugin plugin;
    private FileConfiguration config;

    public GradientConfig(JavaPlugin plugin) {
        this.plugin = plugin;
        loadConfig();
    }

    private void loadConfig() {
        // Читаем из главного config.yml вместо отдельного файла
        config = plugin.getConfig();
        plugin.getLogger().info("Gradient конфиг загружен из config.yml");
    }

    public void reload() {
        plugin.reloadConfig();
        this.config = plugin.getConfig();
    }

    public void save() {
        // Сохранение не требуется, используем главный конфиг
        plugin.saveConfig();
    }

    public boolean isEnabled() {
        return config.getBoolean("gradient.enabled", true);
    }

    public int getMaxColors() {
        return config.getInt("gradient.limits.max-colors", 7);
    }

    public int getMinColors() {
        return config.getInt("gradient.limits.min-colors", 1);
    }

    public int getPricePerColor() {
        return config.getInt("gradient.pricing.price-per-color", 50);
    }

    public int getPrefixPrice() {
        return config.getInt("gradient.pricing.prefix-price", 500);
    }

    public boolean isPrefixOneTimePurchase() {
        return config.getBoolean("gradient.pricing.prefix-one-time-purchase", true);
    }

    public int getColorCooldown() {
        return config.getInt("gradient.cooldowns.color", 60);
    }

    public int getPrefixCooldown() {
        return config.getInt("gradient.cooldowns.prefix", 120);
    }

    public int getMaxPrefixLength() {
        return config.getInt("gradient.limits.max-prefix-length", 16);
    }

    public String getPrefixFormat() {
        return config.getString("gradient.formatting.prefix-format", "[{prefix}] ");
    }

    public boolean isGradientOnPrefix() {
        return config.getBoolean("gradient.formatting.gradient-on-prefix", true);
    }

    public boolean isGradientOnLuckPermsPrefix() {
        return config.getBoolean("gradient.formatting.gradient-on-luckperms-prefix", true);
    }

    public boolean isContinuousGradient() {
        return config.getBoolean("gradient.formatting.continuous-gradient", true);
    }

    public boolean isUseLegacyRgbFormat() {
        return config.getBoolean("gradient.use-legacy-rgb-format", true);
    }

    public String getStorageType() {
        return config.getString("gradient.storage.type", "file");
    }

    public String getSqliteFile() {
        return config.getString("gradient.storage.sqlite-file", "gradient-data.db");
    }

    public List<String> getPrefixBlacklist() {
        return List.of();
    }

    public boolean isPrefixBlacklisted(String prefix) {
        String lowerPrefix = prefix.toLowerCase();
        return getPrefixBlacklist().stream()
                .anyMatch(blacklisted -> lowerPrefix.contains(blacklisted.toLowerCase()));
    }

    public boolean isUpdateDisplayName() {
        return config.getBoolean("gradient.display.update-display-name", true);
    }

    public boolean isUpdateTabList() {
        return config.getBoolean("gradient.display.update-tab-list", true);
    }
}
