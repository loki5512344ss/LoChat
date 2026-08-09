package com.loki.lochat.config;

import com.loki.lochat.LoChat;
import com.loki.lochat.config.core.ConfigAccessor;
import com.loki.lochat.config.core.ConfigLoader;

import java.util.List;

/**
 * Менеджер конфигураций - координатор
 * Делегирует загрузку ConfigLoader, доступ ConfigAccessor
 */
public class ConfigManager {

    private final LoChat plugin;
    private final ConfigLoader loader;
    private final ConfigAccessor accessor;

    private volatile AppearanceConfig appearanceConfig;
    private volatile MessagesConfig messagesConfig;
    private volatile MuteConfig muteConfig;
    private volatile FiltersConfig filtersConfig;
    private volatile SoundsConfig soundsConfig;
    private volatile ChannelConfig channelConfig;

    public ConfigManager(LoChat plugin) {
        this.plugin = plugin;
        this.loader = new ConfigLoader(plugin);
        this.accessor = new ConfigAccessor(plugin.getConfig());

        // Инициализируем конфигурации
        this.appearanceConfig = new AppearanceConfig(plugin);
        this.messagesConfig = new MessagesConfig(plugin);
        this.muteConfig = new MuteConfig(plugin);
        this.filtersConfig = new FiltersConfig(plugin);
        this.soundsConfig = new SoundsConfig(plugin);
        this.channelConfig = new ChannelConfig(plugin);

        // Инициализируем все конфиги
        this.appearanceConfig.init();
        this.messagesConfig.init();
        this.muteConfig.init();
        this.filtersConfig.init();
        this.soundsConfig.init();
        this.channelConfig.init();

        loader.updateConfig();
    }

    // ========== Геттеры конфигураций ==========

    public AppearanceConfig getAppearanceConfig() {
        return appearanceConfig;
    }

    public MessagesConfig getMessagesConfig() {
        return messagesConfig;
    }

    public MuteConfig getMuteConfig() {
        return muteConfig;
    }

    public FiltersConfig getFiltersConfig() {
        return filtersConfig;
    }

    public SoundsConfig getSoundsConfig() {
        return soundsConfig;
    }

    public ChannelConfig getChannelConfig() {
        return channelConfig;
    }

    // ========== Обновление и перезагрузка ==========

    public synchronized void reload() {
        loader.reload();
        accessor.updateConfig(plugin.getConfig());

        this.appearanceConfig.reload();
        this.messagesConfig.reload();
        this.muteConfig.reload();
        this.filtersConfig.reload();
        this.soundsConfig.reload();
        this.channelConfig.reload();

        plugin.getLogger().info("Все конфигурации успешно перезагружены");
    }

    // ========== Базовые методы доступа ==========

    public String getString(String path, String defaultValue) {
        return accessor.getString(path, defaultValue);
    }

    public String getString(String path) {
        return accessor.getString(path);
    }

    public int getInt(String path, int defaultValue) {
        return accessor.getInt(path, defaultValue);
    }

    public boolean getBoolean(String path, boolean defaultValue) {
        return accessor.getBoolean(path, defaultValue);
    }

    public List<String> getStringList(String path) {
        return accessor.getStringList(path);
    }

    // ========== Настройки чата ==========

    public boolean isGlobalEnabled() {
        return accessor.isGlobalEnabled();
    }

    public int getGlobalCooldown() {
        return accessor.getGlobalCooldown();
    }

    public boolean isLocalEnabled() {
        return accessor.isLocalEnabled();
    }

    public int getLocalRadius() {
        return appearanceConfig.getLocalRadius();
    }

    public int getLocalCooldown() {
        return accessor.getLocalCooldown();
    }

    // ========== PM ==========

    public boolean isPmEnabled() {
        return accessor.isPmEnabled();
    }

    public boolean isPmLogEnabled() {
        return accessor.isPmLogEnabled();
    }

