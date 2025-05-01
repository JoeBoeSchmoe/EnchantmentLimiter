package org.conquest.enchantmentLimiter.commandHandler;

import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.enchantments.Enchantment;
import org.conquest.enchantmentLimiter.EnchantmentLimiter;
import org.conquest.enchantmentLimiter.enchantmentHandler.EnchantmentLimitModel;
import org.conquest.enchantmentLimiter.enchantmentHandler.EnchantmentRegistry;
import org.conquest.enchantmentLimiter.responseHandler.MessageModels;
import org.conquest.enchantmentLimiter.responseHandler.MessageResponseManager;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

/**
 * 🧩 CommandManager
 * Handles /enchantlimiter and its subcommands.
 */
public class CommandManager implements CommandExecutor {

    private final EnchantmentLimiter plugin;

    public CommandManager(EnchantmentLimiter plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {

        // 🔐 Permission check
        if (!sender.hasPermission("enchantmentlimiter.admin")) {
            MessageResponseManager.send(sender, MessageModels.NO_PERMISSION);
            return true;
        }

        // 🔁 /enchantlimiter reload
        if (args.length == 1 && args[0].equalsIgnoreCase("reload")) {
            try {
                plugin.reloadPlugin();
                MessageResponseManager.send(sender, MessageModels.RELOAD_SUCCESS);
            } catch (Exception e) {
                MessageResponseManager.send(sender, MessageModels.RELOAD_FAIL);
            }
            return true;
        }

        // ⚙️ /enchantlimiter <enchant> <min|max> <value>
        if (args.length == 3) {
            String enchantRaw = args[0].toLowerCase();
            String type = args[1].toLowerCase();
            String valueStr = args[2];

            int value;
            try {
                value = Integer.parseInt(valueStr);
            } catch (NumberFormatException e) {
                MessageResponseManager.send(sender, MessageModels.INVALID_VALUE, Map.of("value", valueStr));
                return true;
            }

            Enchantment enchantment = Registry.ENCHANTMENT.get(NamespacedKey.minecraft(enchantRaw));
            if (enchantment == null) {
                MessageResponseManager.send(sender, MessageModels.INVALID_ENCHANT, Map.of("enchant", enchantRaw));
                return true;
            }

            if (!type.equals("min") && !type.equals("max")) {
                MessageResponseManager.send(sender, MessageModels.INVALID_COMMAND);
                return true;
            }

            // ✅ Apply update
            EnchantmentRegistry registry = plugin.getConfigurationManager()
                    .getConfigFile()
                    .getEnchantmentRegistry();

            registry.updateLimit(enchantment, type, value);

            // 🟡 Warn if bounds conflict
            int currentMin = registry.get(enchantment).map(EnchantmentLimitModel::getMinLevel).orElse(1);
            int currentMax = registry.get(enchantment).map(EnchantmentLimitModel::getMaxLevel).orElse(1);

            if (type.equals("min") && value > currentMax) {
                MessageResponseManager.send(sender, MessageModels.MIN_OVERRIDES_MAX);
            } else if (type.equals("max") && value < currentMin) {
                MessageResponseManager.send(sender, MessageModels.MAX_BELOW_MIN);
            }

            // ✅ Success message
            Map<String, String> placeholders = new HashMap<>();
            placeholders.put("enchant", enchantRaw);
            placeholders.put("type", type);
            placeholders.put("value", String.valueOf(value));

            MessageResponseManager.send(sender, MessageModels.EDIT_SUCCESS, placeholders);
            return true;
        }

        // ❌ Fallback
        MessageResponseManager.send(sender, MessageModels.INVALID_COMMAND);
        return true;
    }

}
