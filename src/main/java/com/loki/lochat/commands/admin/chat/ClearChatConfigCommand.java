package com.loki.lochat.commands.admin.chat;

import com.loki.lochat.compat.Msg;

import com.loki.lochat.LoChat;
import com.loki.lochat.utils.format.ChatFormatter;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.List;

public class ClearChatConfigCommand implements CommandExecutor, TabCompleter {

    private final LoChat plugin;

    public ClearChatConfigCommand(LoChat plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!sender.hasPermission("lochat.clearchat.config")) {
            Msg.send(sender, plugin.getMessageConfig().getComponent("errors.no-permission"));
            return true;
        }
        if (args.length == 0) {
            Msg.send(sender, ChatFormatter.parse("&#B798A8Использование: &#7858E9/clearchatconfig <enable|disable|message|status>"));
            return true;
        }
        switch (args[0].toLowerCase()) {
            case "enable"  -> {
                plugin.getConfigManager().setClearChatMessageEnabled(true);
                Msg.send(sender, ChatFormatter.parse("&#9878C9Сообщение очистки &#7858E9включено"));
            }
            case "disable" -> {
                plugin.getConfigManager().setClearChatMessageEnabled(false);
                Msg.send(sender, ChatFormatter.parse("&#9878C9Сообщение очистки &#B798A8отключено"));
            }
            case "message" -> {
                if (args.length < 2) {
                    Msg.send(sender, ChatFormatter.parse("&#CF6679Укажите текст!"));
                    return true;
                }
                String msg = String.join(" ", Arrays.copyOfRange(args, 1, args.length));
                plugin.getConfigManager().setClearChatMessage(msg);
                Msg.send(sender, ChatFormatter.parse("&#9878C9Сообщение установлено: &f" + msg));
            }
            case "status"  -> {
                Msg.send(sender, ChatFormatter.parse("&#B798A8Включено: "
                        + (plugin.getConfigManager().isClearChatMessageEnabled() ? "&#7858E9да" : "&#CF6679нет")));
                Msg.send(sender, ChatFormatter.parse("&#B798A8Текст: &f" + plugin.getConfigManager().getClearChatMessage()));
            }
            default -> Msg.send(sender, ChatFormatter.parse("&#CF6679Неизвестный аргумент"));
        }
        return true;
    }

    @Override
    public @Nullable List<String> onTabComplete(
        @NotNull CommandSender sender, @NotNull Command command, @NotNull String alias,
        @NotNull String[] args
    ) {
        if (args.length == 1) {
            return List.of("enable", "disable", "message", "status").stream()
                    .filter(s -> s.startsWith(args[0].toLowerCase())).toList();
        }
        return null;
    }
}
