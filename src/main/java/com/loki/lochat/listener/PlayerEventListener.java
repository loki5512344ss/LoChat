package com.loki.lochat.listener;

import com.loki.lochat.LoChat;
import com.loki.lochat.api.service.MessagingService;
import com.loki.lochat.api.service.NickService;
import com.loki.lochat.api.service.PlayerService;
import com.loki.lochat.compat.Msg;
import com.loki.lochat.config.ConfigManager;
import com.loki.lochat.core.filter.AdvancedMessageFilter;
import com.loki.lochat.utils.format.ChatFormatter;

import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;

/**
 * Слушатель событий игрока
 */
public class PlayerEventListener implements Listener {
    private final LoChat plugin;
    private final PlayerService playerService;
    private final MessagingService messagingService;
    private final NickService nickService;
    private final AdvancedMessageFilter advancedFilter;
    private final ConfigManager configManager;

    public PlayerEventListener(LoChat plugin, PlayerService playerService, MessagingService messagingService,
                                NickService nickService, AdvancedMessageFilter advancedFilter) {
        this.plugin = plugin;
        this.playerService = playerService;
        this.messagingService = messagingService;
        this.nickService = nickService;
        this.advancedFilter = advancedFilter;
        this.configManager = plugin.getConfigManager();
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        if (nickService != null) {
            nickService.updatePlayerDisplay(player);
        }

        // Кастомное сообщение входа
        if (configManager.isJoinMessageEnabled()) {
            String format = configManager.getJoinMessageFormat();
            String message = format.replace("{player}", player.getName());
            event.setJoinMessage(null);
            Msg.broadcast(ChatFormatter.parse(message));
        }
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        java.util.UUID uuid = player.getUniqueId();

        playerService.clearPlayerData(uuid);
        messagingService.removeConversation(uuid);
        messagingService.removeSpy(uuid);

        // Очищаем flood / spam трекеры
        advancedFilter.clearPlayer(uuid);

        // Кастомное сообщение выхода
        if (configManager.isQuitMessageEnabled()) {
            String format = configManager.getQuitMessageFormat();
            String message = format.replace("{player}", player.getName());
            event.setQuitMessage(null);
            Msg.broadcast(ChatFormatter.parse(message));
        }
    }

    @EventHandler
    public void onDeath(PlayerDeathEvent event) {
        if (!configManager.isDeathMessageEnabled()) {
            return;
        }

        Player player = event.getEntity();
        Player killer = player.getKiller();

        String message;

        if (killer != null) {
            // Убийство игроком
            ItemStack weapon = killer.getInventory().getItemInMainHand();
            String weaponName = getWeaponName(weapon);

            message = configManager.getDeathPlayerKillFormat()
                .replace("{player}", player.getName())
                .replace("{killer}", killer.getName())
                .replace("{weapon}", weaponName);
        } else {
            // Проверяем убийцу-моба
            Entity lastDamager = player.getLastDamageCause() != null
                ? player.getLastDamageCause().getEntity() : null;

            if (lastDamager instanceof LivingEntity mob && !(lastDamager instanceof Player)) {
                String mobName = getMobName(mob);
                message = configManager.getDeathMobKillFormat()
                    .replace("{player}", player.getName())
                    .replace("{killer}", mobName);
            } else {
                // Обычная смерть
                String deathMsg = event.getDeathMessage();
                String deathText = deathMsg != null ? ChatFormatter.toPlain(ChatFormatter.parse(deathMsg)) : "умер";
                message = configManager.getDeathDefaultFormat()
                    .replace("{player}", player.getName())
                    .replace("{death_message}", deathText);
            }
        }

        event.setDeathMessage(Msg.legacy(ChatFormatter.parse(message)));
    }

    private String getWeaponName(ItemStack item) {
        if (item == null || item.getType().isAir()) {
            return "Руки";
        }

        if (item.hasItemMeta() && item.getItemMeta().hasDisplayName()) {
            String dn = item.getItemMeta().getDisplayName();
            if (dn != null) {
                return ChatFormatter.toPlain(ChatFormatter.parse(dn));
            }
        }

        return translateMaterial(item.getType().name());
    }

    private String getMobName(LivingEntity mob) {
        String cn = mob.getCustomName();
        if (cn != null) {
            return ChatFormatter.toPlain(ChatFormatter.parse(cn));
        }
        return translateEntityType(mob.getType().name());
    }

    private String translateMaterial(String material) {
        return switch (material) {
            case "DIAMOND_SWORD" -> "Алмазный меч";
            case "IRON_SWORD" -> "Железный меч";
            case "GOLDEN_SWORD" -> "Золотой меч";
            case "STONE_SWORD" -> "Каменный меч";
            case "WOODEN_SWORD" -> "Деревянный меч";
            case "NETHERITE_SWORD" -> "Незеритовый меч";
            case "BOW" -> "Лук";
            case "CROSSBOW" -> "Арбалет";
            case "TRIDENT" -> "Трезубец";
            case "DIAMOND_AXE" -> "Алмазный топор";
            case "IRON_AXE" -> "Железный топор";
            case "NETHERITE_AXE" -> "Незеритовый топор";
            default -> material.replace("_", " ").toLowerCase();
        };
    }

    private String translateEntityType(String entityType) {
        return switch (entityType) {
            case "ZOMBIE" -> "Зомби";
            case "SKELETON" -> "Скелет";
            case "CREEPER" -> "Крипер";
            case "SPIDER" -> "Паук";
            case "ENDERMAN" -> "Эндермен";
            case "BLAZE" -> "Ифрит";
            case "WITCH" -> "Ведьма";
            case "PIGLIN" -> "Пиглин";
            case "HOGLIN" -> "Хоглин";
            case "WITHER_SKELETON" -> "Скелет-иссушитель";
            default -> entityType.replace("_", " ").toLowerCase();
        };
    }
}
