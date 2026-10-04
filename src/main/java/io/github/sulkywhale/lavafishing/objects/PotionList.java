package io.github.sulkywhale.lavafishing.objects;

import io.github.sulkywhale.lavafishing.utilities.RandomUtil;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public final class PotionList {

    private List<PotionEntry> potions;
    private final double totalWeight;

    public PotionList(List<PotionEntry> potions) {
        this.potions = potions;
        this.totalWeight = RandomUtil.getTotalWeight(potions, PotionEntry::weight);
    }

    public PotionEntry getRandomPotion(ThreadLocalRandom random) {
        return RandomUtil.getRandomItem(random, potions, PotionEntry::weight, totalWeight);
    }

}
