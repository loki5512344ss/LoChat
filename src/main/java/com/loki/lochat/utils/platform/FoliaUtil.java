package com.loki.lochat.utils.platform;

import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * Утилита для совместимости с Folia — на Spigot 1.20.1 всегда false.
 */
public class FoliaUtil {
    private FoliaUtil() {
    }

    private static final boolean IS_FOLIA = false;

    public static boolean isFolia() {
        return IS_FOLIA;
    }

    public static void runAsync(JavaPlugin plugin, Runnable task) {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, task);
    }

    public static void runEntityTask(JavaPlugin plugin, Entity entity, Runnable task) {
        Bukkit.getScheduler().runTask(plugin, task);
    }

    /**
     * Запускает глобальную повторяющуюся задачу
     *
     * @param plugin      плагин
     * @param task        задача
     * @param delayTicks  задержка перед первым запуском (в тиках)
     * @param periodTicks период повторения (в тиках)
     */
    public static void runTimerAsync(JavaPlugin plugin, Runnable task, long delayTicks, long periodTicks) {
        Bukkit.getScheduler().runTaskTimerAsynchronously(plugin, task, delayTicks, periodTicks);
    }
}
