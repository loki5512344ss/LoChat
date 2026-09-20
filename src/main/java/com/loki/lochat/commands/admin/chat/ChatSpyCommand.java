package com.loki.lochat.commands.admin.chat;

import com.loki.lochat.compat.Msg;

import com.loki.lochat.LoChat;
import com.loki.lochat.api.service.MessagingService;
import com.loki.lochat.utils.format.ChatFormatter;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class ChatSpyCommand implements CommandExecutor {

    private final LoChat plugin;
    private final MessagingService messagingService;

    public ChatSpyCommand(LoChat plugin) {
        this.plugin = plugin;
        this.messagingService = plugin.getServiceRegistry().get(MessagingService.class);
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(plugin.getConfigManager().getMessagesConfig().getPlayerOnly());
            return true;
        }
        boolean enabled = messagingService.toggleSpy(player.getUniqueId());
        Msg.send(player, ChatFormatter.parse(plugin.getMessageConfig().get(enabled ? "spy.enabled" : "spy.disabled")));
        return true;
    }
}
