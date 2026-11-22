package net.zharok01.coralinesystems.client.entity.skin;

import net.minecraft.resources.ResourceLocation;
import net.zharok01.coralinesystems.CoralineSystems;

import javax.annotation.Nullable;
import java.util.Arrays;

@SuppressWarnings("removal")
public enum SkinVariant {

    DEFAULT(null, "default", "skin.coraline_systems.default", true),

    STEVE(new ResourceLocation(CoralineSystems.MOD_ID, "textures/skin/steve.png"),
            "default",
            "skin.coraline_systems.steve",
            false
    ),

    GREEN_STEVE(new ResourceLocation(CoralineSystems.MOD_ID, "textures/skin/green_steve.png"),
            "default",
            "skin.coraline_systems.green_steve",
            false
    ),

    RIDER(new ResourceLocation(CoralineSystems.MOD_ID, "textures/skin/rider.png"),
            "default",
            "skin.coraline_systems.rider",
            false
    ),

    ALYSSA(new ResourceLocation(CoralineSystems.MOD_ID, "textures/skin/alyssa.png"),
            "default",
            "skin.coraline_systems.alyssa",
            false
    );

    @Nullable
    private final ResourceLocation texture;
    private final String modelName;
    private final String translationKey;
    private final boolean requiresOnlineSkin;

    SkinVariant(
            @Nullable ResourceLocation texture,
            String modelName,
            String translationKey,
            boolean requiresOnlineSkin
    ) {
        this.texture = texture;
        this.modelName = modelName;
        this.translationKey = translationKey;
        this.requiresOnlineSkin = requiresOnlineSkin;
    }

    @Nullable
    public ResourceLocation getTexture() {
        return texture;
    }

    public String getModelName() {
        return modelName;
    }

    public String getTranslationKey() {
        return translationKey;
    }

    public boolean requiresOnlineSkin() {
        return requiresOnlineSkin;
    }

    public static SkinVariant fromId(String id) {
        return Arrays.stream(values())
                .filter(v -> v.name().equalsIgnoreCase(id))
                .findFirst()
                .orElse(STEVE);
    }

    public String toId() {
        return this.name();
    }
}
