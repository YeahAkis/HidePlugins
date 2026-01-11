package org.notionsmp.hidePlugins;

import co.aikar.commands.PaperCommandManager;
import lombok.Getter;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.plugin.java.JavaPlugin;
import java.util.*;

@Getter
public final class HidePlugins extends JavaPlugin {
    @Getter
    private static HidePlugins instance;
    private PaperCommandManager commandManager;
    private MiniMessage miniMessage;
    private String defaultBlockMsg;
    private Map<String, String> commandMessages;
    private Set<String> disabledCommands;


    @Override
    public void onEnable() {
        instance = this;
        this.miniMessage = MiniMessage.miniMessage();

        saveDefaultConfig();
        reloadConfigValues();

        this.commandManager = new PaperCommandManager(this);
        commandManager.registerCommand(new HidePluginsCommand());

        getServer().getPluginManager().registerEvents(new CommandListener(), this);
    }

    public void reloadConfigValues() {
        reloadConfig();
        this.defaultBlockMsg = getConfig().getString("blockMsg", "<red>You are not allowed to use this command");
        this.disabledCommands = new HashSet<>(getConfig().getStringList("disabled-commands"));
        this.commandMessages = new HashMap<>();
        if (getConfig().contains("customBlockMsg")) {
            for (Map<?, ?> map : getConfig().getMapList("customBlockMsg")) {
                String commands = (String) map.get("command");
                String message = (String) map.get("message");
                if (commands != null && message != null) {
                    for (String cmd : commands.split("\\|")) {
                        commandMessages.put(cmd.trim().toLowerCase(), message);
                    }
                }
            }
        }
    }

    public String getMessageForCommand(String command) {
        String cmd = command.toLowerCase();
        return commandMessages.getOrDefault(cmd, defaultBlockMsg);
    }
}