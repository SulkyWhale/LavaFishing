package io.github.sulkywhale.lavafishing.listeners;

import com.gmail.nossr50.datatypes.skills.PrimarySkillType;
import com.gmail.nossr50.events.experience.McMMOPlayerLevelUpEvent;
import com.gmail.nossr50.util.sounds.SoundManager;
import com.gmail.nossr50.util.sounds.SoundType;
import io.github.sulkywhale.lavafishing.LavaFishing;
import io.github.sulkywhale.lavafishing.hooks.McMMOHook;
import org.bukkit.Bukkit;
import org.bukkit.SoundCategory;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;

public class McMMOListener implements Listener {

    @EventHandler
    public void onMcMMOPlayerLevelUp(McMMOPlayerLevelUpEvent event) {
        if (event.getSkill() != PrimarySkillType.FISHING || event.getSkillLevel() != McMMOHook.getUnlockLevel()) {
            return;
        }
        Player player = event.getPlayer();
        player.sendRichMessage("<gold>[ mcMMO <yellow>@</yellow><dark_aqua>Lava Fishing</dark_aqua> Rank <dark_aqua>1</dark_aqua> Unlocked! ]</gold>");
        SoundManager.sendCategorizedSound(player,
                player.getLocation(),
                SoundType.SKILL_UNLOCKED, SoundCategory.MASTER);
    }

    @EventHandler
    public void onPlayerCommandPreprocess(PlayerCommandPreprocessEvent event) {
        Player player = event.getPlayer();
        if (event.getMessage().equalsIgnoreCase("/fishing") && McMMOHook.hasUnlockedLavaFishing(player)) {
            Bukkit.getServer().getScheduler().runTaskLater(
                    LavaFishing.getPlugin(),
                    () -> player.sendRichMessage("<dark_aqua>You have access to <yellow>@</yellow><gold>Lava Fishing</gold></dark_aqua>"),
                    1L
            );
        }
    }

}
