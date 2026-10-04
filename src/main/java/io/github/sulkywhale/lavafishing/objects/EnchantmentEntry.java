package io.github.sulkywhale.lavafishing.objects;

import org.bukkit.enchantments.Enchantment;

public record EnchantmentEntry(Enchantment type, int level, double weight) {
}
