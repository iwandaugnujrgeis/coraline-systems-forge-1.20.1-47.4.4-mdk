package net.zharok01.coralinesystems.client.entity.brume; // Adjust package as needed

import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;
import net.zharok01.coralinesystems.CoralineSystems;
import net.zharok01.coralinesystems.entity.BrumeEntity;
import org.jetbrains.annotations.NotNull;

public class BrumeRenderer extends MobRenderer<BrumeEntity, PlayerModel<BrumeEntity>> {

    // Define the exact location of your single skin file
    private static final ResourceLocation TEXTURE = new ResourceLocation(CoralineSystems.MOD_ID, "textures/entity/brume/brume.png");

    public BrumeRenderer(EntityRendererProvider.Context context) {
        // We use 'false' here for standard Steve (4px) arms, just like your Helper and Monster
        super(context, new PlayerModel<>(context.bakeLayer(ModelLayers.PLAYER), false), 0.5f);
    }

    @Override
    public @NotNull ResourceLocation getTextureLocation(@NotNull BrumeEntity entity) {
        return TEXTURE;
    }
}