package net.zharok01.coralinesystems.util;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextColor;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.UUID;

public final class FounderNameColors {

    private static final Map<UUID, TextColor> coraline$FOUNDER_COLORS = Map.of(
            UUID.fromString("e2f8285b-7d5c-4c54-8226-9b3fbbafe9c0"), TextColor.fromRgb(0xDDD605),
            UUID.fromString("d5ce569a-7891-4a31-93ea-c812a0628d45"), TextColor.fromRgb(0x57F287)
    );

    private FounderNameColors() {
    }

    public static @NotNull Component coraline$applyColor(@NotNull UUID playerId, @NotNull Component name) {
        TextColor nameColor = coraline$FOUNDER_COLORS.get(playerId);
        return nameColor == null ? name : name.copy().withStyle(style -> style.withColor(nameColor));
    }
}
