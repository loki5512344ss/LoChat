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
 * /do <описание сцены>
 * Описывает событие в мире от третьего лица.
 * Формат: [Сцена] описание (Игрок)
 */
public class DoCommand extends PlayerCommand {

    private final PlayerService playerService;

    public DoCommand(LoChat plugin) {
        super(plugin);
        this.playerService = plugin.getServiceRegistry().get(PlayerService.class);
    }

    @Override
    protected boolean executePlayerCommand(@NotNull Player player, @NotNull Command command,
                                           @NotNull String label, @NotNull String[] args) {
        if (!requirePermission(player, "lochat.rp.do")) {
            return true;
        }

        int cooldown = plugin.getConfigManager().getInt("rp.cooldowns.do", 0);
        if (cooldown > 0 && playerService.isOnCooldown(player.getUniqueId(), "rp_do", cooldown)) {
            int remaining = playerService.getRemainingCooldown(player.getUniqueId(), "rp_do", cooldown);
            String msg = plugin.getConfigManager().getMessagesConfig().getCooldownMessage()
                .replace("{remaining}", String.valueOf(remaining));
            Msg.send(player, ChatFormatter.parse(msg));
            return true;
        }

        if (args.length == 0) {
            sendUsage(player, "/do <описание сцены>");
            return true;
        }

        String description = String.join(" ", args);
        String format = plugin.getConfigManager().getString("rp.do-format", "<dark_gray>[</dark_gray>"
                + "<gray>Сцена</gray><dark_gray>]</dark_gray> <white>{description}</white> <dark_gray>({player})</dark_gray>");

        String displayName = RpUtil.getDisplayName(plugin, player);
        Component message = ChatFormatter.parse(
                format.replace("{player}", displayName)
                      .replace("{description}", description)
        );

        int radius = plugin.getConfigManager().getInt("rp.radius", 100);
        RpUtil.sendToRadius(player, message, radius);

        if (cooldown > 0) {
            playerService.setCooldown(player.getUniqueId(), "rp_do");
        }
        plugin.getLogger().info("[DO] " + player.getName() + ": " + description);
        return true;
    }
}
