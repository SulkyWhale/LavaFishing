package io.github.sulkywhale.lavafishing;

import com.destroystokyo.paper.ParticleBuilder;
import com.gmail.nossr50.config.experience.ExperienceConfig;
import com.gmail.nossr50.datatypes.skills.PrimarySkillType;
import com.gmail.nossr50.locale.LocaleLoader;
import com.gmail.nossr50.util.Permissions;
import io.github.sulkywhale.lavafishing.config.Config;
import io.github.sulkywhale.lavafishing.hooks.JobsHook;
import io.github.sulkywhale.lavafishing.hooks.McMMOHook;
import io.github.sulkywhale.lavafishing.objects.EnchantmentEntry;
import io.github.sulkywhale.lavafishing.objects.EnchantmentList;
import io.github.sulkywhale.lavafishing.objects.FishingSession;
import io.github.sulkywhale.lavafishing.objects.LootEntry;
import io.github.sulkywhale.lavafishing.objects.LootTable;
import io.github.sulkywhale.lavafishing.objects.PotionList;
import io.papermc.paper.datacomponent.DataComponentTypes;
import io.papermc.paper.datacomponent.item.ItemEnchantments;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.sound.Sound;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.World;
import org.bukkit.attribute.Attribute;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.ExperienceOrb;
import org.bukkit.entity.FishHook;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.PotionType;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

public class FishingManager {

    private static final Map<Player, FishingSession> fishingPlayers = new HashMap<>();
    private static final LootTable lootTable = new LootTable(Config.getLootTable());
    private static final PotionList potionList = new PotionList(Config.getPotions());
    private static final EnchantmentList enchantmentList = new EnchantmentList(Config.getEnchantments());
    private static final double enchantChance = Config.getEnchantChance();
    private static final int minLootPeriod = Config.getMinLootPeriod();
    private static final int maxLootPeriod = Config.getMaxLootPeriod();
    private static final int minWaitTime = Config.getMinWaitTime();
    private static final int maxWaitTime = Config.getMaxWaitTime();
    private static final boolean usingNotNetherPenalty = Config.usingNotNetherPenalty();
    private static final double notNetherPenaltyChance = Config.notNetherPenaltyChance();
    private static final ParticleBuilder particleBuilder = Particle.DUST.builder()
            .data(new Particle.DustOptions(Color.fromRGB(0x444444), 3))
            .offset(0, 0.3, 0);

    public static void addFishingPlayer(Player player, FishHook fishHook) {
        int startTime = Bukkit.getCurrentTick();
        boolean isNether = player.getWorld().getEnvironment() == World.Environment.NETHER;
        ThreadLocalRandom random = ThreadLocalRandom.current();
        fishingPlayers.put(player, new FishingSession(startTime, generateWaitTime(player, player.getInventory().getItemInMainHand(), random), generateLureTime(random), generateLootPeriodLength(random), fishHook, isNether));
    }

    private static int generateWaitTime(Player player, ItemStack fishingRod, ThreadLocalRandom random) {
        int lureLevel = 0;
        if (fishingRod.containsEnchantment(Enchantment.LURE)) {
            lureLevel = fishingRod.getEnchantmentLevel(Enchantment.LURE);
        }
        // Use wait times from Master Angler if using mcMMO
        if (McMMOHook.isEnabled()) {
            int reducedMinWaitTime = McMMOHook.getMasterAnglerMinWaitTime(player, minWaitTime);
            int reducedMaxWaitTime = McMMOHook.getMasterAnglerMaxWaitTime(player, maxWaitTime, lureLevel);
            // Match mcMMO behavior
            if (reducedMaxWaitTime < reducedMinWaitTime) {
                reducedMaxWaitTime = reducedMinWaitTime + 100;
            }
            return random.nextInt(reducedMinWaitTime, reducedMaxWaitTime + 1);
        }
        // Each level of lure decreases min and max wait time by 5 seconds
        int reducedMinWaitTime = minWaitTime - lureLevel * 100;
        int reducedMaxWaitTime = maxWaitTime + 1 - lureLevel * 100;
        // Replicate vanilla behavior
        if (reducedMinWaitTime >= reducedMaxWaitTime) {
            return reducedMaxWaitTime;
        }
        return random.nextInt(reducedMinWaitTime, reducedMaxWaitTime);
    }

