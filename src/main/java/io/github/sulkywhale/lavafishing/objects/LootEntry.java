package io.github.sulkywhale.lavafishing.objects;

import org.bukkit.Material;
import org.jetbrains.annotations.NotNull;

public record LootEntry(@NotNull Material material, int amount, double weight, int mcMMO_Xp) {
}
