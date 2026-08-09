package com.loki.lochat.api.service;

import com.loki.lochat.channel.Channel;

import org.bukkit.entity.Player;

import java.util.Collection;
import java.util.Optional;

/**
 * Сервис каналов чата: состояние игроков (в каком канале они состоят).
 */
public interface ChannelService {

    boolean isEnabled();

    Channel getDefault();

    Collection<Channel> getChannels();

    Channel find(String nameOrAlias);

    /**
     * Активный (не-дефолтный) канал игрока, если он его присоединил через /join.
     */
    Optional<Channel> getActive(Player player);

    boolean isInChannel(Player player);

    boolean canUse(Player player, Channel channel);

    /**
     * Переводит игрока в канал. Дефолтный канал трактуется как «не присоединён».
     *
     * @return true если присоединение выполнено, false если нет канала или нет прав
     */
    boolean join(Player player, Channel channel);

    void leave(Player player);
}