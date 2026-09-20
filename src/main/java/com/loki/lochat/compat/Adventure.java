package com.loki.lochat.compat;

import net.kyori.adventure.platform.bukkit.BukkitAudiences;

import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.Nullable;

/**
 * Holds BukkitAudiences for Spigot. Falls back to legacy text if creation fails.
 */
public final class Adventure {

    private static BukkitAudiences audiences;
    private static boolean fallback;

    private Adventure() {
    }

    public static void init(Plugin plugin) {
        try {
            audiences = BukkitAudiences.create(plugin);
            fallback = false;
        } catch (Throwable t) {
            audiences = null;
            fallback = true;
            plugin.getLogger().warning(
                    "[LoChat] BukkitAudiences init failed, falling back to legacy text: " + t.getMessage());
        }
    }

    public static void close() {
        if (audiences != null) {
            try {
                audiences.close();
            } catch (Exception ignored) {
            }
            audiences = null;
        }
        fallback = false;
    }

    @Nullable
    public static BukkitAudiences audiences() {
        return audiences;
    }

    public static boolean isFallback() {
        return fallback || audiences == null;
    }
}
