package com.loki.lochat.core.filter;

import com.loki.lochat.core.filter.filters.SpamFilter;

import org.bukkit.entity.Player;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class SpamFilterTest {

    @Test
    void filter_firstMessageAllowed() {
        Player player = mock(Player.class);
        when(player.hasPermission("lochat.bypass.spam")).thenReturn(false);
        when(player.getUniqueId()).thenReturn(UUID.randomUUID());
        SpamFilter filter = new SpamFilter(3, 80, "no spam");
        FilterResult result = filter.filter(player, "hello");
        assertTrue(result.allowed());
    }

    @Test
    void filter_sameMessageRepeated() {
        Player player = mock(Player.class);
        when(player.hasPermission("lochat.bypass.spam")).thenReturn(false);
        when(player.getUniqueId()).thenReturn(UUID.randomUUID());
        SpamFilter filter = new SpamFilter(2, 80, "no spam");
        filter.filter(player, "hello");
        filter.filter(player, "hello");
        FilterResult result = filter.filter(player, "hello");
        assertFalse(result.allowed());
        assertEquals("no spam", result.blockReason());
    }

    @Test
    void filter_differentMessagesAllowed() {
        Player player = mock(Player.class);
        when(player.hasPermission("lochat.bypass.spam")).thenReturn(false);
        when(player.getUniqueId()).thenReturn(UUID.randomUUID());
        SpamFilter filter = new SpamFilter(2, 80, "no spam");
        filter.filter(player, "hello");
        filter.filter(player, "world");
        FilterResult result = filter.filter(player, "foo");
        assertTrue(result.allowed());
    }

    @Test
    void filter_bypassPermission() {
        Player player = mock(Player.class);
        when(player.hasPermission("lochat.bypass.spam")).thenReturn(true);
        when(player.getUniqueId()).thenReturn(UUID.randomUUID());
        SpamFilter filter = new SpamFilter(3, 80, "no spam");
        FilterResult result = filter.filter(player, "spam");
        assertTrue(result.allowed());
    }

    @Test
    void filter_historyLimit() {
        Player player = mock(Player.class);
        when(player.hasPermission("lochat.bypass.spam")).thenReturn(false);
        when(player.getUniqueId()).thenReturn(UUID.randomUUID());
        SpamFilter filter = new SpamFilter(2, 100, "no spam");
        // Send 12 different messages to fill and rotate history
        for (int i = 0; i < 12; i++) {
            filter.filter(player, "msg" + i);
        }
        // Send "hello" twice more to trigger spam detection
        filter.filter(player, "hello");
        filter.filter(player, "hello");
        FilterResult result = filter.filter(player, "hello");
        assertFalse(result.allowed());
    }

    @Test
    void filter_similarButNotExact() {
        Player player = mock(Player.class);
        when(player.hasPermission("lochat.bypass.spam")).thenReturn(false);
        when(player.getUniqueId()).thenReturn(UUID.randomUUID());
        SpamFilter filter = new SpamFilter(2, 90, "no spam");
        filter.filter(player, "hello world");
        filter.filter(player, "hello world");
        FilterResult result = filter.filter(player, "hello world!");
        // 91% similarity (11/12 chars match), should be blocked
        assertFalse(result.allowed());
    }

    @Test
    void clearPlayer_removesData() {
        Player player = mock(Player.class);
        UUID uuid = UUID.randomUUID();
        when(player.hasPermission("lochat.bypass.spam")).thenReturn(false);
        when(player.getUniqueId()).thenReturn(uuid);
        SpamFilter filter = new SpamFilter(2, 100, "no spam");
        filter.filter(player, "hello");
        filter.clearPlayer(uuid);
        filter.filter(player, "hello");
        FilterResult result = filter.filter(player, "hello");
        assertTrue(result.allowed());
    }
}
