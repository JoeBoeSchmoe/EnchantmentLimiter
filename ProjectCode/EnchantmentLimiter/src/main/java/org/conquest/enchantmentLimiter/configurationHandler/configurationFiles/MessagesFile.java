package org.conquest.enchantmentLimiter.configurationHandler.configurationFiles;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.logging.Logger;

public class MessagesFile {

    private final JavaPlugin plugin;
    private final Logger log;

    private File file;
    private FileConfiguration config;

    public MessagesFile(JavaPlugin plugin) {
        this.plugin = plugin;
        this.log = plugin.getLogger();
        this.file = new File(plugin.getDataFolder(), "messages.yml");
    }

    /**
     * Loads or reloads messages.yml from disk.
     */
    public void reload() {
        try {
            if (!file.exists()) {
                plugin.getDataFolder().mkdirs();
                try (InputStream in = plugin.getResource("messages.yml")) {
                    if (in != null) {
                        Files.copy(in, file.toPath());
                        log.info("📄 Created default messages.yml");
                    } else {
                        log.warning("⚠️ Missing embedded messages.yml in plugin jar!");
                    }
                }
            }

            this.config = YamlConfiguration.loadConfiguration(file);
            validate();

            log.info("✅ Loaded messages.yml");

        } catch (Exception e) {
            log.severe("❌  Failed to load messages.yml: " + e.getMessage());
        }
    }

    private void validate() {
        if (!config.contains("reload-success")) {
            log.warning("⚠️  Missing 'reload-success' key in messages.yml");
        }
    }

    public void save() {
        try {
            config.save(file);
        } catch (Exception e) {
            log.severe("❌  Failed to save messages.yml: " + e.getMessage());
        }
    }

    public FileConfiguration get() {
        return config;
    }

    public String getMessage(String path) {
        return config.getString(path, "§c[Missing message: " + path + "]");
    }
}
