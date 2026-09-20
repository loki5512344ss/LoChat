package com.loki.lochat.commands.nick;

import com.loki.lochat.compat.Msg;

import com.loki.lochat.LoChat;
import com.loki.lochat.api.service.MuteService;
import com.loki.lochat.data.model.MuteData;
import com.loki.lochat.utils.format.ChatFormatter;
import com.loki.lochat.utils.player.PlayerUtil;

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

/**
 * /playerinfo — статистика мутов (mute-only)
 */
public class PlayerInfoCommand implements CommandExecutor, TabCompleter {

    private final LoChat plugin;
    private final MuteService muteService;

    public PlayerInfoCommand(LoChat plugin) {
        this.plugin = plugin;
        this.muteService = plugin.getServiceRegistry().get(MuteService.class);
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command,
                             @NotNull String label, @NotNull String[] args) {
        if (!sender.hasPermission("lochat.playerinfo")) {
            Msg.send(sender, ChatFormatter.parse(plugin.getConfigManager().getMessagesConfig().getNoPermission()));
            return true;
        }
        if (args.length < 1) {
            Msg.send(sender, ChatFormatter.parse("&#B798A8Использование: &#7858E9/playerinfo <ник>"));
            return true;
        }

        UUID uuid = PlayerUtil.findPlayerUUID(args[0]);
        if (uuid == null) {
            Msg.send(sender, ChatFormatter.parse("&#CF6679Игрок не найден"));
            return true;
        }

        String name = PlayerUtil.getPlayerName(uuid);
        if (name == null) {
            name = args[0];
        }

        Player target = Bukkit.getPlayer(uuid);

        Msg.send(sender, ChatFormatter.parse("<gradient:#7858E9:#B798A8><bold>═══════ Профиль: " + name + " ═══════</bold></gradient>"));

        if (target != null) {
            Msg.send(sender, ChatFormatter.parse("&#9878C9❤ &fЗдоровье: &#7858E9" + Math.round(target.getHealth()) + "/20"));
            Msg.send(sender, ChatFormatter.parse("&#9878C9✦ &fРежим: &#7858E9" + target.getGameMode().name().toLowerCase()));
            Msg.send(sender, ChatFormatter.parse("&#9878C9◆ &fМир: &#7858E9" + target.getWorld().getName()));
        } else {
            Msg.send(sender, ChatFormatter.parse("&#B798A8Игрок &#7858E9не в сети"));
        }

        if (plugin.getGradientModule() != null && target != null) {
            Msg.send(sender, ChatFormatter.parse("&#9878C9⚡ &fГрадиент: " + plugin.getGradientModule().getFormattedName(target)));
        }

        if (muteService.isMuted(uuid)) {
            MuteData md = muteService.getMuteData(uuid);
            long rem = muteService.getRemainingTime(uuid);
            String remStr = rem < 0 ? "навсегда" : muteService.formatTime(rem);
            Msg.send(sender, ChatFormatter.parse("&#CF6679🔇 &fМут: &cда &7(" + remStr + "&7)"));
            if (md != null) {
                Msg.send(sender, ChatFormatter.parse("&#B798A8Причина: &f" + (md.getReason() != null ? md.getReason() : "—")));
            }
        } else {
            Msg.send(sender, ChatFormatter.parse("&#9878C9🔇 &fМут: &aнет"));
        }

        var muteHist = muteService.getPlayerHistory(uuid);
        if (muteHist != null && !muteHist.isEmpty()) {
            Msg.send(sender, ChatFormatter.parse("&#B798A8Записей в истории мутов: &#7858E9" + muteHist.size()));
        }

        return true;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command,
                                                @NotNull String alias, @NotNull String[] args) {
        List<String> out = new ArrayList<>();
        if (args.length == 1) {
            String p = args[0].toLowerCase();
            for (Player pl : Bukkit.getOnlinePlayers()) {
                if (pl.getName().toLowerCase().startsWith(p)) {
                    out.add(pl.getName());
                }
            }
        }
        return out;
    }
}
