package net.zharok01.coralinesystems.util.triggers;

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

public class PancakeToppingTrigger extends SimpleCriterionTrigger<PancakeToppingTrigger.Instance> {

    @SuppressWarnings("removal")
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
        Topping required = null;
        if (json.has("topping")) {
            String name = GsonHelper.getAsString(json, "topping");
            for (Topping t : Topping.values()) {
                if (t.getSerializedName().equalsIgnoreCase(name)) {
                    required = t;
                    break;
                }
            }
        }
        return new Instance(predicate, required);
    }

    public void trigger(ServerPlayer player, Topping topping) {
        this.trigger(player, instance -> instance.matches(topping));
    }

    public static class Instance extends AbstractCriterionTriggerInstance {

        @Nullable
        private final Topping requiredTopping;

        public Instance(ContextAwarePredicate predicate, @Nullable Topping requiredTopping) {
            super(PancakeToppingTrigger.ID, predicate);
            this.requiredTopping = requiredTopping;
        }

        public boolean matches(Topping applied) {
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