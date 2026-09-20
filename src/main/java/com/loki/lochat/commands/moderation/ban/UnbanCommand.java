package com.loki.lochat.commands.moderation.ban;

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
 * /lunban — снять бан
 */
public class UnbanCommand implements CommandExecutor, TabCompleter {

    private final LoChat plugin;
    private final PunishmentService punishmentService;

    public UnbanCommand(LoChat plugin) {
        this.plugin = plugin;
        this.punishmentService = plugin.getServiceRegistry().get(PunishmentService.class);
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command,
                             @NotNull String label, @NotNull String[] args) {
        MessagesConfig msg = plugin.getConfigManager().getMessagesConfig();
        if (!sender.hasPermission("lochat.unban")) {
            Msg.send(sender, ChatFormatter.parse(msg.getNoPermission()));
            return true;
        }
        if (args.length < 1) {
            Msg.send(sender, ChatFormatter.parse("&#B798A8Использование: &#7858E9/lunban <ник>"));
            return true;
        }

        UUID uuid = PlayerUtil.findPlayerUUID(args[0]);
        if (uuid == null) {
            Msg.send(sender, ChatFormatter.parse(msg.getPlayerNotFound()));
            return true;
        }

        if (!punishmentService.isBanned(uuid)) {
            Msg.send(sender, ChatFormatter.parse(msg.getNotBanned()));
            return true;
        }

        String name = PlayerUtil.getPlayerName(uuid);
        if (name == null) {
            name = args[0];
        }

        punishmentService.unban(uuid);
        Msg.send(sender, ChatFormatter.parse(msg.getUnbanConfirm().replace("{player}", name)));
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
