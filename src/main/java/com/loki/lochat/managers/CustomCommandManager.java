package com.loki.lochat.managers;

import com.loki.lochat.compat.Msg;

import com.loki.lochat.LoChat;
import com.loki.lochat.commands.CustomCommand;
import com.loki.lochat.managers.commands.MessageSender;
import com.loki.lochat.managers.commands.PlaceholderProcessor;
import com.loki.lochat.utils.format.ChatFormatter;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandMap;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

public class CustomCommandManager {

    private final LoChat plugin;
    private final Map<String, CustomCommandData> commands;

    private CustomCommandManager(LoChat plugin) {
        this.plugin = plugin;
        this.commands = new HashMap<>();
    }

    public static CustomCommandManager create(LoChat plugin) {
        CustomCommandManager manager = new CustomCommandManager(plugin);
        manager.loadCommands();
        return manager;
    }

    private void loadCommands() {
        File commandsFile = new File(plugin.getDataFolder(), "custom-commands.yml");
        if (!commandsFile.exists()) {
            plugin.saveResource("custom-commands.yml", false);
        }

        FileConfiguration commandsConfig = YamlConfiguration.loadConfiguration(commandsFile);
        commands.clear();

        for (String commandName : commandsConfig.getKeys(false)) {
            ConfigurationSection section = commandsConfig.getConfigurationSection(commandName);
            if (section == null) {
                continue;
            }

            CustomCommandData data = new CustomCommandData(
                    commandName,
                    section.getString("permission"),
                    section.getString("message", ""),
                    section.getStringList("aliases"),
                    section.getString("type", "chat"),
                    section.getString("target", "sender"),
                    section.getBoolean("enabled", true)
            );

            commands.put(commandName, data);
            registerCommand(data);
        }

        plugin.getLogger().info("Загружено " + commands.size() + " кастомных команд");
    }

    private CommandMap getCommandMap() {
        try {
            java.lang.reflect.Method m = Bukkit.getServer().getClass().getMethod("getCommandMap");
            return (CommandMap) m.invoke(Bukkit.getServer());
        } catch (Exception ignored) {
        }
        try {
            java.lang.reflect.Method m2 = Bukkit.getPluginManager().getClass().getMethod("getCommandMap");
            return (CommandMap) m2.invoke(Bukkit.getPluginManager());
        } catch (Exception ignored) {
        }
        try {
            java.lang.reflect.Field f = Bukkit.getPluginManager().getClass().getDeclaredField("commandMap");
            f.setAccessible(true);
            return (CommandMap) f.get(Bukkit.getPluginManager());
        } catch (Exception ex) {
            throw new RuntimeException("Unable to get CommandMap", ex);
        }
    }

    private void registerCommand(CustomCommandData data) {
        try {
            CommandMap commandMap = getCommandMap();

            CustomCommand command = new CustomCommand(data.name(), this);
            commandMap.register("lochat", command);

            for (String alias : data.aliases()) {
                CustomCommand aliasCommand = new CustomCommand(alias, this);
                commandMap.register("lochat", aliasCommand);
            }

        } catch (Exception e) {
            plugin.getLogger().log(Level.WARNING, "Ошибка регистрации команды " + data.name() + ": " + e.getMessage(), e);
        }
    }

    public void reload() {
        loadCommands();
    }

    public boolean executeCustomCommand(String commandName, Player player, String[] args) {
        CustomCommandData data = commands.get(commandName);
        if (data == null) {
            for (CustomCommandData cmd : commands.values()) {
                if (cmd.aliases().contains(commandName)) {
                    data = cmd;
                    break;
                }
            }
        }

        if (data == null) {
            return false;
        }

        if (!data.enabled()) {
            return true;
        }

        if (data.permission() != null && !player.hasPermission(data.permission())) {
            Msg.send(player, ChatFormatter.parse(plugin.getMessageConfig().getNoPermission()));
            return true;
        }

        String message = PlaceholderProcessor.process(data.message(), player, args);

        switch (data.type().toLowerCase()) {
            case "title" -> MessageSender.sendTitle(data.target(), player, message);
            case "actionbar" -> MessageSender.sendActionBar(data.target(), player, message);
            case "bungee" -> sendBungeeCommand(player, data);
            default -> MessageSender.sendChat(data.target(), player, message);
        }

        return true;
    }

    private void sendBungeeCommand(Player player, CustomCommandData data) {
        if (!data.message().isEmpty()) {
            Msg.send(player, ChatFormatter.parse(data.message()));
        }
        
        String serverName = getServerFromConfig(data);
        if (serverName != null && !serverName.isEmpty()) {
            connectToServer(player, serverName);
        }
    }
    
    private String getServerFromConfig(CustomCommandData data) {
        try {
            File commandsFile = new File(plugin.getDataFolder(), "custom-commands.yml");
            FileConfiguration config = YamlConfiguration.loadConfiguration(commandsFile);
            return config.getString(data.name() + ".server", "hub");
        } catch (Exception e) {
            plugin.getLogger().warning("Ошибка получения сервера для команды " + data.name() + ": " + e.getMessage());
            return "hub";
        }
    }
    
    private void connectToServer(Player player, String serverName) {
        try {
            java.io.ByteArrayOutputStream b = new java.io.ByteArrayOutputStream();
            java.io.DataOutputStream out = new java.io.DataOutputStream(b);
            
            out.writeUTF("Connect");
            out.writeUTF(serverName);
            
            player.sendPluginMessage(plugin, "BungeeCord", b.toByteArray());
        } catch (Exception e) {
            plugin.getLogger().severe("Ошибка подключения к серверу " + serverName + ": " + e.getMessage());
            Msg.send(player, ChatFormatter.parse("<#F44336>❌ Ошибка подключения к серверу"));
        }
    }

    public Map<String, CustomCommandData> getCommands() {
        return commands;
    }

    public record CustomCommandData(String name, String permission, String message, List<String> aliases, String type,
                                    String target, boolean enabled) {
    }
}
