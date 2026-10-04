package io.github.sulkywhale.lavafishing.objects;

import io.github.sulkywhale.lavafishing.config.Config;
import io.github.sulkywhale.lavafishing.utilities.RandomUtil;
import io.papermc.paper.datacomponent.DataComponentTypes;
import io.papermc.paper.datacomponent.item.Enchantable;
import io.papermc.paper.registry.RegistryKey;
import io.papermc.paper.registry.TypedKey;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ItemType;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public final class EnchantmentList {

    private final List<EnchantmentEntry> enchantments;
    private final boolean allowConflictingEnchants = Config.allowConflictingEnchants();

    public EnchantmentList(List<EnchantmentEntry> lootEntries) {
        this.enchantments = lootEntries.stream().sorted(Comparator.comparingDouble(EnchantmentEntry::weight)).toList();
    }

    public List<EnchantmentEntry> getRandomEnchantments(ItemStack itemStack, ThreadLocalRandom random) {
        Enchantable enchantable = itemStack.getData(DataComponentTypes.ENCHANTABLE);
        int enchantability = enchantable == null ? 1 : enchantable.value();
        int enchantmentCost = 1 + random.nextInt(enchantability / 4 + 1) + random.nextInt(enchantability / 4 + 1);
        float randomSpan = (random.nextFloat() + random.nextFloat() - 1.0f) * 0.15F;
        enchantmentCost = Math.clamp(Math.round(enchantmentCost + enchantmentCost * randomSpan), 1, Integer.MAX_VALUE);
        List<EnchantmentEntry> enchantmentEntries = getAvailableEnchantments(itemStack);
        List<EnchantmentEntry> results = new ArrayList<>();
        if (!enchantmentEntries.isEmpty()) {
            results.add(RandomUtil.getRandomItem(random, enchantmentEntries, EnchantmentEntry::weight));
            while (random.nextInt(50) <= enchantmentCost) {
                if (!allowConflictingEnchants && !enchantmentEntries.isEmpty()) {
                    filterCompatibleEnchantments(enchantmentEntries);
                }
                if (enchantmentEntries.isEmpty()) {
                    break;
                }
                results.add(RandomUtil.getRandomItem(random, enchantmentEntries, EnchantmentEntry::weight));
                enchantmentCost /= 2;
            }
        }
        return results;
    }

    private List<EnchantmentEntry> getAvailableEnchantments(ItemStack itemStack) {
        List<EnchantmentEntry> results = new ArrayList<>();
        boolean isBook = itemStack.isSimilar(ItemStack.of(Material.ENCHANTED_BOOK));
        TypedKey<ItemType> itemTypeTypedKey = RegistryKey.ITEM.typedKey(itemStack.getType().key());
        enchantments.stream()
                .filter(enchantmentEntry -> isBook || enchantmentEntry.type().getSupportedItems().contains(itemTypeTypedKey))
                .forEach(results::add);
        return results;
    }

    private void filterCompatibleEnchantments(List<EnchantmentEntry> enchantmentEntries) {
        enchantmentEntries.removeIf(enchantmentEntry -> doConflict(enchantments, enchantmentEntry));
    }

    private boolean doConflict(List<EnchantmentEntry> enchantments, EnchantmentEntry enchantment) {
        for (EnchantmentEntry targetEnchantment : enchantments) {
            if (enchantment.type().conflictsWith(targetEnchantment.type())) {
                return true;
            }
        }
        return false;
    }

}
