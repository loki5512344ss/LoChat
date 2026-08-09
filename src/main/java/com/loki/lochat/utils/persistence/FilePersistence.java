package com.loki.lochat.utils.persistence;

import com.google.gson.Gson;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;

public final class FilePersistence {

    private static final ConcurrentHashMap<String, ReentrantLock> FILE_LOCKS = new ConcurrentHashMap<>();

    private FilePersistence() {}

    public static File getFile(JavaPlugin plugin, String name) {
        return new File(plugin.getDataFolder(), name);
    }

    public static void ensureDataFolder(JavaPlugin plugin) {
        plugin.getDataFolder().mkdirs();
    }

    public static FileConfiguration loadYaml(JavaPlugin plugin, String name) {
        File file = getFile(plugin, name);
        return file.exists() ? YamlConfiguration.loadConfiguration(file) : new YamlConfiguration();
    }

    public static void saveYaml(JavaPlugin plugin, String name, FileConfiguration config) {
        File target = getFile(plugin, name);
        ReentrantLock lock = lockFor(target);
        lock.lock();
        try {
            target.getParentFile().mkdirs();
            File tmp = new File(target.getParentFile(), target.getName() + ".tmp");
            config.save(tmp);
            moveIntoPlace(tmp, target);
        } catch (IOException e) {
            plugin.getLogger().warning("Failed to save " + name + ": " + e.getMessage());
        } finally {
            lock.unlock();
        }
    }

    public static <T> T loadJson(JavaPlugin plugin, String name, Class<T> clazz, Gson gson) {
        File file = getFile(plugin, name);
        if (!file.exists()) {
            return null;
        }
        try (Reader reader = new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8)) {
            return gson.fromJson(reader, clazz);
        } catch (IOException e) {
            plugin.getLogger().warning("Failed to load " + name + ": " + e.getMessage());
            return null;
        }
    }

    public static void saveJson(JavaPlugin plugin, String name, Object data, Gson gson) {
        File target = getFile(plugin, name);
        ReentrantLock lock = lockFor(target);
        lock.lock();
        try {
            target.getParentFile().mkdirs();
            File tmp = new File(target.getParentFile(), target.getName() + ".tmp");
            try (Writer writer = new OutputStreamWriter(new FileOutputStream(tmp), StandardCharsets.UTF_8)) {
                gson.toJson(data, writer);
            }
            moveIntoPlace(tmp, target);
        } catch (IOException e) {
            plugin.getLogger().warning("Failed to save " + name + ": " + e.getMessage());
        } finally {
            lock.unlock();
        }
    }

    private static ReentrantLock lockFor(File target) {
        return FILE_LOCKS.computeIfAbsent(target.getAbsolutePath(), k -> new ReentrantLock());
    }

    private static void moveIntoPlace(File tmp, File target) throws IOException {
        try {
            Files.move(tmp.toPath(), target.toPath(),
                    StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING);
        } catch (AtomicMoveNotSupportedException e) {
            Files.move(tmp.toPath(), target.toPath(), StandardCopyOption.REPLACE_EXISTING);
        }
    }
}
