package com.loki.lochat.core;

import com.loki.lochat.LoChat;
import com.loki.lochat.api.service.MessageService;
import com.loki.lochat.api.service.MessagingService;
import com.loki.lochat.api.service.NickService;
import com.loki.lochat.api.service.PlayerService;
import com.loki.lochat.api.service.PunishmentService;
import com.loki.lochat.config.ConfigManager;
import com.loki.lochat.config.MessageConfig;
import com.loki.lochat.core.filter.AdvancedMessageFilter;
import com.loki.lochat.core.registry.ServiceRegistry;
import com.loki.lochat.gradient.GradientModule;
import com.loki.lochat.integrations.DiscordIntegration;
import com.loki.lochat.integrations.LibertyBansHook;
import com.loki.lochat.integrations.PlaceholderAPIHook;
import com.loki.lochat.listener.ChatEventListener;
import com.loki.lochat.listener.DiscordEventListener;
import com.loki.lochat.listener.ModerationListener;
import com.loki.lochat.listener.PlayerEventListener;
import com.loki.lochat.managers.AutoMessageManager;
import com.loki.lochat.managers.CustomCommandManager;

import org.bukkit.Bukkit;

/**
 * Инициализатор компонентов плагина
 * Выносит логику инициализации из главного класса
 */
public class PluginInitializer {
    
    private final LoChat plugin;
    
    public PluginInitializer(LoChat plugin) {
        this.plugin = plugin;
    }
    
    /**
     * Инициализировать конфигурации
     */
    public ConfigManager initConfigs() {
        return new ConfigManager(plugin);
    }
    
    public MessageConfig initMessageConfig() {
        return new MessageConfig(plugin);
    }
    
    /**
     * Инициализировать Service Registry
     */
    public ServiceRegistry initServiceRegistry(ConfigManager configManager, MessageConfig messageConfig) {
        ServiceRegistry registry = ServiceRegistry.createWithDeps(plugin, configManager, messageConfig);
        plugin.getLogger().info("ServiceRegistry инициализирован (SOLID DI)");
        return registry;
    }
    
    /**
     * Инициализировать менеджеры
     */
    public CustomCommandManager initCustomCommandManager() {
        return CustomCommandManager.create(plugin);
    }
    
    public AutoMessageManager initAutoMessageManager() {
        return new AutoMessageManager(plugin);
    }
    
    /**
     * Инициализировать интеграции
     */
    public void initIntegrations(DiscordIntegration discordIntegration, GradientModule gradientModule) {
        // LibertyBans
        new LibertyBansHook(plugin);
        
        // PlaceholderAPI
        if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
            new PlaceholderAPIHook(plugin).register();
            plugin.getLogger().info("PlaceholderAPI подключен!");
        }
    }
    
    /**
     * Инициализировать Discord интеграцию
     */
    public DiscordIntegration initDiscordIntegration() {
        return new DiscordIntegration(plugin);
    }
    
    /**
     * Инициализировать Gradient модуль
     */
    public GradientModule initGradientModule() {
        GradientModule module = new GradientModule(plugin);
        if (module.init()) {
            plugin.getLogger().info("Gradient модуль (LoPreff) загружен!");
        }
        return module;
    }
    
    /**
     * Зарегистрировать слушатели событий
     */
    public AdvancedMessageFilter registerListeners(ServiceRegistry serviceRegistry, DiscordIntegration discordIntegration,
                                                     MessageService messageService, PlayerService playerService,
                                                     MessagingService messagingService, NickService nickService,
                                                     PunishmentService punishmentService) {
        AdvancedMessageFilter filter = new AdvancedMessageFilter(plugin.getConfig(), plugin);
        plugin.setChatFilter(filter);

        plugin.getServer().getPluginManager().registerEvents(
            new ChatEventListener(plugin, messageService, playerService, filter), plugin
        );
        plugin.getServer().getPluginManager().registerEvents(
            new PlayerEventListener(plugin, playerService, messagingService, nickService, filter), plugin
        );
        plugin.getServer().getPluginManager().registerEvents(
            new ModerationListener(punishmentService), plugin
        );
        
        // Discord listener
        if (discordIntegration.isEnabled()) {
            plugin.getServer().getPluginManager().registerEvents(
                new DiscordEventListener(discordIntegration), plugin
            );
            plugin.getLogger().info("Discord event listener registered");
        }
        
        plugin.getLogger().info("Слушатели зарегистрированы");
        return filter;
    }
    
    /**
     * Зарегистрировать plugin channels
     */
    public void registerPluginChannels() {
        plugin.getServer().getMessenger().registerOutgoingPluginChannel(plugin, "BungeeCord");
        plugin.getLogger().info("BungeeCord plugin messaging channel registered");
    }
}
