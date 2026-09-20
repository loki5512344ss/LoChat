package com.loki.lochat.config;

import com.loki.lochat.compat.Msg;

import com.loki.lochat.LoChat;
import com.loki.lochat.utils.format.ChatFormatter;

import net.kyori.adventure.text.Component;

import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;

public class MessageConfig {

    private final LoChat plugin;
    private FileConfiguration messages;

    public MessageConfig(LoChat plugin) {
        this.plugin = plugin;
        loadMessages();
    }

    private void loadMessages() {
        File messagesFile = new File(plugin.getDataFolder(), "messages.yml");
        if (!messagesFile.exists()) {
            plugin.saveResource("messages.yml", false);
        }
        messages = YamlConfiguration.loadConfiguration(messagesFile);
    }

    public void reload() {
        loadMessages();
    }

    public String get(String path) {
        String message = messages.getString(path, "&#FF0000Message not found: " + path);
        return ChatFormatter.convertAllColors(message);
    }

    public String get(String path, String... replacements) {
        String message = messages.getString(path, "&#FF0000Message not found: " + path);
        for (int i = 0; i < replacements.length - 1; i += 2) {
            message = message.replace(replacements[i], replacements[i + 1]);
        }
        return ChatFormatter.convertAllColors(message);
    }

    /**
     * Получает сообщение как Component
     */
    public Component getComponent(String path) {
        return ChatFormatter.parse(get(path));
    }

    /**
     * Получает сообщение как Component с заменами
     */
    public Component getComponent(String path, String... replacements) {
        return ChatFormatter.parse(get(path, replacements));
    }

    /**
     * Отправляет сообщение игроку
     */
    public void send(CommandSender sender, String path) {
        Msg.send(sender, getComponent(path));
    }

    /**
     * Отправляет сообщение игроку с заменами
     */
    public void send(CommandSender sender, String path, String... replacements) {
        Msg.send(sender, getComponent(path, replacements));
    }

    // Shortcuts
    public String getPrefix() {
        return get("prefix");
    }

    public String getNoPermission() {
        return get("errors.no-permission");
    }

    public String getPlayerNotFound() {
        return get("errors.player-not-found");
    }

    public String getInvalidUsage(String usage) {
        return get("errors.invalid-usage", "{usage}", usage);
    }

    // PM форматы
    public String getPmFormatSent() {
        return get("pm.format-sent");
    }

    public String getPmFormatReceived() {
        return get("pm.format-received");
    }

    public boolean isPmUseGradientNames() {
        return messages.getBoolean("pm.use-gradient-names", true);
    }
}
