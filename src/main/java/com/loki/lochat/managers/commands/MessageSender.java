package com.loki.lochat.managers.commands;

import com.loki.lochat.compat.Msg;

import com.loki.lochat.utils.format.ChatFormatter;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.title.Title;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.time.Duration;

public class MessageSender {
    
    private MessageSender() {
    }

    public static void sendChat(String target, Player sender, String message) {
        Component formatted = ChatFormatter.parse(message);

        switch (target.toLowerCase()) {
            case "player" -> Msg.send(sender, formatted);
            case "broadcast" -> Msg.broadcast(formatted);
            case "server" -> Bukkit.getOnlinePlayers().forEach(p -> Msg.send(p, formatted));
            default -> {
                Player targetPlayer = Bukkit.getPlayer(target);
                if (targetPlayer != null && targetPlayer.isOnline()) {
                    Msg.send(targetPlayer, formatted);
                }
            }
        }
    }
    
    public static void sendTitle(String target, Player sender, String message) {
        String[] parts = message.split("\\|", 2);
        String titleText = parts.length > 0 ? parts[0] : "";
        String subtitleText = parts.length > 1 ? parts[1] : "";

        Component title = ChatFormatter.parse(titleText);
        Component subtitle = ChatFormatter.parse(subtitleText);

        Title.Times times = Title.Times.times(
                Duration.ofMillis(500),
                Duration.ofMillis(3000),
                Duration.ofMillis(500)
        );

        switch (target.toLowerCase()) {
            case "player" -> Msg.title(sender, title, subtitle, times);
            case "broadcast", "server" -> Bukkit.getOnlinePlayers().forEach(p -> Msg.title(p, title, subtitle, times));
            default -> {
                Player targetPlayer = Bukkit.getPlayer(target);
                if (targetPlayer != null && targetPlayer.isOnline()) {
                    Msg.title(targetPlayer, title, subtitle, times);
                }
            }
        }
    }
    
    public static void sendActionBar(String target, Player sender, String message) {
        Component formatted = ChatFormatter.parse(message);
        
        switch (target.toLowerCase()) {
            case "player" -> Msg.actionBar(sender, formatted);
            case "broadcast", "server" -> Bukkit.getOnlinePlayers().forEach(p -> Msg.actionBar(p, formatted));
            default -> {
                Player targetPlayer = Bukkit.getPlayer(target);
                if (targetPlayer != null && targetPlayer.isOnline()) {
                    Msg.actionBar(targetPlayer, formatted);
                }
            }
        }
    }
}
