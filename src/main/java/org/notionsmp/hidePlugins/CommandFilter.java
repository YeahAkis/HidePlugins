package org.notionsmp.hidePlugins;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.permissions.Permissible;

import java.util.ArrayList;
import java.util.ConcurrentModificationException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

final class CommandFilter {

    static final String BYPASS = "hideplugins.bypass";

    private static final boolean BRIGADIER_ERRORS = HidePlugins.classExists("io.papermc.paper.command.brigadier.Commands");

    private final HidePlugins plugin;
    private volatile Map<String, String> index = Map.of();

    CommandFilter(HidePlugins plugin) {
        this.plugin = plugin;
    }

    Settings settings() {
        return plugin.settings();
    }

    void reindex() {
        Map<String, String> rules = settings().rules();
        Map<String, String> aliases = new HashMap<>();
        try {
            for (Map.Entry<String, Command> entry : new ArrayList<>(Bukkit.getCommandMap().getKnownCommands().entrySet())) {
                Command command = entry.getValue();
                List<String> names = new ArrayList<>(command.getAliases());
                names.add(command.getName());
                names.add(entry.getKey());

                String rule = names.stream().map(Settings::normalize).filter(rules::containsKey).findFirst().orElse(null);
                if (rule != null) {
                    for (String name : names) {
                        aliases.putIfAbsent(Settings.normalize(name), rule);
                    }
                }
            }
        } catch (ConcurrentModificationException e) {
            return;
        }
        index = aliases;
    }

    String match(String label) {
        return lookup(settings().rules(), index, Settings.normalize(label));
    }

    private static String lookup(Map<String, String> rules, Map<String, String> aliases, String name) {
        if (name.isEmpty()) {
            return null;
        }
        if (rules.containsKey(name)) {
            return name;
        }
        String rule = aliases.get(name);
        if (rule != null) {
            return rule;
        }
        int colon = name.indexOf(':');
        return colon >= 0 ? lookup(rules, aliases, name.substring(colon + 1)) : null;
    }

    boolean canBypass(Permissible who, String rule) {
        return who.hasPermission(BYPASS) || who.hasPermission(BYPASS + "." + rule);
    }

    boolean isHidden(String label, Permissible who) {
        String rule = match(label);
        return rule != null && !canBypass(who, rule);
    }

    boolean isVisible(String label, Permissible who) {
        if (who.hasPermission(BYPASS)) {
            return true;
        }
        if (settings().hideNamespaced() && label.indexOf(':') >= 0) {
            return false;
        }
        return !isHidden(label, who);
    }

    void reply(CommandSender sender, String rule, String input) {
        String message = settings().rules().get(rule);
        if (message == null || message.isBlank()) {
            return;
        }
        if (!BRIGADIER_ERRORS && message.trim().equals("<unknown_command>")) {
            sender.sendMessage(spigotUnknownCommand());
            return;
        }
        String label = input.split(" ", 2)[0];
        sender.sendMessage(MiniMessage.miniMessage().deserialize(message, TagResolver.resolver(
                Placeholder.unparsed("command", label),
                Placeholder.component("unknown_command", unknownCommand(input))
        )));
    }

    private static String spigotUnknownCommand() {
        String message = Bukkit.spigot().getConfig().getString("messages.unknown-command", "Unknown command. Type \"/help\" for help.");
        return ChatColor.translateAlternateColorCodes('&', message).replace("\\n", "\n");
    }

    static Component unknownCommand(String input) {
        if (!BRIGADIER_ERRORS) {
            return LegacyComponentSerializer.legacySection().deserialize(spigotUnknownCommand());
        }

        Component context = Component.text()
                .color(NamedTextColor.GRAY)
                .clickEvent(ClickEvent.suggestCommand("/" + input))
                .append(Component.text(input, NamedTextColor.RED, TextDecoration.UNDERLINED))
                .append(Component.translatable("command.context.here", NamedTextColor.RED, TextDecoration.ITALIC))
                .build();
        return Component.text()
                .color(NamedTextColor.RED)
                .append(Component.translatable("command.unknown.command"))
                .append(Component.newline())
                .append(context)
                .build();
    }
}
