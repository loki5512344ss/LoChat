package com.loki.lochat.commands.admin.system;

import com.loki.lochat.LoChat;
import com.loki.lochat.utils.format.ChatFormatter;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class LoChatCommand implements CommandExecutor, TabCompleter {

    private final LoChat plugin;

    public LoChatCommand(LoChat plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!sender.hasPermission("lochat.admin")) {
            sender.sendMessage(ChatFormatter.parse(plugin.getMessageConfig().getNoPermission()));
            return true;
        }
        if (args.length == 0) {
            sender.sendMessage(ChatFormatter.parse("&#7858E9LoChat &#B798A8v" + plugin.getPluginMeta().getVersion()));
            sender.sendMessage(ChatFormatter.parse("&#9878C9/lochat reload &#B798A8— перезагрузить конфиги"));
            sender.sendMessage(ChatFormatter.parse("&#9878C9/lochat commands [reload] &#B798A8— кастомные команды"));
            sender.sendMessage(ChatFormatter.parse("&#9878C9/lochat filterstats &#B798A8— статистика фильтров чата"));
            return true;
        }
        switch (args[0].toLowerCase()) {
            case "reload" -> {
                plugin.reload();
                sender.sendMessage(ChatFormatter.parse("&#9878C9Конфигурация &#7858E9перезагружена!"));
            }
            case "commands" -> {
                if (args.length > 1 && args[1].equalsIgnoreCase("reload")) {
                    plugin.getCustomCommandManager().reload();
                    sender.sendMessage(ChatFormatter.parse("&#9878C9Кастомные команды &#7858E9перезагружены!"));
                } else {
                    var cmds = plugin.getCustomCommandManager().getCommands();
                    sender.sendMessage(ChatFormatter.parse("&#B798A8Кастомные команды &#9878C9(" + cmds.size() + "&#9878C9):"));
                    cmds.values().forEach(c -> sender.sendMessage(
                            ChatFormatter.parse("&#7858E9/" + c.name() + " &#B798A8(" + c.type() + ")")));
                }
            }
            case "filterstats" -> {
                var filter = plugin.getChatFilter();
                if (filter == null) {
                    sender.sendMessage(ChatFormatter.parse("&#CF6679Фильтр чата недоступен"));
                    break;
                }
                var stats = filter.getStatistics();
                long total = stats.getTotalBlocked();
                sender.sendMessage(ChatFormatter.parse(
                        "&#B798A8Фильтры чата: &#9878C9заблокировано &#7858E9" + total + "&#9878C9 сообщений"));
                if (total == 0) {
                    sender.sendMessage(ChatFormatter.parse("&#B798A8Пока нечего показывать"));
                    break;
                }
                stats.snapshot().forEach((name, count) -> sender.sendMessage(
                        ChatFormatter.parse("&#7858E9" + name + ":&#9878C9 " + count)));
            }
            default -> sender.sendMessage(ChatFormatter.parse("&#CF6679Неизвестная команда"));
        }
        return true;
    }

    @Override
    public @Nullable List<String> onTabComplete(
        @NotNull CommandSender sender, @NotNull Command command, @NotNull String alias,
        @NotNull String[] args
    ) {
        if (!sender.hasPermission("lochat.admin")) {
            return new ArrayList<>();
        }
        if (args.length == 1) {
            return List.of("reload", "commands", "filterstats").stream()
                    .filter(s -> s.startsWith(args[0].toLowerCase())).toList();
        }
        if (args.length == 2 && args[0].equalsIgnoreCase("commands")) {
            return List.of("reload");
        }
        return new ArrayList<>();
    }
}
