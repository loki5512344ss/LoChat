package com.loki.lochat.commands;

import com.loki.lochat.LoChat;
import com.loki.lochat.commands.admin.broadcast.AnnounceCommand;
import com.loki.lochat.commands.admin.broadcast.DiscordCommand;
import com.loki.lochat.commands.admin.chat.ChatSpyCommand;
import com.loki.lochat.commands.admin.chat.ClearChatCommand;
import com.loki.lochat.commands.admin.chat.ClearChatConfigCommand;
import com.loki.lochat.commands.admin.system.LoChatCommand;
import com.loki.lochat.commands.admin.system.ReloadConfigCommand;
import com.loki.lochat.commands.chat.ChannelCommand;
import com.loki.lochat.commands.chat.GlobalChatCommand;
import com.loki.lochat.commands.chat.JoinChannelCommand;
import com.loki.lochat.commands.chat.LeaveChannelCommand;
import com.loki.lochat.commands.chat.LocalChatCommand;
import com.loki.lochat.commands.messaging.IgnoreCommand;
import com.loki.lochat.commands.messaging.IgnoreListCommand;
import com.loki.lochat.commands.messaging.MsgCommand;
import com.loki.lochat.commands.messaging.ReplyCommand;
import com.loki.lochat.commands.messaging.UnignoreCommand;
import com.loki.lochat.commands.moderation.mute.MuteBlameCommand;
import com.loki.lochat.commands.moderation.mute.MuteCommand;
import com.loki.lochat.commands.moderation.mute.MuteHistoryCommand;
import com.loki.lochat.commands.moderation.mute.MuteListCommand;
import com.loki.lochat.commands.moderation.mute.UnmuteCommand;
import com.loki.lochat.commands.nick.NickCommand;
import com.loki.lochat.commands.nick.PlayerInfoCommand;
import com.loki.lochat.commands.rp.DoCommand;
import com.loki.lochat.commands.rp.MeCommand;
import com.loki.lochat.commands.rp.RollCommand;
import com.loki.lochat.commands.rp.TryCommand;

import org.bukkit.command.PluginCommand;

import java.util.HashMap;
import java.util.Map;

/**
 * Централизованная регистрация всех команд плагина.
 */
public class CommandManager {

    private final LoChat plugin;
    private final Map<String, com.loki.lochat.commands.base.BaseCommand> commands = new HashMap<>();

    public CommandManager(LoChat plugin) {
        this.plugin = plugin;
    }

    public void registerCommands() {

        // ── Чат ──────────────────────────────────────────────────────────────
        reg("g",          new GlobalChatCommand(plugin));
        reg("l",          new LocalChatCommand(plugin));

        // ── Каналы ─────────────────────────────────────────────────────────────
        regTab("join",     new JoinChannelCommand(plugin));
        reg("leave",       new LeaveChannelCommand(plugin));
        regTab("channel",  new ChannelCommand(plugin));

        // ── Личные сообщения / игнор ──────────────────────────────────────────
        reg("msg",        new MsgCommand(plugin));
        reg("reply",      new ReplyCommand(plugin));
        reg("ignore",     new IgnoreCommand(plugin));
        reg("unignore",   new UnignoreCommand(plugin));
        reg("ignorelist", new IgnoreListCommand(plugin));

        // ── Модерация ─────────────────────────────────────────────────────────
        regTab("lmute",        new MuteCommand(plugin));
        regTab("lunmute",      new UnmuteCommand(plugin));
        reg("lmutelist",       new MuteListCommand(plugin));
        regTab("lmutehistory", new MuteHistoryCommand(plugin));
        regTab("lmuteblame",   new MuteBlameCommand(plugin));

        // ── Ник / профиль ─────────────────────────────────────────────────────
        regTab("nick",       new NickCommand(plugin));
        regTab("playerinfo", new PlayerInfoCommand(plugin));

        // ── Админ ─────────────────────────────────────────────────────────────
        reg("announce",         new AnnounceCommand(plugin));
        reg("chatspy",          new ChatSpyCommand(plugin));
        reg("clearchat",        new ClearChatCommand(plugin));
        regTab("clearchatconfig", new ClearChatConfigCommand(plugin));
        regTab("lochat",        new LoChatCommand(plugin));
        reg("lochatreload",     new ReloadConfigCommand(plugin));
        regTab("discordadmin",  new DiscordCommand(plugin, plugin.getDiscordIntegration()));

        // ── RP ────────────────────────────────────────────────────────────────
        registerBaseCommand("me",   new MeCommand(plugin));
        registerBaseCommand("try",  new TryCommand(plugin));
        registerBaseCommand("do",   new DoCommand(plugin));
        regTab("roll",              new RollCommand(plugin));

        // ── Кастомные команды ─────────────────────────────────────────────────
        regTab("customcommands", new CustomCommandsCommand(plugin, plugin.getCustomCommandManager()));

        plugin.getLogger().info("[LoChat] Команды зарегистрированы.");
    }

    // ── Вспомогательные методы ────────────────────────────────────────────────

    /** Регистрирует команду только как CommandExecutor */
    private void reg(String name, org.bukkit.command.CommandExecutor executor) {
        PluginCommand cmd = plugin.getCommand(name);
        if (cmd != null) {
            cmd.setExecutor(executor);
        } else {
            plugin.getLogger().warning("[LoChat] Команда '" + name + "' не найдена в plugin.yml!");
        }
    }

    /** Регистрирует команду как CommandExecutor + TabCompleter */
    private void regTab(String name, Object executor) {
        PluginCommand cmd = plugin.getCommand(name);
        if (cmd != null) {
            cmd.setExecutor((org.bukkit.command.CommandExecutor) executor);
            if (executor instanceof org.bukkit.command.TabCompleter tc) {
                cmd.setTabCompleter(tc);
            }
        } else {
            plugin.getLogger().warning("[LoChat] Команда '" + name + "' не найдена в plugin.yml!");
        }
    }

    /** Регистрирует BaseCommand (executor + tab-completer из одного класса) */
    private void registerBaseCommand(String name, com.loki.lochat.commands.base.BaseCommand command) {
        PluginCommand cmd = plugin.getCommand(name);
        if (cmd != null) {
            cmd.setExecutor(command);
            cmd.setTabCompleter(command);
            commands.put(name, command);
        } else {
            plugin.getLogger().warning("[LoChat] Команда '" + name + "' не найдена в plugin.yml!");
        }
    }
}
