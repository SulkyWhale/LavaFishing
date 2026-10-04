package io.github.sulkywhale.lavafishing.hooks;

import com.gmail.nossr50.api.ExperienceAPI;
import com.gmail.nossr50.datatypes.player.McMMOPlayer;
import com.gmail.nossr50.datatypes.skills.SubSkillType;
import com.gmail.nossr50.skills.fishing.FishingManager;
import com.gmail.nossr50.util.player.UserManager;
import com.gmail.nossr50.util.skills.RankUtils;
import io.github.sulkywhale.lavafishing.config.Config;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

public class McMMOHook {

    private static boolean enabled = false;
    private static int unlockLevel;

    public static void init() {
        enabled = true;
        unlockLevel = Config.getMcMMOUnlockLevel();
    }

    public static boolean isEnabled() {
        return enabled;
    }

    public static int getUnlockLevel() {
        return unlockLevel;
    }

    public static boolean hasUnlockedLavaFishing(Player player) {
        McMMOPlayer mcMMOPlayer = UserManager.getPlayer(player);
        if (mcMMOPlayer == null) {
            return false;
        }
        return mcMMOPlayer.getFishingManager().getSkillLevel() >= unlockLevel;
    }

    public static void grantFishingXp(Player player, int xp) {
        ExperienceAPI.addXP(player, "Fishing", xp, "PVE");
    }

    public static boolean isFishingTooOften(Player player) {
        McMMOPlayer mcMMOPlayer = UserManager.getPlayer(player);
        if (mcMMOPlayer == null)
            return false;

        return mcMMOPlayer.getFishingManager().isFishingTooOften();
    }

    public static boolean isExploiting(Player player, Vector vector) {
        McMMOPlayer mcMMOPlayer = UserManager.getPlayer(player);
        if (mcMMOPlayer == null)
            return false;

        FishingManager mcmmoFishingManager = mcMMOPlayer.getFishingManager();
        mcmmoFishingManager.processExploiting(vector);
        return mcmmoFishingManager.isExploitingFishing();
    }

    public static int getMasterAnglerMinWaitTime(Player player, int minWaitTime) {
        McMMOPlayer mcMMOPlayer = UserManager.getPlayer(player);
        if (mcMMOPlayer == null) {
            return minWaitTime;
        }
        FishingManager mcmmoFishingManager = mcMMOPlayer.getFishingManager();
        int masterAnglerRank = RankUtils.getRank(mcMMOPlayer, SubSkillType.FISHING_MASTER_ANGLER);
        int minWaitTimeReduction = mcmmoFishingManager.getMasterAnglerTickMinWaitReduction(masterAnglerRank, false);
        return mcmmoFishingManager.getReducedTicks(minWaitTime, minWaitTimeReduction, mcmmoFishingManager.getMasterAnglerMinWaitLowerBound());
    }

    public static int getMasterAnglerMaxWaitTime(Player player, int maxWaitTime, int lureLevel) {
        McMMOPlayer mcMMOPlayer = UserManager.getPlayer(player);
        if (mcMMOPlayer == null) {
            return maxWaitTime;
        }
        FishingManager mcmmoFishingManager = mcMMOPlayer.getFishingManager();
        int masterAnglerRank = RankUtils.getRank(mcMMOPlayer, SubSkillType.FISHING_MASTER_ANGLER);
        int maxWaitTimeReduction = mcmmoFishingManager.getMasterAnglerTickMaxWaitReduction(masterAnglerRank, false, lureLevel * 100);
        return mcmmoFishingManager.getReducedTicks(maxWaitTime, maxWaitTimeReduction, mcmmoFishingManager.getMasterAnglerMaxWaitLowerBound());
    }

    public static int getVanillaXpBoost(Player player, int baseXp) {
        McMMOPlayer mcMMOPlayer = UserManager.getPlayer(player);
        if (mcMMOPlayer == null) {
            return baseXp;
        }
        return mcMMOPlayer.getFishingManager().handleVanillaXpBoost(baseXp);
    }

}