    public static int generateLureTime(ThreadLocalRandom random) {
        return random.nextInt(20, 81);
    }

    private static int generateLootPeriodLength(ThreadLocalRandom random) {
        return random.nextInt(minLootPeriod, maxLootPeriod + 1);
    }

    public static void fish(Player player, boolean giveLoot, FishHook hook) {
        FishingSession session = fishingPlayers.get(player);
        if (session == null) {
            return;
        }
        fishingPlayers.remove(player);
        if (!giveLoot) {
            return;
        }
        if (McMMOHook.isEnabled()) {
            if (McMMOHook.isFishingTooOften(player)) {
                // Check sends message to player, so no need to send another
                return;
            }
            if (McMMOHook.isExploiting(player, hook.getLocation().toVector())) {
                player.sendPlainMessage(LocaleLoader.getString("Fishing.ScarcityTip", ExperienceConfig.getInstance().getFishingExploitingOptionMoveRange()));
                return;
            }
        }
        if (isLootPeriod(session.getLootPeriodLength(), session.getLootTime(), Bukkit.getCurrentTick()) && hook.getHookedEntity() == null) {
            ThreadLocalRandom random = ThreadLocalRandom.current();
            giveLoot(player, session.getFishHook(), random);
            giveExperience(player, random);
        }
    }

    private static void giveLoot(Player player, FishHook hook, ThreadLocalRandom random) {
        ItemStack fishingRod = player.getInventory().getItemInMainHand();
        int luckLevel = 0;
        if (fishingRod.getType() == Material.FISHING_ROD && fishingRod.containsEnchantment(Enchantment.LUCK_OF_THE_SEA)) {
            luckLevel += fishingRod.getEnchantmentLevel(Enchantment.LUCK_OF_THE_SEA);
        }
        luckLevel += player.getAttribute(Attribute.LUCK).getValue();
        LootEntry lootEntry = lootTable.pickLoot(luckLevel, random);
        if (lootEntry == null) {
            LavaFishing.getPlugin().getLogger().severe("There was an issue getting the loot. Please ensure you have a valid configuration");
            player.sendMessage(Component.text("An internal error occurred.", NamedTextColor.RED));
            return;
        }
        Location hookLocation = hook.getLocation();
        Material material = lootEntry.material();
        ItemStack itemStack = ItemStack.of(material, lootEntry.amount());
        if (material == Material.POTION || material == Material.SPLASH_POTION || material == Material.LINGERING_POTION) {
            PotionMeta meta = (PotionMeta) itemStack.getItemMeta();
            PotionType potion = potionList.getRandomPotion(random).potion();
            meta.setBasePotionType(potion);
            itemStack.setItemMeta(meta);
        }
        if (material == Material.ENCHANTED_BOOK) {
            List<EnchantmentEntry> enchantmentEntries = enchantmentList.getRandomEnchantments(itemStack, random);
            itemStack.setData(
                    DataComponentTypes.STORED_ENCHANTMENTS,
                    ItemEnchantments.itemEnchantments().addAll(enchantmentEntries.stream().collect(Collectors.toMap(EnchantmentEntry::type, EnchantmentEntry::level)))
            );
        }
        if (material.asItemType().getDefaultDataTypes().contains(DataComponentTypes.ENCHANTABLE) && random.nextDouble() < enchantChance) {
            List<EnchantmentEntry> enchantmentEntries = enchantmentList.getRandomEnchantments(itemStack, random);
            if (!enchantmentEntries.isEmpty()) {
                itemStack.setData(
                        DataComponentTypes.ENCHANTMENTS,
                        ItemEnchantments.itemEnchantments().addAll(enchantmentEntries.stream().collect(Collectors.toMap(EnchantmentEntry::type, EnchantmentEntry::level)))
                );
                itemStack.setData(DataComponentTypes.ENCHANTMENT_GLINT_OVERRIDE, true);
            }
        }
        Item hookedItem = hookLocation.getWorld().spawn(hookLocation, Item.class, item -> {
            item.setItemStack(itemStack);
            item.setInvulnerable(true);
        });
        hook.setHookedEntity(hookedItem);
        hook.pullHookedEntity();
        if (McMMOHook.isEnabled()) {
            McMMOHook.grantFishingXp(player, lootEntry.mcMMO_Xp());
        }
        if (JobsHook.isEnabled()) {
            JobsHook.grantJobsRewards(player, itemStack);
        }
    }

