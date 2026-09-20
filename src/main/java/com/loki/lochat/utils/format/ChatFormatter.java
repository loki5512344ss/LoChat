package com.loki.lochat.utils.format;

import com.loki.lochat.utils.color.ColorConverter;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;

import org.bukkit.entity.Player;

/**
 * Утилита для форматирования сообщений - упрощенная версия
 */
public final class ChatFormatter {

    private static final MiniMessage MM = MiniMessage.miniMessage();
    private static final PlainTextComponentSerializer PLAIN = PlainTextComponentSerializer.plainText();

    private ChatFormatter() {
    }

    // ========== Базовые методы ==========

    public static Component parse(String message) {
        if (message == null) {
            return Component.empty();
        }
        String converted = ColorConverter.convertLegacyFormats(message);
        return MM.deserialize(converted);
    }

    public static Component parseWithDefaultMessageColor(String message, String hexCode) {
        if (message == null) {
            message = "";
        }
        String hex = (hexCode == null || hexCode.isBlank()) ? "#FFFFFF" : 
                     (hexCode.startsWith("#") ? hexCode : "#" + hexCode);
        String escaped = ColorConverter.escapeForMiniMessage(message);
        return parse("<color:" + hex + ">" + escaped + "</color>");
    }

    public static Component parse(String message, String... replacements) {
        if (message == null) {
            return Component.empty();
        }
        String result = message;
        for (int i = 0; i + 1 < replacements.length; i += 2) {
            result = result.replace(replacements[i], replacements[i + 1]);
        }
        String converted = ColorConverter.convertLegacyFormats(result);
        return MM.deserialize(converted);
    }

    // ========== Утилиты ==========

    public static String stripTags(String message) {
        if (message == null) {
            return "";
        }
        return PLAIN.serialize(MM.deserialize(message));
    }

    public static String toPlain(Component component) {
        return component == null ? "" : PLAIN.serialize(component);
    }

    public static String toPlain(Object obj) {
        if (obj == null) {
            return "";
        }
        if (obj instanceof Component component) {
            return PLAIN.serialize(component);
        }
        return obj.toString();
    }

    public static void sendMessage(Player player, Component component) {
        if (player != null && component != null) {
            com.loki.lochat.compat.Msg.send(player, component);
        }
    }

    public static String convertAllColors(String message) {
        if (message == null) {
            return "";
        }
        return ColorConverter.convertLegacyFormats(message);
    }

    // ========== Форматирование сообщений ==========

    public static Component formatAnnouncement(String format, String message) {
        return parse(format.replace("<message>", message));
    }

    public static Component formatPmSentNew(String format, Player sender, Player receiver, String message) {
        String senderDisplay = getPlayerDisplayName(sender);
        String receiverDisplay = getPlayerDisplayName(receiver);

        return parse(format
                .replace("{sender}", sender.getName())
                .replace("{receiver}", receiver.getName())
                .replace("{sender_display}", senderDisplay)
                .replace("{receiver_display}", receiverDisplay)
                .replace("{message}", message));
    }

    public static Component formatPmReceivedNew(String format, Player sender, Player receiver, String message) {
        String senderDisplay = getPlayerDisplayName(sender);
        String receiverDisplay = getPlayerDisplayName(receiver);

        return parse(format
                .replace("{sender}", sender.getName())
                .replace("{receiver}", receiver.getName())
                .replace("{sender_display}", senderDisplay)
                .replace("{receiver_display}", receiverDisplay)
                .replace("{message}", message));
    }

    public static Component format(String format, Component name, Component prefix, Component message) {
        return MM.deserialize(format)
                .replaceText(b -> b.matchLiteral("{name}").replacement(name))
                .replaceText(b -> b.matchLiteral("{prefix}").replacement(prefix))
                .replaceText(b -> b.matchLiteral("{message}").replacement(message));
    }

    // ========== Приватные методы ==========

    private static String getPlayerDisplayName(Player player) {
        try {
            com.loki.lochat.LoChat plugin = com.loki.lochat.LoChat.getInstance();

            if (plugin.getGradientModule() != null && plugin.getGradientModule().isEnabled()) {
                return plugin.getGradientModule().getFormattedName(player);
            }
        } catch (Exception e) {
            com.loki.lochat.LoChat.getInstance().getLogger().warning(
                "Ошибка при получении градиентного имени для " + player.getName() + ": " + e.getMessage()
            );
        }

        return player.getName();
    }

    // ========== Deprecated методы для обратной совместимости ==========

    @Deprecated
    public static String replaceEmojis(String message) {
        return message;
    }

    @Deprecated
    public static String replaceEmojis(String message, Player player) {
        return message;
    }

    @Deprecated
    public static String processPlayerMessage(String message, Player player, boolean hasColorPermission) {
        return message;
    }
}
