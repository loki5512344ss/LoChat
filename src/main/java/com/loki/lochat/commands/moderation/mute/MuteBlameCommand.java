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

public class MuteBlameCommand implements CommandExecutor, TabCompleter {

    private final LoChat plugin;
    private final MuteService muteService;
    private final SimpleDateFormat fmt = new SimpleDateFormat("dd.MM.yyyy HH:mm");

    public MuteBlameCommand(LoChat plugin) {
        this.plugin = plugin;
        this.muteService = plugin.getServiceRegistry().get(MuteService.class);
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!sender.hasPermission("lochat.muteblame")) {
            Msg.send(sender, plugin.getMessageConfig().getComponent("errors.no-permission"));
            return true;
        }
        if (args.length < 1) {
            sender.sendMessage("&#CF6679Использование: /lmuteblame <модератор>");
            return true;
        }

        List<MuteData.MuteHistoryEntry> mutes = muteService.getMutesByOperator(args[0]);
        if (mutes.isEmpty()) {
            sender.sendMessage("&#9878C9Модератор &#7858E9" + args[0] + " &#9878C9не выдавал мутов");
            return true;
        }

        sender.sendMessage("&#7858E9▬▬▬▬▬ &#B798A8Муты от &#9878C9" + args[0] + " &#9878C9(" + mutes.size() + "&#9878C9) &#7858E9▬▬▬▬▬");
        int idx = 1;
        for (MuteData.MuteHistoryEntry e : mutes) {
            if (idx > 15) {
                sender.sendMessage("&#B798A8...и ещё " + (mutes.size() - 15) + " записей");
                break;
            }
            String status = e.unmuted ? "&#9878C9Размучен" : (e.duration == 0 ? "&#CF6679Активен (навсегда)" :
                    System.currentTimeMillis() > e.mutedAt + e.duration ? "&#B798A8Истёк" : "&#CF6679Активен");
            sender.sendMessage("&#9878C9" + idx + ". &f" + (e.playerName != null ? e.playerName : "???") + " &#B798A8— " + status);
            sender.sendMessage("   &f" + fmt.format(new Date(e.mutedAt)) + "  &#B798A8Длительность: &f" + (e.duration
                == 0 ? "навсегда" : muteService.formatTime(e.duration)));

            sender.sendMessage("   &#B798A8Причина: &f" + (e.reason != null ? e.reason : "—"));
            idx++;
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
