package com.loki.lochat.commands.moderation.ban;

import com.loki.lochat.compat.Msg;

import com.loki.lochat.LoChat;
import com.loki.lochat.api.service.MuteService;
import com.loki.lochat.api.service.PunishmentService;
import com.loki.lochat.config.MessagesConfig;
import com.loki.lochat.data.model.BanRecord;
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
 * /lban — бан с красивым киком
 */
public class BanCommand implements CommandExecutor, TabCompleter {

    private final LoChat plugin;
    private final PunishmentService punishmentService;
    private final MuteService muteService;

    public BanCommand(LoChat plugin) {
        this.plugin = plugin;
        this.punishmentService = plugin.getServiceRegistry().get(PunishmentService.class);
        this.muteService = plugin.getServiceRegistry().get(MuteService.class);
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command,
                             @NotNull String label, @NotNull String[] args) {
        MessagesConfig msg = plugin.getConfigManager().getMessagesConfig();
        if (!sender.hasPermission("lochat.ban")) {
            Msg.send(sender, ChatFormatter.parse(msg.getNoPermission()));
            return true;
        }
        if (args.length < 1) {
            Msg.send(sender, ChatFormatter.parse(msg.getBanUsage()));
            Msg.send(sender, ChatFormatter.parse(msg.getMuteTimeHelp()));
            return true;
        }

        String targetName = args[0];
        UUID targetUuid = PlayerUtil.findPlayerUUID(targetName);
        if (targetUuid == null) {
            Msg.send(sender, ChatFormatter.parse(msg.getPlayerNotFound()));
            return true;
        }

        if (sender instanceof Player p && p.getUniqueId().equals(targetUuid)) {
            Msg.send(sender, ChatFormatter.parse("&#CF6679Нельзя забанить самого себя"));
            return true;
        }

        Player onlineTarget = Bukkit.getPlayer(targetUuid);
        if (onlineTarget != null && onlineTarget.hasPermission("lochat.bypass.ban") && !sender.hasPermission("lochat.ban.override")) {
            Msg.send(sender, ChatFormatter.parse("&#CF6679Нельзя забанить этого игрока"));
            return true;
        }

        if (punishmentService.isBanned(targetUuid)) {
            Msg.send(sender, ChatFormatter.parse(msg.getAlreadyBanned()));
            return true;
        }

        DurationResult parsed = parseDuration(sender, args, msg);
        if (!parsed.valid) {
            return true;
        }

        long durationMs = parsed.durationMs;
        String reason = parsed.reason;
        String finalName = PlayerUtil.getPlayerName(targetUuid);
        if (finalName == null) {
            finalName = targetName;
        }

        punishmentService.ban(targetUuid, finalName, durationMs, reason, sender.getName());

        String durationLabel = durationMs == 0
                ? msg.getModerationDurationPermanent()
                : muteService.formatTime(durationMs);
        Msg.send(sender, ChatFormatter.parse(msg.getBanConfirm().replace("{player}", finalName).replace("{duration}", durationLabel)));

        BanRecord ban = punishmentService.getActiveBan(targetUuid);
        if (ban != null) {
            Player kicked = Bukkit.getPlayer(targetUuid);
            if (kicked != null && kicked.isOnline()) {
                Msg.kick(kicked, punishmentService.buildBanKickMessage(ban));
            }
        }
        return true;
    }

    private record DurationResult(long durationMs, String reason, boolean valid) { }

    private DurationResult parseDuration(CommandSender sender, String[] args, MessagesConfig msg) {
        String timeStr = null;
        StringBuilder reasonBuilder = new StringBuilder();
        for (int i = 1; i < args.length; i++) {
            if (timeStr == null && isTimeFormat(args[i])) {
                timeStr = args[i];
            } else {
                if (reasonBuilder.length() > 0) {
                    reasonBuilder.append(" ");
                }
                reasonBuilder.append(args[i]);
            }
        }

        long durationMs;
        if (timeStr == null || timeStr.isEmpty()) {
            durationMs = 0;
        } else if ("perm".equalsIgnoreCase(timeStr) || "0".equals(timeStr)) {
            durationMs = 0;
        } else {
            durationMs = muteService.parseTime(timeStr);
            if (durationMs < 0) {
                Msg.send(sender, ChatFormatter.parse(msg.getInvalidTime()));
                return new DurationResult(0, "", false);
            }
        }

        String reason = reasonBuilder.length() > 0 ? reasonBuilder.toString() : msg.getModerationDefaultReason();
        return new DurationResult(durationMs, reason, true);
    }

    private boolean isTimeFormat(String s) {
        return "perm".equalsIgnoreCase(s) || s.matches("\\d+[dhms]?");
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command,
                                                @NotNull String alias, @NotNull String[] args) {
        List<String> c = new ArrayList<>();
        if (args.length == 1) {
            for (Player p : Bukkit.getOnlinePlayers()) {
                if (p.getName().toLowerCase().startsWith(args[0].toLowerCase())) {
                    c.add(p.getName());
                }
            }
        } else if (args.length == 2) {
            c.addAll(List.of("1h", "1d", "7d", "30d", "perm"));
        }
        return c;
    }
}
