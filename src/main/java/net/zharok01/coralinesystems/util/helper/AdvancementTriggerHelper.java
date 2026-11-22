package net.zharok01.coralinesystems.util.helper;

import com.google.gson.JsonObject;
import net.minecraft.advancements.critereon.AbstractCriterionTriggerInstance;
import net.minecraft.advancements.critereon.ContextAwarePredicate;
import net.minecraft.advancements.critereon.DeserializationContext;
import net.minecraft.advancements.critereon.SimpleCriterionTrigger;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.NotNull;

public class AdvancementTriggerHelper extends SimpleCriterionTrigger<AdvancementTriggerHelper.Instance> {

    private final ResourceLocation id;

    public AdvancementTriggerHelper(ResourceLocation id) {
        this.id = id;
    }

    @Override
    public @NotNull ResourceLocation getId() {
        return this.id;
    }

    @Override
    protected @NotNull Instance createInstance(
            @NotNull JsonObject json,
            @NotNull ContextAwarePredicate predicate,
            @NotNull DeserializationContext context
    ) {
        return new Instance(this.id, predicate);
    }

    public void trigger(ServerPlayer player) {
        this.trigger(player, instance -> true);
    }

    public static class Instance extends AbstractCriterionTriggerInstance {

        public Instance(ResourceLocation id, ContextAwarePredicate predicate) {
            super(id, predicate);
        }

        @Override
        public @NotNull JsonObject serializeToJson(@NotNull net.minecraft.advancements.critereon.SerializationContext context) {
            return super.serializeToJson(context);
        }
    }
}