package com.loki.lochat.core.service.messaging;

import org.bukkit.plugin.java.JavaPlugin;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class IgnoreServiceTest {

    private JavaPlugin plugin;
    private IgnoreService ignoreService;

    @BeforeEach
    void setUp() {
        plugin = mock(JavaPlugin.class);
        ignoreService = new IgnoreService(plugin);
    }

    @AfterEach
    void tearDown() {
        plugin = null;
        ignoreService = null;
    }

    @Test
    @SuppressWarnings({"try", "PMD.UnusedLocalVariable"})
    void isIgnoring_notIgnored() {
        try (MockedStatic<com.loki.lochat.utils.platform.FoliaUtil> ignored = Mockito.mockStatic(com.loki.lochat.utils.platform.FoliaUtil.class)) {
            UUID player = UUID.randomUUID();
            UUID target = UUID.randomUUID();
            assertFalse(ignoreService.isIgnoring(player, target));
        }
    }

    @Test
    @SuppressWarnings({"try", "PMD.UnusedLocalVariable"})
    void addIgnore_thenIsIgnoring() {
        try (MockedStatic<com.loki.lochat.utils.platform.FoliaUtil> fu = Mockito.mockStatic(com.loki.lochat.utils.platform.FoliaUtil.class)) {
            UUID player = UUID.randomUUID();
            UUID target = UUID.randomUUID();
            assertTrue(ignoreService.addIgnore(player, target));
            assertTrue(ignoreService.isIgnoring(player, target));
        }
    }

    @Test
    @SuppressWarnings({"try", "PMD.UnusedLocalVariable"})
    void addIgnore_duplicateReturnsFalse() {
        try (MockedStatic<com.loki.lochat.utils.platform.FoliaUtil> fu = Mockito.mockStatic(com.loki.lochat.utils.platform.FoliaUtil.class)) {
            UUID player = UUID.randomUUID();
            UUID target = UUID.randomUUID();
            assertTrue(ignoreService.addIgnore(player, target));
            assertFalse(ignoreService.addIgnore(player, target));
        }
    }

    @Test
    @SuppressWarnings({"try", "PMD.UnusedLocalVariable"})
    void removeIgnore() {
        try (MockedStatic<com.loki.lochat.utils.platform.FoliaUtil> fu = Mockito.mockStatic(com.loki.lochat.utils.platform.FoliaUtil.class)) {
            UUID player = UUID.randomUUID();
            UUID target = UUID.randomUUID();
            ignoreService.addIgnore(player, target);
            assertTrue(ignoreService.removeIgnore(player, target));
            assertFalse(ignoreService.isIgnoring(player, target));
        }
    }

    @Test
    @SuppressWarnings({"try", "PMD.UnusedLocalVariable"})
    void removeIgnore_notIgnored() {
        try (MockedStatic<com.loki.lochat.utils.platform.FoliaUtil> fu = Mockito.mockStatic(com.loki.lochat.utils.platform.FoliaUtil.class)) {
            assertFalse(ignoreService.removeIgnore(UUID.randomUUID(), UUID.randomUUID()));
        }
    }

    @Test
    @SuppressWarnings({"try", "PMD.UnusedLocalVariable"})
    void getIgnoredCount() {
        try (MockedStatic<com.loki.lochat.utils.platform.FoliaUtil> fu = Mockito.mockStatic(com.loki.lochat.utils.platform.FoliaUtil.class)) {
            UUID player = UUID.randomUUID();
            assertEquals(0, ignoreService.getIgnoredCount(player));
            ignoreService.addIgnore(player, UUID.randomUUID());
            assertEquals(1, ignoreService.getIgnoredCount(player));
            ignoreService.addIgnore(player, UUID.randomUUID());
            assertEquals(2, ignoreService.getIgnoredCount(player));
        }
    }

    @Test
    @SuppressWarnings({"try", "PMD.UnusedLocalVariable"})
    void getIgnoredPlayers_returnsSet() {
        try (MockedStatic<com.loki.lochat.utils.platform.FoliaUtil> fu = Mockito.mockStatic(com.loki.lochat.utils.platform.FoliaUtil.class)) {
            UUID player = UUID.randomUUID();
            UUID target = UUID.randomUUID();
            ignoreService.addIgnore(player, target);
            assertTrue(ignoreService.getIgnoredPlayers(player).contains(target));
        }
    }

    @Test
    @SuppressWarnings({"try", "PMD.UnusedLocalVariable"})
    void clearIgnores() {
        try (MockedStatic<com.loki.lochat.utils.platform.FoliaUtil> fu = Mockito.mockStatic(com.loki.lochat.utils.platform.FoliaUtil.class)) {
            UUID player = UUID.randomUUID();
            ignoreService.addIgnore(player, UUID.randomUUID());
            ignoreService.clearIgnores(player);
            assertEquals(0, ignoreService.getIgnoredCount(player));
        }
    }

    @Test
    @SuppressWarnings({"try", "PMD.UnusedLocalVariable"})
    void multiplePlayersIndepedent() {
        try (MockedStatic<com.loki.lochat.utils.platform.FoliaUtil> fu = Mockito.mockStatic(com.loki.lochat.utils.platform.FoliaUtil.class)) {
            UUID a = UUID.randomUUID();
            UUID b = UUID.randomUUID();
            UUID target = UUID.randomUUID();
            ignoreService.addIgnore(a, target);
            assertTrue(ignoreService.isIgnoring(a, target));
            assertFalse(ignoreService.isIgnoring(b, target));
        }
    }
}
