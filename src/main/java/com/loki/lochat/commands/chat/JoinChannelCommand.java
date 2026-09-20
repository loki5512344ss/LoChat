package com.loki.lochat.commands.chat;

import com.loki.lochat.compat.Msg;

import com.loki.lochat.LoChat;
import com.loki.lochat.api.service.ChannelService;
import com.loki.lochat.channel.Channel;
import com.loki.lochat.commands.base.PlayerCommand;
import com.loki.lochat.utils.format.ChatFormatter;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Locale;

/**
 * /join &lt;канал|алиас&gt; — войти в канал чата.
 */
public class JoinChannelCommand extends PlayerCommand {

    public JoinChannelCommand(LoChat plugin) {
        super(plugin);
    }

    @Override
    protected boolean executePlayerCommand(@NotNull Player player, @NotNull Command command,
                                           @NotNull String label, @NotNull String[] args) {
        if (!plugin.getConfigManager().getChannelConfig().isEnabled()) {
            Msg.send(player, ChatFormatter.parse("&#CF6679Система каналов отключена"));
            return true;
        }
        if (args.length == 0) {
            Msg.send(player, ChatFormatter.parse("&#CF6679Использование: &#7858E9/join <канал>"));
            return true;
        }

        ChannelService channels = plugin.getServiceRegistry().get(ChannelService.class);
        Channel channel = channels.find(args[0]);
        if (channel == null) {
            Msg.send(player, ChatFormatter.parse("&#CF6679Канал &#7858E9" + args[0] + " &#CF6679не найден"));
            return true;
        }
        if (!channels.canUse(player, channel)) {
            Msg.send(player, ChatFormatter.parse(
                    plugin.getConfigManager().getMessagesConfig().getNoPermission()));
            return true;
        }

        Channel active = channels.getActive(player).orElse(null);
        channels.join(player, channel);
        if (channel.equals(channels.getDefault())) {
            Msg.send(player, ChatFormatter.parse("&#9878C9Вы в канале [&#7858E9"
                    + channel.display() + "&8 " + channel.prefix() + "&#9878C9] (дефолтный)"));
        } else if (channel.equals(active)) {
            Msg.send(player, ChatFormatter.parse("&#B798A8Вы уже в канале [&#7858E9"
                    + channel.display() + "&8 " + channel.prefix() + "&#B798A8]"));
        } else {
            Msg.send(player, ChatFormatter.parse("&#9878C9Вы вошли в канал [&#7858E9"
                    + channel.display() + "&8 " + channel.prefix() + "&#9878C9]"));
        }
        return true;
    }

    @Override
    protected @Nullable List<String> getTabCompletions(@NotNull CommandSender sender, @NotNull Command command,
                                                       @NotNull String alias, @NotNull String[] args) {
        if (args.length != 1) {
            return null;
        }
        ChannelService channels = plugin.getServiceRegistry().get(ChannelService.class);
        String input = args[0].toLowerCase(Locale.ROOT);
        return channels.getChannels().stream()
                .filter(c -> !(sender instanceof Player p) || channels.canUse(p, c))
                .map(Channel::id)
                .filter(id -> id.toLowerCase(Locale.ROOT).startsWith(input))
                .toList();
    }
}