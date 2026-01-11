package org.notionsmp.hidePlugins;

import net.kyori.adventure.text.Component;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerCommandSendEvent;
import org.bukkit.event.server.TabCompleteEvent;

public class CommandListener implements Listener {

    @EventHandler
    public void onDisabledCommandExecution(PlayerCommandPreprocessEvent event) {
        if (!event.getPlayer().hasPermission("hide_plugins.bypass")) {
            String[] args = event.getMessage().split(" ");
            if (args.length == 0) return;
            String command = normalizeCommand(args[0]);
            if (isDisabledCommand(command)) {
                String message = HidePlugins.getInstance().getMessageForCommand(command);
                Component msg = HidePlugins.getInstance().getMiniMessage().deserialize(message);
                event.getPlayer().sendMessage(msg);
                event.setCancelled(true);
            }
        }
    }


    @EventHandler
    public void onTabComplete(TabCompleteEvent event) {
        if (event.getSender().hasPermission("hide_plugins.bypass")) return;

        String buffer = event.getBuffer();
        if (buffer.startsWith("/")) {
            String[] parts = buffer.split(" ", 2);
            String command = normalizeCommand(parts[0]);
            if (isDisabledCommand(command)) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onCommandSend(PlayerCommandSendEvent event) {
        if (event.getPlayer().hasPermission("hide_plugins.bypass")) return;

        event.getCommands().removeIf(this::isDisabledCommand);
    }

    private boolean isDisabledCommand(String command) {
        String cmd = normalizeCommand(command);
        if (cmd.isEmpty()) return false;
        HidePlugins plugin = HidePlugins.getInstance();
        for (String disabledCmd : plugin.getDisabledCommands()) {
            if (disabledCmd.equalsIgnoreCase(cmd)) {
                return true;
            }
        }
        for (String customCmd : plugin.getCommandMessages().keySet()) {
            if (customCmd.equalsIgnoreCase(cmd)) {
                return true;
            }
        }
        return false;
    }

    private String normalizeCommand(String rawCommand) {
        String cmd = rawCommand.toLowerCase();
        if (cmd.startsWith("/")) {
            cmd = cmd.substring(1);
        }
        return cmd.trim();
    }
}