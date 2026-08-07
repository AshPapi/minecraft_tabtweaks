package com.pob.tabtweaks;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import com.electronwill.nightconfig.core.Config;
import com.electronwill.nightconfig.core.file.FileConfig;
import net.minecraftforge.fml.loading.FMLPaths;

public final class TabConfig {

    private static final String FILE_NAME = "tabtweaks.toml";

    private TabConfig() {
    }

    public static void ensureConfigExists() {
        Path path = FMLPaths.CONFIGDIR.get().resolve(FILE_NAME);
        if (!Files.exists(path)) {
            writeDefault(path);
        }
    }

    public static List<TabRule> load() {
        ensureConfigExists();
        Path path = FMLPaths.CONFIGDIR.get().resolve(FILE_NAME);

        List<TabRule> rules = new ArrayList<>();
        try (FileConfig config = FileConfig.of(path)) {
            config.load();

            List<Config> entries = config.get("tab");
            if (entries == null) {
                return rules;
            }

            for (Config entry : entries) {
                String id = entry.get("id");
                if (id == null || id.isBlank()) {
                    TabTweaksMod.LOGGER.warn("В {} есть запись [[tab]] без id — пропущена", FILE_NAME);
                    continue;
                }
                rules.add(new TabRule(
                        id.trim(),
                        entry.get("icon"),
                        readInt(entry, "page"),
                        readInt(entry, "slot"),
                        Boolean.TRUE.equals(entry.get("hidden"))));
            }
        } catch (RuntimeException e) {
            TabTweaksMod.LOGGER.error("Не удалось прочитать {} — правки вкладок не применены", FILE_NAME, e);
            return List.of();
        }

        return rules;
    }

    private static int readInt(Config entry, String key) {
        Object value = entry.get(key);
        return value instanceof Number number ? number.intValue() : -1;
    }

    private static void writeDefault(Path path) {
        try (InputStream template = TabConfig.class.getResourceAsStream("/tabtweaks-default.toml")) {
            if (template == null) {
                return;
            }
            Files.createDirectories(path.getParent());
            Files.copy(template, path);
            TabTweaksMod.LOGGER.info("Создан {} с примерами", path);
        } catch (IOException e) {
            TabTweaksMod.LOGGER.error("Не удалось создать {}", path, e);
        }
    }
}
