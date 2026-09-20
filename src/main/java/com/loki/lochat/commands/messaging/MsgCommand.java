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

public class MsgCommand implements CommandExecutor {

    private final LoChat plugin;
    private final MessagingService messagingService;
    private final PrivateMessageService pmService;

    public MsgCommand(LoChat plugin) {
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
        if (args.length < 2) {
            Msg.send(player, ChatFormatter.parse(plugin.getMessageConfig().getInvalidUsage("/msg <ник> <сообщение>")));
            return true;
        }

        Player target = Bukkit.getPlayer(args[0]);
        if (target == null) {
            Msg.send(player, ChatFormatter.parse(plugin.getMessageConfig().get("pm.player-offline")));
            return true;
        }
        if (target.equals(player)) {
            Msg.send(player, ChatFormatter.parse(plugin.getMessageConfig().get("pm.self")));
            return true;
        }
        if (messagingService.isIgnoring(target.getUniqueId(), player.getUniqueId())) {
            Msg.send(player, ChatFormatter.parse(plugin.getMessageConfig().get("pm.ignored")));
            return true;
        }
        if (messagingService.isIgnoring(player.getUniqueId(), target.getUniqueId())) {
            Msg.send(player, ChatFormatter.parse(plugin.getMessageConfig().get("pm.ignored")));
            return true;
        }

        String message = String.join(" ", java.util.Arrays.copyOfRange(args, 1, args.length));
        pmService.sendPrivateMessage(player, target, message);
        return true;
    }
}
