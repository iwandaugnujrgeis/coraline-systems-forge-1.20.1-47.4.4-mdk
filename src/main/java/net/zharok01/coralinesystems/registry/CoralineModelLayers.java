package net.zharok01.coralinesystems.registry;

import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.resources.ResourceLocation;
import net.zharok01.coralinesystems.CoralineSystems;

@SuppressWarnings("removal")
public class CoralineModelLayers {

    public static final ModelLayerLocation ORB_LAYER = new ModelLayerLocation(
            new ResourceLocation(CoralineSystems.MOD_ID, "orb"), "main");

    public static final ModelLayerLocation OBSIDIAN_BOAT_LAYER = new ModelLayerLocation(
            new ResourceLocation(CoralineSystems.MOD_ID, "obsidian_boat"), "main");

    public static final ModelLayerLocation CRYPTID = new ModelLayerLocation(
            new ResourceLocation(CoralineSystems.MOD_ID, "cryptid"), "main");
}