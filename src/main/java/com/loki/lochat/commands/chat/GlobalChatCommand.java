package com.loki.lochat.commands.chat;

import com.loki.lochat.compat.Msg;

import com.loki.lochat.LoChat;
import com.loki.lochat.api.service.ChatService;
import com.loki.lochat.utils.format.ChatFormatter;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class GlobalChatCommand implements CommandExecutor {

    private final LoChat plugin;
    private final ChatService chatService;

    public GlobalChatCommand(LoChat plugin) {
        this.plugin = plugin;
        this.chatService = plugin.getServiceRegistry().get(ChatService.class);
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(plugin.getConfigManager().getMessagesConfig().getPlayerOnly());
            return true;
        }

        if (command.getName().equalsIgnoreCase("globalchat")) {
            if (args.length > 0 && args[0].equalsIgnoreCase("toggle")) {
                boolean enabled = chatService.toggleGlobalChat(player.getUniqueId());
                Msg.send(player, ChatFormatter.parse(
                        plugin.getMessageConfig().get(enabled ? "global.toggled-on" : "global.toggled-off")));
            } else {
                Msg.send(player, ChatFormatter.parse(plugin.getMessageConfig().getInvalidUsage("/globalchat toggle")));
            }
            return true;
        }

        if (args.length == 0) {
            Msg.send(player, ChatFormatter.parse(plugin.getMessageConfig().getInvalidUsage("/g <сообщение>")));
            return true;
        }

        chatService.sendGlobalMessage(player, String.join(" ", args));
        return true;
    }
}
