package com.loki.lochat.core.service;

import com.loki.lochat.utils.persistence.FilePersistence;

import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import java.io.File;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class PlayerServiceCooldownTest {

    @Test
    void cooldown_typesAreIndependent() {
        try (MockedStatic<FilePersistence> fp = Mockito.mockStatic(FilePersistence.class)) {
            JavaPlugin plugin = mock(JavaPlugin.class);
            fp.when(() -> FilePersistence.getFile(plugin, "data/statistics.yml"))
                    .thenReturn(new File("/tmp/nonexistent-statistics.yml"));
            fp.when(() -> FilePersistence.loadYaml(plugin, "data/statistics.yml"))
                    .thenReturn(new YamlConfiguration());
            PlayerServiceImpl service = new PlayerServiceImpl(plugin);

            UUID player = UUID.randomUUID();
            service.setCooldown(player, "rp_me");
            service.setCooldown(player, "local");

            // rp_me на кулдауне
            assertTrue(service.isOnCooldown(player, "rp_me", 5));
            // local на кулдауне
            assertTrue(service.isOnCooldown(player, "local", 5));
            // rp_try НЕ на кулдауне (разные типы не блокируют друг друга)
            assertFalse(service.isOnCooldown(player, "rp_try", 5));
            // global НЕ на кулдауне
            assertFalse(service.isOnCooldown(player, "global", 5));
            // pm НЕ на кулдауне
            assertFalse(service.isOnCooldown(player, "pm", 5));
        }
    }

    @Test
    void removeCooldown_clearsAllTypes() {
        try (MockedStatic<FilePersistence> fp = Mockito.mockStatic(FilePersistence.class)) {
            JavaPlugin plugin = mock(JavaPlugin.class);
            fp.when(() -> FilePersistence.getFile(plugin, "data/statistics.yml"))
                    .thenReturn(new File("/tmp/nonexistent-statistics.yml"));
            fp.when(() -> FilePersistence.loadYaml(plugin, "data/statistics.yml"))
                    .thenReturn(new YamlConfiguration());
            PlayerServiceImpl service = new PlayerServiceImpl(plugin);

            UUID player = UUID.randomUUID();
            service.setCooldown(player, "global");
            service.setCooldown(player, "rp_do");
            service.removeCooldown(player);

            assertFalse(service.isOnCooldown(player, "global", 5));
            assertFalse(service.isOnCooldown(player, "rp_do", 5));
        }
    }
}