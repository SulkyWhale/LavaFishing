package io.github.sulkywhale.lavafishing.hooks;

import com.gamingmesh.jobs.Jobs;
import com.gamingmesh.jobs.PlayerManager;
import com.gamingmesh.jobs.actions.ItemActionInfo;
import com.gamingmesh.jobs.container.ActionType;
import com.gamingmesh.jobs.container.JobsPlayer;
import com.gamingmesh.jobs.listeners.JobsPaymentListener;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class JobsHook {

    private static boolean enabled = false;

    public static void init() {
        enabled = true;
    }

    public static boolean isEnabled() {
        return enabled;
    }

    public static void grantJobsRewards(Player player, ItemStack loot) {
        PlayerManager playerManager = Jobs.getPlayerManager();
        JobsPlayer jobsPlayer = playerManager.getJobsPlayer(player);
        if (!Jobs.getGeneralConfigManager().canPerformActionInWorld(player))
            return;

        if (!JobsPaymentListener.payIfCreative(player))
            return;

        if (!Jobs.getPermissionHandler().hasWorldPermission(player))
            return;

        if (Jobs.getGeneralConfigManager().disablePaymentIfRiding && player.isInsideVehicle() && !player.getVehicle().getType().toString().contains("BOAT"))
            return;

        if (!JobsPaymentListener.payForItemDurabilityLoss(player))
            return;

        Jobs.action(jobsPlayer, new ItemActionInfo(loot, ActionType.FISH));
    }

}
