package org.notionsmp.hidePlugins;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.Collections;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.logging.Logger;

public record Settings(Map<String, String> rules, boolean hideNamespaced) {

    static Settings load(FileConfiguration config, Logger logger) {
        String message = config.getString(pick(config, "message", "blockMsg"), "");
        Map<String, String> rules = new HashMap<>();

        for (String name : config.getStringList(pick(config, "hidden-commands", "disabled-commands"))) {
            put(rules, name, message);
        }

        ConfigurationSection custom = config.getConfigurationSection("custom-messages");
        if (custom != null) {
            for (String name : custom.getKeys(false)) {
                put(rules, name, custom.getString(name, message));
            }
        }

        for (Map<?, ?> entry : config.getMapList("customBlockMsg")) {
            if (entry.get("command") instanceof String names && entry.get("message") instanceof String text) {
                for (String name : names.split("\\|")) {
                    put(rules, name, text);
                }
            }
        }

        if (config.isSet("disabled-commands")) {
            logger.warning("You're using a 2.x config. It still works, but check the README for the new format.");
        }

        return new Settings(Collections.unmodifiableMap(rules), config.getBoolean("hide-namespaced-commands", true));
    }

    static String normalize(String label) {
        String name = label.trim().toLowerCase(Locale.ROOT);
        return name.startsWith("/") ? name.substring(1) : name;
    }

    private static String pick(FileConfiguration config, String key, String oldKey) {
        return !config.isSet(key) && config.isSet(oldKey) ? oldKey : key;
    }

    private static void put(Map<String, String> rules, String name, String message) {
        String key = normalize(name);
        if (!key.isEmpty()) {
            rules.put(key, message);
        }
    }
}
