package org.conquest.enchantmentLimiter.responseHandler;

/**
 * 📬 MessageModels
 * Enum for referencing MiniMessage-compatible messages.yml paths.
 */
public enum MessageModels {

    PREFIX("prefix"),

    // ✅ Success
    RELOAD_SUCCESS("reload-success"),
    EDIT_SUCCESS("edit-success"),

    // ❌ Failures
    RELOAD_FAIL("reload-fail"),
    EDIT_FAIL("edit-fail"),

    // 🚫 Permissions & Errors
    NO_PERMISSION("no-permission"),
    INVALID_COMMAND("invalid-command"),
    INVALID_ENCHANT("invalid-enchant"),
    INVALID_VALUE("invalid-value"),

    // 🧪 Utility
    CURRENT_LIMIT("current-limit");

    private final String path;

    MessageModels(String path) {
        this.path = path;
    }

    public String getPath() {
        return path;
    }
}
