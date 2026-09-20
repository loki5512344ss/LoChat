package com.loki.lochat.core.service;

import com.loki.lochat.compat.Msg;

import com.loki.lochat.api.service.NickService;
import com.loki.lochat.config.RatConfig;
import com.loki.lochat.utils.format.ChatFormatter;
import com.loki.lochat.utils.persistence.FilePersistence;
import com.loki.lochat.utils.platform.FoliaUtil;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Реализация сервиса кастомных ников
 */
public class NickServiceImpl implements NickService {
    private final JavaPlugin plugin;
    private final Map<UUID, String> nicknames = new ConcurrentHashMap<>();
    private FileConfiguration data;

    public NickServiceImpl(JavaPlugin plugin) {
        this.plugin = plugin;
        load();
    }

    private void load() {
        data = FilePersistence.loadYaml(plugin, "nicknames.yml");

        // Загружаем ники
        if (data.contains("nicknames")) {
            for (String key : data.getConfigurationSection("nicknames").getKeys(false)) {
                UUID uuid = UUID.fromString(key);
                String nickname = data.getString("nicknames." + key);
                nicknames.put(uuid, nickname);
            }
        }

        plugin.getLogger().info("Загружено " + nicknames.size() + " кастомных ников");
    }

    @Override
    public void save() {
        // Сохраняем ники
        data.set("nicknames", null); // Очищаем
        for (Map.Entry<UUID, String> entry : nicknames.entrySet()) {
            data.set("nicknames." + entry.getKey().toString(), entry.getValue());
        }

        FilePersistence.saveYaml(plugin, "nicknames.yml", data);
    }

    @Override
    public boolean setNickname(UUID player, String nickname) {
        // Валидация
        if (!isValidNickname(nickname)) {
            return false;
        }

        // Проверка уникальности
        if (isNicknameTaken(nickname)) {
            // Проверяем не свой ли это ник
            String currentNick = nicknames.get(player);
            if (currentNick == null || !stripColors(currentNick).equalsIgnoreCase(stripColors(nickname))) {
                return false;
            }
        }

        // Устанавливаем ник
        nicknames.put(player, nickname);

        // Персистим сразу — не теряем ники при падении сервера
        FoliaUtil.runAsync(plugin, this::save);

        // Обновляем display
        Player onlinePlayer = Bukkit.getPlayer(player);
        if (onlinePlayer != null) {
            updatePlayerDisplay(onlinePlayer);
        }

        return true;
    }

    @Override
    public void resetNickname(UUID player) {
        nicknames.remove(player);

        // Персистим сразу
        FoliaUtil.runAsync(plugin, this::save);

        // Сбрасываем display
        Player onlinePlayer = Bukkit.getPlayer(player);
        if (onlinePlayer != null) {
            Component reset = Component.text(onlinePlayer.getName());
            Msg.setDisplayName(onlinePlayer, reset);
            Msg.setPlayerListName(onlinePlayer, reset);
        }
    }

    @Override
    public Optional<String> getNickname(UUID player) {
        return Optional.ofNullable(nicknames.get(player));
    }

    @Override
    public boolean isNicknameTaken(String nickname) {
        String cleanNick = stripColors(nickname).toLowerCase();

        // Проверяем существующие ники
        for (String existingNick : nicknames.values()) {
            if (stripColors(existingNick).equalsIgnoreCase(cleanNick)) {
                return true;
            }
        }

        // Проверяем реальные ники онлайн игроков
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (player.getName().equalsIgnoreCase(cleanNick)) {
                return true;
            }
        }

        return false;
    }

    @Override
    public boolean isValidNickname(String nickname) {
        if (nickname == null || nickname.isEmpty()) {
            return false;
        }

        // Убираем цвета для проверки длины
        String cleanNick = stripColors(nickname);

        // Проверка длины
        if (cleanNick.length() < RatConfig.NICK_MIN_LENGTH || cleanNick.length() > RatConfig.NICK_MAX_LENGTH) {
            return false;
        }

        // Проверка символов (разрешены буквы, цифры, _, русские буквы)
        return cleanNick.matches("[a-zA-Zа-яА-Я0-9_]+");
    }

    @Override
    public void updatePlayerDisplay(Player player) {
        Optional<String> nickname = getNickname(player.getUniqueId());

        if (nickname.isPresent()) {
            // Парсим ник с цветами
            Component nickComponent = ChatFormatter.parse(nickname.get());
            Msg.setDisplayName(player, nickComponent);
            Msg.setPlayerListName(player, nickComponent);
        } else {
            // Используем обычное имя
            Component reset = Component.text(player.getName());
            Msg.setDisplayName(player, reset);
            Msg.setPlayerListName(player, reset);
        }
    }

    /**
     * Убирает цвета из строки
     */
    private String stripColors(String text) {
        Component component = ChatFormatter.parse(text);
        return PlainTextComponentSerializer.plainText().serialize(component);
    }
}
