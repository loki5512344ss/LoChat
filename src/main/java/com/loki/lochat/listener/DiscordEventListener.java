package com.loki.lochat.listener;

import com.loki.lochat.integrations.DiscordIntegration;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

/**
 * Слушатель событий для Discord интеграции
 */
public class DiscordEventListener implements Listener {
    private final DiscordIntegration discord;

    public DiscordEventListener(DiscordIntegration discord) {
        this.discord = discord;
    }

    /**
     * Обработка входа игрока
     */
    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerJoin(PlayerJoinEvent event) {
        if (!discord.isEnabled()) {
            return;
        }
        
        Player player = event.getPlayer();
        discord.sendPlayerJoin(player);
    }

    /**
     * Обработка выхода игрока
     */
    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerQuit(PlayerQuitEvent event) {
        if (!discord.isEnabled()) {
            return;
        }
        
        Player player = event.getPlayer();
        discord.sendPlayerQuit(player);
    }

    /**
     * Обработка смерти игрока
     */
    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerDeath(PlayerDeathEvent event) {
        if (!discord.isEnabled()) {
            return;
        }

        Player player = event.getEntity();
        String raw = event.getDeathMessage();
        String deathMessage = raw != null ? raw : "умер";

        discord.sendPlayerDeath(player, deathMessage);
    }
}
