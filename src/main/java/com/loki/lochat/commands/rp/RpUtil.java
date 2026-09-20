package com.loki.lochat.commands.rp;

import com.loki.lochat.compat.Msg;

import com.loki.lochat.LoChat;
import com.loki.lochat.utils.player.DistanceUtil;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

/**
 * Вспомогательные методы для RP-команд
 */
public final class RpUtil {

    private RpUtil() {}

    /**
     * Отправляет компонент всем игрокам в радиусе, включая отправителя.
     */
    public static void sendToRadius(Player sender, Component message, int radius) {
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (DistanceUtil.isInRange(sender, player, radius)) {
                Msg.send(player, message);
            }
        }
    }

    /**
     * Возвращает отображаемое имя игрока — градиентное если модуль включён,
     * иначе обычное. Возвращает plain-string для подстановки в шаблон.
     */
    public static String getDisplayName(LoChat plugin, Player player) {
        try {
            if (plugin.getGradientModule() != null && plugin.getGradientModule().isEnabled()) {
                return plugin.getGradientModule().getFormattedName(player);
            }
        } catch (Exception ignored) {
        }

        // Пробуем взять displayName (может содержать градиент от NickService) — Spigot: legacy String
        String displayNameLegacy = player.getDisplayName();
        Component displayName = net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer
                .legacySection().deserialize(displayNameLegacy);
        String plain = PlainTextComponentSerializer.plainText().serialize(displayName);
        // Если displayName == обычное имя — возвращаем его напрямую (без потери цветов не нужно)
        if (plain.equals(player.getName())) {
            return player.getName();
        }

        // Если displayName содержит кастомный ник — сериализуем обратно в MiniMessage строку
        // чтобы она правильно спарсилась через ChatFormatter.parse()
        return net.kyori.adventure.text.minimessage.MiniMessage.miniMessage().serialize(displayName);
    }
}
