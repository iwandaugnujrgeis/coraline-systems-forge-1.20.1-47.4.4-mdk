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

/**
 * Custom geometry for the Brewing Cauldron.
 *
 * JSON shape (under "loader": "coraline_systems:brewing_cauldron"):
 * <pre>
 * {
 *   "loader":   "coraline_systems:brewing_cauldron",
 *   "cauldron": "<shell model location>",
 *   "fluid":    "<fluid-surface model location>"
 * }
 * </pre>
 *
 * The shell is rendered in the solid/cutout pass with normal shading.
 * The fluid surface is rendered in the translucent pass with ambient
 * occlusion disabled, keeping the two passes cleanly separated.
 * Tinting is handled entirely by our existing CoralineBlockColors
 * BlockColor registration — Forge calls it automatically for any quad
 * whose tintIndex >= 0.
 */
public class BrewingCauldronGeometry implements IUnbakedGeometry<BrewingCauldronGeometry> {

    private final UnbakedModel shellModel;
    private final UnbakedModel fluidModel;

    private BrewingCauldronGeometry(UnbakedModel shellModel, UnbakedModel fluidModel) {
        this.shellModel = shellModel;
        this.fluidModel = fluidModel;
    }

    // ── Baking ───────────────────────────────────────────────────────────────

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

        // Particle sprite comes from the shell (same as the cauldron side texture).
        TextureAtlasSprite particle = spriteGetter.apply(
                context.getMaterial("particle"));

        return new BrewingCauldronBakedModel(bakedShell, bakedFluid, particle);
    }

    @Override
    public void resolveParents(
            Function<ResourceLocation, UnbakedModel> modelGetter,
            IGeometryBakingContext context) {
        shellModel.resolveParents(modelGetter);
        fluidModel.resolveParents(modelGetter);
    }

    // ── Loader (inner class, registered as "coraline_systems:brewing_cauldron") ──

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

        /**
         * Mirrors the pattern in Amendments' CauldronModelLoader#parseModel:
         * the value can be either a plain string (model resource location)
         * or an inline JSON object (anonymous block model with a "parent" key
         * and optional "textures" overrides).
         */
        private static UnbakedModel parseInlineOrRef(
                JsonElement element,
                JsonDeserializationContext context) {

            if (element == null) {
                throw new JsonParseException(
                        "BrewingCauldronGeometry: missing required 'cauldron' or 'fluid' field");
            }
            // Inline object → deserialise as a BlockModel directly.
            // String reference → wrap in a minimal inline model that parents to it,
            // which causes ModelBaker to resolve it during resolveParents().
            if (element.isJsonObject()) {
                return context.deserialize(element, BlockModel.class);
            } else {
                // Plain string — create a minimal block model that just parents to
                // the referenced location. Forge's ModelBaker resolves parents
                // during the bake phase, so this is sufficient.
                String loc = element.getAsString();
                JsonObject wrapper = new JsonObject();
                wrapper.addProperty("parent", loc);
                return context.deserialize(wrapper, BlockModel.class);
            }
        }
    }
}