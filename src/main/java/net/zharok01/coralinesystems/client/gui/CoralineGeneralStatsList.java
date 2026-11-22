package net.zharok01.coralinesystems.client.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.ObjectSelectionList;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.stats.Stat;
import net.minecraft.stats.Stats;
import net.minecraft.stats.StatsCounter;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.zharok01.coralinesystems.client.gui.CoralineScreenLayout;
import net.zharok01.coralinesystems.util.CoralineStatBlacklist;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@OnlyIn(Dist.CLIENT)
public class CoralineGeneralStatsList extends ObjectSelectionList<CoralineGeneralStatsList.Entry> {

    private static final int TEXT_LINE_HEIGHT = 9;
    private static final int LIST_TOP = 32;
    private static final int STAT_INDENT = 10;
    private static final int VALUE_RIGHT_EDGE = 213;
    private static final int TEXT_TOP_OFFSET = 1;
    private static final int COLOR_BRIGHT = 16777215;
    private static final int COLOR_DIMMED = 9474192;

    private final Screen screen;

    public CoralineGeneralStatsList(Minecraft minecraft, Screen screen, StatsCounter stats) {
        super(minecraft, screen.width, screen.height, LIST_TOP, screen.height - CoralineScreenLayout.LIST_BOTTOM_OFFSET, TEXT_LINE_HEIGHT);
        this.screen = screen;

        List<Stat<ResourceLocation>> unlockedStats = new ArrayList<>();
        for (Stat<ResourceLocation> stat : Stats.CUSTOM) {
            if (stats.getValue(stat) > 0 && !CoralineStatBlacklist.isBlacklisted(stat)) {
                unlockedStats.add(stat);
            }
        }
        unlockedStats.sort(Comparator.comparingInt(stat -> BuiltInRegistries.CUSTOM_STAT.getId(stat.getValue())));

        Map<StatCategory, List<Stat<ResourceLocation>>> grouped = new EnumMap<>(StatCategory.class);
        for (Stat<ResourceLocation> stat : unlockedStats) {
            grouped.computeIfAbsent(StatCategory.of(stat.getValue()), category -> new ArrayList<>()).add(stat);
        }

        for (Map.Entry<StatCategory, List<Stat<ResourceLocation>>> group : grouped.entrySet()) {
            this.addEntry(new Entry(group.getKey().displayName(), null));
            for (Stat<ResourceLocation> stat : group.getValue()) {
                Component name = Component.translatable("stat." + stat.getValue().toString().replace(':', '.'));
                this.addEntry(new Entry(name, stat.format(stats.getValue(stat))));
            }
            this.addEntry(new Entry(CommonComponents.EMPTY, null));
        }
    }

    @Override
    protected void renderBackground(GuiGraphics guiGraphics) {
        this.screen.renderBackground(guiGraphics);
    }

    @OnlyIn(Dist.CLIENT)
    public class Entry extends ObjectSelectionList.Entry<Entry> {
        private final Component label;
        @Nullable
        private final String value;

        Entry(Component label, @Nullable String value) {
            this.label = label;
            this.value = value;
        }

        @Override
        public void render(GuiGraphics guiGraphics, int index, int top, int left, int width, int height, int mouseX, int mouseY, boolean hovering, float partialTick) {
            Font font = CoralineGeneralStatsList.this.minecraft.font;
            if (this.value == null) {
                guiGraphics.drawString(font, this.label, left + 2, top + TEXT_TOP_OFFSET, COLOR_BRIGHT);
            } else {
                guiGraphics.drawString(font, this.label, left + 2 + STAT_INDENT, top + TEXT_TOP_OFFSET, COLOR_DIMMED);
                guiGraphics.drawString(font, this.value, left + 2 + VALUE_RIGHT_EDGE - font.width(this.value), top + TEXT_TOP_OFFSET, COLOR_BRIGHT);
            }
        }

        @Override
        public Component getNarration() {
            Component spoken = this.value == null
                    ? this.label
                    : Component.empty().append(this.label).append(CommonComponents.SPACE).append(this.value);
            return Component.translatable("narrator.select", spoken);
        }
    }

    private enum StatCategory {
        GENERAL, MOVEMENT, COMBAT, CONTAINERS, WORKSTATIONS, MISC;

        Component displayName() {
            return Component.translatable("coraline_systems.stat_category." + this.name().toLowerCase(Locale.ROOT));
        }

        static StatCategory of(ResourceLocation statId) {
            String path = statId.getPath();
            if (path.endsWith("_one_cm")) {
                return MOVEMENT;
            }
            if (path.startsWith("damage_")) {
                return COMBAT;
            }
            if (path.startsWith("interact_with_")) {
                return WORKSTATIONS;
            }
            if (path.startsWith("inspect_")) {
                return CONTAINERS;
            }
            return switch (path) {
                case "leave_game", "play_time", "total_world_time", "time_since_death" -> GENERAL;
                case "jump", "sneak_time" -> MOVEMENT;
                case "deaths", "mob_kills", "player_kills", "target_hit" -> COMBAT;
                case "open_chest", "open_enderchest", "trigger_trapped_chest" -> CONTAINERS;
                default -> MISC;
            };
        }
    }
}