package com.loki.lochat.gradient.util;

import com.loki.lochat.compat.Msg;

import com.loki.lochat.gradient.GradientModule;
import com.loki.lochat.gradient.config.GradientConfig;
import com.loki.lochat.gradient.data.GradientPlayerData;

import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;

import org.bukkit.entity.Player;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Утилита для обновления отображаемого имени игрока
 */
public final class DisplayNameUtil {

    private static final LegacyComponentSerializer SERIALIZER = LegacyComponentSerializer.builder()
            .character('&')
            .hexCharacter('#')
            .hexColors()
            .build();

    private DisplayNameUtil() {
    }

    public static void updateDisplayName(GradientModule module, Player player, GradientPlayerData data) {
        // Используем новый PrefixService для получения полного имени
        String displayName = module.getFormattedName(player);
        
        // Конвертируем в legacy формат для display name
        displayName = convertMiniMessageToLegacy(displayName);
        var component = SERIALIZER.deserialize(displayName);
        Msg.setDisplayName(player, component);
        Msg.setPlayerListName(player, component);
    }

    /**
     * Конвертирует MiniMessage <#RRGGBB> формат в §x§R§R§G§G§B§B для display name
     */
    private static String convertMiniMessageToLegacy(String text) {
        if (text == null) {
            return "";
        }

        // Конвертируем <#RRGGBB> в §x§R§R§G§G§B§B
        Pattern pattern = Pattern.compile("<#([0-9a-fA-F]{6})>");
        Matcher matcher = pattern.matcher(text);
        StringBuffer sb = new StringBuffer();

        while (matcher.find()) {
            String hex = matcher.group(1).toLowerCase();
            StringBuilder replacement = new StringBuilder("§x");
            for (char c : hex.toCharArray()) {
                replacement.append("§").append(c);
            }
            matcher.appendReplacement(sb, replacement.toString());
        }
        matcher.appendTail(sb);

        return sb.toString();
    }

    public static String buildColoredPrefix(GradientModule module, GradientPlayerData data) {
        if (!data.hasPrefix() || !data.isPrefixEnabled()) {
            return null;
        }

        GradientConfig cfg = module.getConfig();
        // ✅ FIX: Убрал stripTrailing() чтобы сохранить пробел из prefix-format
        String prefix = cfg.getPrefixFormat().replace("{prefix}", data.getPrefix());

        // Применяем градиент только если цвета включены И есть цвета
        if (data.hasColors() && data.isColorEnabled() && cfg.isGradientOnPrefix()) {
            return GradientUtil.applyGradient(prefix, data.getColors(), cfg.isUseLegacyRgbFormat());
        }

        // Возвращаем префикс без цветов
        return prefix;
    }

    /**
     * Получает полное отображаемое имя игрока с градиентом
     */
    public static String getFullDisplayName(GradientModule module, Player player) {
        GradientPlayerData data = module.getDataManager().getPlayerData(player.getUniqueId());
        GradientConfig cfg = module.getConfig();

        String prefix = null;
        if (data.isPrefixEnabled() && data.hasPrefix()) {
            prefix = data.getPrefix();
        }

        return GradientUtil.buildDisplayName(
                prefix,
                player.getName(),
                data.isColorEnabled() ? data.getColors() : null,
                cfg.isGradientOnPrefix(),
                cfg.isContinuousGradient(),
                cfg.getPrefixFormat(),
                cfg.isUseLegacyRgbFormat()
        );
    }
}
