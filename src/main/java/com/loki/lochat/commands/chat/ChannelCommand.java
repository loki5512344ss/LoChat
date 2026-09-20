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
 * /channel — инфо о текущем канале со списком доступных.
 * /channel &lt;канал&gt; — переключиться. /channel off — выйти.
 * /channel list — список каналов.
 */
public class ChannelCommand extends PlayerCommand {

    public ChannelCommand(LoChat plugin) {
        super(plugin);
    }

    @Override
    protected boolean executePlayerCommand(@NotNull Player player, @NotNull Command command,
                                           @NotNull String label, @NotNull String[] args) {
        ChannelService channels = plugin.getServiceRegistry().get(ChannelService.class);
        if (!channels.isEnabled()) {
            Msg.send(player, ChatFormatter.parse("&#CF6679Система каналов отключена"));
            return true;
        }

        if (args.length == 0) {
            showCurrent(player, channels);
            return true;
        }

        switch (args[0].toLowerCase(Locale.ROOT)) {
            case "off", "leave" -> {
                if (channels.isInChannel(player)) {
                    channels.leave(player);
                    Msg.send(player, ChatFormatter.parse("&#9878C9Вы вышли из канала"));
                } else {
                    Msg.send(player, ChatFormatter.parse("&#B798A8Вы не находитесь в канале"));
                }
            }
            case "list" -> listChannels(player, channels);
            default -> joinChannel(player, channels, args[0]);
        }
        return true;
    }

    private void showCurrent(Player player, ChannelService channels) {
        Channel active = channels.getActive(player).orElse(null);
        if (active == null) {
            Channel def = channels.getDefault();
            Msg.send(player, ChatFormatter.parse("&#B798A8Вы в дефолтном канале [&#7858E9"
                    + (def == null ? "?" : def.display() + "&8 " + def.prefix()) + "&#B798A8]"));
        } else {
            Msg.send(player, ChatFormatter.parse("&#9878C9Вы в канале [&#7858E9"
                    + active.display() + "&8 " + active.prefix() + "&#9878C9]"));
        }
        Msg.send(player, ChatFormatter.parse("&#B798A8/join <канал> &#9878C9— войти, "
                + "&#B798A8/channel list &#9878C9— все каналы"));
    }

    private void listChannels(Player player, ChannelService channels) {
        Msg.send(player, ChatFormatter.parse("&#B798A8Доступные каналы:"));
        for (Channel c : channels.getChannels()) {
            boolean canUse = channels.canUse(player, c);
            boolean active = c.equals(channels.getActive(player).orElse(null));
            Msg.send(player, ChatFormatter.parse((canUse ? "&#9878C9" : "&#CF6679")
                    + (active ? "\u25B8 " : "\u25AA ") + c.display() + "&8 " + c.prefix()
                    + (canUse ? "" : " &#CF6679(нет прав)")
                    + " &#B798A8— " + (c.isGlobalScope() ? "глобальный" : "локальный, " + c.radius() + "б")));
        }
    }

    private void joinChannel(Player player, ChannelService channels, String name) {
        Channel channel = channels.find(name);
        if (channel == null) {
            Msg.send(player, ChatFormatter.parse("&#CF6679Канал &#7858E9" + name + " &#CF6679не найден"));
            return;
        }
        if (!channels.canUse(player, channel)) {
            Msg.send(player, ChatFormatter.parse(
                    plugin.getConfigManager().getMessagesConfig().getNoPermission()));
            return;
        }
        Channel active = channels.getActive(player).orElse(null);
        channels.join(player, channel);
        if (channel.equals(active)) {
            Msg.send(player, ChatFormatter.parse("&#B798A8Вы уже в канале [&#7858E9"
                    + channel.display() + "&8 " + channel.prefix() + "&#B798A8]"));
        } else {
            Msg.send(player, ChatFormatter.parse("&#9878C9Вы вошли в канал [&#7858E9"
                    + channel.display() + "&8 " + channel.prefix() + "&#9878C9]"));
        }
    }

    @Override
    protected @Nullable List<String> getTabCompletions(@NotNull CommandSender sender, @NotNull Command command,
                                                       @NotNull String alias, @NotNull String[] args) {
        if (args.length != 1) {
            return null;
        }
        ChannelService channels = plugin.getServiceRegistry().get(ChannelService.class);
        String input = args[0].toLowerCase(Locale.ROOT);
        return java.util.stream.Stream.concat(
                        java.util.stream.Stream.of("list", "off"),
                        channels.getChannels().stream().map(Channel::id))
                .filter(s -> s.startsWith(input))
                .toList();
    }
}