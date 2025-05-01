package org.conquest.enchantmentLimiter.configurationHandler.configurationFiles;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;
import org.conquest.enchantmentLimiter.enchantmentHandler.EnchantmentRegistry;

import java.io.File;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.logging.Logger;

/**
 * 📁 ConfigFile
 * Manages config.yml and populates the enchantment registry.
 */
public class ConfigFile {

    private final JavaPlugin plugin;
    private final Logger log;
    private final File file;
    private FileConfiguration config;

    private final EnchantmentRegistry enchantmentRegistry;

    public ConfigFile(JavaPlugin plugin) {
        this.plugin = plugin;
        this.log = plugin.getLogger();
        this.file = new File(plugin.getDataFolder(), "config.yml");
        this.enchantmentRegistry = new EnchantmentRegistry(); // Init the registry
    }

    /**
     * Loads or reloads config.yml from disk and updates registry.
     */
    public void reload() {
        try {
            if (!file.exists()) {
                plugin.getDataFolder().mkdirs();
                try (InputStream in = plugin.getResource("config.yml")) {
                    if (in != null) {
                        Files.copy(in, file.toPath());
                        log.info("📄  Created default config.yml");
                    } else {
                        log.warning("⚠️  Missing embedded config.yml in plugin jar!");
                    }
                }
            }

            this.config = YamlConfiguration.loadConfiguration(file);
            validate();

            // 🧠 Populate the registry now
            enchantmentRegistry.loadFromConfig();

            log.info("✅  Loaded config.yml and enchantment registry");

        } catch (Exception e) {
            log.severe("❌  Failed to load config.yml: " + e.getMessage());
        }
    }

    private void validate() {
        if (!config.contains("limits")) {
            log.warning("⚠️  Missing 'limits' section in config.yml — plugin may misbehave.");
        }
    }

    public void save() {
        try {
            config.save(file);
        } catch (Exception e) {
            log.severe("❌  Failed to save config.yml: " + e.getMessage());
        }
    }

    public FileConfiguration get() {
        return config;
    }

    public EnchantmentRegistry getEnchantmentRegistry() {
        return enchantmentRegistry;
    }
}
