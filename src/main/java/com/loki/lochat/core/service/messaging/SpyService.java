package com.loki.lochat.core.service.messaging;

import com.loki.lochat.compat.Msg;

import com.loki.lochat.config.MessageConfig;
import com.loki.lochat.utils.format.ChatFormatter;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Сервис шпионажа за личными сообщениями
 * Позволяет админам видеть PM других игроков
 */
public class SpyService {

    private final Set<UUID> spyEnabled = ConcurrentHashMap.newKeySet();
    private final MessageConfig messageConfig;

    public SpyService(MessageConfig messageConfig) {
        this.messageConfig = messageConfig;
    }

    public boolean toggleSpy(UUID player) {
        if (spyEnabled.contains(player)) {
            spyEnabled.remove(player);
            return false;
        } else {
            spyEnabled.add(player);
            return true;
        }
    }

    public boolean isSpying(UUID player) {
        return spyEnabled.contains(player);
    }

    public void broadcastPM(Player sender, Player receiver, String message) {
        String format = messageConfig.get("spy.format");

        for (UUID spyUuid : spyEnabled) {
            Player spy = Bukkit.getPlayer(spyUuid);
            if (spy != null && spy.isOnline() && !spy.equals(sender) && !spy.equals(receiver)) {
                Msg.send(spy, ChatFormatter.parse(format
                        .replace("{sender}", sender.getName())
                        .replace("{receiver}", receiver.getName())
                        .replace("{message}", message)));
            }
        }
    }

    public void sendToSpies(Player sender, Component message, boolean isGlobal) {
        if (spyEnabled.isEmpty()) {
            return;
        }

        String chatType = isGlobal ? "Global" : "Local";
        String plainMessage = PlainTextComponentSerializer.plainText().serialize(message);
        String format = messageConfig.get("spy.chat-format",
                "§7[SPY] §e{type} §7{sender}: §f{message}");

        for (UUID spyUuid : spyEnabled) {
            Player spy = Bukkit.getPlayer(spyUuid);
            if (spy != null && spy.isOnline() && !spy.equals(sender)) {
                Msg.send(spy, ChatFormatter.parse(format
                        .replace("{type}", chatType)
                        .replace("{sender}", sender.getName())
                        .replace("{message}", plainMessage)));
            }
        }
    }

    public void removeSpy(UUID player) {
        spyEnabled.remove(player);
    }
}
