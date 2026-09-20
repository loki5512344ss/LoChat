package com.loki.lochat.commands.moderation.mute;

import com.loki.lochat.compat.Msg;

import com.loki.lochat.LoChat;
import com.loki.lochat.api.service.MuteService;
import com.loki.lochat.data.model.MuteData;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;
import java.util.UUID;

public class MuteListCommand implements CommandExecutor {

    private final LoChat plugin;
    private final MuteService muteService;
    private final SimpleDateFormat fmt = new SimpleDateFormat("dd.MM.yyyy HH:mm");

    public MuteListCommand(LoChat plugin) {
        this.plugin = plugin;
        this.muteService = plugin.getServiceRegistry().get(MuteService.class);
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!sender.hasPermission("lochat.mutelist")) {
            Msg.send(sender, plugin.getMessageConfig().getComponent("errors.no-permission"));
            return true;
        }

        Map<UUID, MuteData> mutes = muteService.getActiveMutes();
        if (mutes.isEmpty()) {
            sender.sendMessage("&#9878C9Нет активных мутов");
            return true;
        }

        sender.sendMessage("&#7858E9▬▬▬▬▬ &#B798A8Активные муты &#9878C9(" + mutes.size() + "&#9878C9) &#7858E9▬▬▬▬▬");
        int i = 1;
        for (Map.Entry<UUID, MuteData> e : mutes.entrySet()) {
            MuteData d = e.getValue();
            String name = d.getPlayerName() != null ? d.getPlayerName() : Bukkit.getOfflinePlayer(e.getKey()).getName();
            String time = d.isPermanent() ? "&#CF6679навсегда"
                    : "&#7858E9" + muteService.formatTime(d.getEndTime() - System.currentTimeMillis());
            sender.sendMessage("&#9878C9" + i + ". &f" + name + " &#B798A8— " + time);
            sender.sendMessage("   &#B798A8Причина: &f" + (d.getReason() != null ? d.getReason() : "—"));
            sender.sendMessage("   &#B798A8Выдал: &#7858E9" + d.getMutedBy() + " &#B798A8(" + fmt.format(new Date(d.getMutedAt())) + ")");
            i++;
        }
        return true;
    }
}
