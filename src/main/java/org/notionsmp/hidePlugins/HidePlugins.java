package org.notionsmp.hidePlugins;

import org.bukkit.command.PluginCommand;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public final class HidePlugins extends JavaPlugin {

    private static final boolean FOLIA = classExists("io.papermc.paper.threadedregions.RegionizedServer");

    private volatile Settings settings;
    private CommandFilter filter;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        settings = Settings.load(getConfig(), getLogger());

        filter = new CommandFilter(this);
        filter.reindex();
        getServer().getPluginManager().registerEvents(new CommandListener(filter), this);
        if (classExists("com.destroystokyo.paper.event.brigadier.AsyncPlayerSendSuggestionsEvent")) {
            getServer().getPluginManager().registerEvents(new SuggestionListener(filter), this);
        }

        PluginCommand command = getCommand("hideplugins");
        if (command != null) {
            HidePluginsCommand executor = new HidePluginsCommand(this, filter);
            command.setExecutor(executor);
            command.setTabCompleter(executor);
        }
    }

    public Settings settings() {
        return settings;
    }

    public void reload() {
        reloadConfig();
        settings = Settings.load(getConfig(), getLogger());
        filter.reindex();
        for (Player player : getServer().getOnlinePlayers()) {
            if (FOLIA) {
                player.getScheduler().run(this, task -> player.updateCommands(), null);
            } else {
                getServer().getScheduler().runTask(this, player::updateCommands);
            }
        }
    }

    static boolean classExists(String name) {
        try {
            Class.forName(name);
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }
}
