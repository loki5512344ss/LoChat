package com.loki.lochat.commands.admin.broadcast;

import com.loki.lochat.compat.Msg;

import com.loki.lochat.LoChat;
import com.loki.lochat.utils.format.ChatFormatter;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.title.Title;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.time.Duration;

public class AnnounceCommand implements CommandExecutor {

    private final LoChat plugin;

    public AnnounceCommand(LoChat plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (args.length == 0) {
            Msg.send(sender, ChatFormatter.parse(plugin.getMessageConfig().getInvalidUsage("/announce <сообщение>")));
            return true;
        }
        if (args[0].equalsIgnoreCase("reload") && sender.hasPermission("chat.announce.reload")) {
            plugin.reload();
            Msg.send(sender, ChatFormatter.parse(plugin.getMessageConfig().get("announce.reloaded")));
            return true;
        }

        String message = String.join(" ", args);
        Component formatted = ChatFormatter.formatAnnouncement(plugin.getConfigManager().getAnnouncementFormat(), message);

        for (Player player : Bukkit.getOnlinePlayers()) {
            Msg.send(player, formatted);
            if (plugin.getConfigManager().isAnnouncementActionBarEnabled()) {
                Msg.actionBar(player, formatted);
            }
            if (plugin.getConfigManager().isAnnouncementTitleEnabled()) {
                Msg.title(player,
                        ChatFormatter.parse(plugin.getConfigManager().getAnnouncementTitleHeader()),
                        ChatFormatter.parse(message),
                        Title.Times.times(Duration.ofMillis(500),
                                Duration.ofSeconds(plugin.getConfigManager().getAnnouncementTitleDuration()),
                                Duration.ofMillis(500)));
            }
        }
        plugin.getLogger().info("[ANNOUNCE] " + sender.getName() + ": " + ChatFormatter.stripTags(message));
        return true;
    }
}
