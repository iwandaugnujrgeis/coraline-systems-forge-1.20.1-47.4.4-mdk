package net.zharok01.coralinesystems.client.entity.skin;

import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.config.ModConfig;

public final class CoralineSkinConfig {

    private static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();
    public static final ForgeConfigSpec SPEC;

    private static final ForgeConfigSpec.ConfigValue<String> SELECTED_SKIN;

    static {
        BUILDER.comment("Coraline Systems — Skin Selector Settings");
        SELECTED_SKIN = BUILDER
                .comment("The last skin variant chosen by this player.",
                         "Valid values: " + buildValidValues())
                .define("selectedSkin", SkinVariant.STEVE.toId());
        SPEC = BUILDER.build();
    }

    private CoralineSkinConfig() {}

    @SuppressWarnings("removal")
    public static void register() {
        ModLoadingContext.get().registerConfig(ModConfig.Type.CLIENT, SPEC);
    }

    public static SkinVariant getSelectedVariant() {
        return SkinVariant.fromId(SELECTED_SKIN.get());
    }

    public static void setSelectedVariant(SkinVariant variant) {
        SELECTED_SKIN.set(variant.toId());
        SPEC.save();
    }

    private static String buildValidValues() {
        StringBuilder sb = new StringBuilder();
        SkinVariant[] values = SkinVariant.values();
        for (int i = 0; i < values.length; i++) {
            sb.append(values[i].toId());
            if (i < values.length - 1) sb.append(", ");
        }
        return sb.toString();
    }
}
