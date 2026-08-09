package com.loki.lochat.core.service;

import com.loki.lochat.api.service.ChannelService;
import com.loki.lochat.channel.Channel;
import com.loki.lochat.config.ChannelConfig;

import org.bukkit.entity.Player;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ChannelServiceImplTest {

    private final Channel global = new Channel("global", "\u2726", "GLOBAL", "&b", -1, 3, "", List.of("g"));
    private final Channel staff = new Channel("staff", "\u2694", "STAFF", "&6", -1, 2,
            "lochat.channel.staff", List.of("s"));

    private Player player() {
        Player player = mock(Player.class);
        when(player.getUniqueId()).thenReturn(UUID.randomUUID());
        return player;
    }

    private ChannelService createService() {
        ChannelConfig config = mock(ChannelConfig.class);
        when(config.isEnabled()).thenReturn(true);
        when(config.getDefault()).thenReturn(global);
        when(config.get("global")).thenReturn(global);
        when(config.get("staff")).thenReturn(staff);
        when(config.find("global")).thenReturn(global);
        when(config.find("staff")).thenReturn(staff);
        when(config.find("s")).thenReturn(staff);
        when(config.getChannels()).thenReturn(List.of(global, staff));
        return new ChannelServiceImpl(config);
    }

    @Test
    void getActive_emptyByDefault() {
        Player player = player();
        ChannelService service = createService();
        assertTrue(service.getActive(player).isEmpty());
        assertFalse(service.isInChannel(player));
    }

    @Test
    void join_nonDefaultChannel() {
        Player player = player();
        when(player.hasPermission("lochat.channel.staff")).thenReturn(true);
        ChannelService service = createService();
        assertTrue(service.join(player, staff));
        Optional<Channel> active = service.getActive(player);
        assertTrue(active.isPresent());
        assertEquals("staff", active.get().id());
        assertTrue(service.isInChannel(player));
    }

    @Test
    void join_defaultChannelMeansNotJoined() {
        Player player = player();
        ChannelService service = createService();
        assertTrue(service.join(player, global));
        assertTrue(service.getActive(player).isEmpty());
        assertFalse(service.isInChannel(player));
    }

    @Test
    void join_withoutPermissionFails() {
        Player player = player();
        when(player.hasPermission("lochat.channel.staff")).thenReturn(false);
        ChannelService service = createService();
        assertFalse(service.join(player, staff));
        assertFalse(service.isInChannel(player));
    }

    @Test
    void join_withPermission() {
        Player player = player();
        when(player.hasPermission("lochat.channel.staff")).thenReturn(true);
        ChannelService service = createService();
        assertTrue(service.join(player, staff));
        assertEquals("staff", service.getActive(player).orElseThrow().id());
    }

    @Test
    void leave_clearsActiveChannel() {
        Player player = player();
        when(player.hasPermission("lochat.channel.staff")).thenReturn(true);
        ChannelService service = createService();
        service.join(player, staff);
        service.leave(player);
        assertFalse(service.isInChannel(player));
        assertTrue(service.getActive(player).isEmpty());
    }

    @Test
    void find_byAlias() {
        ChannelService service = createService();
        Channel byId = service.find("staff");
        Channel byAlias = service.find("s");
        assertEquals("staff", byId.id());
        assertEquals("staff", byAlias.id());
    }
}