package com.loki.lochat.commands.rp;

import com.loki.lochat.compat.Msg;

import com.loki.lochat.LoChat;
import com.loki.lochat.commands.base.PlayerCommand;
import com.loki.lochat.config.RatConfig;
import com.loki.lochat.utils.format.ChatFormatter;

import net.kyori.adventure.text.Component;

import org.bukkit.command.Command;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Random;

/**
 * /roll [максимум]
 * Бросает кубик от 1 до максимума (по умолчанию 100).
 * Формат: * ИгрокИмя бросает кубик и получает N из M
 */
public class RollCommand extends PlayerCommand {

    private static final Random RANDOM = new Random();

    public RollCommand(LoChat plugin) {
        super(plugin);
    }

    @Override
    protected boolean executePlayerCommand(@NotNull Player player, @NotNull Command command,
                                           @NotNull String label, @NotNull String[] args) {
        if (!requirePermission(player, "lochat.rp.roll")) {
            return true;
        }

        int max = RatConfig.ROLL_DEFAULT_MAX;

        if (args.length >= 1) {
            try {
                max = Integer.parseInt(args[0]);
                if (max < 2) {
                    Msg.send(player, ChatFormatter.parse(
                            plugin.getConfigManager().getString("rp.roll-min-error",
                                    "<red>Минимальное значение кубика: 2</red>")));
                    return true;
                }
                if (max > RatConfig.ROLL_ABSOLUTE_MAX) {
                    Msg.send(player, ChatFormatter.parse(
                            plugin.getConfigManager().getString("rp.roll-max-error",
                                    "<red>Максимальное значение кубика: 1000000</red>")));
                    return true;
                }
            } catch (NumberFormatException e) {
                Msg.send(player, ChatFormatter.parse(
                        plugin.getConfigManager().getString("rp.roll-invalid-number",
                                "<red>Укажите число: /roll [максимум]</red>")));
                return true;
            }
        }

        int result = RANDOM.nextInt(max) + 1; // 1..max включительно

        String format = plugin.getConfigManager().getString("rp.roll-format", "<gray>* </gray>"
                + "<white>{player}</white><gray> бросает кубик и получает </gray><yellow><bold>{result}</bold></yellow>"
                + "<gray> из </gray><yellow>{max}</yellow>");

        String displayName = RpUtil.getDisplayName(plugin, player);
        Component message = ChatFormatter.parse(
                format.replace("{player}", displayName)
                      .replace("{result}", String.valueOf(result))
                      .replace("{max}", String.valueOf(max))
        );

        int radius = plugin.getConfigManager().getInt("rp.radius", 100);
        RpUtil.sendToRadius(player, message, radius);

        plugin.getLogger().info("[ROLL] " + player.getName() + ": " + result + "/" + max);
        return true;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull org.bukkit.command.CommandSender sender,
                                                @NotNull Command command, @NotNull String alias,
                                                @NotNull String[] args) {
        if (args.length == 1) {
            return List.of("6", "10", "20", "100");
        }
        return List.of();
    }
}
