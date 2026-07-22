package net.zharok01.coralinesystems.client.model;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import net.minecraft.client.renderer.block.model.BlockModel;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.client.model.geometry.IGeometryBakingContext;
import net.minecraftforge.client.model.geometry.IGeometryLoader;
import net.minecraftforge.client.model.geometry.IUnbakedGeometry;

import java.util.function.Function;

public class BrewingCauldronGeometry implements IUnbakedGeometry<BrewingCauldronGeometry> {

    private final UnbakedModel shellModel;
    private final UnbakedModel fluidModel;

    private BrewingCauldronGeometry(UnbakedModel shellModel, UnbakedModel fluidModel) {
        this.shellModel = shellModel;
        this.fluidModel = fluidModel;
    }

    @Override
    public BakedModel bake(
            IGeometryBakingContext context,
            ModelBaker baker,
            Function<Material, TextureAtlasSprite> spriteGetter,
            ModelState modelState,
            ItemOverrides overrides,
            ResourceLocation modelLocation) {

        BakedModel bakedShell = shellModel.bake(baker, spriteGetter, modelState, modelLocation);
        BakedModel bakedFluid = fluidModel.bake(baker, spriteGetter, modelState, modelLocation);

        // Fetch the particle sprite directly by resource location rather than
        // going through context.getMaterial("particle"), which has nothing to
        // resolve because the Loader never registers that texture slot.
        // cauldron_side is the correct break-particle for any cauldron variant.
        TextureAtlasSprite particle = spriteGetter.apply(
                new Material(
                        net.minecraft.client.renderer.texture.TextureAtlas.LOCATION_BLOCKS,
                        new ResourceLocation("minecraft", "block/cauldron_side")));

        return new BrewingCauldronBakedModel(bakedShell, bakedFluid, particle);
    }

    @Override
    public void resolveParents(
            Function<ResourceLocation, UnbakedModel> modelGetter,
            IGeometryBakingContext context) {
        shellModel.resolveParents(modelGetter);
        fluidModel.resolveParents(modelGetter);
    }

    // ── Loader ───────────────────────────────────────────────────────────────

    public static final class Loader implements IGeometryLoader<BrewingCauldronGeometry> {

        public static final Loader INSTANCE = new Loader();

        private Loader() {}

        @Override
        public BrewingCauldronGeometry read(
                JsonObject json,
                JsonDeserializationContext context) throws JsonParseException {

            UnbakedModel shell = parseInlineOrRef(json.get("cauldron"), context);
            UnbakedModel fluid = parseInlineOrRef(json.get("fluid"), context);
            return new BrewingCauldronGeometry(shell, fluid);
        }

        private static UnbakedModel parseInlineOrRef(
                JsonElement element,
                JsonDeserializationContext context) {

            if (element == null) {
                throw new JsonParseException(
                        "BrewingCauldronGeometry: missing required 'cauldron' or 'fluid' field");
            }
            if (element.isJsonObject()) {
                return context.deserialize(element, BlockModel.class);
            } else {
                String loc = element.getAsString();
                JsonObject wrapper = new JsonObject();
                wrapper.addProperty("parent", loc);
                return context.deserialize(wrapper, BlockModel.class);
            }
        }
    }
}