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
 * Fires when a player right-clicks a Repeater and cycles its delay to 4 (maximum).
 * No conditions — the advancement JSON just references the trigger ID.
 * Detection is handled in RepeaterMaxDelayEvent.
 */
public class RepeaterMaxDelayTrigger extends SimpleCriterionTrigger<RepeaterMaxDelayTrigger.Instance> {

    public static final ResourceLocation ID =
            CoralineSystems.of("repeater_max_delay");

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
            super(RepeaterMaxDelayTrigger.ID, predicate);
        }
    }
}