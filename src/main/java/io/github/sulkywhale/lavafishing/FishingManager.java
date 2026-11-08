package io.github.sulkywhale.lavafishing;

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
import io.github.sulkywhale.lavafishing.objects.FishingTask;
import io.github.sulkywhale.lavafishing.objects.LootEntry;
import io.github.sulkywhale.lavafishing.objects.LootTable;
import io.github.sulkywhale.lavafishing.objects.PotionList;
import io.papermc.paper.datacomponent.DataComponentTypes;
import io.papermc.paper.datacomponent.item.ItemEnchantments;
import io.papermc.paper.threadedregions.scheduler.ScheduledTask;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.attribute.Attribute;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.ExperienceOrb;
import org.bukkit.entity.FishHook;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ItemType;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.PotionType;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

public class FishingManager {

    private static final Map<Player, FishingSession> fishingSessions = new ConcurrentHashMap<>();
    private static final LootTable lootTable = new LootTable(Config.getLootTable());
    private static final PotionList potionList = new PotionList(Config.getPotions());
    private static final EnchantmentList enchantmentList = new EnchantmentList(Config.getEnchantments());
    private static final double enchantChance = Config.getEnchantChance();
    private static final int minLootPeriod = Config.getMinLootPeriod();
    private static final int maxLootPeriod = Config.getMaxLootPeriod();
    private static final int minWaitTime = Config.getMinWaitTime();
    private static final int maxWaitTime = Config.getMaxWaitTime();

    public static void addFishingPlayer(Player player, FishHook fishHook) {
        boolean isNether = player.getWorld().getEnvironment() == World.Environment.NETHER;
        ThreadLocalRandom random = ThreadLocalRandom.current();
        FishingSession fishingSession = new FishingSession(generateWaitTime(player, player.getInventory().getItemInMainHand(), random), generateLureTime(random), generateLootPeriodLength(random), fishHook, isNether);
        ScheduledTask scheduledTask = player.getScheduler().runAtFixedRate(LavaFishing.getPlugin(), new FishingTask(player, fishingSession), null, 1L, 1L);
        if (scheduledTask == null) {
            LavaFishing.getPlugin().getLogger().warning("Failed to schedule task.");
            return;
        }
        fishingSessions.put(player, fishingSession);
    }

    public static int generateWaitTime(Player player, ItemStack fishingRod, ThreadLocalRandom random) {
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

    private static int generateLureTime(ThreadLocalRandom random) {
        return random.nextInt(20, 81);
    }

    private static int generateLootPeriodLength(ThreadLocalRandom random) {
        return random.nextInt(minLootPeriod, maxLootPeriod + 1);
    }

    public static void removeFishingPlayer(Player player) {
        fishingSessions.remove(player);
    }

    public static void fish(Player player, boolean giveLoot, FishHook hook) {
        FishingSession session = fishingSessions.get(player);
        if (session == null) {
            return;
        }
        session.setInvalid();
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
        if (session.isLootPeriod(Bukkit.getCurrentTick()) && hook.getHookedEntity() == null) {
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
                    ItemEnchantments.itemEnchantments().addAll(enchantmentEntries.stream().collect(Collectors.toMap(EnchantmentEntry::type, EnchantmentEntry::level, Math::max)))
            );
        }
        if (itemStack.getItemMeta() instanceof Damageable damageableMeta && material.getMaxDurability() > 0) {
            damageableMeta.setDamage(material.getMaxDurability() * random.nextInt(91) / 100);
            itemStack.setItemMeta(damageableMeta);
        }
        if (material.getDefaultDataTypes().contains(DataComponentTypes.ENCHANTABLE) && random.nextDouble() < enchantChance) {
            List<EnchantmentEntry> enchantmentEntries = enchantmentList.getRandomEnchantments(itemStack, random);
            if (!enchantmentEntries.isEmpty()) {
                itemStack.setData(
                        DataComponentTypes.ENCHANTMENTS,
                        ItemEnchantments.itemEnchantments().addAll(enchantmentEntries.stream().collect(Collectors.toMap(EnchantmentEntry::type, EnchantmentEntry::level, Math::max)))
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

}
