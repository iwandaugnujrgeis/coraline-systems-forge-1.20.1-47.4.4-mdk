package net.zharok01.coralinesystems.registry;

import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.zharok01.coralinesystems.CoralineSystems;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@OnlyIn(Dist.CLIENT)
public class CoralineCapeRegistry {

    private static final ResourceLocation LEAD_DEV_CAPE = cape("developer_cape");
    private static final ResourceLocation CONTRIBUTOR_CAPE = cape("contributor_cape");
    private static final ResourceLocation SUGGESTER_CAPE = cape("suggester_cape");

    @SuppressWarnings("removal")
    private static ResourceLocation cape(String name) {
        return new ResourceLocation(CoralineSystems.MOD_ID, "textures/capes/" + name + ".png");
    }

    private static final Map<UUID, ResourceLocation> CAPES = new HashMap<>();

    static {
        // Founders:
        register("e2f8285b-7d5c-4c54-8226-9b3fbbafe9c0", LEAD_DEV_CAPE); // Anya
        register("d5ce569a-7891-4a31-93ea-c812a0628d45", LEAD_DEV_CAPE); // Me

        // Contributors:
        // register("d5ce569a-7891-4a31-93ea-c812a0628d45", CONTRIBUTOR_CAPE); // Me (test)
        register("125b3a71-0988-4407-b6de-43b5662aa504", CONTRIBUTOR_CAPE); // Silkishly
        register("eed6fdab-469e-46cd-8f86-7efe60b1b691", CONTRIBUTOR_CAPE); // Soumeh
        register("a6a566d3-6038-4b7f-99d4-74a4532a2776", CONTRIBUTOR_CAPE); // AJ
        register("b582223b-ff29-4277-ab55-0d3e9586163c", CONTRIBUTOR_CAPE); // Sweety
        register("60d9ff77-ddd5-49fd-9f34-3a3c782fea1f", CONTRIBUTOR_CAPE); // LogNok
        register("58a69126-8e7f-447f-9290-267eb99a5cb0", CONTRIBUTOR_CAPE); // PastaOnVenus
        register("1191f56f-4025-467d-be5f-3d09acd29df9", CONTRIBUTOR_CAPE); // unknown.gif

        // Suggester:
        // register("d5ce569a-7891-4a31-93ea-c812a0628d45", SUGGESTER_CAPE); // Me (test)
        register("7688c431-17f0-47ac-80c7-699fd9164838", SUGGESTER_CAPE); // Tanner
        register("1117b8f0-e934-4959-8e95-517a672b4c2e", SUGGESTER_CAPE); // Unnamed_Player72
        register("8b497198-9b70-4d64-acbc-2a8ee59ea48b", SUGGESTER_CAPE); // ApenaZ
        register("f991828d-8203-4d74-96f7-5a444f0e35c4", SUGGESTER_CAPE); // XxBulbasaur64xX
        register("7715f698-560a-4f31-9051-22fd9adc23fe", SUGGESTER_CAPE); // Potochnyy
        register("4ae0b55d-f7f1-4362-8d59-8e0958ed4388", SUGGESTER_CAPE); // asof
        register("7041d083-ded7-4332-92c4-29b96e615c08", SUGGESTER_CAPE); // Dorigami
    }

    private static void register(String uuidString, ResourceLocation cape) {
        CAPES.put(UUID.fromString(uuidString), cape);
    }

    public static boolean hasCape(AbstractClientPlayer player) {
        return CAPES.containsKey(player.getGameProfile().getId());
    }

    @Nullable
    public static ResourceLocation getCape(AbstractClientPlayer player) {
        return CAPES.get(player.getGameProfile().getId());
    }

    @Nullable
    public static ResourceLocation getCapeByUUID(UUID uuid) {
        return CAPES.get(uuid);
    }
}