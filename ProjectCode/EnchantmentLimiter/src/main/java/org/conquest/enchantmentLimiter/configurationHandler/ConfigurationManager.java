package org.conquest.enchantmentLimiter.configurationHandler;

import org.conquest.enchantmentLimiter.EnchantmentLimiter;
import org.conquest.enchantmentLimiter.configurationHandler.configurationFiles.ConfigFile;
import org.conquest.enchantmentLimiter.configurationHandler.configurationFiles.MessagesFile;

import java.io.File;
import java.util.logging.Logger;

/**
 * 🧩 ConfigurationManager
 * Handles loading of core config files like config.yml and messages.yml.
 * Delegates config file logic to dedicated handlers.
 */
public class ConfigurationManager {

    private final EnchantmentLimiter plugin;
    private final Logger log;

    private final ConfigFile configFile;
    private final MessagesFile messagesFile;

    public ConfigurationManager(EnchantmentLimiter plugin) {
        this.plugin = plugin;
        this.log = plugin.getLogger();

        this.configFile = new ConfigFile(plugin);
        this.messagesFile = new MessagesFile(plugin);
    }

    /**
     * Initializes configuration files.
     */
    public void initialize() {
        try {
            File dataFolder = plugin.getDataFolder();
            if (!dataFolder.exists() && !dataFolder.mkdirs()) {
                log.warning("⚠️  Failed to create plugin data folder: " + dataFolder.getAbsolutePath());
            }

            configFile.reload();
            messagesFile.reload();

            log.info("✅  Configuration initialization complete.");
        } catch (Exception e) {
            log.severe("❌  Failed to initialize configuration: " + e.getMessage());
        }
    }

    public ConfigFile getConfigFile() {
        return configFile;
    }

    public MessagesFile getMessagesFile() {
        return messagesFile;
    }
}
