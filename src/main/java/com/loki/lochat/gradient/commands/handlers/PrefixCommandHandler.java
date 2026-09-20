package com.loki.lochat.gradient.commands.handlers;

import com.loki.lochat.gradient.GradientModule;
import com.loki.lochat.gradient.config.GradientMessages;
import com.loki.lochat.gradient.data.GradientPlayerData;
import com.loki.lochat.gradient.util.DisplayNameUtil;
import com.loki.lochat.utils.platform.FoliaUtil;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * Обработчик команд для управления префиксами
 */
public class PrefixCommandHandler {
    
    private final GradientModule module;
    
    public PrefixCommandHandler(GradientModule module) {
        this.module = module;
    }
    
    /**
     * Установить префикс игроку
     */
    @SuppressWarnings("deprecation")
    public void handleSetPrefix(CommandSender sender, String[] args, GradientMessages msg) {
        if (args.length < 3) {
            sender.sendMessage("§cИспользование: /aprefix setprefix <игрок> <префикс>");
            return;
        }
        
        OfflinePlayer target = Bukkit.getOfflinePlayer(args[1]);
        if (!target.hasPlayedBefore() && !target.isOnline()) {
            msg.send(sender, "player-not-found");
            return;
        }

        StringBuilder prefixBuilder = new StringBuilder();
        for (int i = 2; i < args.length; i++) {
            if (i > 2) {
                prefixBuilder.append(" ");
            }
            prefixBuilder.append(args[i]);
        }
        String prefix = prefixBuilder.toString();

        if (prefix.length() > module.getConfig().getMaxPrefixLength()) {
            msg.send(sender, "prefix-too-long", "max", String.valueOf(module.getConfig().getMaxPrefixLength()));
            return;
        }

        GradientPlayerData data = module.getDataManager().getPlayerData(target.getUniqueId());
        data.setPrefix(prefix);
        data.setPrefixEnabled(true);
        data.setPrefixPurchased(true);

        updatePlayerDisplay(target, data);
        savePlayerData(target);

        sender.sendMessage("§aПрефикс для игрока §f" + args[1] + " §aустановлен: §f" + prefix);
    }
    
    /**
     * Включить/выключить префикс
     */
    @SuppressWarnings("deprecation")
    public void handlePrefixToggle(CommandSender sender, String[] args, boolean enable, GradientMessages msg) {
        if (args.length < 2) {
            sender.sendMessage("§cИспользование: /aprefix " + (enable ? "prefixon" : "prefixoff") + " <игрок>");
            return;
        }

        OfflinePlayer target = Bukkit.getOfflinePlayer(args[1]);
        if (!target.hasPlayedBefore() && !target.isOnline()) {
            msg.send(sender, "player-not-found");
            return;
        }

        GradientPlayerData data = module.getDataManager().getPlayerData(target.getUniqueId());
        data.setPrefixEnabled(enable);

        updatePlayerDisplay(target, data);
        savePlayerData(target);

        sender.sendMessage("§aПрефикс для игрока §f" + args[1] + (enable ? " §aвключён" : " §cвыключен"));
    }
    
    /**
     * Сбросить префикс
     */
    @SuppressWarnings("deprecation")
    public void handleResetPrefix(CommandSender sender, String[] args, GradientMessages msg) {
        if (args.length < 2) {
            sender.sendMessage("§cИспользование: /aprefix resetprefix <игрок>");
            return;
        }

        OfflinePlayer target = Bukkit.getOfflinePlayer(args[1]);
        if (!target.hasPlayedBefore() && !target.isOnline()) {
            msg.send(sender, "player-not-found");
            return;
        }

        GradientPlayerData data = module.getDataManager().getPlayerData(target.getUniqueId());
        data.setPrefix(null);
        data.setPrefixEnabled(false);

        updatePlayerDisplay(target, data);
        savePlayerData(target);

        sender.sendMessage("§aПрефикс для игрока §f" + args[1] + " §aсброшен");
    }
    
    private void updatePlayerDisplay(OfflinePlayer target, GradientPlayerData data) {
        Player onlineTarget = target.getPlayer();
        if (onlineTarget != null) {
            FoliaUtil.runEntityTask(module.getPlugin(), onlineTarget,
                () -> DisplayNameUtil.updateDisplayName(module, onlineTarget, data));
        }
    }
    
    private void savePlayerData(OfflinePlayer target) {
        FoliaUtil.runAsync(module.getPlugin(),
            () -> module.getDataManager().savePlayerData(target.getUniqueId()));
    }
}
