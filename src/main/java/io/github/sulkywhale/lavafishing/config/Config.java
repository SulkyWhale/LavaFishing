package io.github.sulkywhale.lavafishing.config;

import io.github.sulkywhale.lavafishing.objects.EnchantmentEntry;
import io.github.sulkywhale.lavafishing.objects.LootEntry;
import io.github.sulkywhale.lavafishing.objects.PotionEntry;
import io.papermc.paper.registry.RegistryAccess;
import io.papermc.paper.registry.RegistryKey;
import net.kyori.adventure.key.Key;
import org.bukkit.Material;
import org.bukkit.Registry;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.potion.PotionType;

import java.util.List;

public class Config {

    private static FileConfiguration config;

    public static void init(FileConfiguration config) {
        Config.config = config;
    }

    public static int getMinWaitTime() {
        return config.getInt("lavafishing.min_wait_time");
    }

    public static int getMaxWaitTime() {
        return config.getInt("lavafishing.max_wait_time");
    }

    public static int getMinLootPeriod() {
        return config.getInt("lavafishing.min_looting_period");
    }

    public static int getMaxLootPeriod() {
        return config.getInt("lavafishing.max_looting_period");
    }

    public static boolean usingNotNetherPenalty() {
        return config.getBoolean("lavafishing.not_nether_penalty.enabled");
    }

    public static double notNetherPenaltyChance() {
        return config.getDouble("lavafishing.not_nether_penalty.chance");
    }

    public static List<LootEntry> getLootTable() {
        return config.getMapList("lavafishing.loot_table").stream().map(entry -> {
            Material material = Material.matchMaterial((String) entry.get("material"));
            int amount = (int) entry.get("amount");
            double weight = (double) entry.get("weight");
            Object mcMMO_Xp = entry.get("mcMMO_xp");
            if (mcMMO_Xp == null) {
                mcMMO_Xp = 0;
            }
            return new LootEntry(material, amount, weight, (int) mcMMO_Xp);
        }).toList();
    }

    public static List<PotionEntry> getPotions() {
        final Registry<PotionType> registry = RegistryAccess.registryAccess().getRegistry(RegistryKey.POTION);
        return config.getMapList("lavafishing.potions").stream().map(entry -> {
            PotionType type = registry.getOrThrow(RegistryKey.POTION.typedKey(Key.key((String) entry.get("type"))));
            double weight = (double) entry.get("weight");
            return new PotionEntry(type, weight);
        }).toList();
    }

    public static List<EnchantmentEntry> getEnchantments() {
        final Registry<Enchantment> registry = RegistryAccess.registryAccess().getRegistry(RegistryKey.ENCHANTMENT);
        return config.getMapList("lavafishing.enchantments").stream().map(entry -> {
            Enchantment type = registry.getOrThrow(RegistryKey.ENCHANTMENT.typedKey(Key.key((String) entry.get("type"))));
            int level = (int) entry.get("level");
            double weight = (double) entry.get("weight");
            return new EnchantmentEntry(type, level, weight);
        }).toList();
    }

    public static int getMcMMOUnlockLevel() {
        return config.getInt("lavafishing.mcMMO.unlock_level");
    }

    public static boolean allowConflictingEnchants() {
        return config.getBoolean("lavafishing.allow_conflicting_enchants");
    }

    public static double getEnchantChance() {
        return config.getDouble("lavafishing.enchant_chance");
    }

}
