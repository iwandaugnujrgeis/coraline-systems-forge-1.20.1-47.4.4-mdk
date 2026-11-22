package net.zharok01.coralinesystems.registry;

import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.SimpleCraftingRecipeSerializer;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import net.zharok01.coralinesystems.CoralineSystems;
import net.zharok01.coralinesystems.util.recipe.BackpackRepairRecipe;

public class CoralineRecipeSerializers {
    public static final DeferredRegister<RecipeSerializer<?>> RECIPE_SERIALIZERS = DeferredRegister.create(ForgeRegistries.RECIPE_SERIALIZERS, CoralineSystems.MOD_ID);

    public static final RegistryObject<RecipeSerializer<BackpackRepairRecipe>> BACKPACK_REPAIR = RECIPE_SERIALIZERS.register("backpack_repair",
            () -> new SimpleCraftingRecipeSerializer<>(BackpackRepairRecipe::new));

    public static void register(IEventBus bus) {
        RECIPE_SERIALIZERS.register(bus);
    }
}