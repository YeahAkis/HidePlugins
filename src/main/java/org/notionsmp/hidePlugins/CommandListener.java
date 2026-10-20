package org.notionsmp.hidePlugins;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerCommandSendEvent;
import org.bukkit.event.server.ServerLoadEvent;
import org.bukkit.event.server.TabCompleteEvent;

import java.util.ArrayList;
import java.util.List;

final class CommandListener implements Listener {

    private final CommandFilter filter;

    CommandListener(CommandFilter filter) {
        this.filter = filter;
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onCommand(PlayerCommandPreprocessEvent event) {
        Player player = event.getPlayer();
        String input = event.getMessage().startsWith("/") ? event.getMessage().substring(1) : event.getMessage();
        String rule = filter.match(input.split(" ", 2)[0]);
        if (rule == null || filter.canBypass(player, rule)) {
            return;
        }

        event.setCancelled(true);
        filter.reply(player, rule, input);
    }

    @EventHandler
    public void onServerLoad(ServerLoadEvent event) {
        filter.reindex();
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onCommandSend(PlayerCommandSendEvent event) {
        filter.reindex();
        Player player = event.getPlayer();
        event.getCommands().removeIf(name -> !filter.isVisible(name, player));
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onTabComplete(TabCompleteEvent event) {
        List<String> completions = filtered(event.getSender(), event.getBuffer(), event.getCompletions());
        if (completions == null) {
            event.setCancelled(true);
        } else if (completions != event.getCompletions()) {
            event.setCompletions(completions);
        }
    }

    private List<String> filtered(CommandSender sender, String buffer, List<String> completions) {
        if (!buffer.startsWith("/")) {
            return completions;
        }

        int space = buffer.indexOf(' ');
        if (space >= 0) {
            return filter.isHidden(buffer.substring(1, space), sender) ? null : completions;
        }

        List<String> visible = new ArrayList<>(completions);
        return visible.removeIf(label -> !filter.isVisible(label, sender)) ? visible : completions;
    }
}
