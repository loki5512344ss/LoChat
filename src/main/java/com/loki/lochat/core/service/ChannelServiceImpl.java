package com.loki.lochat.core.service;

import com.loki.lochat.api.service.ChannelService;
import com.loki.lochat.channel.Channel;
import com.loki.lochat.config.ChannelConfig;

import org.bukkit.entity.Player;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Реализация сервиса каналов чата.
 * Хранит активный (присоединённый) канал каждого игрока в памяти.
 */
public class ChannelServiceImpl implements ChannelService {

    private final ChannelConfig config;
    private final Map<UUID, String> activeChannels = new ConcurrentHashMap<>();

    public ChannelServiceImpl(ChannelConfig config) {
        this.config = config;
    }

    @Override
    public boolean isEnabled() {
        return config.isEnabled();
    }

    @Override
    public Channel getDefault() {
        return config.getDefault();
    }

    @Override
    public Collection<Channel> getChannels() {
        return config.getChannels();
    }

    @Override
    public Channel find(String nameOrAlias) {
        return config.find(nameOrAlias);
    }

    @Override
    public Optional<Channel> getActive(Player player) {
        String id = activeChannels.get(player.getUniqueId());
        if (id == null) {
            return Optional.empty();
        }
        return Optional.ofNullable(config.get(id));
    }

    @Override
    public boolean isInChannel(Player player) {
        return activeChannels.containsKey(player.getUniqueId());
    }

    @Override
    public boolean canUse(Player player, Channel channel) {
        return !channel.hasPermission() || player.hasPermission(channel.permission());
    }

    @Override
    public boolean join(Player player, Channel channel) {
        if (channel == null || !canUse(player, channel)) {
            return false;
        }
        Channel defaultChannel = config.getDefault();
        if (defaultChannel != null && channel.id().equals(defaultChannel.id())) {
            activeChannels.remove(player.getUniqueId());
            return true;
        }
        activeChannels.put(player.getUniqueId(), channel.id());
        return true;
    }

    @Override
    public void leave(Player player) {
        activeChannels.remove(player.getUniqueId());
    }
}