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
 * Fires once per smelt batch when the player physically takes the smelted
 * result out of the furnace output slot (covers both click and shift-click).
 *
 * Internally this hooks ForgeEventFactory.firePlayerSmeltedEvent, which is
 * called from FurnaceResultSlot.checkTakeAchievements — guaranteed to have
 * a real player reference, so no ServerPlayer cast risk.
 */
public class PlayerSmeltedTrigger extends SimpleCriterionTrigger<PlayerSmeltedTrigger.Instance> {

    public static final ResourceLocation ID =
            new ResourceLocation(CoralineSystems.MOD_ID, "player_smelted");

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

    /** Call this from your ItemSmeltedEvent handler. */
    public void trigger(ServerPlayer player) {
        this.trigger(player, instance -> true);
    }

    public static class Instance extends AbstractCriterionTriggerInstance {

        public Instance(ContextAwarePredicate predicate) {
            super(PlayerSmeltedTrigger.ID, predicate);
        }
    }
}