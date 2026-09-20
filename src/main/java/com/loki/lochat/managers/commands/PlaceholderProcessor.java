package com.loki.lochat.managers.commands;

import org.bukkit.entity.Player;

public class PlaceholderProcessor {
    
    private PlaceholderProcessor() {
    }

    public static String process(String message, Player player, String[] args) {
        String result = message;
        
        // Базовые плейсхолдеры
        result = result.replace("{player}", player.getName());
        result = result.replace("{displayname}", player.getDisplayName());
        result = result.replace("{world}", player.getWorld().getName());
        result = result.replace("{x}", String.valueOf(player.getLocation().getBlockX()));
        result = result.replace("{y}", String.valueOf(player.getLocation().getBlockY()));
        result = result.replace("{z}", String.valueOf(player.getLocation().getBlockZ()));
        result = result.replace("{health}", String.valueOf(Math.round(player.getHealth())));
        result = result.replace("{food}", String.valueOf(player.getFoodLevel()));
        result = result.replace("{level}", String.valueOf(player.getLevel()));
        result = result.replace("{exp}", String.valueOf(player.getExp()));
        
        // Аргументы команды
        for (int i = 0; i < args.length; i++) {
            result = result.replace("{arg" + i + "}", args[i]);
        }
        
        // Все аргументы вместе
        if (args.length > 0) {
            result = result.replace("{args}", String.join(" ", args));
        } else {
            result = result.replace("{args}", "");
        }
        
        return result;
    }
}
