package com.loki.lochat.managers;

import com.loki.lochat.compat.Msg;

import com.loki.lochat.LoChat;
import com.loki.lochat.utils.format.ChatFormatter;

import net.kyori.adventure.text.Component;

import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.scheduler.BukkitTask;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;

public class AutoMessageManager {

    private final LoChat plugin;
    private final Random random = new Random();
    private final AtomicInteger currentIndex = new AtomicInteger(0);

    private BukkitTask scheduledTask;

    private Map<String, List<String>> messages;
    private List<String> messageOrder;
    private String prefix;
    private String mode;

    public AutoMessageManager(LoChat plugin) {
        this.plugin = plugin;
        loadMessages();
    }

    private void loadMessages() {
        FileConfiguration config = plugin.getConfig();

        // Настройки
        prefix = plugin.getConfigManager().getString("prefix", "&#00D4FF[LoChat]");
        mode = config.getBoolean("automessages.random", false) ? "random" : "sequential";

        // Загружаем сообщения из config.yml
        messages = new LinkedHashMap<>();
        ConfigurationSection msgSection = config.getConfigurationSection("automessages.messages");

        if (msgSection != null) {
            for (String key : msgSection.getKeys(false)) {
                List<String> lines = new ArrayList<>();

                if (msgSection.isList(key)) {
                    lines.addAll(msgSection.getStringList(key));
                } else {
                    String single = msgSection.getString(key);
                    if (single != null) {
                        lines.add(single);
                    }
                }

                if (!lines.isEmpty()) {
                    messages.put(key, lines);
                }
            }
        }

        messageOrder = new ArrayList<>(messages.keySet());

        plugin.getLogger().info("Загружено " + messages.size() + " автосообщений");
    }

    public void start() {
        if (!plugin.getConfigManager().isAutoMessagesEnabled()) {
            return;
        }

        if (messages == null || messages.isEmpty()) {
            plugin.getLogger().warning("Автосообщения не настроены!");
            return;
        }

        // ✅ FIX: Отменяем старую задачу перед созданием новой
        stop();

        int intervalSeconds = plugin.getConfigManager().getAutoMessagesInterval();
        long intervalTicks = intervalSeconds * 20L;

        scheduledTask = Bukkit.getScheduler().runTaskTimerAsynchronously(
            plugin,
            this::broadcastNextMessage,
            intervalTicks,
            intervalTicks
        );

        plugin.getLogger().info("Автосообщения запущены (интервал: " + intervalSeconds + " сек, режим: " + mode + ")");
    }

    public void stop() {
        if (scheduledTask != null) {
            scheduledTask.cancel();
            scheduledTask = null;
        }
    }

    private void broadcastNextMessage() {
        if (messageOrder.isEmpty() || Bukkit.getOnlinePlayers().isEmpty()) {
            return;
        }

        String messageKey;

        if ("random".equals(mode)) {
            messageKey = messageOrder.get(random.nextInt(messageOrder.size()));
        } else {
            int index = currentIndex.getAndUpdate(i -> (i + 1) % messageOrder.size());
            messageKey = messageOrder.get(index);
        }

        List<String> lines = messages.get(messageKey);
        if (lines == null || lines.isEmpty()) {
            return;
        }

        boolean isFirstLine = true;
        for (String line : lines) {
            // Заменяем плейсхолдеры
            String processed = line.replace("%online%", String.valueOf(Bukkit.getOnlinePlayers().size()));

            // Добавляем префикс к первой непустой строке
            Component formatted;
            if (isFirstLine && !line.trim().isEmpty()) {
                formatted = ChatFormatter.parse(prefix + " " + processed);
                isFirstLine = false;
            } else {
                formatted = ChatFormatter.parse(processed);
            }

            for (var player : Bukkit.getOnlinePlayers()) {
                Msg.send(player, formatted);
            }
        }
    }

    public void reload() {
        stop();
        loadMessages();
        currentIndex.set(0);
        start();
    }
}
