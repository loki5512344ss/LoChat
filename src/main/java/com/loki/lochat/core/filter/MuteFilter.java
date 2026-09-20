package com.loki.lochat.core.filter;

import com.loki.lochat.compat.Msg;

import com.loki.lochat.api.filter.MessageFilter;
import com.loki.lochat.api.service.MuteService;
import com.loki.lochat.config.MessagesConfig;
import com.loki.lochat.data.model.ChatMessage;
import com.loki.lochat.utils.format.ChatFormatter;

import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * Фильтр проверки мута
 */
public class MuteFilter implements MessageFilter {
    private final MuteService muteService;
    private final MessagesConfig messagesConfig;

    public MuteFilter(MuteService muteService, JavaPlugin plugin) {
        this.muteService = muteService;
        // Получаем MessagesConfig через LoChat
        com.loki.lochat.LoChat loChat = (com.loki.lochat.LoChat) plugin;
        this.messagesConfig = loChat.getConfigManager().getMessagesConfig();
    }

    @Override
    public boolean apply(Player player, ChatMessage message) {
        if (muteService.isMuted(player.getUniqueId())) {
            sendMuteNotification(player);
            return false;
        }
        return true;
    }

    private void sendMuteNotification(Player player) {
        long remaining = muteService.getRemainingTime(player.getUniqueId());
        com.loki.lochat.data.model.MuteData muteData = muteService.getMuteData(player.getUniqueId());
        String reason = muteData != null && muteData.getReason() != null ? muteData.getReason() : "";

        String msg;
        if (remaining < 0) {
            // Permanent mute
            msg = messagesConfig.getMutedPermanent()
                    .replace("{reason}", reason);
        } else {
            String timeStr = muteService.formatTime(remaining);
            msg = messagesConfig.getMutedMessage()
                    .replace("{reason}", reason)
                    .replace("{time}", timeStr);
        }

        Msg.send(player, ChatFormatter.parse(msg));
    }
}
