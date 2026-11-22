package net.zharok01.coralinesystems.client.entity.variant;

import net.minecraft.resources.ResourceLocation;

public record CoralineMobVariant(ResourceLocation id, int weight, String nametagOverride) {

    public CoralineMobVariant(ResourceLocation id, int weight) {
        this(id, weight, null);
    }

    public boolean isDefault() {
        return id.equals(CoralineVariants.DEFAULT_ID);
    }

    public boolean hasNametagOverride() {
        return nametagOverride != null && !nametagOverride.isEmpty();
    }
}
