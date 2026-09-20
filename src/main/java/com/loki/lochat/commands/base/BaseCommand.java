package com.loki.lochat.commands.base;

import com.loki.lochat.LoChat;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.logging.Level;

/**
 * Базовый класс для всех команд плагина
 * Реализует общую логику и предоставляет утилиты
 */
public abstract class BaseCommand implements CommandExecutor, TabCompleter {
    
    protected final LoChat plugin;
    
    public BaseCommand(LoChat plugin) {
        this.plugin = plugin;
    }
    
    @Override
    public final boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, 
                                 @NotNull String label, @NotNull String[] args) {
        try {
            return executeCommand(sender, command, label, args);
        } catch (Exception e) {
            plugin.getLogger().log(Level.WARNING, "Ошибка в команде " + command.getName() + ": " + e.getMessage(), e);
            sender.sendMessage("§cПроизошла ошибка при выполнении команды!");
            return true;
        }
    }
    
    /**
     * Выполнить команду (переопределяется в наследниках)
     */
    protected abstract boolean executeCommand(@NotNull CommandSender sender, @NotNull Command command, 
                                            @NotNull String label, @NotNull String[] args);
    
    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, 
                                              @NotNull String alias, @NotNull String[] args) {
        return getTabCompletions(sender, command, alias, args);
    }
    
    /**
     * Получить автодополнения (переопределяется в наследниках при необходимости)
     */
    protected @Nullable List<String> getTabCompletions(@NotNull CommandSender sender, @NotNull Command command, 
                                                     @NotNull String alias, @NotNull String[] args) {
        return null;
    }
    
    /**
     * Проверить права доступа
     */
    protected boolean hasPermission(CommandSender sender, String permission) {
        return sender.hasPermission(permission);
    }
    
    /**
     * Проверить что отправитель - игрок
     */
    protected boolean requirePlayer(CommandSender sender) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(plugin.getConfigManager().getMessagesConfig().getPlayerOnly());
            return false;
        }
        return true;
    }
    
    /**
     * Проверить права и отправить сообщение об ошибке если нет прав
     */
    protected boolean requirePermission(CommandSender sender, String permission) {
        if (!hasPermission(sender, permission)) {
            sender.sendMessage(plugin.getConfigManager().getMessagesConfig().getNoPermission());
            return false;
        }
        return true;
    }
    
    /**
     * Получить игрока из CommandSender (с проверкой)
     */
    protected Player getPlayer(CommandSender sender) {
        return sender instanceof Player ? (Player) sender : null;
    }
    
    /**
     * Отправить сообщение об использовании команды
     */
    protected void sendUsage(CommandSender sender, String usage) {
        sender.sendMessage("§cИспользование: " + usage);
    }
}
