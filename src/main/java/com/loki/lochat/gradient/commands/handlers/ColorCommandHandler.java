package com.loki.lochat.gradient.commands.handlers;

import com.loki.lochat.gradient.GradientModule;
import com.loki.lochat.gradient.config.GradientMessages;
import com.loki.lochat.gradient.data.GradientPlayerData;
import com.loki.lochat.gradient.util.DisplayNameUtil;
import com.loki.lochat.gradient.util.GradientUtil;
import com.loki.lochat.utils.platform.FoliaUtil;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

/**
 * Обработчик команд для управления цветами
 */
public class ColorCommandHandler {
    
    private final GradientModule module;
    
    public ColorCommandHandler(GradientModule module) {
        this.module = module;
    }
    
    /**
     * Установить цвет игроку
     */
    @SuppressWarnings("deprecation")
    public void handleSetColor(CommandSender sender, String[] args, GradientMessages msg) {
        if (args.length < 3) {
            sender.sendMessage("§cИспользование: /aprefix setcolor <игрок> <цвет1> [цвет2] ...");
            return;
        }
        
        OfflinePlayer target = Bukkit.getOfflinePlayer(args[1]);
        if (!target.hasPlayedBefore() && !target.isOnline()) {
            msg.send(sender, "player-not-found");
            return;
        }

        List<String> colors = new ArrayList<>();
        for (int i = 2; i < args.length; i++) {
            String color = args[i].startsWith("#") ? args[i] : "#" + args[i];
            if (!GradientUtil.isValidHex(color)) {
                msg.send(sender, "invalid-color", "color", args[i]);
                return;
            }
            colors.add(color.toUpperCase());
        }

        if (colors.size() > module.getConfig().getMaxColors()) {
            msg.send(sender, "too-many-colors", "max", String.valueOf(module.getConfig().getMaxColors()));
            return;
        }

        GradientPlayerData data = module.getDataManager().getPlayerData(target.getUniqueId());
        data.setColors(colors);
        data.setColorEnabled(true);

        updatePlayerDisplay(target, data);
        savePlayerData(target);

        sender.sendMessage("§aЦвет для игрока §f" + args[1] + " §aустановлен: §f" + String.join(" ", colors));
    }
    
    /**
     * Включить/выключить цвет
     */
    @SuppressWarnings("deprecation")
    public void handleColorToggle(CommandSender sender, String[] args, boolean enable, GradientMessages msg) {
        if (args.length < 2) {
            sender.sendMessage("§cИспользование: /aprefix " + (enable ? "coloron" : "coloroff") + " <игрок>");
            return;
        }

        OfflinePlayer target = Bukkit.getOfflinePlayer(args[1]);
        if (!target.hasPlayedBefore() && !target.isOnline()) {
            msg.send(sender, "player-not-found");
            return;
        }

        GradientPlayerData data = module.getDataManager().getPlayerData(target.getUniqueId());
        data.setColorEnabled(enable);

        updatePlayerDisplay(target, data);
        savePlayerData(target);

        sender.sendMessage("§aЦвет для игрока §f" + args[1] + (enable ? " §aвключён" : " §cвыключен"));
    }
    
    /**
     * Сбросить цвет
     */
    @SuppressWarnings("deprecation")
    public void handleResetColor(CommandSender sender, String[] args, GradientMessages msg) {
        if (args.length < 2) {
            sender.sendMessage("§cИспользование: /aprefix resetcolor <игрок>");
            return;
        }

        OfflinePlayer target = Bukkit.getOfflinePlayer(args[1]);
        if (!target.hasPlayedBefore() && !target.isOnline()) {
            msg.send(sender, "player-not-found");
            return;
        }

        GradientPlayerData data = module.getDataManager().getPlayerData(target.getUniqueId());
        data.setColors(new ArrayList<>());
        data.setColorEnabled(false);

        updatePlayerDisplay(target, data);
        savePlayerData(target);

        sender.sendMessage("§aЦвет для игрока §f" + args[1] + " §aсброшен");
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
