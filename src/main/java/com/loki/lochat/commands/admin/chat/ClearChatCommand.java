package com.loki.lochat.commands.admin.chat;

import com.loki.lochat.compat.Msg;

import com.loki.lochat.LoChat;
import com.loki.lochat.utils.format.ChatFormatter;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class ClearChatCommand implements CommandExecutor {

    private final LoChat plugin;

    public ClearChatCommand(LoChat plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        int lines = plugin.getConfigManager().getAppearanceConfig().getClearLines();
        for (Player player : Bukkit.getOnlinePlayers()) {
            for (int i = 0; i < lines; i++) {
                player.sendMessage("");
            }
            if (plugin.getConfigManager().isClearChatMessageEnabled()) {
                Msg.send(player, ChatFormatter.parse(
                        plugin.getConfigManager().getClearChatMessage().replace("{player}", sender.getName())));
            }
        }
        return true;
    }
}
