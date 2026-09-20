package com.loki.lochat.commands.rp;

import com.loki.lochat.compat.Msg;

import com.loki.lochat.LoChat;
import com.loki.lochat.api.service.PlayerService;
import com.loki.lochat.commands.base.PlayerCommand;
import com.loki.lochat.utils.format.ChatFormatter;

import net.kyori.adventure.text.Component;

import org.bukkit.command.Command;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

/**
 * /me <действие>
 * Видят игроки в радиусе rp.radius блоков.
 * Формат: * ИгрокИмя действие
 */
public class MeCommand extends PlayerCommand {

    private final PlayerService playerService;

    public MeCommand(LoChat plugin) {
        super(plugin);
        this.playerService = plugin.getServiceRegistry().get(PlayerService.class);
    }

    @Override
    protected boolean executePlayerCommand(@NotNull Player player, @NotNull Command command,
                                           @NotNull String label, @NotNull String[] args) {
        if (!requirePermission(player, "lochat.rp.me")) {
            return true;
        }

        int cooldown = plugin.getConfigManager().getInt("rp.cooldowns.me", 0);
        if (cooldown > 0 && playerService.isOnCooldown(player.getUniqueId(), "rp_me", cooldown)) {
            int remaining = playerService.getRemainingCooldown(player.getUniqueId(), "rp_me", cooldown);
            String msg = plugin.getConfigManager().getMessagesConfig().getCooldownMessage()
                .replace("{remaining}", String.valueOf(remaining));
            Msg.send(player, ChatFormatter.parse(msg));
            return true;
        }

        if (args.length == 0) {
            sendUsage(player, "/me <действие>");
            return true;
        }

        String action = String.join(" ", args);
        String format = plugin.getConfigManager().getString("rp.me-format",
                "<gray>* </gray><white>{player}</white><gray> {action}</gray>");

        String displayName = RpUtil.getDisplayName(plugin, player);
        Component message = ChatFormatter.parse(
                format.replace("{player}", displayName)
                      .replace("{action}", action)
        );

        int radius = plugin.getConfigManager().getInt("rp.radius", 100);
        RpUtil.sendToRadius(player, message, radius);

        if (cooldown > 0) {
            playerService.setCooldown(player.getUniqueId(), "rp_me");
        }
        plugin.getLogger().info("[ME] " + player.getName() + ": " + action);
        return true;
    }
}
