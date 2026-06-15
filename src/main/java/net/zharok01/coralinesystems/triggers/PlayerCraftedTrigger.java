// net/zharok01/coralinesystems/triggers/PlayerCraftedTrigger.java

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
 * Optional JSON field "item" — a standard ItemPredicate block, written directly
 * inside "conditions". When omitted, the trigger fires for any crafted item.
 *
 * Example — any crafted item:
 * { "trigger": "coraline_systems:player_crafted" }
 *
 * Example — specific item:
 * {
 *   "trigger": "coraline_systems:player_crafted",
 *   "conditions": {
 *     "item": { "item": "minecraft:stick" }
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
     * By the time this method is called, the vanilla advancement engine has already
     * stripped the outer "conditions" wrapper. The JsonObject received here IS the
     * conditions content — identical to how InventoryChangeTrigger reads json.get("items")
     * directly. Reading json.get("item") here correctly corresponds to:
     *   "conditions": { "item": { ... } }
     * in the advancement JSON.
     */
    @Override
    protected @NotNull Instance createInstance(
            @NotNull JsonObject json,
            @NotNull ContextAwarePredicate predicate,
            @NotNull DeserializationContext context
    ) {
        // json.get("item") returns null when the field is absent → fromJson(null) → ANY
        ItemPredicate itemPredicate = ItemPredicate.fromJson(json.get("item"));
        return new Instance(predicate, itemPredicate);
    }

    /** Call this from your ItemCraftedEvent handler, passing the crafted stack. */
    public void trigger(ServerPlayer player, ItemStack craftedStack) {
        this.trigger(player, instance -> instance.matches(craftedStack));
    }

    public static class Instance extends AbstractCriterionTriggerInstance {

        private final ItemPredicate itemPredicate;

        public Instance(ContextAwarePredicate playerPredicate, ItemPredicate itemPredicate) {
            super(PlayerCraftedTrigger.ID, playerPredicate);
            this.itemPredicate = itemPredicate;
        }

        public boolean matches(ItemStack craftedStack) {
            return this.itemPredicate.matches(craftedStack);
        }

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