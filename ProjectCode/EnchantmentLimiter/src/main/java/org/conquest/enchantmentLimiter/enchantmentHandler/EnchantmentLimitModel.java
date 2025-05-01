package org.conquest.enchantmentLimiter.enchantmentHandler;

import org.bukkit.enchantments.Enchantment;

/**
 * 📦 EnchantmentLimitModel
 * Represents the min/max level limits for a specific enchantment.
 */
public class EnchantmentLimitModel {

    private final Enchantment enchantment;
    private final int minLevel;
    private final int maxLevel;

    public EnchantmentLimitModel(Enchantment enchantment, int minLevel, int maxLevel) {
        this.enchantment = enchantment;
        this.minLevel = minLevel;
        this.maxLevel = maxLevel;
    }

    public Enchantment getEnchantment() {
        return enchantment;
    }

    public int getMinLevel() {
        return minLevel;
    }

    public int getMaxLevel() {
        return maxLevel;
    }

    /**
     * Checks if the given level is within the defined bounds.
     * @param level The enchantment level to check.
     * @return True if the level is within min and max (inclusive).
     */
    public boolean isWithinBounds(int level) {
        return level >= minLevel && level <= maxLevel;
    }

    @Override
    public String toString() {
        return enchantment.getKey().getKey().toUpperCase() + " → Min: " + minLevel + ", Max: " + maxLevel;
    }
}
