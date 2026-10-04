package io.github.sulkywhale.lavafishing.objects;

import com.destroystokyo.paper.ParticleBuilder;
import io.github.sulkywhale.lavafishing.FishingManager;
import io.github.sulkywhale.lavafishing.config.Config;
import io.papermc.paper.threadedregions.scheduler.ScheduledTask;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.sound.Sound;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.entity.FishHook;
import org.bukkit.entity.Player;

import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Consumer;

public class FishingTask implements Consumer<ScheduledTask> {

    private final Player player;
    private final FishingSession session;
    private static final ParticleBuilder particleBuilder = Particle.DUST.builder()
            .data(new Particle.DustOptions(Color.fromRGB(0x444444), 3))
            .offset(0, 0.3, 0);

    public FishingTask(Player player, FishingSession session) {
        this.player = player;
        this.session = session;
    }

    @Override
    public void accept(ScheduledTask task) {
        FishHook fishHook = session.getFishHook();
        if (!fishHook.isInLava()) {
            return;
        }
        ThreadLocalRandom random = ThreadLocalRandom.current();
        if (session.getWaitTime() < 0) {
            // If lure made wait time less than 0, generate new wait time
            session.setWaitTime(FishingManager.generateWaitTime(player, player.getInventory().getItemInMainHand(), random));
            keepBobberUp(session.getFishHook());
            return;
        }
        int lootTime = session.getLootTime();
        int currentTick = Bukkit.getCurrentTick();
        if (session.isPastLootPeriod(currentTick)) {
            task.cancel();
            FishingManager.addFishingPlayer(player, fishHook);
            return;
        }
        if (currentTick >= lootTime && !session.hasFinishedWaitTime()) {
            if (fishHook.getHookedEntity() != null) {
                task.cancel();
                return;
            }
            float pitch = random.nextFloat(0.6f, 1.4f);
            player.playSound(Sound.sound(Key.key("block.pointed_dripstone.drip_lava"), Sound.Source.PLAYER, 1.0f, pitch), fishHook.getX(), fishHook.getY(), fishHook.getZ());
            spawnParticle(fishHook);
            session.setFinishedWaitTime(true);
            return;
        }
        if (Config.usingNotNetherPenalty() && !session.isInNether() && random.nextDouble() < Config.notNetherPenaltyChance()) {
            // Add tick to emulate not decreasing time (not a perfect solution because some ticks are skipped)
            session.setWaitTime(session.getWaitTime() + 1);
        }
        if (session.isLootPeriod(currentTick)) {
            // Allow bobber to sink
            return;
        }
        keepBobberUp(fishHook);
    }

    private static void spawnParticle(FishHook hook) {
        Location hookLocation = hook.getLocation();
        float height = hookLocation.getWorld().getFluidData(hookLocation).computeHeight(hookLocation);
        particleBuilder.location(hookLocation.add(0, height, 0)).receivers(24).spawn();
    }

    private static void keepBobberUp(FishHook hook) {
        Location hookLocation = hook.getLocation();
        float height = hookLocation.getWorld().getFluidData(hookLocation).computeHeight(hookLocation);
        hook.teleport(hookLocation.add(0, height, 0));
    }

}
