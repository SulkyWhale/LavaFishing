package io.github.sulkywhale.lavafishing.listeners;

import io.github.sulkywhale.lavafishing.FishingManager;
import io.github.sulkywhale.lavafishing.hooks.McMMOHook;
import org.bukkit.entity.FishHook;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerFishEvent;

public class PlayerFishListener implements Listener {

    @EventHandler
    public void onPlayerFish(PlayerFishEvent event) {
        Player player = event.getPlayer();
        if (!player.hasPermission("lavafishing.fishing"))
            return;

        if (!McMMOHook.isEnabled() || !McMMOHook.hasUnlockedLavaFishing(player))
            return;

        FishHook hook = event.getHook();
        if (event.getState() == PlayerFishEvent.State.FISHING) {
            FishingManager.addFishingPlayer(player, hook);
            return;
        }
        if (!hook.isInLava()) {
            FishingManager.fish(player, false, hook);
            return;
        }
        FishingManager.fish(player, true, hook);
    }

}
