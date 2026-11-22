package net.zharok01.coralinesystems.util.triggers;

import com.google.gson.JsonObject;
import net.minecraft.advancements.critereon.AbstractCriterionTriggerInstance;
import net.minecraft.advancements.critereon.ContextAwarePredicate;
import net.minecraft.advancements.critereon.DeserializationContext;
import net.minecraft.advancements.critereon.SerializationContext;
import net.minecraft.advancements.critereon.SimpleCriterionTrigger;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.GsonHelper;
import net.zharok01.coralinesystems.CoralineSystems;
import org.jetbrains.annotations.NotNull;

public class MinecartDistanceTrigger extends SimpleCriterionTrigger<MinecartDistanceTrigger.Instance> {

    private static final int DEFAULT_REQUIRED_BLOCKS = 1000;

    @SuppressWarnings("removal")
    public static final ResourceLocation ID =
            new ResourceLocation(CoralineSystems.MOD_ID, "minecart_distance");

    @Override
    public @NotNull ResourceLocation getId() {
        return ID;
    }

    @Override
    protected @NotNull Instance createInstance(
            @NotNull JsonObject json,
            @NotNull ContextAwarePredicate predicate,
            @NotNull DeserializationContext context
    ) {
        int blocks = GsonHelper.getAsInt(json, "blocks", DEFAULT_REQUIRED_BLOCKS);
        return new Instance(predicate, blocks);
    }

    public void trigger(ServerPlayer player, int blocksTravelled) {
        this.trigger(player, instance -> blocksTravelled >= instance.requiredBlocks);
    }

    public static class Instance extends AbstractCriterionTriggerInstance {

        private final int requiredBlocks;

        public Instance(ContextAwarePredicate predicate, int requiredBlocks) {
            super(MinecartDistanceTrigger.ID, predicate);
            this.requiredBlocks = requiredBlocks;
        }

        @Override
        public @NotNull JsonObject serializeToJson(@NotNull SerializationContext context) {
            JsonObject json = super.serializeToJson(context);
            json.addProperty("blocks", requiredBlocks);
            return json;
        }
    }
}