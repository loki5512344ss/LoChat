package com.loki.lochat.commands.chat;

import com.loki.lochat.LoChat;
import com.loki.lochat.api.service.ChatService;
import com.loki.lochat.utils.format.ChatFormatter;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class LocalChatCommand implements CommandExecutor {

    private final LoChat plugin;
    private final ChatService chatService;

    public LocalChatCommand(LoChat plugin) {
        this.plugin = plugin;
        this.chatService = plugin.getServiceRegistry().get(ChatService.class);
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(plugin.getConfigManager().getMessagesConfig().getPlayerOnly());
            return true;
        }

        if (args.length == 0) {
            player.sendMessage(ChatFormatter.parse(plugin.getMessageConfig().getInvalidUsage("/l <сообщение>")));
            return true;
        }

        chatService.sendLocalMessage(player, String.join(" ", args));
        return true;
    }
}
