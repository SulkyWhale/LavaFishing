package io.github.sulkywhale.lavafishing.hooks;

import com.gamingmesh.jobs.Jobs;
import com.gamingmesh.jobs.actions.ItemActionInfo;
import com.gamingmesh.jobs.container.ActionType;
import com.gamingmesh.jobs.container.JobsPlayer;
import org.bukkit.inventory.ItemStack;

public class JobsImplementation {

    public static void processAction(JobsPlayer jobsPlayer, ItemStack loot) {
        Jobs.action(jobsPlayer, new ItemActionInfo(loot, ActionType.FISH));
    }
}