    public boolean isPmSoundEnabled() {
        return soundsConfig != null ? soundsConfig.isPmSoundEnabled() :
               getBoolean("chat.pm.sound", true);
    }

    public String getPmSoundType() {
        return soundsConfig != null ? soundsConfig.getPmSound() :
               getString("chat.pm.sound-type", "ENTITY_EXPERIENCE_ORB_PICKUP");
    }

    // ========== Mentions ==========

    public boolean isMentionsEnabled() {
        return accessor.isMentionsEnabled();
    }

    public boolean isMentionSoundEnabled() {
        return soundsConfig != null ? soundsConfig.isMentionSoundEnabled() :
               getBoolean("mentions.sound", true);
    }

    public String getMentionSoundType() {
        return soundsConfig != null ? soundsConfig.getMentionSound() :
               getString("mentions.sound-type", "BLOCK_NOTE_BLOCK_PLING");
    }

    public String getMentionHighlight() {
        return accessor.getMentionHighlight();
    }

    public String getSelfMentionHighlight() {
        return accessor.getSelfMentionHighlight();
    }

    // ========== Clear chat ==========

    public boolean isClearChatMessageEnabled() {
        return accessor.isClearChatMessageEnabled();
    }

    public void setClearChatMessageEnabled(boolean enabled) {
        plugin.getConfig().set("clearchat.message.enabled", enabled);
        plugin.saveConfig();
    }

    public String getClearChatMessage() {
        return accessor.getClearChatMessage();
    }

    public void setClearChatMessage(String message) {
        plugin.getConfig().set("clearchat.message.text", message);
        plugin.saveConfig();
    }

    // ========== Auto messages ==========

    public boolean isAutoMessagesEnabled() {
        return accessor.isAutoMessagesEnabled();
    }

    public int getAutoMessagesInterval() {
        return accessor.getAutoMessagesInterval();
    }

    public boolean isAutoMessagesRandom() {
        return accessor.isAutoMessagesRandom();
    }

    // ========== Formats ==========

    public String getAnnouncementFormat() {
        return accessor.getAnnouncementFormat();
    }

    // ========== Announcements ==========

    public boolean isAnnouncementTitleEnabled() {
        return accessor.isAnnouncementTitleEnabled();
    }

    public String getAnnouncementTitleHeader() {
        return accessor.getAnnouncementTitleHeader();
    }

    public boolean isAnnouncementActionBarEnabled() {
        return accessor.isAnnouncementActionBarEnabled();
    }

    public int getAnnouncementTitleDuration() {
        return accessor.getAnnouncementTitleDuration();
    }

    // ========== Chat Translation ==========

    public boolean isTranslationEnabled() {
        return accessor.isTranslationEnabled();
    }

    public String getTranslationEndpoint() {
        return accessor.getTranslationEndpoint();
    }

    public String getTranslationApiKey() {
        return accessor.getTranslationApiKey();
    }

    // ========== LoPreff integration ==========

    public boolean isLopreffEnabled() {
        return accessor.isLopreffEnabled();
    }

    public boolean useGradientName() {
        return accessor.useGradientName();
    }

    public boolean useCustomPrefix() {
        return accessor.useCustomPrefix();
    }

    // ========== Кастомные сообщения ==========

    public boolean isJoinMessageEnabled() {
        return accessor.isJoinMessageEnabled();
    }

    public String getJoinMessageFormat() {
        return accessor.getJoinMessageFormat();
    }

    public boolean isQuitMessageEnabled() {
        return accessor.isQuitMessageEnabled();
    }

    public String getQuitMessageFormat() {
        return accessor.getQuitMessageFormat();
    }

    public boolean isDeathMessageEnabled() {
        return accessor.isDeathMessageEnabled();
    }

    public String getDeathPlayerKillFormat() {
        return accessor.getDeathPlayerKillFormat();
    }

    public String getDeathMobKillFormat() {
        return accessor.getDeathMobKillFormat();
    }

    public String getDeathDefaultFormat() {
        return accessor.getDeathDefaultFormat();
    }
}
