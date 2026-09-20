package com.loki.lochat.commands.moderation.mute;

import com.loki.lochat.compat.Msg;

import com.loki.lochat.LoChat;
import com.loki.lochat.api.service.MuteService;
import com.loki.lochat.config.MessagesConfig;
import com.loki.lochat.utils.format.ChatFormatter;

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
 * /mute <ник> [время] [-s] [причина]
 */
public class MuteCommand implements CommandExecutor, TabCompleter {

    private final LoChat plugin;
    private final MuteService muteService;

    public MuteCommand(LoChat plugin) {
        this.plugin = plugin;
        this.muteService = plugin.getServiceRegistry().get(MuteService.class);
    }

    private record ParsedArgs(String targetName, long duration, String reason, boolean silent) { }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command,
                             @NotNull String label, @NotNull String[] args) {

        MessagesConfig hm = plugin.getConfigManager().getMessagesConfig();

        if (!sender.hasPermission("lochat.mute")) {
            Msg.send(sender, plugin.getMessageConfig().getComponent("errors.no-permission"));
            return true;
        }

        if (args.length < 1) {
            Msg.send(sender, ChatFormatter.parse(hm.getMuteUsage()));
            Msg.send(sender, ChatFormatter.parse(hm.getMuteTimeHelp()));
            Msg.send(sender, ChatFormatter.parse(hm.getMuteSilentHelp()));
            return true;
        }

        ParsedArgs parsed = parseArgs(sender, args, hm);
        if (parsed == null) {
            return true;
        }

        String targetName = parsed.targetName;
        long duration = parsed.duration;
        String reason = parsed.reason;
        boolean silent = parsed.silent;

        if (sender instanceof Player p && !muteService.canMuteForDuration(p, duration)) {
            long max = muteService.getMaxDuration(p);
            Msg.send(sender, ChatFormatter.parse("&#CF6679Нельзя мутить на такой срок! &#B798A8Максимум: &#7858E9" +
                    (max == 0 ? "навсегда" : muteService.formatTime(max))));
            return true;
        }

        UUID targetUUID = com.loki.lochat.utils.player.PlayerUtil.findPlayerUUID(targetName);
        if (targetUUID == null) {
            Msg.send(sender, plugin.getMessageConfig().getComponent("errors.player-not-found"));
            return true;
        }
        if (muteService.isMuted(targetUUID)) {
            Msg.send(sender, ChatFormatter.parse("&#CF6679Игрок уже замучен!"));
            return true;
        }

        doMute(sender, hm, targetUUID, targetName, duration, reason, silent);
        return true;
    }

    private ParsedArgs parseArgs(CommandSender sender, String[] args, MessagesConfig hm) {
        String targetName = null;
        String timeStr = null;
        boolean silent = false;
        StringBuilder reasonBuilder = new StringBuilder();

        for (String arg : args) {
            if ("-s".equalsIgnoreCase(arg)) {
                silent = true;
            } else if (targetName == null) {
                targetName = arg;
            } else if (timeStr == null && isTimeFormat(arg)) {
                timeStr = arg;
            } else {
                if (reasonBuilder.length() > 0) {
                    reasonBuilder.append(" ");
                }
                reasonBuilder.append(arg);
            }
        }

        if (silent && !sender.hasPermission("lochat.mute.silent")) {
            Msg.send(sender, ChatFormatter.parse(hm.getNoSilentMutePermission()));
            return null;
        }

        String reason = reasonBuilder.length() > 0 ? reasonBuilder.toString() : hm.getDefaultMuteReason();

        long duration = resolveDuration(sender, timeStr, hm);
        if (duration < 0) {
            return null;
        }

        return new ParsedArgs(targetName, duration, reason, silent);
    }

    private long resolveDuration(CommandSender sender, String timeStr, MessagesConfig hm) {
        if (timeStr == null || timeStr.isEmpty()) {
            long dur = sender instanceof Player p ? muteService.getMaxDuration(p) : 0;
            if (dur == -1) {
                dur = muteService.parseTime(hm.getDefaultMuteDuration());
            }
            return dur;
        }
        if ("perm".equalsIgnoreCase(timeStr) || "0".equals(timeStr)) {
            return 0;
        }
        long dur = muteService.parseTime(timeStr);
        if (dur < 0) {
            Msg.send(sender, ChatFormatter.parse(hm.getMuteTimeHelp()));
        }
        return dur;
    }

    private void doMute(CommandSender sender, MessagesConfig hm, UUID targetUUID,
                        String targetName, long duration, String reason, boolean silent) {
        Player target = Bukkit.getPlayer(targetUUID);
        String finalName = com.loki.lochat.utils.player.PlayerUtil.getPlayerName(targetUUID);
        String op = sender.getName();

        muteService.mute(targetUUID, finalName, duration, reason, op);
        String timeDisplay = duration == 0 ? "навсегда" : muteService.formatTime(duration);

        Msg.send(sender, ChatFormatter.parse("&#9878C9Игрок &#7858E9" + finalName + " &#9878C9замучен на &#7858E9" + timeDisplay));
        Msg.send(sender, ChatFormatter.parse("&#B798A8Причина: &f" + reason));

        if (target != null) {
            String youMsg = duration == 0
                    ? hm.getMutedPermanent().replace("{reason}", reason)
                    : hm.getMutedMessage().replace("{reason}", reason).replace("{time}", timeDisplay);
            Msg.send(target, ChatFormatter.parse(youMsg));
        }

        if (silent) {
            String msgText = hm.getPlayerMutedSilent().replace("{player}", finalName).replace("{time}", timeDisplay);
            for (Player p : Bukkit.getOnlinePlayers()) {
                if (p.hasPermission("lochat.mute.see-silent")) {
                    Msg.send(p, ChatFormatter.parse(msgText));
                }
            }
        } else {
            String broadcastMsg = (duration == 0 ? hm.getPlayerMutedPermanent() : hm.getPlayerMuted())
                    .replace("{player}", finalName).replace("{time}", timeDisplay).replace("{reason}", reason);
            Msg.broadcast(ChatFormatter.parse(broadcastMsg));
        }

        String voiceCmd = plugin.getConfig().getString("moderation.voice-mute-console-command", "");
        if (target != null && voiceCmd != null && !voiceCmd.isEmpty()) {
            String c = voiceCmd.replace("{player}", finalName).replace("{uuid}", targetUUID.toString());
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), c);
        }
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
            c.addAll(List.of("10m", "30m", "1h", "6h", "12h", "1d", "7d", "30d", "perm", "-s"));
        } else if (args.length == 3) {
            c.add("-s");
        }
        return c;
    }
}
