package io.github.sulkywhale.lavafishing.utilities;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.ToDoubleFunction;

public class RandomUtil {

    public static <T> T getRandomItem(ThreadLocalRandom random, List<T> items, ToDoubleFunction<T> weightGetter, double modifier, double totalWeight) {
        double roll = random.nextDouble(totalWeight);
        for (T item : items) {
            roll -= weightGetter.applyAsDouble(item);
            if (roll < 0) {
                return item;
            }
        }
        return items.isEmpty() ? null : items.getLast();
    }

    public static <T> T getRandomItem(ThreadLocalRandom random, List<T> items, ToDoubleFunction<T> weightGetter, double totalWeight) {
        return getRandomItem(random, items, weightGetter, 0, totalWeight);
    }

    public static <T> T getRandomItem(ThreadLocalRandom random, List<T> items, ToDoubleFunction<T> weightGetter) {
        return getRandomItem(random, items, weightGetter, getTotalWeight(items, weightGetter));
    }

    public static <T> double getTotalWeight(List<T> items, ToDoubleFunction<T> weightGetter) {
        return items.stream().mapToDouble(weightGetter).sum();
    }

}
