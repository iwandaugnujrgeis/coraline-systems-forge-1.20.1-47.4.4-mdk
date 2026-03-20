package net.zharok01.coralinesystems.client.helper;

import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;
import net.zharok01.coralinesystems.CoralineSystems;
import net.zharok01.coralinesystems.content.entity.custom.HelperEntity;
import org.jetbrains.annotations.NotNull;

public class HelperRenderer extends MobRenderer<HelperEntity, PlayerModel<HelperEntity>> {
    public HelperRenderer(EntityRendererProvider.Context context) {
        // 'false' here means we use the classic Steve arms (4px), not Alex arms (3px)
        super(context, new PlayerModel<>(context.bakeLayer(ModelLayers.PLAYER), false), 0.5f);
        this.addLayer(new HelperStaticLayer(this));
        this.addLayer(new HelperCarriedBlockLayer(this));
    }

    @Override
    public @NotNull ResourceLocation getTextureLocation(HelperEntity entity) {
        // Dynamic texture switching based on the SkinID we set in finalizeSpawn
        return new ResourceLocation(CoralineSystems.MOD_ID,
                "textures/entity/helper/helper_" + (entity.getSkinId() + 1) + ".png");
    }
}


