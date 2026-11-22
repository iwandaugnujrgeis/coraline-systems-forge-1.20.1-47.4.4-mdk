package net.zharok01.coralinesystems.util.triggers;

import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import net.minecraft.advancements.critereon.AbstractCriterionTriggerInstance;
import net.minecraft.advancements.critereon.ContextAwarePredicate;
import net.minecraft.advancements.critereon.DeserializationContext;
import net.minecraft.advancements.critereon.SerializationContext;
import net.minecraft.advancements.critereon.SimpleCriterionTrigger;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.stats.Stat;
import net.minecraft.stats.StatType;
import net.minecraft.util.GsonHelper;
import net.zharok01.coralinesystems.CoralineSystems;
import org.jetbrains.annotations.NotNull;

public class StatThresholdTrigger extends SimpleCriterionTrigger<StatThresholdTrigger.Instance> {

    @SuppressWarnings("removal")
    public static final ResourceLocation ID =
            new ResourceLocation(CoralineSystems.MOD_ID, "stat_threshold");

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
        ResourceLocation typeId = new ResourceLocation(GsonHelper.getAsString(json, "stat_type"));
        ResourceLocation statId = new ResourceLocation(GsonHelper.getAsString(json, "stat"));
        int min = GsonHelper.getAsInt(json, "min");

        StatType<?> type = BuiltInRegistries.STAT_TYPE.getOptional(typeId)
                .orElseThrow(() -> new JsonSyntaxException("Unknown stat type: " + typeId));
        return new Instance(predicate, typeId, statId, coraline$resolveStat(type, statId), min);
    }

    private static <T> Stat<?> coraline$resolveStat(StatType<T> type, ResourceLocation statId) {
        Registry<T> registry = type.getRegistry();
        if (!registry.containsKey(statId)) {
            throw new JsonSyntaxException("Unknown stat: " + statId);
        }
        return type.get(registry.get(statId));
    }

    public void trigger(ServerPlayer player) {
        this.trigger(player, instance -> player.getStats().getValue(instance.stat) >= instance.min);
    }

    public static class Instance extends AbstractCriterionTriggerInstance {

        private final ResourceLocation typeId;
        private final ResourceLocation statId;
        private final Stat<?> stat;
        private final int min;

        public Instance(ContextAwarePredicate predicate, ResourceLocation typeId,
                        ResourceLocation statId, Stat<?> stat, int min) {
            super(StatThresholdTrigger.ID, predicate);
            this.typeId = typeId;
            this.statId = statId;
            this.stat = stat;
            this.min = min;
        }

        @Override
        public @NotNull JsonObject serializeToJson(@NotNull SerializationContext context) {
            JsonObject json = super.serializeToJson(context);
            json.addProperty("stat_type", typeId.toString());
            json.addProperty("stat", statId.toString());
            json.addProperty("min", min);
            return json;
        }
    }
}