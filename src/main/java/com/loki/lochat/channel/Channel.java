package com.loki.lochat.channel;

import java.util.List;

/**
 * Канал чата (global, local, staff, trade...).
 * Иммутабельное описание канала из config/channels.yml.
 */
public record Channel(
        String id,
        String display,
        String prefix,
        String color,
        int radius,
        int cooldown,
        String permission,
        List<String> aliases) {

    /** radius &lt; 1 — глобальный канал (видны всем игрокам) */
    public boolean isGlobalScope() {
        return radius < 1;
    }

    /** radius &gt; 0 — локальный канал с радиусом действия */
    public boolean isLocalScope() {
        return radius > 0;
    }

    /** Есть ли требование по праву для входа/использования */
    public boolean hasPermission() {
        return permission != null && !permission.isBlank();
    }
}