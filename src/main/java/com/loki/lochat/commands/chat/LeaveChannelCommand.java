package com.loki.lochat.commands.chat;

import com.loki.lochat.LoChat;
import com.loki.lochat.api.service.ChannelService;
import com.loki.lochat.commands.base.PlayerCommand;
import com.loki.lochat.utils.format.ChatFormatter;

import org.bukkit.command.Command;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

/**
 * /leave — выйти из текущего канала (вернуться к дефолтному чату).
 */
public class LeaveChannelCommand extends PlayerCommand {

    public LeaveChannelCommand(LoChat plugin) {
        super(plugin);
    }

    @Override
    protected boolean executePlayerCommand(@NotNull Player player, @NotNull Command command,
                                           @NotNull String label, @NotNull String[] args) {
        ChannelService channels = plugin.getServiceRegistry().get(ChannelService.class);
        if (!channels.isEnabled()) {
            player.sendMessage(ChatFormatter.parse("&#CF6679Система каналов отключена"));
            return true;
        }
        if (!channels.isInChannel(player)) {
            player.sendMessage(ChatFormatter.parse("&#B798A8Вы не находитесь в канале"));
            return true;
        }
        channels.leave(player);
        player.sendMessage(ChatFormatter.parse("&#9878C9Вы вышли из канала"));
        return true;
    }
}