    private static void giveExperience(Player player, ThreadLocalRandom random) {
        int baseXp = random.nextInt(1, 7);
        int experience = (McMMOHook.isEnabled() && Permissions.vanillaXpBoost(player, PrimarySkillType.FISHING)) ?
                McMMOHook.getVanillaXpBoost(player, baseXp) :
                baseXp;
        player.getWorld().spawn(player.getLocation(), ExperienceOrb.class, experienceOrb -> experienceOrb.setExperience(experience));
    }

    public static void runScheduler(LavaFishing plugin) {
        Bukkit.getScheduler().runTaskTimer(plugin, _ -> {
            for (Map.Entry<Player, FishingSession> fishingPlayer : fishingPlayers.entrySet()) {
                FishingSession session = fishingPlayer.getValue();
                FishHook fishHook = session.getFishHook();
                if (!fishHook.isInLava()) {
                    continue;
                }
                Player player = fishingPlayer.getKey();
                ThreadLocalRandom random = ThreadLocalRandom.current();
                if (session.getWaitTime() < 0) {
                    // If lure made wait time less than 0, generate new wait time
                    session.setWaitTime(generateWaitTime(player, player.getInventory().getItemInMainHand(), random));
                    keepBobberUp(session.getFishHook());
                    continue;
                }
                int lootTime = session.getLootTime();
                int currentTick = Bukkit.getCurrentTick();
                if (isPastLootPeriod(session.getLootPeriodLength(), lootTime, currentTick)) {
                    fishingPlayers.remove(player);
                    addFishingPlayer(player, fishHook);
                    continue;
                }
                if (currentTick >= lootTime && !session.hasFinishedWaitTime()) {
                    if (fishHook.getHookedEntity() != null) {
                        fishingPlayers.remove(player);
                        continue;
                    }
                    float pitch = random.nextFloat(0.6f, 1.4f);
                    player.playSound(Sound.sound(Key.key("block.pointed_dripstone.drip_lava"), Sound.Source.PLAYER, 1.0f, pitch), fishHook.getX(), fishHook.getY(), fishHook.getZ());
                    spawnParticle(fishHook);
                    session.setFinishedWaitTime(true);
                    continue;
                }
                if (usingNotNetherPenalty && !session.isInNether() && random.nextDouble() < notNetherPenaltyChance) {
                    // Add tick to emulate not decreasing time (not a perfect solution because some ticks are skipped)
                    session.setWaitTime(session.getWaitTime() + 1);
                }
                if (isLootPeriod(session.getLootPeriodLength(), lootTime, currentTick)) {
                    // Allow bobber to sink
                    continue;
                }
                keepBobberUp(fishHook);
            }
        }, 0L, 1L);
    }

    private static void spawnParticle(FishHook hook) {
        Location hookLocation = hook.getLocation();
        float height = hookLocation.getWorld().getFluidData(hookLocation).computeHeight(hookLocation);
        particleBuilder.location(hookLocation.add(0, height, 0)).receivers(24).spawn();
    }

    private static boolean isLootPeriod(int lootPeriodLength, int lootTime, int currentTick) {
        return currentTick >= lootTime && currentTick < lootTime + lootPeriodLength;
    }

    private static boolean isPastLootPeriod(int lootPeriodLength, int lootTime, int currentTick) {
        return currentTick > lootTime + lootPeriodLength;
    }

    private static void keepBobberUp(FishHook hook) {
        Location hookLocation = hook.getLocation();
        float height = hookLocation.getWorld().getFluidData(hookLocation).computeHeight(hookLocation);
        hook.teleport(hookLocation.add(0, height, 0));
    }

}
