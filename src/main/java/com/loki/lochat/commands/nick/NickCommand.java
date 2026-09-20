package com.loki.lochat.commands.nick;

import com.loki.lochat.compat.Msg;

import com.loki.lochat.LoChat;
import com.loki.lochat.api.service.NickService;
import com.loki.lochat.utils.format.ChatFormatter;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class NickCommand implements CommandExecutor, TabCompleter {

    private final LoChat plugin;
    private final NickService nickService;

    public NickCommand(LoChat plugin) {
        this.plugin = plugin;
        this.nickService = plugin.getServiceRegistry().get(NickService.class);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            Msg.send(sender, Component.text(plugin.getConfigManager().getMessagesConfig().getPlayerOnly(), NamedTextColor.RED));
            return true;
        }
        if (!player.hasPermission("lochat.nick")) {
            Msg.send(player, ChatFormatter.parse(plugin.getConfigManager().getMessagesConfig().getNoPermission()));
            return true;
        }

        if (args.length == 0) {
            var nick = nickService.getNickname(player.getUniqueId());
            if (nick.isPresent()) {
                Msg.send(player, ChatFormatter.parse(plugin.getMessageConfig().get("nick.current", "{nick}", nick.get())));
                Msg.send(player, ChatFormatter.parse(plugin.getMessageConfig().get("nick.reset-usage")));
            } else {
                Msg.send(player, ChatFormatter.parse(plugin.getMessageConfig().get("nick.no-nick")));
                Msg.send(player, ChatFormatter.parse(plugin.getMessageConfig().get("nick.set-usage")));
            }
            return true;
        }

        if (args[0].equalsIgnoreCase("reset")) {
            if (nickService.getNickname(player.getUniqueId()).isEmpty()) {
                Msg.send(player, ChatFormatter.parse(plugin.getMessageConfig().get("nick.no-nick")));
                return true;
            }
            nickService.resetNickname(player.getUniqueId());
            Msg.send(player, ChatFormatter.parse(plugin.getMessageConfig().get("nick.reset-success")));
            return true;
        }

        String nickname = String.join(" ", args);
        if (!nickService.isValidNickname(nickname)) {
            Msg.send(player, ChatFormatter.parse(plugin.getMessageConfig().get("nick.invalid")));
            Msg.send(player, ChatFormatter.parse(plugin.getMessageConfig().get("nick.requirements-header")));
            Msg.send(player, ChatFormatter.parse(plugin.getMessageConfig().get("nick.requirements-length")));
            Msg.send(player, ChatFormatter.parse(plugin.getMessageConfig().get("nick.requirements-chars")));
            return true;
        }
        if (nickService.isNicknameTaken(nickname)) {
            var cur = nickService.getNickname(player.getUniqueId());
            if (cur.isEmpty() || !cur.get().equals(nickname)) {
                Msg.send(player, ChatFormatter.parse(plugin.getMessageConfig().get("nick.taken")));
                return true;
            }
        }
        if (nickService.setNickname(player.getUniqueId(), nickname)) {
            Msg.send(player, ChatFormatter.parse(plugin.getMessageConfig().get("nick.set-success", "{nick}", nickname)));
        } else {
            Msg.send(player, ChatFormatter.parse(plugin.getMessageConfig().get("nick.error")));
        }
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> c = new ArrayList<>();
        if (args.length == 1) {
            if ("reset".startsWith(args[0].toLowerCase())) {
                c.add("reset");
            }
            if (sender instanceof Player p) {
                nickService.getNickname(p.getUniqueId()).ifPresent(c::add);
            }
        }
        return c;
    }
}
