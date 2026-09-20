package com.loki.lochat.commands.moderation.mute;

import com.loki.lochat.compat.Msg;

import com.loki.lochat.LoChat;
import com.loki.lochat.api.service.MuteService;
import com.loki.lochat.data.model.MuteData;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

public class MuteHistoryCommand implements CommandExecutor, TabCompleter {

    private final LoChat plugin;
    private final MuteService muteService;
    private final SimpleDateFormat fmt = new SimpleDateFormat("dd.MM.yyyy HH:mm");

    public MuteHistoryCommand(LoChat plugin) {
        this.plugin = plugin;
        this.muteService = plugin.getServiceRegistry().get(MuteService.class);
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!sender.hasPermission("lochat.mutehistory")) {
            Msg.send(sender, plugin.getMessageConfig().getComponent("errors.no-permission"));
            return true;
        }
        if (args.length < 1) {
            sender.sendMessage("&#CF6679Использование: /lmutehistory <ник>");
            return true;
        }

        UUID uuid = com.loki.lochat.utils.player.PlayerUtil.findPlayerUUID(args[0]);
        if (uuid == null) {
            uuid = muteService.getUUIDByName(args[0]);
        }
        if (uuid == null) {
            Msg.send(sender, plugin.getMessageConfig().getComponent("errors.player-not-found"));
            return true;
        }

        List<MuteData.MuteHistoryEntry> history = muteService.getPlayerHistory(uuid);
        if (history.isEmpty()) {
            sender.sendMessage("&#9878C9У &#7858E9" + args[0] + " &#9878C9нет истории мутов");
            return true;
        }

        sender.sendMessage("&#7858E9▬▬▬▬▬ &#B798A8История мутов &#9878C9" + args[0]
                + " &#9878C9(" + history.size() + "&#9878C9) &#7858E9▬▬▬▬▬");
        int idx = 1;
        for (int i = history.size() - 1; i >= 0 && idx <= 10; i--, idx++) {
            MuteData.MuteHistoryEntry e = history.get(i);
            String status = e.unmuted ? "&#9878C9Размучен" : (e.duration == 0 ? "&#CF6679Активен (навсегда)" :
                    System.currentTimeMillis() > e.mutedAt + e.duration ? "&#B798A8Истёк"
                            : "&#CF6679Активен (" + muteService.formatTime(
                                    e.mutedAt + e.duration - System.currentTimeMillis()) + ")");
            sender.sendMessage("&#9878C9" + idx + ". &f" + fmt.format(new Date(e.mutedAt)) + " &#B798A8— " + status);
            sender.sendMessage("   &#B798A8Длительность: &f" + (e.duration == 0 ? "навсегда" : muteService.formatTime(e.duration)));
            sender.sendMessage("   &#B798A8Причина: &f" + (e.reason != null ? e.reason : "—") + "  &#B798A8Выдал: &#7858E9" + e.mutedBy);
            if (e.unmuted && e.unmutedBy != null) {
                sender.sendMessage("   &#B798A8Размутил: &#7858E9" + e.unmutedBy + " &f(" + fmt.format(new Date(e.unmutedAt)) + ")");
            }
        }
        if (history.size() > 10) {
            sender.sendMessage("&#B798A8...и ещё " + (history.size() - 10) + " записей");
        }
        return true;
    }

    @Override
    public @Nullable List<String> onTabComplete(
        @NotNull CommandSender sender, @NotNull Command command, @NotNull String alias,
        @NotNull String[] args
    ) {
        List<String> c = new ArrayList<>();
        if (args.length == 1) {
            for (Player p : Bukkit.getOnlinePlayers()) {
                if (p.getName().toLowerCase().startsWith(args[0].toLowerCase())) {
                    c.add(p.getName());
                }
            }
        }
        return c;
    }
}
