package net.zharok01.coralinesystems.triggers;

import com.google.gson.JsonObject;
import net.mehvahdjukaar.supplementaries.common.block.ModBlockProperties.Topping;
import net.minecraft.advancements.critereon.AbstractCriterionTriggerInstance;
import net.minecraft.advancements.critereon.ContextAwarePredicate;
import net.minecraft.advancements.critereon.DeserializationContext;
import net.minecraft.advancements.critereon.SimpleCriterionTrigger;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.GsonHelper;
import net.zharok01.coralinesystems.CoralineSystems;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Fires when a player successfully applies a topping to a PancakeBlock.
 *
 * The advancements JSON can either:
 *   - Omit "topping" entirely → fires for ANY topping applied.
 *   - Include "topping": "honey" → fires only for that specific topping.
 *
 * The topping string must match the Topping enum's serialized name
 * (i.e. the lowercase name used in block states, e.g. "honey", "jam").
 */
public class PancakeToppingTrigger extends SimpleCriterionTrigger<PancakeToppingTrigger.Instance> {

    public static final ResourceLocation ID =
            new ResourceLocation(CoralineSystems.MOD_ID, "pancake_topping");

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
        // "topping" is optional. If absent, the instance matches any topping.
        Topping required = null;
        if (json.has("topping")) {
            String name = GsonHelper.getAsString(json, "topping");
            // Topping is a StringRepresentable enum — iterate values() to find
            // the one whose serialized name matches (case-insensitive).
            for (Topping t : Topping.values()) {
                if (t.getSerializedName().equalsIgnoreCase(name)) {
                    required = t;
                    break;
                }
            }
            if (required == null) {
                throw new IllegalArgumentException(
                        "[CoralineSystems] PancakeToppingTrigger: unknown topping \"" + name + "\" in advancements JSON."
                );
            }
        }
        return new Instance(predicate, required);
    }

    /**
     * Called from the Mixin when a topping is successfully applied.
     *
     * @param player  The ServerPlayer who applied the topping.
     * @param topping The topping that was applied (never NONE).
     */
    public void trigger(ServerPlayer player, Topping topping) {
        this.trigger(player, instance -> instance.matches(topping));
    }

    // -------------------------------------------------------------------------

    public static class Instance extends AbstractCriterionTriggerInstance {

        /**
         * Null means "any topping" — the advancements JSON omitted the field.
         */
        @Nullable
        private final Topping requiredTopping;

        public Instance(ContextAwarePredicate predicate, @Nullable Topping requiredTopping) {
            super(PancakeToppingTrigger.ID, predicate);
            this.requiredTopping = requiredTopping;
        }

        public boolean matches(Topping applied) {
            // If no specific topping was required, any applied topping matches.
            return requiredTopping == null || requiredTopping == applied;
        }

        @Override
        public @NotNull JsonObject serializeToJson(
                @NotNull net.minecraft.advancements.critereon.SerializationContext context
        ) {
            JsonObject json = super.serializeToJson(context);
            if (requiredTopping != null) {
                json.addProperty("topping", requiredTopping.getSerializedName());
            }
            return json;
        }
    }
}