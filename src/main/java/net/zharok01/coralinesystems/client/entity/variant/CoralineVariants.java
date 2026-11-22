package net.zharok01.coralinesystems.client.entity.variant;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EntityType;
import net.zharok01.coralinesystems.CoralineSystems;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class CoralineVariants {

    public static final String DEFAULT_NAME = "default";
    public static final ResourceLocation DEFAULT_ID = CoralineSystems.of(DEFAULT_NAME);

    private static final Map<EntityType<?>, List<CoralineMobVariant>> VARIANTS = buildDefaults();

    private CoralineVariants() {
    }

    public static List<CoralineMobVariant> getVariants(EntityType<?> mob) {
        List<CoralineMobVariant> list = VARIANTS.get(mob);
        return list != null ? list : List.of(new CoralineMobVariant(DEFAULT_ID, 1));
    }

    public static CoralineMobVariant getDefaultVariant(EntityType<?> mob) {
        for (CoralineMobVariant variant : getVariants(mob)) {
            if (variant.isDefault()) {
                return variant;
            }
        }
        return new CoralineMobVariant(DEFAULT_ID, 1);
    }

    public static CoralineMobVariant getRandomVariant(EntityType<?> mob, RandomSource random) {
        List<CoralineMobVariant> variants = getVariants(mob);
        if (variants.isEmpty()) {
            return new CoralineMobVariant(DEFAULT_ID, 1);
        }

        int totalWeight = 0;
        for (CoralineMobVariant variant : variants) {
            totalWeight += Math.max(variant.weight(), 0);
        }
        if (totalWeight <= 0) {
            return getDefaultVariant(mob);
        }

        int roll = random.nextInt(totalWeight);
        int cumulative = 0;
        for (CoralineMobVariant variant : variants) {
            cumulative += Math.max(variant.weight(), 0);
            if (roll < cumulative) {
                return variant;
            }
        }
        return getDefaultVariant(mob);
    }

    public static CoralineMobVariant getVariant(EntityType<?> mob, ResourceLocation id) {
        for (CoralineMobVariant variant : getVariants(mob)) {
            if (variant.id().equals(id)) {
                return variant;
            }
        }
        return getDefaultVariant(mob);
    }

    public static CoralineMobVariant getVariantFromNametag(EntityType<?> mob, String nametag) {
        if (nametag == null || nametag.isEmpty()) {
            return null;
        }
        for (CoralineMobVariant variant : getVariants(mob)) {
            if (variant.hasNametagOverride() && variant.nametagOverride().equalsIgnoreCase(nametag)) {
                return variant;
            }
        }
        return null;
    }


    private static Map<EntityType<?>, List<CoralineMobVariant>> buildDefaults() {
        Map<EntityType<?>, List<CoralineMobVariant>> map = new HashMap<>();

        map.put(EntityType.COW, unmod(
                variant("umbra", 1),
                variant("ashen", 2),
                variant("cookie", 2),
                variant("dairy", 2),
                variant("pinto", 2),
                variant("sunset", 2),
                variant("wooly", 2),
                variant("albino", 1),
                variant("cream", 1),
                defaultVariant(3)
        ));

        map.put(EntityType.PIG, unmod(
                variant("mottled", 1),
                variant("piebald", 1),
                variant("pale", 1),
                variant("pink_footed", 1),
                variant("sooty", 1),
                variant("spotted", 1),
                defaultVariant(2)
        ));

        map.put(EntityType.CHICKEN, unmod(
                variant("midnight", 1),
                variant("amber", 2),
                variant("gold_crested", 2),
                variant("bronzed", 2),
                variant("skewbald", 2),
                variant("stormy", 2),
                variant("duck", 1),
                defaultVariant(3)
        ));

        map.put(EntityType.ZOMBIE, unmod(
                variant("green", 2),
                variant("suit", 2),
                defaultVariant(3),
                new CoralineMobVariant(CoralineSystems.of("spiderman"), 0, "spiderman")
        ));

        map.put(EntityType.SPIDER, unmod(
                variant("bone", 1),
                variant("brown", 3),
                variant("tarantula", 2),
                variant("black_widow", 1),
                defaultVariant(5)
        ));

        map.put(EntityType.WOLF, unmod(
                variant("woods", 1),
                variant("ashen", 1),
                variant("black", 1),
                variant("chestnut", 1),
                variant("rusty", 1),
                variant("spotted", 1),
                variant("striped", 1),
                variant("snowy", 1),
                variant("lab", 1),
                defaultVariant(1)
        ));

        return Collections.unmodifiableMap(map);
    }

    private static CoralineMobVariant variant(String name, int weight) {
        return new CoralineMobVariant(CoralineSystems.of(name), weight);
    }

    private static CoralineMobVariant defaultVariant(int weight) {
        return new CoralineMobVariant(DEFAULT_ID, weight);
    }

    private static List<CoralineMobVariant> unmod(CoralineMobVariant... variants) {
        List<CoralineMobVariant> list = new ArrayList<>(variants.length);
        Collections.addAll(list, variants);
        return Collections.unmodifiableList(list);
    }
}
