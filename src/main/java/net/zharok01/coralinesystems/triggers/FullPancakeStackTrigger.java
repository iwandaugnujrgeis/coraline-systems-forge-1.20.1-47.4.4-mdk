package net.zharok01.coralinesystems.triggers;

import com.google.gson.JsonObject;
import net.minecraft.advancements.critereon.AbstractCriterionTriggerInstance;
import net.minecraft.advancements.critereon.ContextAwarePredicate;
import net.minecraft.advancements.critereon.DeserializationContext;
import net.minecraft.advancements.critereon.SimpleCriterionTrigger;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.zharok01.coralinesystems.CoralineSystems;
import org.jetbrains.annotations.NotNull;

/**
 * Fires when a player places a pancake that completes an 8-pancake stack.
 * No conditions — the advancements JSON just references the trigger ID.
 */
public class FullPancakeStackTrigger extends SimpleCriterionTrigger<FullPancakeStackTrigger.Instance> {

    public static final ResourceLocation ID =
            new ResourceLocation(CoralineSystems.MOD_ID, "full_pancake_stack");

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
        return new Instance(predicate);
    }

    public void trigger(ServerPlayer player) {
        this.trigger(player, instance -> true);
    }

    public static class Instance extends AbstractCriterionTriggerInstance {

        public Instance(ContextAwarePredicate predicate) {
            super(FullPancakeStackTrigger.ID, predicate);
        }
    }
}