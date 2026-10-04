package io.github.sulkywhale.lavafishing.objects;

import io.github.sulkywhale.lavafishing.utilities.RandomUtil;

import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public final class LootTable {

    private final List<LootEntry> lootTable;
    private final double totalWeight;

    public LootTable(List<LootEntry> lootEntries) {
        this.lootTable = lootEntries.stream().sorted(Comparator.comparingDouble(LootEntry::weight)).toList();
        this.totalWeight = RandomUtil.getTotalWeight(lootTable, LootEntry::weight);
    }

    public LootEntry pickLoot(int luckLevel, ThreadLocalRandom random) {
        return RandomUtil.getRandomItem(random, lootTable, LootEntry::weight, 2 * luckLevel, totalWeight);
    }

}
