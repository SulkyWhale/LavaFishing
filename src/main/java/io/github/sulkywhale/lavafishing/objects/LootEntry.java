package io.github.sulkywhale.lavafishing.objects;

import org.bukkit.Material;

public record LootEntry(Material material, int amount, double weight, int mcMMO_Xp) {
}
