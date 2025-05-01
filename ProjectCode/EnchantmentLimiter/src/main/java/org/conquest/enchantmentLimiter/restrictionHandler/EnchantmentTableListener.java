package org.conquest.enchantmentLimiter.restrictionHandler;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.enchantments.EnchantmentOffer;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.enchantment.EnchantItemEvent;
import org.bukkit.event.enchantment.PrepareItemEnchantEvent;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.conquest.enchantmentLimiter.EnchantmentLimiter;
import org.conquest.enchantmentLimiter.enchantmentHandler.EnchantmentLimitModel;
import org.conquest.enchantmentLimiter.enchantmentHandler.EnchantmentRegistry;

import java.util.*;

public class EnchantmentTableListener implements Listener {

    private final EnchantmentRegistry registry;
    private final Map<String, EnchantmentOffer[]> cachedOffers = new HashMap<>();

    public EnchantmentTableListener(JavaPlugin plugin) {
        this.registry = EnchantmentLimiter.getInstance()
                .getConfigurationManager()
                .getConfigFile()
                .getEnchantmentRegistry();

        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    private int getScaledLevel(int min, int max, int slotCost) {
        if (min >= max) return min;

        double xpProgress = Math.min(slotCost / 30.0, 1.0); // 0 to 1
        double maxRollChance = 0.95; // Allow almost full max level

        // 🎲 Random value that's weighted TOWARD 1
        double randomBoost = 1.0 - Math.pow(Math.random(), 2); // heavily weighted toward 1

        // 📈 Increase reward for higher XP levels
        double roll = xpProgress * maxRollChance + (1 - xpProgress) * randomBoost * 0.4;

        int scaled = min + (int) Math.round((max - min) * roll);
        return Math.min(scaled, max);
    }


    @EventHandler
    public void onPrepareEnchant(PrepareItemEnchantEvent event) {
        ItemStack item = event.getItem();
        int[] costs = event.getExpLevelCostsOffered();
        EnchantmentOffer[] customOffers = new EnchantmentOffer[3];

        // Build weighted pool
        List<Enchantment> weightedPool = new ArrayList<>();

        for (Map.Entry<Enchantment, EnchantmentLimitModel> entry : registry.getAll().entrySet()) {
            Enchantment enchant = entry.getKey();
            EnchantmentLimitModel model = entry.getValue();

            if (!enchant.canEnchantItem(item)) continue;
            if (model.getMinLevel() == 0 && model.getMaxLevel() == 0) continue;

            int weight = getWeight(enchant);

            for (int i = 0; i < weight; i++) {
                weightedPool.add(enchant);
            }
        }

        if (weightedPool.isEmpty()) {
            for (int i = 0; i < 3; i++) customOffers[i] = null;
            return;
        }

        Collections.shuffle(weightedPool);

        // Select up to 3 distinct enchantments
        Set<Enchantment> selected = new HashSet<>();
        int index = 0;
        for (int i = 0; i < 3; i++) {
            while (index < weightedPool.size()) {
                Enchantment e = weightedPool.get(index++);
                if (selected.add(e)) {
                    EnchantmentLimitModel model = registry.get(e).orElse(null);
                    if (model == null) continue;

                    int scaled = getScaledLevel(model.getMinLevel(), model.getMaxLevel(), costs[i]);
                    customOffers[i] = new EnchantmentOffer(e, scaled, costs[i]);

                    EnchantmentLimiter.getInstance().getLogger().info("[EnchantmentLimiter] Offer " + i + " → " + e.getKey().getKey() + " lvl " + scaled);
                    break;
                }

            }
        }

        // Apply to event
        EnchantmentOffer[] offers = event.getOffers();
        System.arraycopy(customOffers, 0, offers, 0, 3);

        // Cache
        String key = event.getEnchanter().getUniqueId() + "|" + item.hashCode();
        cachedOffers.put(key, customOffers);
    }

    private int getWeight(Enchantment enchantment) {
        String key = enchantment.getKey().getKey();

        // ⚔️ Combat - Common
        if (key.contains("sharpness") || key.contains("protection") || key.contains("power")) return 6;
        if (key.contains("unbreaking") || key.contains("efficiency") || key.contains("fire_aspect")) return 5;

        // ⚙️ Utility - Uncommon
        if (key.contains("looting") || key.contains("fortune") || key.contains("flame")) return 4;
        if (key.contains("punch") || key.contains("knockback") || key.contains("smite")) return 3;

        // 💎 Rare
        if (key.contains("mending") || key.contains("frost_walker") || key.contains("soul_speed")) return 2;
        if (key.contains("binding") || key.contains("vanishing")) return 1;

        // Default fallback
        return 3;
    }

    @EventHandler
    public void onEnchantItem(EnchantItemEvent event) {
        ItemStack item = event.getItem();
        int slot = event.whichButton(); // 0, 1, or 2
        String key = event.getEnchanter().getUniqueId() + "|" + item.hashCode();

        EnchantmentOffer[] offers = cachedOffers.remove(key);
        if (offers == null || slot < 0 || slot >= offers.length) return;

        EnchantmentOffer selected = offers[slot];
        if (selected == null) return;

        Enchantment enchant = selected.getEnchantment();
        int level = selected.getEnchantmentLevel();

        event.getEnchantsToAdd().clear();

        new BukkitRunnable() {
            @Override
            public void run() {
                item.getEnchantments().keySet().forEach(item::removeEnchantment);
                item.addUnsafeEnchantment(enchant, Math.min(level, 255));

                ItemMeta meta = item.getItemMeta();
                PersistentDataContainer container = meta.getPersistentDataContainer();
                NamespacedKey namespacedKey = new NamespacedKey(EnchantmentLimiter.getInstance(), enchant.getKey().getKey());
                container.set(namespacedKey, PersistentDataType.INTEGER, level);

                String readable = Arrays.stream(enchant.getKey().getKey().split("_"))
                        .map(s -> s.substring(0, 1).toUpperCase() + s.substring(1))
                        .reduce((a, b) -> a + " " + b)
                        .orElse(enchant.getKey().getKey());

                meta.lore(List.of(Component.text(readable + " " + level, NamedTextColor.GRAY)));
                meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
                item.setItemMeta(meta);
            }
        }.runTaskLater(EnchantmentLimiter.getInstance(), 1L);
    }
}
