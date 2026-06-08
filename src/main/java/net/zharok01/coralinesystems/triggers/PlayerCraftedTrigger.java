package net.zharok01.coralinesystems.triggers;

import com.google.gson.JsonObject;
import net.minecraft.advancements.critereon.AbstractCriterionTriggerInstance;
import net.minecraft.advancements.critereon.ContextAwarePredicate;
import net.minecraft.advancements.critereon.DeserializationContext;
import net.minecraft.advancements.critereon.ItemPredicate;
import net.minecraft.advancements.critereon.SerializationContext;
import net.minecraft.advancements.critereon.SimpleCriterionTrigger;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.zharok01.coralinesystems.CoralineSystems;
import org.jetbrains.annotations.NotNull;

/**
 * Fires when a player actually crafts an item — i.e. physically takes the
 * result out of a crafting output slot (covers both click and shift-click).
 *
 * This is guaranteed by the removeCount > 0 guard inside
 * ResultSlot.checkTakeAchievements, which is where Forge fires
 * PlayerEvent.ItemCraftedEvent. The event cannot trigger from
 * simply having an item in your inventory.
 *
 * Optional JSON field "item" — a standard ItemPredicate block.
 * When omitted, the trigger fires for any crafted item.
 *
 * Example — any crafted item:
 * {
 *   "trigger": "coraline_systems:player_crafted"
 * }
 *
 * Example — specific item:
 * {
 *   "trigger": "coraline_systems:player_crafted",
 *   "conditions": {
 *     "item": {
 *       "item": "minecraft:stick"
 *     }
 *   }
 * }
 *
 * Example — item tag:
 * {
 *   "trigger": "coraline_systems:player_crafted",
 *   "conditions": {
 *     "item": {
 *       "tag": "forge:ingots/iron"
 *     }
 *   }
 * }
 */
public class PlayerCraftedTrigger extends SimpleCriterionTrigger<PlayerCraftedTrigger.Instance> {

    public static final ResourceLocation ID =
            new ResourceLocation(CoralineSystems.MOD_ID, "player_crafted");

    @Override
    public @NotNull ResourceLocation getId() {
        return ID;
    }

    /**
     * Called by the advancement engine when deserializing a criterion that
     * references this trigger. Reads the optional "item" key from the
     * conditions object. If absent, json.get("item") is null and
     * ItemPredicate.fromJson(null) returns ItemPredicate.ANY, which
     * matches every ItemStack.
     */
    @Override
    protected @NotNull Instance createInstance(
            @NotNull JsonObject json,
            @NotNull ContextAwarePredicate predicate,
            @NotNull DeserializationContext context
    ) {
        ItemPredicate itemPredicate = ItemPredicate.fromJson(json.get("item"));
        return new Instance(predicate, itemPredicate);
    }

    /**
     * Called from your ItemCraftedEvent handler.
     * Passes the crafted stack so the instance can evaluate the predicate.
     */
    public void trigger(ServerPlayer player, ItemStack craftedStack) {
        this.trigger(player, instance -> instance.matches(craftedStack));
    }

    public static class Instance extends AbstractCriterionTriggerInstance {

        private final ItemPredicate itemPredicate;

        public Instance(ContextAwarePredicate playerPredicate, ItemPredicate itemPredicate) {
            super(PlayerCraftedTrigger.ID, playerPredicate);
            this.itemPredicate = itemPredicate;
        }

        /**
         * Returns true when:
         * - itemPredicate is ItemPredicate.ANY (no "item" field in JSON), or
         * - the crafted stack satisfies the predicate defined in the JSON.
         */
        public boolean matches(ItemStack craftedStack) {
            return this.itemPredicate.matches(craftedStack);
        }

        /**
         * Serializes back to JSON for round-tripping (advancement commands,
         * datagen). Only writes the "item" key when a real predicate was
         * provided — ANY serializes to an empty object which we skip.
         */
        @Override
        public @NotNull JsonObject serializeToJson(@NotNull SerializationContext context) {
            JsonObject json = super.serializeToJson(context);
            JsonObject serializedItem = this.itemPredicate.serializeToJson().getAsJsonObject();
            if (serializedItem.size() > 0) {
                json.add("item", serializedItem);
            }
            return json;
        }
    }
}