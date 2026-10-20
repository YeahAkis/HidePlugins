package org.notionsmp.hidePlugins;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;

import java.util.List;
import java.util.Locale;

final class HidePluginsCommand implements TabExecutor {

    private final HidePlugins plugin;
    private final CommandFilter filter;

    HidePluginsCommand(HidePlugins plugin, CommandFilter filter) {
        this.plugin = plugin;
        this.filter = filter;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        String sub = args.length == 0 ? "" : args[0].toLowerCase(Locale.ROOT);

        if (sub.equals("reload")) {
            plugin.reload();
            sender.sendMessage(Component.text("HidePlugins reloaded, " + plugin.settings().rules().size() + " commands hidden.", NamedTextColor.GREEN));
            return true;
        }

        if (sub.equals("check") && args.length == 2) {
            String rule = filter.match(args[1]);
            if (rule == null) {
                sender.sendMessage(Component.text("/" + Settings.normalize(args[1]) + " is not hidden.", NamedTextColor.YELLOW));
            } else {
                sender.sendMessage(Component.text("/" + Settings.normalize(args[1]) + " is hidden by the \"" + rule + "\" entry.", NamedTextColor.GREEN));
            }
            return true;
        }

        sender.sendMessage(Component.text("Usage: /" + label + " reload | check <command>", NamedTextColor.RED));
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 1) {
            return List.of("reload", "check").stream().filter(s -> s.startsWith(args[0].toLowerCase(Locale.ROOT))).toList();
        }
        return List.of();
    }
}
