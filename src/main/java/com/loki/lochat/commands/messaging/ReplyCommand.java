package com.loki.lochat.commands.messaging;

import com.loki.lochat.compat.Msg;

import com.loki.lochat.LoChat;
import com.loki.lochat.api.service.MessagingService;
import com.loki.lochat.api.service.pm.PrivateMessageService;
import com.loki.lochat.utils.format.ChatFormatter;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;
import java.util.UUID;

public class ReplyCommand implements CommandExecutor {

    private final LoChat plugin;
    private final MessagingService messagingService;
    private final PrivateMessageService pmService;

    public ReplyCommand(LoChat plugin) {
        this.plugin = plugin;
        this.messagingService = plugin.getServiceRegistry().get(MessagingService.class);
        this.pmService = plugin.getServiceRegistry().get(PrivateMessageService.class);
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(plugin.getConfigManager().getMessagesConfig().getPlayerOnly());
            return true;
        }
        if (!plugin.getConfigManager().isPmEnabled()) {
            return true;
        }
        if (args.length == 0) {
            Msg.send(player, ChatFormatter.parse(plugin.getMessageConfig().getInvalidUsage("/reply <сообщение>")));
            return true;
        }

        Optional<UUID> lastUuid = messagingService.getLastConversation(player.getUniqueId());
        if (lastUuid.isEmpty()) {
            Msg.send(player, ChatFormatter.parse(plugin.getMessageConfig().get("pm.no-reply")));
            return true;
        }

        Player target = Bukkit.getPlayer(lastUuid.get());
        if (target == null) {
            Msg.send(player, ChatFormatter.parse(plugin.getMessageConfig().get("pm.player-offline")));
            return true;
        }
        if (messagingService.isIgnoring(target.getUniqueId(), player.getUniqueId())) {
            Msg.send(player, ChatFormatter.parse(plugin.getMessageConfig().get("pm.ignored")));
            return true;
        }

        String message = String.join(" ", args);
        pmService.sendPrivateMessage(player, target, message);
        return true;
    }
}
