package com.loki.lochat.core;

import com.loki.lochat.LoChat;
import com.loki.lochat.api.service.MessagingService;
import com.loki.lochat.api.service.MuteService;
import com.loki.lochat.api.service.NickService;
import com.loki.lochat.api.service.PlayerService;
import com.loki.lochat.core.registry.ServiceRegistry;
import com.loki.lochat.gradient.GradientModule;
import com.loki.lochat.integrations.DiscordIntegration;
import com.loki.lochat.managers.AutoMessageManager;

import org.bukkit.event.HandlerList;

/**
 * Обработчик выключения плагина
 * Выносит логику shutdown из главного класса
 */
public class PluginShutdown {
    
    private final LoChat plugin;
    
    public PluginShutdown(LoChat plugin) {
        this.plugin = plugin;
    }
    
    /**
     * Выполнить полное выключение плагина
     */
    public void shutdown(ServiceRegistry serviceRegistry, 
                        AutoMessageManager autoMessageManager,
                        DiscordIntegration discordIntegration,
                        GradientModule gradientModule) {
        
        // ✅ ПЕРВЫМ ДЕЛОМ отменяем все event listeners
        HandlerList.unregisterAll(plugin);
        
        // Останавливаем менеджеры
        if (autoMessageManager != null) {
            autoMessageManager.stop();
        }
        
        // Сохраняем данные через сервисы
        saveAllData(serviceRegistry);
        
        // Останавливаем интеграции
        if (discordIntegration != null) {
            discordIntegration.shutdown();
        }
        
        if (gradientModule != null) {
            gradientModule.shutdown();
        }
        
        plugin.getLogger().info("LoChat отключен!");
    }
    
    /**
     * Сохранить все данные через сервисы
     */
    private void saveAllData(ServiceRegistry serviceRegistry) {
        if (serviceRegistry == null) {
            return;
        }
        
        // Статистика игроков
        PlayerService playerService = serviceRegistry.get(PlayerService.class);
        if (playerService != null) {
            playerService.saveAll();
        }
        
        // Messaging (Ignore + PM + Spy)
        MessagingService messagingService = serviceRegistry.get(MessagingService.class);
        if (messagingService != null) {
            messagingService.save();
        }
        
        // Муты
        MuteService muteService = serviceRegistry.get(MuteService.class);
        if (muteService != null) {
            muteService.save();
        }

        // Ники
        NickService nickService = serviceRegistry.get(NickService.class);
        if (nickService != null) {
            nickService.save();
        }
    }
}
