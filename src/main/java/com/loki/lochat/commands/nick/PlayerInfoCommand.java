package com.loki.lochat.commands.nick;

import com.loki.lochat.compat.Msg;

import com.loki.lochat.LoChat;
import com.loki.lochat.api.service.MuteService;
import com.loki.lochat.api.service.PunishmentService;
import com.loki.lochat.data.model.BanRecord;
import com.loki.lochat.data.model.MuteData;
import com.loki.lochat.data.model.WarnEntry;
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
 * /playerinfo — статистика варнов, банов и мутов
 */
public class PlayerInfoCommand implements CommandExecutor, TabCompleter {

    private final LoChat plugin;
    private final MuteService muteService;
    private final PunishmentService punishmentService;

    public PlayerInfoCommand(LoChat plugin) {
        this.plugin = plugin;
        this.muteService = plugin.getServiceRegistry().get(MuteService.class);
        this.punishmentService = plugin.getServiceRegistry().get(PunishmentService.class);
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

        int warns = punishmentService.getWarnCount(uuid);
        Msg.send(sender, ChatFormatter.parse("&#CF6679⚠ &fВарнов всего: &#7858E9" + warns));

        List<WarnEntry> recent = punishmentService.getWarns(uuid);
        int show = Math.min(5, recent.size());
        for (int i = recent.size() - show; i < recent.size(); i++) {
            WarnEntry w = recent.get(i);
            String tag = w.silent ? "&#B798A8[тихий] " : "";
            String reason = w.reason != null ? w.reason : "—";
            long t = w.time;
            Msg.send(sender, ChatFormatter.parse(
                    tag + "&#9878C9• &7" + formatTime(t) + " &#B798A8от &#7858E9" + w.moderator + "&#9878C9: &f" + reason));
        }

        BanRecord ban = punishmentService.getActiveBan(uuid);
        if (ban != null) {
            String until = ban.isPermanent()
                    ? "&#CF6679навсегда"
                    : "&#7858E9" + muteService.formatTime(Math.max(0, ban.until - System.currentTimeMillis()));
            Msg.send(sender, ChatFormatter.parse("&#CF6679🚫 &fБан: &cактивен &7(" + until + "&7)"));
            Msg.send(sender, ChatFormatter.parse("&#B798A8Причина: &f" + (ban.reason != null ? ban.reason : "—")));
        } else {
            Msg.send(sender, ChatFormatter.parse("&#9878C9🚫 &fБан: &aнет"));
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

    private static String formatTime(long ms) {
        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("dd.MM.yyyy HH:mm");
        return sdf.format(new java.util.Date(ms));
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
