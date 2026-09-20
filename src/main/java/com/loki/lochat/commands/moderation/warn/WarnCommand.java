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
 * /warn — игрок видит предупреждение в чате
 */
public class WarnCommand implements CommandExecutor, TabCompleter {

    private final LoChat plugin;
    private final PunishmentService punishmentService;

    public WarnCommand(LoChat plugin) {
        this.plugin = plugin;
        this.punishmentService = plugin.getServiceRegistry().get(PunishmentService.class);
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command,
                             @NotNull String label, @NotNull String[] args) {
        MessagesConfig msg = plugin.getConfigManager().getMessagesConfig();
        if (!sender.hasPermission("lochat.warn")) {
            Msg.send(sender, ChatFormatter.parse(msg.getNoPermission()));
            return true;
        }
        if (args.length < 1) {
            Msg.send(sender, ChatFormatter.parse(msg.getWarnUsage()));
            return true;
        }

        UUID targetUuid = PlayerUtil.findPlayerUUID(args[0]);
        if (targetUuid == null) {
            Msg.send(sender, ChatFormatter.parse(msg.getPlayerNotFound()));
            return true;
        }

        if (sender instanceof Player p && p.getUniqueId().equals(targetUuid)) {
            Msg.send(sender, ChatFormatter.parse("&#CF6679Нельзя выдать варн самому себе"));
            return true;
        }

        String mod = sender.getName();
        String reason = buildReason(msg, args, 1);
        String name = PlayerUtil.getPlayerName(targetUuid);
        if (name == null) {
            name = args[0];
        }

        Player targetOnline = Bukkit.getPlayer(targetUuid);
        if (targetOnline != null && targetOnline.hasPermission("lochat.bypass.warn")) {
            Msg.send(sender, ChatFormatter.parse("&#CF6679У этого игрока защита от варнов"));
            return true;
        }

        punishmentService.addWarn(targetUuid, name, mod, reason, false);

        Msg.send(sender, ChatFormatter.parse(msg.getWarnStaffConfirm().replace("{player}", name).replace("{reason}", reason)));

        if (targetOnline != null && targetOnline.isOnline()) {
            Msg.send(targetOnline, ChatFormatter.parse(msg.getWarnReceived().replace("{reason}", reason)));
        }
        return true;
    }

    private static String buildReason(MessagesConfig msg, String[] args, int from) {
        if (args.length <= from) {
            return msg.getModerationDefaultReason();
        }
        StringBuilder sb = new StringBuilder();
        for (int i = from; i < args.length; i++) {
            if (sb.length() > 0) {
                sb.append(" ");
            }
            sb.append(args[i]);
        }
        return sb.toString();
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
