package com.loki.lochat.core.service;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.loki.lochat.api.service.PunishmentService;
import com.loki.lochat.config.MessagesConfig;
import com.loki.lochat.data.model.BanRecord;
import com.loki.lochat.data.model.WarnEntry;
import com.loki.lochat.utils.format.ChatFormatter;
import com.loki.lochat.utils.format.TimeFormatter;
import com.loki.lochat.utils.persistence.FilePersistence;
import com.loki.lochat.utils.platform.FoliaUtil;

import net.kyori.adventure.text.Component;

import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Варны и баны в punishments.json
 */
public class PunishmentServiceImpl implements PunishmentService {

    private final JavaPlugin plugin;
    private final MessagesConfig messages;
    private final Gson gson = new GsonBuilder().setPrettyPrinting().create();

    private final Map<UUID, List<WarnEntry>> warns = new ConcurrentHashMap<>();
    private final Map<UUID, BanRecord> bans = new ConcurrentHashMap<>();

    public PunishmentServiceImpl(JavaPlugin plugin, MessagesConfig messagesConfig) {
        this.plugin = plugin;
        this.messages = messagesConfig;
        load();
    }

    @Override
    public void addWarn(UUID uuid, String playerName, String moderator, String reason, boolean silent) {
        WarnEntry entry = new WarnEntry(System.currentTimeMillis(), moderator, reason, silent);
        warns.computeIfAbsent(uuid, k -> Collections.synchronizedList(new ArrayList<>())).add(entry);
        saveAsync();
    }

    @Override
    public List<WarnEntry> getWarns(UUID uuid) {
        List<WarnEntry> list = warns.get(uuid);
        if (list == null) {
            return List.of();
        }
        synchronized (list) {
            return List.copyOf(list);
        }
    }

    @Override
    public int getWarnCount(UUID uuid) {
        List<WarnEntry> list = warns.get(uuid);
        return list == null ? 0 : list.size();
    }

    @Override
    public void ban(UUID uuid, String playerName, long durationMs, String reason, String bannedBy) {
        long until = durationMs <= 0 ? 0 : System.currentTimeMillis() + durationMs;
        BanRecord record = new BanRecord(uuid, playerName, reason, bannedBy, until, System.currentTimeMillis());
        bans.put(uuid, record);
        saveAsync();
    }

    @Override
    public boolean unban(UUID uuid) {
        BanRecord removed = bans.remove(uuid);
        if (removed != null) {
            saveAsync();
            return true;
        }
        return false;
    }

    @Override
    public boolean isBanned(UUID uuid) {
        BanRecord b = bans.get(uuid);
        if (b == null) {
            return false;
        }
        if (b.isPermanent()) {
            return true;
        }
        if (b.isExpired()) {
            bans.remove(uuid, b);
            return false;
        }
        return true;
    }

    @Override
    public BanRecord getActiveBan(UUID uuid) {
        if (!isBanned(uuid)) {
            return null;
        }
        return bans.get(uuid);
    }

    @Override
    public Component buildBanKickMessage(BanRecord ban) {
        String reason = ban.reason != null ? ban.reason : messages.getModerationDefaultReason();
        String mod = ban.bannedBy != null ? ban.bannedBy : "?";
        String duration;
        if (ban.isPermanent()) {
            duration = messages.getModerationDurationPermanent();
        } else {
            long left = Math.max(0, ban.until - System.currentTimeMillis());
            duration = TimeFormatter.format(left);
        }
        String template = messages.getBanKickMessage();
        String filled = template
                .replace("{reason}", reason)
                .replace("{moderator}", mod)
                .replace("{duration}", duration)
                .replace("{player}", ban.playerName != null ? ban.playerName : "?");
        return ChatFormatter.parse(filled);
    }

    @Override
    public void save() {
        PunishmentSnapshot snap = new PunishmentSnapshot();
        for (Map.Entry<UUID, List<WarnEntry>> e : warns.entrySet()) {
            List<WarnEntry> list = e.getValue();
            synchronized (list) {
                snap.getWarns().put(e.getKey().toString(), new ArrayList<>(list));
            }
        }
        for (Map.Entry<UUID, BanRecord> e : bans.entrySet()) {
            snap.getBans().put(e.getKey().toString(), e.getValue());
        }
        FilePersistence.saveJson(plugin, "punishments.json", snap, gson);
    }

    @Override
    public void reload() {
        warns.clear();
        bans.clear();
        load();
    }

    private void saveAsync() {
        FoliaUtil.runAsync(plugin, this::save);
    }

    private void load() {
        PunishmentSnapshot snap = FilePersistence.loadJson(plugin, "punishments.json", PunishmentSnapshot.class, gson);
        if (snap == null) {
            return;
        }
        if (snap.getWarns() != null) {
            for (Map.Entry<String, List<WarnEntry>> e : snap.getWarns().entrySet()) {
                try {
                    warns.put(UUID.fromString(e.getKey()), Collections.synchronizedList(new ArrayList<>(e.getValue())));
                } catch (IllegalArgumentException ignored) {
                }
            }
        }
        if (snap.getBans() != null) {
            for (Map.Entry<String, BanRecord> e : snap.getBans().entrySet()) {
                try {
                    UUID id = UUID.fromString(e.getKey());
                    BanRecord br = e.getValue();
                    if (br != null) {
                        br.uuid = id;
                        if (!br.isPermanent() && br.isExpired()) {
                            continue;
                        }
                        bans.put(id, br);
                    }
                } catch (IllegalArgumentException ignored) {
                }
            }
        }
    }

    private static class PunishmentSnapshot {
        private Map<String, List<WarnEntry>> warns = new HashMap<>();
        private Map<String, BanRecord> bans = new HashMap<>();

        Map<String, List<WarnEntry>> getWarns() {
            return warns;
        }

        Map<String, BanRecord> getBans() {
            return bans;
        }
    }
}
