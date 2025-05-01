package org.conquest.enchantmentLimiter;

import org.bukkit.plugin.java.JavaPlugin;
import org.conquest.enchantmentLimiter.commandHandler.AutoTabManager;
import org.conquest.enchantmentLimiter.commandHandler.CommandManager;
import org.conquest.enchantmentLimiter.configurationHandler.ConfigurationManager;
import org.conquest.enchantmentLimiter.restrictionHandler.EnchantmentTableListener;

public final class EnchantmentLimiter extends JavaPlugin {

    private static EnchantmentLimiter instance;

    private ConfigurationManager configurationManager;

    @Override
    public void onEnable() {
        instance = this;

        // 🧩 Load configuration
        this.configurationManager = new ConfigurationManager(this);
        this.configurationManager.initialize();

        // 🔗 Register features
        registerCommands();
        registerListeners();

        getLogger().info("✨  EnchantmentLimiter enabled.");
    }

    @Override
    public void onDisable() {
        getLogger().info("🛑  EnchantmentLimiter disabled.");
    }

    /**
     * Static access to plugin instance.
     */
    public static EnchantmentLimiter getInstance() {
        return instance;
    }

    /**
     * Reloads all plugin configuration files and resets memory cache (if any).
     */
    public void reloadPlugin() {
        configurationManager.initialize();
        getLogger().info("🔁  EnchantmentLimiter reloaded.");
    }

    /**
     * Registers all plugin event listeners.
     */
    private void registerListeners() {
        new EnchantmentTableListener(this);
        // new AnvilListener(this);      ← ready to plug in later
        // new ChunkGenerationListener(this); ← for library bookshelves?
    }

    /**
     * Register commands and tab completions.
     */
    private void registerCommands() {
        var enchantLimiterCommand = getCommand("enchantlimiter");

        if (enchantLimiterCommand != null) {
            enchantLimiterCommand.setExecutor(new CommandManager(this));
            enchantLimiterCommand.setTabCompleter(new AutoTabManager());
        } else {
            getLogger().severe("❌  Failed to register /enchantlimiter command! Check your plugin.yml.");
        }
    }

    public ConfigurationManager getConfigurationManager() {
        return configurationManager;
    }
}
