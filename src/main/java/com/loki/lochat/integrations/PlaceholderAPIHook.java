package com.loki.lochat.integrations;

import com.loki.lochat.LoChat;
import com.loki.lochat.api.service.ChatService;
import com.loki.lochat.api.service.MessagingService;
import com.loki.lochat.gradient.GradientModule;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;
import java.util.UUID;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;

public class PlaceholderAPIHook extends PlaceholderExpansion {

    private final LoChat plugin;
    private final MessagingService messagingService;
    private final ChatService chatService;

    public PlaceholderAPIHook(LoChat plugin) {
        this.plugin = plugin;
        this.messagingService = plugin.getServiceRegistry().get(MessagingService.class);
        this.chatService = plugin.getServiceRegistry().get(ChatService.class);
    }

    @Override
    public @NotNull String getIdentifier() {
        return "lochat";
    }

    @Override
    public @NotNull String getAuthor() {
        return "loki";
    }

    @Override
    public @NotNull String getVersion() {
        return plugin.getDescription().getVersion();
    }

    @Override
    public boolean persist() {
        return true;
    }

    @Override
    public @Nullable String onRequest(OfflinePlayer player, @NotNull String params) {
        if (player == null) {
            return "";
        }

        UUID uuid = player.getUniqueId();

        return switch (params.toLowerCase()) {
            case "ignored_count" -> String.valueOf(messagingService.getIgnoredCount(uuid));
            case "global_enabled" -> String.valueOf(!chatService.isGlobalChatDisabled(uuid));
            case "last_pm" -> resolveLastPm(player, uuid);
            case "spy_enabled" -> String.valueOf(messagingService.isSpying(uuid));
            case "gradient_full", "full" -> resolveGradient(player, GradientQuery.FULL);
            case "gradient_name", "name" -> resolveGradient(player, GradientQuery.NAME);
            case "gradient_prefix", "prefix" -> resolveGradient(player, GradientQuery.PREFIX);
            case "lp_prefix" -> resolveGradient(player, GradientQuery.LP_PREFIX);
            default -> null;
        };
    }

    private enum GradientQuery { FULL, NAME, PREFIX, LP_PREFIX }

    private String resolveGradient(OfflinePlayer player, GradientQuery query) {
        GradientModule gradient = plugin.getGradientModule();
        Player onlinePlayer = player.getPlayer();

        if (onlinePlayer == null) {
            return switch (query) {
                case FULL, NAME -> player.getName() != null ? player.getName() : "";
                case PREFIX, LP_PREFIX -> "";
            };
        }

        return switch (query) {
            case FULL -> {
                if (gradient == null || !gradient.isEnabled()) {
                    yield player.getName() != null ? player.getName() : "";
                }
                yield gradient.getFormattedNameForTab(onlinePlayer);
            }
            case NAME -> {
                if (gradient == null || !gradient.isEnabled()) {
                    yield player.getName();
                }
                yield gradient.getGradientNick(onlinePlayer);
            }
            case PREFIX -> {
                if (gradient == null || !gradient.isEnabled()) {
                    yield "";
                }
                yield gradient.getPrefix(onlinePlayer);
            }
            case LP_PREFIX -> {
                if (gradient == null || !gradient.isEnabled()) {
                    yield "";
                }
                yield gradient.getLuckPermsPrefix(onlinePlayer);
            }
        };
    }

    private String resolveLastPm(OfflinePlayer player, UUID uuid) {
        Optional<UUID> lastOpt = messagingService.getLastConversation(uuid);
        if (lastOpt.isEmpty()) {
            return "";
        }
        var lastPlayer = Bukkit.getOfflinePlayer(lastOpt.get());
        return lastPlayer.getName() != null ? lastPlayer.getName() : "";
    }
}
