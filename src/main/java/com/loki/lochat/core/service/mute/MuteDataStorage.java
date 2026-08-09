package com.loki.lochat.core.service.mute;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.loki.lochat.data.model.MuteData;
import com.loki.lochat.utils.persistence.FilePersistence;
import com.loki.lochat.utils.platform.FoliaUtil;

import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Хранилище данных о мутах
 */
public class MuteDataStorage {
    
    private final JavaPlugin plugin;
    private final Map<UUID, MuteData> mutes = new ConcurrentHashMap<>();
    private final File dataFile;
    private final Gson gson = new GsonBuilder().setPrettyPrinting().create();
    
    public MuteDataStorage(JavaPlugin plugin) {
        this.plugin = plugin;
        this.dataFile = new File(plugin.getDataFolder(), "mutes.json");
    }
    
    public void save(MuteData data) {
        mutes.put(data.getUuid(), data);
        saveAsync();
    }

    public MuteData remove(UUID uuid) {
        MuteData removed = mutes.remove(uuid);
        if (removed != null) {
            saveAsync();
        }
        return removed;
    }

    public MuteData get(UUID uuid) {
        return mutes.get(uuid);
    }

    public Map<UUID, MuteData> getActiveMutes() {
        mutes.entrySet().removeIf(entry -> isExpired(entry.getValue()));
        return new HashMap<>(mutes);
    }
    
    private boolean isExpired(MuteData data) {
        return data.getEndTime() > 0 && System.currentTimeMillis() > data.getEndTime();
    }
    
    public void load() {
        if (!dataFile.exists()) {
            return;
        }

        try (Reader reader = new FileReader(dataFile)) {
            Type type = new TypeToken<Map<String, MuteData>>() { }.getType();
            Map<String, MuteData> loaded = gson.fromJson(reader, type);
            if (loaded != null) {
                loaded.forEach((key, value) -> {
                    try {
                        mutes.put(UUID.fromString(key), value);
                    } catch (IllegalArgumentException ignored) {
                    }
                });
            }
        } catch (IOException e) {
            plugin.getLogger().warning("Error loading mutes: " + e.getMessage());
        }
    }
    
    public void saveToFile() {
        save();
    }

    private void save() {
        Map<String, MuteData> toSave = new HashMap<>();
        mutes.forEach((uuid, data) -> toSave.put(uuid.toString(), data));
        FilePersistence.saveJson(plugin, "mutes.json", toSave, gson);
    }
    
    private void saveAsync() {
        FoliaUtil.runAsync(plugin, this::save);
    }
}
