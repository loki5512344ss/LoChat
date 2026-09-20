package com.loki.lochat.commands.messaging;

import com.loki.lochat.compat.Msg;

import com.loki.lochat.LoChat;
import com.loki.lochat.api.service.MessagingService;
import com.loki.lochat.utils.format.ChatFormatter;
import com.loki.lochat.utils.player.PlayerUtil;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Set;
import java.util.UUID;

public class IgnoreListCommand implements CommandExecutor {

    private final LoChat plugin;
    private final MessagingService messagingService;

    public IgnoreListCommand(LoChat plugin) {
        this.plugin = plugin;
        this.messagingService = plugin.getServiceRegistry().get(MessagingService.class);
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(plugin.getConfigManager().getMessagesConfig().getPlayerOnly());
            return true;
        }

        Set<UUID> ignored = messagingService.getIgnoredPlayers(player.getUniqueId());
        if (ignored.isEmpty()) {
            Msg.send(player, ChatFormatter.parse(plugin.getMessageConfig().get("ignore.list-empty")));
            return true;
        }

        Msg.send(player, ChatFormatter.parse(plugin.getMessageConfig().get("ignore.list-header")));
        for (UUID uuid : ignored) {
            String name = PlayerUtil.getPlayerName(uuid);
            if (name != null) {
                Msg.send(player, ChatFormatter.parse("&#9878C9◆ &f" + name));
            }
        }
        Msg.send(player, ChatFormatter.parse(plugin.getMessageConfig().get("ignore.list-footer")
            .replace("{count}", String.valueOf(ignored.size()))));

        return true;
    }
}
