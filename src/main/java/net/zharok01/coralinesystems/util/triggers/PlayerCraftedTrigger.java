package net.zharok01.coralinesystems.util.triggers;

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

public class PlayerCraftedTrigger extends SimpleCriterionTrigger<PlayerCraftedTrigger.Instance> {

    @SuppressWarnings("removal")
    public static final ResourceLocation ID =
            new ResourceLocation(CoralineSystems.MOD_ID, "player_crafted");

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
        ItemPredicate itemPredicate = ItemPredicate.fromJson(json.get("item"));
        return new Instance(predicate, itemPredicate);
    }

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