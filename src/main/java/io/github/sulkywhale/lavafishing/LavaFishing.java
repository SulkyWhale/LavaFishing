package io.github.sulkywhale.lavafishing;

import io.github.sulkywhale.lavafishing.config.Config;
import io.github.sulkywhale.lavafishing.hooks.JobsHook;
import io.github.sulkywhale.lavafishing.hooks.McMMOHook;
import io.github.sulkywhale.lavafishing.listeners.McMMOListener;
import io.github.sulkywhale.lavafishing.listeners.PlayerFishListener;
import org.bukkit.plugin.java.JavaPlugin;

public class LavaFishing extends JavaPlugin {

    private static LavaFishing plugin;

    public LavaFishing() {
        plugin = this;
    }

    @Override
    public void onEnable() {
        saveDefaultConfig();
        Config.init(getConfig());
        getServer().getPluginManager().registerEvents(new PlayerFishListener(), this);
        FishingManager.runScheduler(this);
        if (getServer().getPluginManager().getPlugin("mcMMO") != null) {
            McMMOHook.init();
            getServer().getPluginManager().registerEvents(new McMMOListener(), this);
            getLogger().info("Successfully hooked with mcMMO.");
        }
        if (getServer().getPluginManager().getPlugin("Jobs") != null) {
            JobsHook.init();
            getLogger().info("Successfully hooked with Jobs.");
        }
        getLogger().info("Plugin enabled.");
    }

    @Override
    public void onDisable() {
        getLogger().info("Plugin disabled.");
    }

    public static LavaFishing getPlugin() {
        return plugin;
    }

}
