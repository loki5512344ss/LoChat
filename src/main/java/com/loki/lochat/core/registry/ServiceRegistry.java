package com.loki.lochat.core.registry;

import com.loki.lochat.LoChat;
import com.loki.lochat.api.service.ChannelService;
import com.loki.lochat.api.service.ChatService;
import com.loki.lochat.api.service.MentionService;
import com.loki.lochat.api.service.MessageService;
import com.loki.lochat.api.service.MessagingService;
import com.loki.lochat.api.service.MuteService;
import com.loki.lochat.api.service.NickService;
import com.loki.lochat.api.service.PlayerService;
import com.loki.lochat.api.service.pm.PrivateMessageService;
import com.loki.lochat.config.ConfigManager;
import com.loki.lochat.config.MessageConfig;
import com.loki.lochat.core.factory.ServiceFactory;
import com.loki.lochat.core.service.ChatServiceImpl;
import com.loki.lochat.core.service.ChannelServiceImpl;
import com.loki.lochat.core.service.MentionServiceImpl;
import com.loki.lochat.core.service.MessageServiceImpl;
import com.loki.lochat.core.service.NickServiceImpl;
import com.loki.lochat.core.service.PlayerServiceImpl;
import com.loki.lochat.core.service.messaging.PrivateMessageServiceImpl;

import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;
import java.util.Map;

public class ServiceRegistry {
    private final Map<Class<?>, Object> services = new HashMap<>();
    private final JavaPlugin plugin;

    private ServiceRegistry(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    public static ServiceRegistry createWithDeps(JavaPlugin plugin, ConfigManager configManager, MessageConfig messageConfig) {
        ServiceRegistry registry = new ServiceRegistry(plugin);
        registry.registerServicesWithDeps(configManager, messageConfig);
        return registry;
    }

    private void registerServicesWithDeps(ConfigManager configManager, MessageConfig messageConfig) {
        MuteService muteService = ServiceFactory.createMuteService(plugin);
        register(MuteService.class, muteService);

        PlayerService playerService = new PlayerServiceImpl(plugin);
        register(PlayerService.class, playerService);

        register(ChannelService.class, new ChannelServiceImpl(configManager.getChannelConfig()));

        MessageService messageService = new MessageServiceImpl(plugin, muteService, playerService);
        register(MessageService.class, messageService);

        register(ChatService.class, new ChatServiceImpl(plugin, messageService));

        PrivateMessageServiceImpl pmService = new PrivateMessageServiceImpl((LoChat) plugin, playerService);
        MessagingService messagingService = ServiceFactory.createMessagingService(plugin, messageConfig, pmService);
        pmService.init(messagingService);
        register(PrivateMessageService.class, pmService);
        register(MessagingService.class, messagingService);

        register(MentionService.class, new MentionServiceImpl(configManager));
        register(NickService.class, new NickServiceImpl(plugin));
    }

    public <T> void register(Class<T> serviceClass, T implementation) {
        services.put(serviceClass, implementation);
    }
    
    /**
     * Регистрирует сервис с возможностью приведения типов (для обратной совместимости)
     */
    @SuppressWarnings("unchecked")
    public void registerCompat(Class<?> serviceClass, Object implementation) {
        services.put(serviceClass, implementation);
    }

    @SuppressWarnings("unchecked")
    public <T> T get(Class<T> serviceClass) {
        Object service = services.get(serviceClass);
        if (service == null) {
            throw new IllegalStateException("Service not registered: " + serviceClass.getName());
        }
        return (T) service;
    }

    public void shutdown() {
        services.clear();
    }
}
