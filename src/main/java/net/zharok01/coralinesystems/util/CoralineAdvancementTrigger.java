package net.zharok01.coralinesystems.util;

import com.google.gson.JsonObject;
import net.minecraft.advancements.critereon.AbstractCriterionTriggerInstance;
import net.minecraft.advancements.critereon.ContextAwarePredicate;
import net.minecraft.advancements.critereon.DeserializationContext;
import net.minecraft.advancements.critereon.SimpleCriterionTrigger;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.NotNull;

/**
 * A simple no-condition advancements trigger.
 * Equivalent to AMAdvancementTrigger but without the Alex's Mobs dependency.
 *
 * Usage: call trigger.trigger(serverPlayer) from your event handler
 * whenever the condition you care about is met. The advancements JSON
 * just needs to reference this trigger's ID with no extra conditions.
 */
public class CoralineAdvancementTrigger extends SimpleCriterionTrigger<CoralineAdvancementTrigger.Instance> {

    private final ResourceLocation id;

    public CoralineAdvancementTrigger(ResourceLocation id) {
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

    /**
     * Fire this trigger for the given player.
     * The lambda always returns true — conditions are checked on the caller's side
     * before ever calling this method.
     */
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