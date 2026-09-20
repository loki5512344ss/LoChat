package com.loki.lochat.compat;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import net.kyori.adventure.title.Title;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.meta.ItemMeta;

import java.time.Duration;
import java.util.Collection;
import java.util.List;

/**
 * Static helpers for sending Adventure components on Spigot via BukkitAudiences.
 * Falls back to legacy String when audiences not available.
 */
public final class Msg {

    private static final LegacyComponentSerializer LEGACY = LegacyComponentSerializer.builder()
            .character('§')
            .hexColors()
            .useUnusualXRepeatedCharacterHexFormat()
            .build();

    private static final PlainTextComponentSerializer PLAIN = PlainTextComponentSerializer.plainText();

    private Msg() {
    }

    public static void send(CommandSender sender, Component component) {
        if (sender == null || component == null) {
            return;
        }
        if (!Adventure.isFallback() && Adventure.audiences() != null) {
            try {
                Adventure.audiences().sender(sender).sendMessage(component);
                return;
            } catch (Throwable ignored) {
            }
        }
        sender.sendMessage(legacy(component));
    }

    public static void send(Collection<? extends CommandSender> senders, Component component) {
        if (senders == null || component == null) {
            return;
        }
        for (CommandSender s : senders) {
            send(s, component);
        }
    }

    public static void broadcast(Component component) {
        if (component == null) {
            return;
        }
        if (!Adventure.isFallback() && Adventure.audiences() != null) {
            try {
                Adventure.audiences().all().sendMessage(component);
                return;
            } catch (Throwable ignored) {
            }
        }
        String legacy = legacy(component);
        Bukkit.getOnlinePlayers().forEach(p -> p.sendMessage(legacy));
        Bukkit.getConsoleSender().sendMessage(legacy);
    }

    public static void actionBar(Player player, Component component) {
        if (player == null || component == null) {
            return;
        }
        if (!Adventure.isFallback() && Adventure.audiences() != null) {
            try {
                Adventure.audiences().player(player).sendActionBar(component);
                return;
            } catch (Throwable ignored) {
            }
        }
        // Fallback: send as chat
        player.sendMessage(legacy(component));
    }

    public static void title(Player player, Component title, Component subtitle, Title.Times times) {
        if (player == null) {
            return;
        }
        Component t = title != null ? title : Component.empty();
        Component s = subtitle != null ? subtitle : Component.empty();
        Title.Times tm = times != null ? times : Title.Times.times(
                Duration.ofMillis(500), Duration.ofMillis(3000), Duration.ofMillis(500));
        if (!Adventure.isFallback() && Adventure.audiences() != null) {
            try {
                Adventure.audiences().player(player).showTitle(Title.title(t, s, tm));
                return;
            } catch (Throwable ignored) {
            }
        }
        // Fallback via Bukkit API (legacy strings, 1.20.1 supports titles)
        player.sendTitle(legacy(t), legacy(s),
                (int) (tm.fadeIn().toMillis() / 50),
                (int) (tm.stay().toMillis() / 50),
                (int) (tm.fadeOut().toMillis() / 50));
    }

    public static String legacy(Component component) {
        if (component == null) {
            return "";
        }
        return LEGACY.serialize(component);
    }

    public static String plain(Component component) {
        if (component == null) {
            return "";
        }
        return PLAIN.serialize(component);
    }

    public static void setDisplayName(Player player, Component component) {
        if (player == null) {
            return;
        }
        player.setDisplayName(component == null ? "" : legacy(component));
    }

    public static void setPlayerListName(Player player, Component component) {
        if (player == null) {
            return;
        }
        player.setPlayerListName(component == null ? "" : legacy(component));
    }

    public static void kick(Player player, Component reason) {
        if (player == null) {
            return;
        }
        String msg = reason == null ? "" : legacy(reason);
        // Must be on main thread; caller should ensure via scheduler if needed
        if (Bukkit.isPrimaryThread()) {
            player.kickPlayer(msg);
        } else {
            Bukkit.getScheduler().runTask(
                    Bukkit.getPluginManager().getPlugin("LoChat"), () -> player.kickPlayer(msg));
        }
    }

    public static void itemName(ItemMeta meta, Component name) {
        if (meta == null) {
            return;
        }
        meta.setDisplayName(name == null ? null : legacy(name));
    }

    public static void itemLore(ItemMeta meta, List<Component> lore) {
        if (meta == null) {
            return;
        }
        if (lore == null) {
            meta.setLore(null);
            return;
        }
        List<String> lines = lore.stream().map(Msg::legacy).toList();
        meta.setLore(lines);
    }

    public static String inventoryTitle(Component title) {
        return legacy(title);
    }
}
