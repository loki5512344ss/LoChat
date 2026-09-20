package com.loki.lochat.config;

import org.bukkit.plugin.java.JavaPlugin;

/**
 * Конфигурация системных сообщений (config/messages.yml)
 */
public class MessagesConfig extends BaseConfig {

    public MessagesConfig(JavaPlugin plugin) {
        super(plugin, "messages.yml", true); // true = в папке config/
    }

    // ── Общие сообщения ────────────────────────────────────────────────────────
    
    public String getNoPermission() {
        return getConfig().getString("general.no_permission", "&#CF6679Нет прав!");
    }

    public String getPlayerOnly() {
        return getConfig().getString("general.player_only", "&#CF6679Только для игроков!");
    }

    public String getPlayerNotFound() {
        return getConfig().getString("general.player_not_found", "&#CF6679Игрок не найден!");
    }

    // ── Кулдаун ────────────────────────────────────────────────────────────────
    
    public String getCooldownMessage() {
        return getConfig().getString("commands.cooldown.message", "&#B798A8Подождите &#7858E9{remaining} &#B798A8сек");
    }

    // ── Локальный чат ──────────────────────────────────────────────────────────
    
    public String getNobodyHeard() {
        return getConfig().getString("local_chat.nobody_heard", "&#9878C9Вас никто не услышал &#B798A8— рядом нет игроков");
    }

    // ── Мут ────────────────────────────────────────────────────────────────────
    
    public String getMutedMessage() {
        return getConfig().getString("mute.muted_message", "&#CF6679Вы замучены! &#B798A8Причина: &#7858E9{reason}"
                + "&#B798A8. Осталось: &#7858E9{time}");
    }

    public String getMutedPermanent() {
        return getConfig().getString("mute.muted_permanent", "&#CF6679Вы замучены навсегда! &#B798A8Причина: &#7858E9{reason}");
    }

    public String getPlayerMuted() {
        return getConfig().getString("mute.player_muted", "&#9878C9Игрок &#7858E9{player} &#9878C9замучен на"
                + "&#7858E9{time}&#9878C9. Причина: &#B798A8{reason}");
    }

    public String getPlayerMutedPermanent() {
        return getConfig().getString("mute.player_muted_permanent", "&#9878C9Игрок &#7858E9{player} &#9878C9замучен"
                + "навсегда. Причина: &#B798A8{reason}");
    }

    public String getPlayerMutedSilent() {
        return getConfig().getString("mute.player_muted_silent", "&#B798A8[&#9878C9ТИХИЙ МУТ&#B798A8] &#7858E9{player}"
                + "&#B798A8замучен на &#7858E9{time}");
    }

    public String getPlayerUnmuted() {
        return getConfig().getString("mute.player_unmuted", "&#9878C9Игрок &#7858E9{player} &#9878C9размучен");
    }

    public String getPlayerNotMuted() {
        return getConfig().getString("mute.not_muted", "&#B798A8Игрок &#7858E9{player} &#B798A8не замучен");
    }

    public String getPlayerNotMutedSimple() {
        return getConfig().getString("mute.player_not_muted_simple", "&#CF6679Игрок не замучен!");
    }

    public String getInvalidTime() {
        return getConfig().getString("mute.invalid_time", "&#CF6679Неверный формат времени!");
    }

    public String getAlreadyMuted() {
        return getConfig().getString("mute.already_muted", "&#CF6679Игрок уже замучен!");
    }

    public String getNoSilentUnmutePermission() {
        return getConfig().getString("mute.no_silent_unmute_permission", "&#CF6679Нет права на тихий размут!");
    }

    public String getNoSilentMutePermission() {
        return getConfig().getString("commands.mute.no_silent_permission", "&#CF6679Нет права на тихий мут");
    }

    public String getMuteUsage() {
        return getConfig().getString("commands.mute.usage", "&#B798A8Использование: &#7858E9/mute <ник> [время] [-s] [причина]");
    }

    public String getMuteTimeHelp() {
        return getConfig().getString("commands.mute.time_help",
                "&#9878C9Время: &#7858E91d&#9878C9, &#7858E92h&#9878C9, &#7858E930m&#9878C9, &#7858E960s&#9878C9, &#7858E9perm");
    }

    public String getMuteSilentHelp() {
        return getConfig().getString("commands.mute.silent_help", "&#B798A8-s &#9878C9— тихий мут (право"
                + "&#7858E9lochat.mute.silent&#9878C9)");
    }

    public String getDefaultMuteReason() {
        return getConfig().getString("commands.mute.default_reason", "Без причины");
    }

    public String getDefaultMuteDuration() {
        return getConfig().getString("commands.mute.default_duration", "7d");
    }

    // ── ЛС ─────────────────────────────────────────────────────────────────────
    
    public String getNoReplyTarget() {
        return getConfig().getString("private_messages.no_reply_target", "&#CF6679Нет цели для ответа");
    }

    public String getPlayerOffline() {
        return getConfig().getString("private_messages.player_offline", "&#CF6679Игрок &#7858E9{player} &#CF6679не в сети");
    }

    public String getCannotMessageSelf() {
        return getConfig().getString("private_messages.cannot_message_self", "&#CF6679Нельзя писать самому себе");
    }

    // ── Игнор ──────────────────────────────────────────────────────────────────
    
    public String getPlayerIgnored() {
        return getConfig().getString("ignore.player_ignored", "&#9878C9Игрок &#7858E9{player} &#9878C9добавлен в игнор");
    }

    public String getPlayerUnignored() {
        return getConfig().getString("ignore.player_unignored", "&#9878C9Игрок &#7858E9{player} &#9878C9убран из игнора");
    }

    public String getAlreadyIgnored() {
        return getConfig().getString("ignore.already_ignored", "&#B798A8Игрок &#7858E9{player} &#B798A8уже в игноре");
    }

    public String getNotIgnored() {
        return getConfig().getString("ignore.not_ignored", "&#B798A8Игрок &#7858E9{player} &#B798A8не в игноре");
    }

    public String getCannotIgnoreSelf() {
        return getConfig().getString("ignore.cannot_ignore_self", "&#CF6679Нельзя игнорировать самого себя");
    }

    public String getIgnoreListEmpty() {
        return getConfig().getString("ignore.ignore_list_empty", "&#B798A8Список игнора пуст");
    }

    public String getIgnoreListHeader() {
        return getConfig().getString("ignore.ignore_list_header", "&#B798A8Игнорируемые игроки&#9878C9:");
    }

    // ── Модерация ────────────────────────────────────────────────────────────────

    public String getModerationDefaultReason() {
        return getConfig().getString("moderation.default_reason", "Без причины");
    }

    public String getModerationDurationPermanent() {
        return getConfig().getString("moderation.duration_permanent", "навсегда");
    }
}
