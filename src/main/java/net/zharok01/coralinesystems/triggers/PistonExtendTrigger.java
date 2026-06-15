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
 * Fires when a Piston extends within proximity of a ServerPlayer.
 * No conditions — the advancement JSON just references the trigger ID.
 * Detection is proximity-based (see PistonExtendEvent).
 */
public class PistonExtendTrigger extends SimpleCriterionTrigger<PistonExtendTrigger.Instance> {

    public static final ResourceLocation ID =
            CoralineSystems.of("piston_extend");

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
            super(PistonExtendTrigger.ID, predicate);
        }
    }
}