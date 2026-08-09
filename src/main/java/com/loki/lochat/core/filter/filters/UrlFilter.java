package com.loki.lochat.core.filter.filters;

import com.loki.lochat.config.FiltersConfig;
import com.loki.lochat.core.filter.FilterResult;

import org.bukkit.entity.Player;

import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class UrlFilter {
    private static final Pattern URL_PATTERN = Pattern.compile(
        "(https?://)?([\\w-]+\\.)+[\\w-]+(/[\\w-./?%&=]*)?", Pattern.CASE_INSENSITIVE
    );
    
    private static final Pattern IP_PATTERN = Pattern.compile(
        "\\b(?:\\d{1,3}\\.){3}\\d{1,3}\\b"
    );
    
    private static final Pattern HIDDEN_URL_PATTERN = Pattern.compile(
        "(?i)(h\\s*t\\s*t\\s*p|w\\s*w\\s*w|d\\s*o\\s*t\\s*c\\s*o\\s*m)"
    );

    private static final String URL_BLOCKED_MESSAGE = "&#CF6679Ссылки запрещены!";
    private static final String URL_BLACKLISTED_MESSAGE = "&#CF6679Эта ссылка запрещена!";
    private static final String IP_BLOCKED_MESSAGE = "&#CF6679IP адреса запрещены!";
    private static final String HIDDEN_URL_BLOCKED_MESSAGE = "&#CF6679Реклама запрещена!";

    private final boolean blockHiddenUrls;
    private final boolean blockIp;
    private final String ipReplacement;
    private final Set<String> whitelistedDomains;
    private final Set<String> blacklistedDomains;

    public UrlFilter(FiltersConfig filters) {
        this.blockHiddenUrls = filters.isBlockHiddenUrls();
        this.blockIp = filters.isIpBlock();
        this.ipReplacement = filters.getIpReplacement();
        this.whitelistedDomains = new HashSet<>(filters.getWhitelistedDomains());
        this.blacklistedDomains = new HashSet<>(filters.getBlacklistedDomains());
    }

    public FilterResult filterUrls(Player player, String message) {
        if (player.hasPermission("lochat.bypass.urlfilter")) {
            return FilterResult.ok(message);
        }
        
        Matcher matcher = URL_PATTERN.matcher(message);
        while (matcher.find()) {
            String domain = extractDomain(matcher.group());
            
            if (!whitelistedDomains.isEmpty()) {
                boolean ok = whitelistedDomains.stream()
                    .anyMatch(d -> domain.toLowerCase().contains(d.toLowerCase()));
                if (!ok) {
                    return FilterResult.blocked(URL_BLOCKED_MESSAGE);
                }
            }
            
            if (!blacklistedDomains.isEmpty()) {
                boolean bad = blacklistedDomains.stream()
                    .anyMatch(d -> domain.toLowerCase().contains(d.toLowerCase()));
                if (bad) {
                    return FilterResult.blocked(URL_BLACKLISTED_MESSAGE);
                }
            }
        }
        
        return FilterResult.ok(message);
    }

    public FilterResult filterIPs(Player player, String message) {
        if (player.hasPermission("lochat.bypass.ipfilter")) {
            return FilterResult.ok(message);
        }
        
        Matcher matcher = IP_PATTERN.matcher(message);
        if (matcher.find()) {
            if (blockIp) {
                return FilterResult.blocked(IP_BLOCKED_MESSAGE);
            }
            return FilterResult.ok(matcher.replaceAll(ipReplacement));
        }
        
        return FilterResult.ok(message);
    }

    public FilterResult filterHiddenUrls(Player player, String message) {
        if (!blockHiddenUrls) {
            return FilterResult.ok(message);
        }
        if (player.hasPermission("lochat.bypass.hiddenurls")) {
            return FilterResult.ok(message);
        }
        
        if (HIDDEN_URL_PATTERN.matcher(message).find()) {
            return FilterResult.blocked(HIDDEN_URL_BLOCKED_MESSAGE);
        }
        
        return FilterResult.ok(message);
    }

    private String extractDomain(String url) {
        url = url.replaceFirst("^https?://", "");
        int slash = url.indexOf('/');
        return slash != -1 ? url.substring(0, slash) : url;
    }
}
