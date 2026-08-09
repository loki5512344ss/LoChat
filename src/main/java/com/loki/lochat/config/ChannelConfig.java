package com.loki.lochat.config;

import com.loki.lochat.channel.Channel;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Конфигурация каналов чата (config/channels.yml).
 */
public class ChannelConfig extends BaseConfig {

    private volatile Map<String, Channel> channels = new LinkedHashMap<>();
    private volatile Map<String, Channel> byAlias = new LinkedHashMap<>();
    private volatile Channel defaultChannel;

    public ChannelConfig(JavaPlugin plugin) {
        super(plugin, "channels.yml", true);
    }

    @Override
    protected void onLoad() {
        loadChannels();
        buildAliasIndex();
    }

    private void loadChannels() {
        Map<String, Channel> result = new LinkedHashMap<>();
        ConfigurationSection section = getConfig().getConfigurationSection("channels.list");
        if (section != null) {
            for (String id : section.getKeys(false)) {
                String path = "channels.list." + id;
                result.put(id, new Channel(
                        id,
                        getConfig().getString(path + ".display", ""),
                        getConfig().getString(path + ".prefix", id.toUpperCase(Locale.ROOT)),
                        getConfig().getString(path + ".color", "&7"),
                        getConfig().getInt(path + ".radius", -1),
                        getConfig().getInt(path + ".cooldown", 0),
                        getConfig().getString(path + ".permission", ""),
                        getConfig().getStringList(path + ".aliases")));
            }
        }
        this.channels = result;
    }

    private void buildAliasIndex() {
        Map<String, Channel> index = new LinkedHashMap<>();
        for (Channel channel : channels.values()) {
            index.put(channel.id().toLowerCase(Locale.ROOT), channel);
            for (String alias : channel.aliases()) {
                if (alias == null || alias.isBlank()) {
                    continue;
                }
                index.putIfAbsent(alias.toLowerCase(Locale.ROOT), channel);
            }
        }
        this.byAlias = index;

        String defaultId = getConfig().getString("channels.default", "global");
        this.defaultChannel = channels.get(defaultId);
        if (defaultChannel == null) {
            this.defaultChannel = channels.isEmpty() ? null : channels.values().iterator().next();
        }
    }

    public boolean isEnabled() {
        return getConfig().getBoolean("channels.enabled", true);
    }

    public Channel getDefault() {
        return defaultChannel;
    }

    public Channel get(String id) {
        return id == null ? null : channels.get(id.toLowerCase(Locale.ROOT));
    }

    /**
     * Поиск канала по имени или алиасу (без учёта регистра).
     */
    public Channel find(String name) {
        return name == null ? null : byAlias.get(name.toLowerCase(Locale.ROOT));
    }

    public Collection<Channel> getChannels() {
        return channels.values();
    }

    public List<Channel> getChannelList() {
        return List.copyOf(channels.values());
    }
}