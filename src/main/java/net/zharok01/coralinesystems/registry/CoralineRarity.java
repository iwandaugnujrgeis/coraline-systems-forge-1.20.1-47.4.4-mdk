package net.zharok01.coralinesystems.registry;

import net.minecraft.network.chat.TextColor;
import net.minecraft.world.item.Rarity;

public final class CoralineRarity {

    public static final Rarity SPECIAL = Rarity.create(
            "SPECIAL",
            style -> style.withColor(TextColor.fromRgb(0x57F287))
    );

    public static final Rarity LEGENDARY = Rarity.create(
            "LEGENDARY",
            style -> style.withColor(TextColor.fromRgb(0x3B88FF))
    );

    private CoralineRarity() {}
}