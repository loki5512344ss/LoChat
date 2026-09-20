package com.loki.lochat.listener;

import com.loki.lochat.api.service.PunishmentService;
import com.loki.lochat.compat.Msg;
import com.loki.lochat.data.model.BanRecord;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;

/**
 * Блокировка входа забаненным игрокам
 */
public class ModerationListener implements Listener {

    private final PunishmentService punishmentService;

    public ModerationListener(PunishmentService punishmentService) {
        this.punishmentService = punishmentService;
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPreLogin(AsyncPlayerPreLoginEvent event) {
        if (!punishmentService.isBanned(event.getUniqueId())) {
            return;
        }
        BanRecord ban = punishmentService.getActiveBan(event.getUniqueId());
        if (ban == null) {
            return;
        }
        event.disallow(AsyncPlayerPreLoginEvent.Result.KICK_BANNED, Msg.legacy(punishmentService.buildBanKickMessage(ban)));
    }
}
