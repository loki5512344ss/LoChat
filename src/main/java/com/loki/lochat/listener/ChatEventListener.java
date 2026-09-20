package com.loki.lochat.listener;

import com.loki.lochat.LoChat;
import com.loki.lochat.api.service.ChannelService;
import com.loki.lochat.api.service.MessageService;
import com.loki.lochat.api.service.PlayerService;
import com.loki.lochat.channel.Channel;
import com.loki.lochat.compat.Msg;
import com.loki.lochat.core.filter.AdvancedMessageFilter;
import com.loki.lochat.core.filter.FilterResult;
import com.loki.lochat.renderer.EnhancedChatRenderer;
import com.loki.lochat.translate.TranslationService;
import com.loki.lochat.utils.format.ChatFormatter;
import com.loki.lochat.utils.format.LuckPermsAffixes;

import net.kyori.adventure.text.Component;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashSet;
import java.util.Set;

public class ChatEventListener implements Listener {
    private final JavaPlugin plugin;
    private final MessageService messageService;
    private final PlayerService playerService;
    private final AdvancedMessageFilter advancedFilter;
    private final ChannelService channelService;

    public ChatEventListener(JavaPlugin plugin, MessageService messageService, PlayerService playerService,
                             AdvancedMessageFilter filter, ChannelService channelService) {
        this.plugin = plugin;
        this.messageService = messageService;
        this.playerService = playerService;
        this.advancedFilter = filter;
        this.channelService = channelService;
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onChat(AsyncPlayerChatEvent event) {
        Player sender = event.getPlayer();
        String rawMessage = event.getMessage();
        LoChat loChat = (LoChat) plugin;

        // Система каналов: если игрок в присоединённом канале — маршрутизируем туда
        Channel active = channelService.isEnabled() ? channelService.getActive(sender).orElse(null) : null;
        if (active != null) {
            handleChannelChat(event, sender, active, rawMessage);
            return;
        }

        // Определяем режим чата (глобальный/локальный)
        ChatMode mode = determineChatMode(loChat, rawMessage);
        if (mode == null) {
            event.setCancelled(true);
            Msg.send(sender, ChatFormatter.parse("&#CF6679Этот режим чата отключен!"));
            return;
        }

        String plainMessage = mode.message;
        boolean isGlobal = mode.isGlobal;

        // Проверки: мут, кулдаун, фильтры
        Component messageComponent = processFilters(event, sender, plainMessage, isGlobal);
        if (messageComponent == null) {
            return;
        }

        // Обработка локального чата (радиус)
        if (!isGlobal) {
            handleLocalChat(event, sender, loChat);
        }

        // Рендеринг и доставка
        EnhancedChatRenderer renderer = new EnhancedChatRenderer(plugin, isGlobal);
        Component displayName = buildDisplayName(sender);
        Set<Player> recipients = new HashSet<>(event.getRecipients());
        event.setCancelled(true);
        prefetchTranslation(sender, messageComponent, recipients);
        for (Player viewer : recipients) {
            Component rendered = renderer.render(sender, displayName, messageComponent, viewer);
            Msg.send(viewer, rendered);
        }
        Component consoleRendered = renderer.render(sender, displayName, messageComponent, null);
        Bukkit.getConsoleSender().sendMessage(Msg.plain(consoleRendered));
        loChat.getDiscordIntegration().sendChatMessage(sender, plainMessage, isGlobal);
        recordStatistics(loChat, sender, isGlobal);
    }

    /**
     * Маршрутизация сообщения в присоединённый (не-дефолтный) канал.
     */
    private void handleChannelChat(AsyncPlayerChatEvent event, Player sender, Channel channel, String rawMessage) {
        String plainMessage = rawMessage.stripLeading();

        // Кулдаун канала
        int cooldown = channel.cooldown();
        if (cooldown > 0 && !sender.hasPermission("chat.bypass.cooldown")) {
            String cooldownType = "channel_" + channel.id();
            if (playerService.isOnCooldown(sender.getUniqueId(), cooldownType, cooldown)) {
                int remaining = playerService.getRemainingCooldown(sender.getUniqueId(), cooldownType, cooldown);
                String msg = ((LoChat) plugin).getConfigManager().getMessagesConfig().getCooldownMessage()
                        .replace("{remaining}", String.valueOf(remaining));
                event.setCancelled(true);
                Msg.send(sender, ChatFormatter.parse(msg));
                return;
            }
            playerService.setCooldown(sender.getUniqueId(), cooldownType);
        }

        Component messageComponent = processFilters(event, sender, plainMessage, channel.isGlobalScope());
        if (messageComponent == null) {
            return;
        }

        // Радиус локального канала
        if (channel.isLocalScope()) {
            int radius = channel.radius();
            event.getRecipients().removeIf(p -> {
                try {
                    return !com.loki.lochat.utils.player.DistanceUtil.isInRange(sender, p, radius);
                } catch (Exception e) {
                    return false;
                }
            });
        }

        EnhancedChatRenderer renderer = new EnhancedChatRenderer(plugin, channel);
        Component displayName = buildDisplayName(sender);
        Set<Player> recipients = new HashSet<>(event.getRecipients());
        event.setCancelled(true);
        prefetchTranslation(sender, messageComponent, recipients);
        for (Player viewer : recipients) {
            Component rendered = renderer.render(sender, displayName, messageComponent, viewer);
            Msg.send(viewer, rendered);
        }
        Component consoleRendered = renderer.render(sender, displayName, messageComponent, null);
        Bukkit.getConsoleSender().sendMessage(Msg.plain(consoleRendered));
        LoChat loChat = (LoChat) plugin;
        loChat.getDiscordIntegration().sendChatMessage(sender, plainMessage, channel.isGlobalScope());
        playerService.recordMessage(sender.getUniqueId(), "channel_" + channel.id());
    }

    private ChatMode determineChatMode(LoChat loChat, String rawMessage) {
        boolean localEnabled = loChat.getConfigManager().isLocalEnabled();
        boolean isGlobal;
        String message;

        if (localEnabled) {
            isGlobal = rawMessage.startsWith("!");
            message = isGlobal ? rawMessage.substring(1).stripLeading() : rawMessage;
        } else {
            isGlobal = true;
            message = rawMessage;
        }

        // Проверка: включен ли режим
        if (isGlobal && !loChat.getConfigManager().isGlobalEnabled()) {
            return null;
        }
        if (!isGlobal && !localEnabled) {
            return null;
        }

        return new ChatMode(isGlobal, message);
    }

    private Component processFilters(AsyncPlayerChatEvent event, Player sender, String plainMessage, boolean isGlobal) {
        LoChat loChat = (LoChat) plugin;

        // Мут, кулдаун
        if (!messageService.processMessage(sender, plainMessage)) {
            event.setCancelled(true);
            return null;
        }

        // Капс, реклама, повторы
        FilterResult filterResult = advancedFilter.filterMessage(sender, plainMessage);
        if (!filterResult.allowed()) {
            event.setCancelled(true);
            Msg.send(sender, ChatFormatter.parse(filterResult.blockReason()));
            return null;
        }

        String filteredMessage = filterResult.filteredMessage();

        // Цвет текста сообщения
        Component messageComponent;
        if (sender.hasPermission("lochat.chat.colors")) {
            messageComponent = ChatFormatter.parse(filteredMessage);
        } else {
            String defaultColor = isGlobal
                    ? loChat.getConfigManager().getAppearanceConfig().getGlobalMessageColor()
                    : loChat.getConfigManager().getAppearanceConfig().getLocalMessageColor();
            messageComponent = ChatFormatter.parseWithDefaultMessageColor(filteredMessage, defaultColor);
        }

        return messageComponent;
    }

    private void handleLocalChat(AsyncPlayerChatEvent event, Player sender, LoChat loChat) {
        int radius = loChat.getConfigManager().getAppearanceConfig().getLocalRadius();

        event.getRecipients().removeIf(p -> {
            try {
                return !com.loki.lochat.utils.player.DistanceUtil.isInRange(sender, p, radius);
            } catch (Exception e) {
                return false;
            }
        });

        long recipients = event.getRecipients().stream()
                .filter(p -> !p.equals(sender))
                .count();

        if (recipients == 0) {
            String msg = loChat.getConfigManager().getMessagesConfig().getNobodyHeard();
            Bukkit.getScheduler().runTask(plugin, () -> Msg.send(sender, ChatFormatter.parse(msg)));
        }
    }

    private Component buildDisplayName(Player sender) {
        String legacyName = sender.getDisplayName();
        if (legacyName == null || legacyName.isBlank()) {
            return Component.text(sender.getName());
        }
        // LuckPerms prefix/suffix are added separately ({player_prefix}/{player_suffix}), do not show them twice
        legacyName = LuckPermsAffixes.withoutAffixes(legacyName, LuckPermsAffixes.prefix(sender),
                LuckPermsAffixes.suffix(sender));
        return ChatFormatter.parse(legacyName);
    }

    /** Translates the message for every viewer language before rendering (runs off the main thread only). */
    private void prefetchTranslation(Player sender, Component messageComponent, Set<Player> recipients) {
        try {
            LoChat loChat = (LoChat) plugin;
            var cfg = loChat.getConfigManager();
            TranslationService ts = loChat.getTranslationService();
            if (!cfg.isTranslationEnabled() || !ts.isEnabled() || Bukkit.isPrimaryThread()
                    || "actionbar".equalsIgnoreCase(cfg.getTranslationMode())
                    || sender.hasPermission(cfg.getTranslationBypassPermission())) {
                return;
            }
            Set<String> languages = new HashSet<>();
            for (Player viewer : recipients) {
                if (!viewer.equals(sender)) {
                    languages.add(TranslationService.localeToLanguageCode(viewer.getLocale()));
                }
            }
            if (!languages.isEmpty()) {
                ts.prefetch(Msg.plain(messageComponent), languages, cfg.getTranslationTimeoutMs());
            }
        } catch (Exception e) {
            plugin.getLogger().fine("Translation prefetch failed: " + e.getMessage());
        }
    }

    private void recordStatistics(LoChat loChat, Player sender, boolean isGlobal) {
        playerService.recordMessage(sender.getUniqueId(), isGlobal ? "global" : "local");
    }

    private record ChatMode(boolean isGlobal, String message) {
    }
}
