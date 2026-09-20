package com.loki.lochat.gradient.config;

import com.loki.lochat.compat.Msg;

import com.loki.lochat.utils.format.ChatFormatter;

import net.kyori.adventure.text.Component;

import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

/**
 * Сообщения градиентного модуля
 */
public class GradientMessages {

    private final JavaPlugin plugin;
    private FileConfiguration messages;
    private File messagesFile;

    public GradientMessages(JavaPlugin plugin) {
        this.plugin = plugin;
        loadMessages();
    }

    private void loadMessages() {
        messagesFile = new File(plugin.getDataFolder(), "messages.yml");
        if (!messagesFile.exists()) {
            plugin.saveResource("messages.yml", false);
        }
        messages = YamlConfiguration.loadConfiguration(messagesFile);

        try (InputStream defStream = plugin.getResource("messages.yml")) {
            if (defStream != null) {
                YamlConfiguration defConfig = YamlConfiguration.loadConfiguration(
                        new InputStreamReader(defStream, StandardCharsets.UTF_8));
                messages.setDefaults(defConfig);
            }
        } catch (java.io.IOException e) {
            plugin.getLogger().warning("Failed to load default messages.yml: " + e.getMessage());
        }
    }

    public void reload() {
        loadMessages();
    }

    /**
     * Получает сообщение как строку с MiniMessage тегами
     */
    public String get(String key) {
        String message = messages.getString("gradient." + key, "&#FF0000Сообщение не найдено: " + key);
        return ChatFormatter.convertAllColors(message);
    }

    /**
     * Получает сообщение как строку с заменами
     */
    public String get(String key, String... replacements) {
        String message = messages.getString("gradient." + key, "&#FF0000Сообщение не найдено: " + key);
        for (int i = 0; i < replacements.length; i += 2) {
            if (i + 1 < replacements.length) {
                message = message.replace("{" + replacements[i] + "}", replacements[i + 1]);
            }
        }
        return ChatFormatter.convertAllColors(message);
    }

    /**
     * Получает сообщение как Component
     */
    public Component getComponent(String key) {
        return ChatFormatter.parse(get(key));
    }

    /**
     * Получает сообщение как Component с заменами
     */
    public Component getComponent(String key, String... replacements) {
        return ChatFormatter.parse(get(key, replacements));
    }

    /**
     * Отправляет сообщение игроку
     */
    public void send(CommandSender sender, String key) {
        Msg.send(sender, getComponent(key));
    }

    /**
     * Отправляет сообщение игроку с заменами
     */
    public void send(CommandSender sender, String key, String... replacements) {
        Msg.send(sender, getComponent(key, replacements));
    }
}
