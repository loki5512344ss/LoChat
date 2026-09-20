package com.loki.lochat.gradient.commands;

import com.loki.lochat.config.RatConfig;
import com.loki.lochat.gradient.GradientModule;
import com.loki.lochat.gradient.commands.handlers.ColorCommandHandler;
import com.loki.lochat.gradient.commands.handlers.PrefixCommandHandler;
import com.loki.lochat.gradient.config.GradientMessages;
import com.loki.lochat.gradient.data.GradientPlayerData;
import com.loki.lochat.gradient.util.DisplayNameUtil;
import com.loki.lochat.utils.platform.FoliaUtil;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Админ команда /aprefix - упрощенная версия с делегированием
 */
public class GradientAdminCommand implements CommandExecutor, TabCompleter {

    private final GradientModule module;
    private final ColorCommandHandler colorHandler;
    private final PrefixCommandHandler prefixHandler;

    public GradientAdminCommand(GradientModule module) {
        this.module = module;
        this.colorHandler = new ColorCommandHandler(module);
        this.prefixHandler = new PrefixCommandHandler(module);
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command,
                             @NotNull String label, @NotNull String[] args) {
        GradientMessages msg = module.getMessages();

        if (!sender.hasPermission("gradient.admin")) {
            msg.send(sender, "no-permission");
            return true;
        }

        if (args.length == 0) {
            sendHelp(sender);
            return true;
        }

        String subCommand = args[0].toLowerCase();

        switch (subCommand) {
            case "reload" -> {
                module.reload();
                msg.send(sender, "reload-success");
            }
            case "info" -> {
                if (args.length < 2) {
                    sender.sendMessage("§cИспользование: /aprefix info <игрок>");
                    return true;
                }
                handleInfo(sender, args[1], msg);
            }
            case "setcolor" -> colorHandler.handleSetColor(sender, args, msg);
            case "setprefix" -> prefixHandler.handleSetPrefix(sender, args, msg);
            case "coloron" -> colorHandler.handleColorToggle(sender, args, true, msg);
            case "coloroff" -> colorHandler.handleColorToggle(sender, args, false, msg);
            case "prefixon" -> prefixHandler.handlePrefixToggle(sender, args, true, msg);
            case "prefixoff" -> prefixHandler.handlePrefixToggle(sender, args, false, msg);
            case "resetcolor" -> colorHandler.handleResetColor(sender, args, msg);
            case "resetprefix" -> prefixHandler.handleResetPrefix(sender, args, msg);
            case "updateall" -> handleUpdateAll(sender, msg);
            default -> sendHelp(sender);
        }

        return true;
    }

    private void sendHelp(CommandSender sender) {
        sender.sendMessage("§e=== LoChat Gradient Admin ===");
        sender.sendMessage("§e/aprefix reload §7— перезагрузить конфиг");
        sender.sendMessage("§e/aprefix info <игрок> §7— информация об игроке");
        sender.sendMessage("§e/aprefix setcolor <игрок> <цвета> §7— установить цвет");
        sender.sendMessage("§e/aprefix setprefix <игрок> <префикс> §7— установить префикс");
        sender.sendMessage("§e/aprefix coloron/coloroff <игрок> §7— вкл/выкл цвет");
        sender.sendMessage("§e/aprefix prefixon/prefixoff <игрок> §7— вкл/выкл префикс");
        sender.sendMessage("§e/aprefix resetcolor <игрок> §7— сбросить цвет");
        sender.sendMessage("§e/aprefix resetprefix <игрок> §7— сбросить префикс");
        sender.sendMessage("§e/aprefix updateall §7— обновить display names всех игроков");
    }

    @SuppressWarnings("deprecation")
    private void handleInfo(CommandSender sender, String playerName, GradientMessages msg) {
        OfflinePlayer target = Bukkit.getOfflinePlayer(playerName);
        if (!target.hasPlayedBefore() && !target.isOnline()) {
            msg.send(sender, "player-not-found");
            return;
        }

        GradientPlayerData data = module.getDataManager().getPlayerData(target.getUniqueId());

        msg.send(sender, "info-header", "player", playerName);
        msg.send(sender, "info-prefix", "prefix", data.hasPrefix() ? data.getPrefix() : "нет");
        msg.send(sender, "info-prefix-enabled", "enabled", data.isPrefixEnabled() ? "да" : "нет");
        msg.send(sender, "info-colors", "colors", data.hasColors() ? String.join(", ", data.getColors()) : "нет");
        msg.send(sender, "info-color-enabled", "enabled", data.isColorEnabled() ? "да" : "нет");
        msg.send(sender, "info-prefix-purchased", "purchased", data.isPrefixPurchased() ? "да" : "нет");
    }

    private void handleUpdateAll(CommandSender sender, GradientMessages msg) {
        int count = 0;
        for (Player player : Bukkit.getOnlinePlayers()) {
            GradientPlayerData data = module.getDataManager().getPlayerData(player.getUniqueId());
            FoliaUtil.runEntityTask(module.getPlugin(), player,
                () -> DisplayNameUtil.updateDisplayName(module, player, data));
            count++;
        }
        sender.sendMessage("§aОбновлены display names для §f" + count + " §aигроков");
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command,
                                                @NotNull String alias, @NotNull String[] args) {
        if (!sender.hasPermission("gradient.admin")) {
            return new ArrayList<>();
        }

        if (args.length == 1) {
            return Arrays.asList("reload", "info", "setcolor", "setprefix", "coloron", "coloroff",
                            "prefixon", "prefixoff", "resetcolor", "resetprefix", "updateall").stream()
                    .filter(s -> s.startsWith(args[0].toLowerCase()))
                    .toList();
        }

        if (args.length == 2) {
            String sub = args[0].toLowerCase();
            if (Arrays.asList("info", "setcolor", "setprefix", "coloron", "coloroff",
                    "prefixon", "prefixoff", "resetcolor", "resetprefix").contains(sub)) {
                return Bukkit.getOnlinePlayers().stream()
                        .map(Player::getName)
                        .filter(s -> s.toLowerCase().startsWith(args[1].toLowerCase()))
                        .toList();
            }
        }

        if (args.length >= 3 && args[0].equalsIgnoreCase("setcolor")) {
            return RatConfig.PRESET_COLORS.stream()
                    .filter(s -> s.toLowerCase().startsWith(args[args.length - 1].toLowerCase()))
                    .toList();
        }

        return new ArrayList<>();
    }
}
