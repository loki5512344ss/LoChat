package com.loki.lochat.utils.format;

import me.clip.placeholderapi.PlaceholderAPI;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.regex.Pattern;

/**
 * LuckPerms prefix and suffix through PlaceholderAPI (%luckperms_prefix% / %luckperms_suffix%).
 * Returns an empty string when PlaceholderAPI or its LuckPerms expansion is not available.
 */
public final class LuckPermsAffixes {

    private static final Pattern COLOR = Pattern.compile("(?i)(&#[0-9a-f]{6}|[&\u00A7][0-9a-fk-orx])");

    private LuckPermsAffixes() {
    }

    public static String prefix(Player player) {
        return resolve(player, "%luckperms_prefix%");
    }

    public static String suffix(Player player) {
        return resolve(player, "%luckperms_suffix%");
    }

    private static String resolve(Player player, String placeholder) {
        if (player == null || !Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI")) {
            return "";
        }
        try {
            String result = PlaceholderAPI.setPlaceholders(player, placeholder);
            if (result == null || result.contains("%")) {
                return "";
            }
            return result;
        } catch (Exception | LinkageError e) {
            return "";
        }
    }

    /** Text without color codes. */
    public static String strip(String text) {
        return text == null ? "" : COLOR.matcher(text).replaceAll("");
    }

    /**
     * Removes the LuckPerms prefix/suffix from a display name that already contains them
     * (for example when another plugin put them into the nickname), so they are not shown twice.
     * Returns the original text if nothing was removed.
     */
    public static String withoutAffixes(String displayName, String prefix, String suffix) {
        String plain = strip(displayName).trim();
        String pp = strip(prefix).trim();
        String sp = strip(suffix).trim();
        String result = plain;
        if (!pp.isEmpty() && result.startsWith(pp)) {
            result = result.substring(pp.length()).trim();
        }
        if (!sp.isEmpty() && result.endsWith(sp)) {
            result = result.substring(0, result.length() - sp.length()).trim();
        }
        return result.equals(plain) || result.isEmpty() ? displayName : result;
    }
}
