package org.conquest.enchantmentLimiter;

import org.bukkit.plugin.java.JavaPlugin;
import org.conquest.enchantmentLimiter.configurationHandler.ConfigurationManager;

public final class EnchantmentLimiter extends JavaPlugin {

    private static EnchantmentLimiter instance;
    private ConfigurationManager configurationManager;

    @Override
    public void onEnable() {
        instance = this;

        // 🧩 Initialize config system
        this.configurationManager = new ConfigurationManager();
        this.configurationManager.initialize();

        // 🧪 Register commands
        //getCommand("enchantlimiter").setExecutor(new EnchantLimiterCommand());

        getLogger().info("✨ EnchantmentLimiter enabled!");
    }

    @Override
    public void onDisable() {
        getLogger().info("🛑 EnchantmentLimiter disabled.");
    }

    public static EnchantmentLimiter getInstance() {
        return instance;
    }

    public ConfigurationManager getConfigurationManager() {
        return configurationManager;
    }

    public void reloadPlugin() {
        configurationManager.initialize();
        getLogger().info("🔁 EnchantmentLimiter reloaded.");
    }
}
