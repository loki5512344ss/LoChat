package com.loki.lochat.commands;

import com.loki.lochat.compat.Msg;

import com.loki.lochat.LoChat;
import com.loki.lochat.managers.CustomCommandManager;
import com.loki.lochat.utils.format.ChatFormatter;

import net.kyori.adventure.text.Component;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class CustomCommandsCommand implements CommandExecutor, TabCompleter {

    private final LoChat plugin;
    private final CustomCommandManager commandManager;

    public CustomCommandsCommand(LoChat plugin, CustomCommandManager commandManager) {
        this.plugin = plugin;
        this.commandManager = commandManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("lochat.customcommands")) {
            Msg.send(sender, ChatFormatter.parse("<#F44336>❌ У вас нет прав для использования этой команды!"));
            return true;
        }

        if (args.length == 0) {
            showHelp(sender);
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "list" -> showCommandsList(sender);
            case "reload" -> reloadCommands(sender);
            case "enable" -> {
                if (args.length < 2) {
                    Msg.send(sender, ChatFormatter.parse("<#FFA726>⚠️ Использование: /customcommands enable <команда>"));
                    return true;
                }
                toggleCommand(sender, args[1], true);
            }
            case "disable" -> {
                if (args.length < 2) {
                    Msg.send(sender, ChatFormatter.parse("<#FFA726>⚠️ Использование: /customcommands disable <команда>"));
                    return true;
                }
                toggleCommand(sender, args[1], false);
            }
            default -> showHelp(sender);
        }

        return true;
    }

    private void showHelp(CommandSender sender) {
        Component message = ChatFormatter.parse("""
                <gradient:#FFD700:#FFA500><bold>▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬</bold></gradient>
                <gradient:#87CEEB:#4169E1><bold>🛠️ Управление кастомными командами</bold></gradient>
                
                <#FFEB3B>📋 Доступные команды:</color>
                <#9E9E9E>• <#FFFFFF>/customcommands list</color> - список команд
                <#9E9E9E>• <#FFFFFF>/customcommands reload</color> - перезагрузить команды
                <#9E9E9E>• <#FFFFFF>/customcommands enable <команда></color> - включить команду
                <#9E9E9E>• <#FFFFFF>/customcommands disable <команда></color> - выключить команду
                
                <gradient:#FFD700:#FFA500><bold>▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬</bold></gradient>
                """);
        Msg.send(sender, message);
    }

    private void showCommandsList(CommandSender sender) {
        net.kyori.adventure.text.TextComponent.Builder message = Component.text()
                .append(ChatFormatter.parse("<gradient:#FFD700:#FFA500><bold>▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬</bold></gradient>\n"))
                .append(ChatFormatter.parse("<gradient:#87CEEB:#4169E1><bold>📋 Кастомные команды</bold></gradient>\n\n"));

        for (CustomCommandManager.CustomCommandData data : commandManager.getCommands().values()) {
            String status = data.enabled() ? "<#4CAF50>✅ Включена" : "<#F44336>❌ Выключена";
            String permission = data.permission() != null ? " <#9E9E9E>(Права: " + data.permission() + ")" : "";
            
            message.append(ChatFormatter.parse(
                    "<#FFFFFF>/" + data.name() + "</color> " + status + permission + "\n"
            ));
        }

        message.append(ChatFormatter.parse("\n<gradient:#FFD700:#FFA500><bold>▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬</bold></gradient>"));
        Msg.send(sender, message.build());
    }

    private void reloadCommands(CommandSender sender) {
        try {
            commandManager.reload();
            Msg.send(sender, ChatFormatter.parse("<#4CAF50>✅ Кастомные команды успешно перезагружены!"));
        } catch (Exception e) {
            Msg.send(sender, ChatFormatter.parse("<#F44336>❌ Ошибка при перезагрузке команд: " + e.getMessage()));
            plugin.getLogger().severe("Ошибка перезагрузки кастомных команд: " + e.getMessage());
        }
    }

    private void toggleCommand(CommandSender sender, String commandName, boolean enable) {
        Msg.send(sender, ChatFormatter.parse(
                "<#FFA726>⚠️ Для изменения статуса команд отредактируйте файл custom-commands.yml и выполните /customcommands reload"
        ));
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (!sender.hasPermission("lochat.customcommands")) {
            return new ArrayList<>();
        }

        if (args.length == 1) {
            return Arrays.asList("list", "reload", "enable", "disable");
        }

        if (args.length == 2 && (args[0].equalsIgnoreCase("enable") || args[0].equalsIgnoreCase("disable"))) {
            return new ArrayList<>(commandManager.getCommands().keySet());
        }

        return new ArrayList<>();
    }
}
