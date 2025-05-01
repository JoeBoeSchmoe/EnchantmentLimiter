package org.conquest.enchantmentLimiter.responseHandler;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;

public class MiniMessageManager {

    private static final MiniMessage miniMessage = MiniMessage.miniMessage();

    public static Component format(String message) {
        return miniMessage.deserialize(message);
    }

    public static Component format(String message, String prefix) {
        return miniMessage.deserialize(prefix + message);
    }

    public static Component format(String message, String prefix, java.util.Map<String, String> placeholders) {
        for (var entry : placeholders.entrySet()) {
            message = message.replace("<" + entry.getKey() + ">", entry.getValue());
        }
        return miniMessage.deserialize(prefix + message);
    }
}
