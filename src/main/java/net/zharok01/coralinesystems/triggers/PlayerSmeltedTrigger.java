// net/zharok01/coralinesystems/triggers/PlayerSmeltedTrigger.java

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
 * Fires once per smelt batch when the player physically takes the smelted
 * result out of the furnace output slot (covers both click and shift-click).
 *
 * Optional JSON field "item" — a standard ItemPredicate block, written directly
 * inside "conditions". When omitted, the trigger fires for any smelted item.
 *
 * Example — any smelted item:
 * { "trigger": "coraline_systems:player_smelted" }
 *
 * Example — specific item:
 * {
 *   "trigger": "coraline_systems:player_smelted",
 *   "conditions": {
 *     "item": { "item": "minecraft:iron_ingot" }
 *   }
 * }
 */
public class PlayerSmeltedTrigger extends SimpleCriterionTrigger<PlayerSmeltedTrigger.Instance> {

    public static final ResourceLocation ID =
            new ResourceLocation(CoralineSystems.MOD_ID, "player_smelted");

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
        // TEMP DEBUG - remove after diagnosis
        org.apache.logging.log4j.LogManager.getLogger("CoralineSystems")
                .info("PlayerSmeltedTrigger createInstance JSON: {}", json);

        ItemPredicate itemPredicate = ItemPredicate.fromJson(json.get("item"));
        return new Instance(predicate, itemPredicate);
    }

    /** Call this from your ItemSmeltedEvent handler, passing the smelted stack. */
    public void trigger(ServerPlayer player, ItemStack smeltedStack) {
        this.trigger(player, instance -> instance.matches(smeltedStack));
    }

    public static class Instance extends AbstractCriterionTriggerInstance {

        private final ItemPredicate itemPredicate;

        public Instance(ContextAwarePredicate playerPredicate, ItemPredicate itemPredicate) {
            super(PlayerSmeltedTrigger.ID, playerPredicate);
            this.itemPredicate = itemPredicate;
        }

        public boolean matches(ItemStack smeltedStack) {
            return this.itemPredicate.matches(smeltedStack);
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