package net.zharok01.coralinesystems.util.triggers;

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

public class FullSacksTrigger extends SimpleCriterionTrigger<FullSacksTrigger.Instance> {

    @SuppressWarnings("removal")
    public static final ResourceLocation ID = new ResourceLocation(CoralineSystems.MOD_ID, "full_sacks");

    @SuppressWarnings("removal")
    private static final TagKey<Item> SACKS_TAG = TagKey.create(
            Registries.ITEM, new ResourceLocation("supplementaries", "sacks")
    );

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
        this.trigger(player, instance -> instance.matches(player.getInventory()));
    }

    public static class Instance extends AbstractCriterionTriggerInstance {

        public Instance(ContextAwarePredicate predicate) {
            super(FullSacksTrigger.ID, predicate);
        }

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