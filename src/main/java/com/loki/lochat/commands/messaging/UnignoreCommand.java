package com.loki.lochat.commands.messaging;

import com.loki.lochat.compat.Msg;

import com.loki.lochat.LoChat;
import com.loki.lochat.api.service.MessagingService;
import com.loki.lochat.utils.format.ChatFormatter;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class UnignoreCommand implements CommandExecutor {

    private final LoChat plugin;
    private final MessagingService messagingService;

    public UnignoreCommand(LoChat plugin) {
        this.plugin = plugin;
        this.messagingService = plugin.getServiceRegistry().get(MessagingService.class);
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(plugin.getConfigManager().getMessagesConfig().getPlayerOnly());
            return true;
        }
        if (args.length == 0) {
            Msg.send(player, ChatFormatter.parse(plugin.getMessageConfig().getInvalidUsage("/unignore <ник>")));
            return true;
        }

        Player target = Bukkit.getPlayer(args[0]);
        if (target == null) {
            Msg.send(player, ChatFormatter.parse(plugin.getMessageConfig().getPlayerNotFound()));
            return true;
        }
        if (!messagingService.isIgnoring(player.getUniqueId(), target.getUniqueId())) {
            Msg.send(player, ChatFormatter.parse(plugin.getMessageConfig().get("ignore.not-ignored")));
            return true;
        }

        messagingService.removeIgnore(player.getUniqueId(), target.getUniqueId());
        Msg.send(player, ChatFormatter.parse(plugin.getMessageConfig().get("ignore.removed", "{player}", target.getName())));
        return true;
    }
}
