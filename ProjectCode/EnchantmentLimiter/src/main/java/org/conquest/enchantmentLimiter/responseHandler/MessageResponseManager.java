package org.conquest.enchantmentLimiter.responseHandler;

import net.kyori.adventure.text.Component;
import org.bukkit.command.CommandSender;
import org.conquest.enchantmentLimiter.EnchantmentLimiter;

import java.util.Map;

/**
 * 📬 MessageResponseManager
 * Centralized manager for sending MiniMessage-formatted responses with placeholders and prefix.
 */
public class MessageResponseManager {

    public static void send(CommandSender sender, MessageModels model) {
        send(sender, model, null);
    }

    public static void send(CommandSender sender, MessageModels model, Map<String, String> placeholders) {
        String rawPrefix = getRaw(MessageModels.PREFIX);
        String rawMessage = getRaw(model);

        Component formatted;
        if (placeholders != null && !placeholders.isEmpty()) {
            formatted = MiniMessageManager.format(rawMessage, rawPrefix, placeholders);
        } else {
            formatted = MiniMessageManager.format(rawMessage, rawPrefix);
        }

        sender.sendMessage(formatted);
    }

    private static String getRaw(MessageModels model) {
        return EnchantmentLimiter.getInstance()
                .getConfigurationManager()
                .getMessagesFile()
                .get()
                .getString(model.getPath(), "<red>[Missing: " + model.getPath() + "]</red>");
    }
}
