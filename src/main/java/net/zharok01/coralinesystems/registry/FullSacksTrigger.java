// net/zharok01/coralinesystems/triggers/FullSacksTrigger.java
package net.zharok01.coralinesystems.registry;

import com.google.gson.JsonObject;
import net.minecraft.advancements.critereon.AbstractCriterionTriggerInstance;
import net.minecraft.advancements.critereon.ContextAwarePredicate;
import net.minecraft.advancements.critereon.DeserializationContext;
import net.minecraft.advancements.critereon.SimpleCriterionTrigger;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.zharok01.coralinesystems.CoralineSystems;
import org.jetbrains.annotations.NotNull;

public class FullSacksTrigger extends SimpleCriterionTrigger<FullSacksTrigger.TriggerInstance> {

    public static final ResourceLocation ID = new ResourceLocation(CoralineSystems.MOD_ID, "full_sacks");
    // The item tag from Supplementaries - all sack-type items are grouped here
    private static final TagKey<Item> SACKS_TAG = TagKey.create(
            Registries.ITEM, new ResourceLocation("supplementaries", "sacks")
    );

    @Override
    public @NotNull ResourceLocation getId() {
        return ID;
    }

    @Override
    protected @NotNull TriggerInstance createInstance(
            @NotNull JsonObject json,
            @NotNull ContextAwarePredicate predicate,
            @NotNull DeserializationContext context
    ) {
        return new TriggerInstance(predicate);
    }

    /**
     * Called from the event subscriber whenever we want to attempt the check.
     * SimpleCriterionTrigger handles iterating all listening players internally.
     */
    public void trigger(ServerPlayer player) {
        this.trigger(player, instance -> instance.matches(player.getInventory()));
    }

    public static class TriggerInstance extends AbstractCriterionTriggerInstance {

        public TriggerInstance(ContextAwarePredicate predicate) {
            super(FullSacksTrigger.ID, predicate);
        }

        /**
         * Checks the 36 main hotbar + main inventory slots (0-35).
         * Slots 36-39 are armor, slot 40 is offhand — intentionally excluded.
         */
        public boolean matches(Inventory inventory) {
            for (int slot = 0; slot < 36; slot++) {
                ItemStack stack = inventory.getItem(slot);
                if (stack.isEmpty() || !stack.is(SACKS_TAG)) {
                    return false;
                }
            }
            return true;
        }
    }
}