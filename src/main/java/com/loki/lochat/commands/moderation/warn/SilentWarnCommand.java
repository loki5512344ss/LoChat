package com.loki.lochat.commands.moderation.warn;

import com.loki.lochat.compat.Msg;

import com.loki.lochat.LoChat;
import com.loki.lochat.api.service.PunishmentService;
import com.loki.lochat.config.MessagesConfig;
import com.loki.lochat.utils.format.ChatFormatter;
import com.loki.lochat.utils.player.PlayerUtil;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * /silentwarn — варн в базе, цель не видит; модераторы с lochat.warn.notify видят
 */
public class SilentWarnCommand implements CommandExecutor, TabCompleter {

    private final LoChat plugin;
    private final PunishmentService punishmentService;

    public SilentWarnCommand(LoChat plugin) {
        this.plugin = plugin;
        this.punishmentService = plugin.getServiceRegistry().get(PunishmentService.class);
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command,
                             @NotNull String label, @NotNull String[] args) {
        MessagesConfig msg = plugin.getConfigManager().getMessagesConfig();
        if (!sender.hasPermission("lochat.warn.silent")) {
            Msg.send(sender, ChatFormatter.parse(msg.getNoPermission()));
            return true;
        }
        if (args.length < 1) {
            Msg.send(sender, ChatFormatter.parse(msg.getSilentWarnUsage()));
            return true;
        }

        UUID targetUuid = PlayerUtil.findPlayerUUID(args[0]);
        if (targetUuid == null) {
            Msg.send(sender, ChatFormatter.parse(msg.getPlayerNotFound()));
            return true;
        }

        String mod = sender.getName();
        String reason = args.length > 1
                ? String.join(" ", java.util.Arrays.copyOfRange(args, 1, args.length))
                : msg.getModerationDefaultReason();
        String name = PlayerUtil.getPlayerName(targetUuid);
        if (name == null) {
            name = args[0];
        }

        punishmentService.addWarn(targetUuid, name, mod, reason, true);

        Msg.send(sender, ChatFormatter.parse(msg.getWarnStaffConfirm().replace("{player}", name).replace("{reason}", reason)));

        String staffLine = msg.getSilentWarnStaff().replace("{player}", name).replace("{reason}", reason);
        for (Player p : Bukkit.getOnlinePlayers()) {
            if (p.hasPermission("lochat.warn.notify")) {
                Msg.send(p, ChatFormatter.parse(staffLine));
            }
        }
        return true;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command,
                                                @NotNull String alias, @NotNull String[] args) {
        List<String> out = new ArrayList<>();
        if (args.length == 1) {
            String p = args[0].toLowerCase();
            for (Player pl : Bukkit.getOnlinePlayers()) {
                if (pl.getName().toLowerCase().startsWith(p)) {
                    out.add(pl.getName());
                }
            }
        }
        return out;
    }
}
