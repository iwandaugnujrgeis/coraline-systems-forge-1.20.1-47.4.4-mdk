package net.zharok01.coralinesystems.registry;

import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.SimpleCraftingRecipeSerializer;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import net.zharok01.coralinesystems.CoralineSystems;
import net.zharok01.coralinesystems.util.recipe.NameTagRenameRecipe;
import net.zharok01.coralinesystems.util.compat.crafttweaker.recipe.serializer.CoralineShapedRecipeSerializer;
import net.zharok01.coralinesystems.util.compat.crafttweaker.recipe.serializer.CoralineShapelessRecipeSerializer;

public class CoralineSerializers {

    public static final DeferredRegister<RecipeSerializer<?>> SERIALIZERS =
            DeferredRegister.create(ForgeRegistries.RECIPE_SERIALIZERS, CoralineSystems.MOD_ID);

    public static final RegistryObject<CoralineShapedRecipeSerializer> SHAPED_WITH_META =
            SERIALIZERS.register("shaped_with_meta", CoralineShapedRecipeSerializer::new);

    public static final RegistryObject<CoralineShapelessRecipeSerializer> SHAPELESS_WITH_META =
            SERIALIZERS.register("shapeless_with_meta", CoralineShapelessRecipeSerializer::new);

    public static final RegistryObject<SimpleCraftingRecipeSerializer<NameTagRenameRecipe>> NAME_TAG_RENAME =
            SERIALIZERS.register("name_tag_rename", () -> new SimpleCraftingRecipeSerializer<>(NameTagRenameRecipe::new));

    public static void register(IEventBus eventBus) {
        SERIALIZERS.register(eventBus);
    }
}