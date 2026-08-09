package com.loki.lochat;

import com.loki.lochat.api.service.MessageService;
import com.loki.lochat.api.service.MessagingService;
import com.loki.lochat.api.service.NickService;
import com.loki.lochat.api.service.PlayerService;
import com.loki.lochat.api.service.PunishmentService;
import com.loki.lochat.commands.CommandManager;
import com.loki.lochat.config.ConfigManager;
import com.loki.lochat.config.MessageConfig;
import com.loki.lochat.core.PluginInitializer;
import com.loki.lochat.core.PluginShutdown;
import com.loki.lochat.core.registry.ServiceRegistry;
import com.loki.lochat.gradient.GradientModule;
import com.loki.lochat.integrations.DiscordIntegration;
import com.loki.lochat.managers.AutoMessageManager;
import com.loki.lochat.managers.CustomCommandManager;
import com.loki.lochat.translate.TranslationService;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * Главный класс плагина LoChat
 * Использует SOLID архитектуру с Dependency Injection через ServiceRegistry
 */
public final class LoChat extends JavaPlugin {

    private static LoChat instance;
    
    // Основные компоненты
    private ServiceRegistry serviceRegistry;
    private ConfigManager configManager;
    private MessageConfig messageConfig;
    private TranslationService translationService;
    private GradientModule gradientModule;
    private AutoMessageManager autoMessageManager;
    private CustomCommandManager customCommandManager;
    private DiscordIntegration discordIntegration;
    private CommandManager commandManager;
    
    // Вспомогательные классы
    private PluginInitializer initializer;
    private PluginShutdown shutdown;
    private com.loki.lochat.core.filter.AdvancedMessageFilter chatFilter;

    @Override
    public void onEnable() {
        instance = this;
        getLogger().info("Загрузка LoChat v" + getPluginMeta().getVersion() + " для Paper/Folia " + Bukkit.getMinecraftVersion() + "...");

        // Инициализация
        initializer = new PluginInitializer(this);
        shutdown = new PluginShutdown(this);
        
        // Конфиги
        configManager = initializer.initConfigs();
        messageConfig = initializer.initMessageConfig();
        translationService = new TranslationService(
            configManager.getTranslationEndpoint(),
            configManager.getTranslationApiKey(),
            configManager.isTranslationEnabled(),
            getLogger()
        );
        
        // Service Registry (SOLID DI)
        serviceRegistry = initializer.initServiceRegistry(configManager, messageConfig);
        
        // Менеджеры
        customCommandManager = initializer.initCustomCommandManager();
        autoMessageManager = initializer.initAutoMessageManager();
        
        // Интеграции
        discordIntegration = initializer.initDiscordIntegration();
        gradientModule = initializer.initGradientModule();
        initializer.initIntegrations(discordIntegration, gradientModule);
        
        // Команды и слушатели
        commandManager = new CommandManager(this);
        commandManager.registerCommands();
        initializer.registerListeners(serviceRegistry, discordIntegration,
            serviceRegistry.get(MessageService.class),
            serviceRegistry.get(PlayerService.class),
            serviceRegistry.get(MessagingService.class),
            serviceRegistry.get(NickService.class),
            serviceRegistry.get(PunishmentService.class));
        initializer.registerPluginChannels();
        
        // Автосообщения
        autoMessageManager.start();
        
        getLogger().info("LoChat успешно запущен!");
    }

    @Override
    public void onDisable() {
        shutdown.shutdown(serviceRegistry, autoMessageManager, discordIntegration, gradientModule);
    }

    public void reload() {
        configManager.reload();
        messageConfig.reload();
        autoMessageManager.reload();
        if (gradientModule != null) {
            gradientModule.reload();
        }
        if (customCommandManager != null) {
            customCommandManager.reload();
        }
        if (discordIntegration != null) {
            discordIntegration.reload();
        }
        if (serviceRegistry != null) {
            try {
                serviceRegistry.get(PunishmentService.class).save();
            } catch (IllegalStateException ignored) {
            }
        }
    }

    // Геттеры
    public static LoChat getInstance() {
        return instance;
    }

    public ServiceRegistry getServiceRegistry() {
        return serviceRegistry;
    }

    public ConfigManager getConfigManager() {
        return configManager;
    }

    public MessageConfig getMessageConfig() {
        return messageConfig;
    }

    public GradientModule getGradientModule() {
        return gradientModule;
    }

    public CustomCommandManager getCustomCommandManager() {
        return customCommandManager;
    }

    public CommandManager getCommandManager() {
        return commandManager;
    }

    public DiscordIntegration getDiscordIntegration() {
        return discordIntegration;
    }

    public TranslationService getTranslationService() {
        return translationService;
    }

    public com.loki.lochat.core.filter.AdvancedMessageFilter getChatFilter() {
        return chatFilter;
    }

    public void setChatFilter(com.loki.lochat.core.filter.AdvancedMessageFilter chatFilter) {
        this.chatFilter = chatFilter;
    }
}
