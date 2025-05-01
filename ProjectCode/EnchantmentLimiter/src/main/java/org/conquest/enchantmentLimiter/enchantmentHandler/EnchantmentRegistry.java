package org.conquest.enchantmentLimiter.enchantmentHandler;

import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.conquest.enchantmentLimiter.EnchantmentLimiter;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.logging.Logger;

/**
 * 🧠 EnchantmentRegistry
 * Holds all enchantment limits in memory and handles config sync.
 */
public class EnchantmentRegistry {

    private final Map<Enchantment, EnchantmentLimitModel> registry = new HashMap<>();
    private final Logger log = EnchantmentLimiter.getInstance().getLogger();

    /**
     * Loads all enchantment limits from config.yml into memory.
     */
    public void loadFromConfig() {
        registry.clear();

        FileConfiguration config = EnchantmentLimiter.getInstance()
                .getConfigurationManager()
                .getConfigFile()
                .get();

        ConfigurationSection root = config.getConfigurationSection("limits");
        if (root == null) {
            log.warning("⚠️  'limits' section missing in config.yml");
            return;
        }

        Registry<@NotNull Enchantment> enchantmentRegistry = Bukkit.getRegistry(Enchantment.class);

        for (String category : root.getKeys(false)) {
            ConfigurationSection section = root.getConfigurationSection(category);
            if (section == null) continue;

            for (String enchantName : section.getKeys(false)) {
                NamespacedKey key = NamespacedKey.minecraft(enchantName.toLowerCase());
                Enchantment enchantment = enchantmentRegistry.get(key);

                if (enchantment == null) {
                    log.warning("❌  Unknown enchantment in config: '" + enchantName + "' (key: " + key + ")");
                    continue;
                }

                int min = section.getInt(enchantName + ".min", 1);
                int max = section.getInt(enchantName + ".max", 1);

                registry.put(enchantment, new EnchantmentLimitModel(enchantment, min, max));
            }
        }

        log.info("✅  Loaded " + registry.size() + " enchantment limits from config.");
    }

    /**
     * Updates the min or max value for a specific enchantment and persists it.
     */
    public void updateLimit(Enchantment enchantment, String type, int value) {
        EnchantmentLimitModel current = registry.getOrDefault(
                enchantment, new EnchantmentLimitModel(enchantment, 1, 1)
        );

        int min = current.getMinLevel();
        int max = current.getMaxLevel();

        if (type.equalsIgnoreCase("min")) {
            min = value;
        } else if (type.equalsIgnoreCase("max")) {
            max = value;
        } else {
            log.warning("⚠️  Invalid type in updateLimit: " + type + ". Must be 'min' or 'max'.");
            return;
        }

        registry.put(enchantment, new EnchantmentLimitModel(enchantment, min, max));

        FileConfiguration config = EnchantmentLimiter.getInstance()
                .getConfigurationManager()
                .getConfigFile()
                .get();

        boolean saved = false;

        ConfigurationSection root = config.getConfigurationSection("limits");
        if (root != null) {
            for (String category : root.getKeys(false)) {
                ConfigurationSection section = root.getConfigurationSection(category);
                if (section == null) continue;

                if (section.contains(enchantment.getKey().getKey())) {
                    section.set(enchantment.getKey().getKey() + ".min", min);
                    section.set(enchantment.getKey().getKey() + ".max", max);
                    saved = true;
                    break;
                }
            }
        }

        if (saved) {
            EnchantmentLimiter.getInstance()
                    .getConfigurationManager()
                    .getConfigFile()
                    .save();

            log.info("💾  Updated limit for " + enchantment.getKey().getKey() + ": min=" + min + ", max=" + max);
        } else {
            log.warning("⚠️  Could not find enchantment in config to save: " + enchantment.getKey().getKey());
        }
    }

    /**
     * Gets the limit model for a given enchantment.
     */
    public Optional<EnchantmentLimitModel> get(Enchantment enchantment) {
        return Optional.ofNullable(registry.get(enchantment));
    }

    /**
     * Checks if a level is allowed for the specified enchantment.
     */
    public boolean isAllowed(Enchantment enchantment, int level) {
        EnchantmentLimitModel model = registry.get(enchantment);
        return model == null || model.isWithinBounds(level);
    }

    /**
     * Returns the full internal map of enchantment limits.
     */
    public Map<Enchantment, EnchantmentLimitModel> getAll() {
        return registry;
    }
}
