package com.loki.lochat.config;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.InputStream;
import java.nio.file.Files;

/**
 * Базовый класс для всех конфигураций
 * Избегает дублирования кода загрузки конфигов
 */
public abstract class BaseConfig {
    protected final JavaPlugin plugin;
    private File configFile;
    private FileConfiguration config;
    private final String fileName;
    private final boolean inConfigFolder;

    /**
     * Конструктор для конфигов в корне папки плагина
     * @param plugin плагин
     * @param fileName имя файла (например "config.yml")
     */
    protected BaseConfig(JavaPlugin plugin, String fileName) {
        this(plugin, fileName, false);
    }

    /**
     * Конструктор для конфигов в подпапке config/
     * @param plugin плагин
     * @param fileName имя файла (например "messages.yml")
     * @param inConfigFolder true если файл в папке config/
     */
    protected BaseConfig(JavaPlugin plugin, String fileName, boolean inConfigFolder) {
        this.plugin = plugin;
        this.fileName = fileName;
        this.inConfigFolder = inConfigFolder;
        // ✅ FIX: Не вызываем loadConfig() здесь чтобы избежать this-escape
        // Вызывается явно после создания объекта
    }
    
    /**
     * Инициализация конфига (вызывать после создания)
     */
    public void init() {
        loadConfig();
    }

    /**
     * Загрузить конфигурацию
     */
    protected void loadConfig() {
        // Определяем путь к файлу
        if (inConfigFolder) {
            File configFolder = new File(plugin.getDataFolder(), "config");
            if (!configFolder.exists()) {
                configFolder.mkdirs();
            }
            configFile = new File(configFolder, fileName);
        } else {
            configFile = new File(plugin.getDataFolder(), fileName);
        }

        // Копируем из ресурсов если не существует
        if (!configFile.exists()) {
            try {
                String resourcePath = inConfigFolder ? "config/" + fileName : fileName;
                try (InputStream in = plugin.getResource(resourcePath)) {
                    if (in != null) {
                        Files.copy(in, configFile.toPath());
                        plugin.getLogger().info("Created config file: " + (inConfigFolder ? "config/" : "") + fileName);
                    } else {
                        plugin.getLogger().warning("Resource not found: " + resourcePath);
                    }
                }
            } catch (Exception e) {
                plugin.getLogger().warning("Failed to copy " + fileName + ": " + e.getMessage());
            }
        }

        config = YamlConfiguration.loadConfiguration(configFile);
        onLoad();
    }

    /**
     * Перезагрузить конфигурацию
     */
    public void reload() {
        config = YamlConfiguration.loadConfiguration(configFile);
        onLoad();
    }

    /**
     * Сохранить конфигурацию
     */
    public void save() {
        try {
            config.save(configFile);
        } catch (Exception e) {
            plugin.getLogger().warning("Failed to save " + fileName + ": " + e.getMessage());
        }
    }

    /**
     * Вызывается после загрузки конфига
     * Переопределите для дополнительной инициализации
     */
    protected void onLoad() {
        // Переопределяется в подклассах
    }

    protected File getConfigFile() {
        return configFile;
    }

    /**
     * Получить FileConfiguration
     */
    public FileConfiguration getConfig() {
        return config;
    }
}
