package com.loki.lochat.commands.moderation.mute;

import com.loki.lochat.compat.Msg;

import com.loki.lochat.LoChat;
import com.loki.lochat.api.service.MuteService;
import com.loki.lochat.utils.format.ChatFormatter;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class UnmuteCommand implements CommandExecutor, TabCompleter {

    private final LoChat plugin;
    private final MuteService muteService;

    public UnmuteCommand(LoChat plugin) {
        this.plugin = plugin;
        this.muteService = plugin.getServiceRegistry().get(MuteService.class);
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!sender.hasPermission("lochat.unmute")) {
            Msg.send(sender, plugin.getMessageConfig().getComponent("errors.no-permission"));
            return true;
        }
        if (args.length < 1) {
            Msg.send(sender, ChatFormatter.parse("&#CF6679Использование: /unmute <ник> [-s]"));
            return true;
        }

        String targetName = null;
        boolean silent = false;
        for (String a : args) {
            if ("-s".equalsIgnoreCase(a)) {
                silent = true;
            } else if (targetName == null) {
                targetName = a;
            }
        }
        if (silent && !sender.hasPermission("lochat.mute.silent")) { 
            Msg.send(sender, ChatFormatter.parse(plugin.getConfigManager().getMessagesConfig().getNoSilentUnmutePermission())); 
            return true; 
        }

        UUID uuid = com.loki.lochat.utils.player.PlayerUtil.findPlayerUUID(targetName);
        if (uuid == null) {
            Msg.send(sender, plugin.getMessageConfig().getComponent("errors.player-not-found"));
            return true;
        }

        String name = com.loki.lochat.utils.player.PlayerUtil.getPlayerName(uuid);
        Player target = Bukkit.getPlayer(uuid);
        String op = sender.getName();

        if (muteService.unmute(uuid, op)) {
            Msg.send(sender, ChatFormatter.parse("&#9878C9Игрок &#7858E9" + name + " &#9878C9размучен"));
            if (target != null) {
                Msg.send(target, ChatFormatter.parse("&#9878C9Вы были &#7858E9размучены!"));
            }
            if (silent) {
                String msg = "&#B798A8[&#9878C9ТИХО&#B798A8] &#7858E9" + name + " &#B798A8размучен &#9878C9(" + op + ")";
                for (Player p : Bukkit.getOnlinePlayers()) {
                    if (p.hasPermission("lochat.mute.see-silent")) {
                        Msg.send(p, ChatFormatter.parse(msg));
                    }
                }
            } else {
                Msg.broadcast(ChatFormatter.parse("&#9878C9Игрок &#7858E9" + name + " &#9878C9размучен модератором &#7858E9" + op));
            }
        } else {
            Msg.send(sender, ChatFormatter.parse(plugin.getConfigManager().getMessagesConfig().getPlayerNotMutedSimple()));
        }
        return true;
    }

    @Override
    public @Nullable List<String> onTabComplete(
        @NotNull CommandSender sender, @NotNull Command command, @NotNull String alias,
        @NotNull String[] args
    ) {
        List<String> c = new ArrayList<>();
        if (args.length == 1) {
            for (Player p : Bukkit.getOnlinePlayers()) {
                if (p.getName().toLowerCase().startsWith(args[0].toLowerCase())) {
                    c.add(p.getName());
                }
            }
        } else if (args.length == 2) {
            c.add("-s");
        }
        return c;
    }
}
