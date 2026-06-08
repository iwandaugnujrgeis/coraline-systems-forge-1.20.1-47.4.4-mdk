package net.zharok01.coralinesystems.registry;

import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.resources.ResourceLocation;
import net.zharok01.coralinesystems.CoralineSystems;
import net.zharok01.coralinesystems.triggers.*;
import net.zharok01.coralinesystems.triggers.FullSacksTrigger;
import net.zharok01.coralinesystems.util.CoralineAdvancementTrigger;

public class CoralineTriggers {

    // Simple triggers
    public static final CoralineAdvancementTrigger CORRUPT_PORTAL = new CoralineAdvancementTrigger(new ResourceLocation(CoralineSystems.MOD_ID, "corrupt_portal"));
    public static final CoralineAdvancementTrigger COBALT_PANTS = new CoralineAdvancementTrigger(new ResourceLocation(CoralineSystems.MOD_ID, "cobalt_pants"));
    public static final CoralineAdvancementTrigger HELPER_DANCING = new CoralineAdvancementTrigger(new ResourceLocation(CoralineSystems.MOD_ID, "helper_dancing"));
    public static final CoralineAdvancementTrigger MINESHAFT_DISCOVERED = new CoralineAdvancementTrigger(new ResourceLocation(CoralineSystems.MOD_ID, "mineshaft_discovered"));
    public static final CoralineAdvancementTrigger SCARE_MONSTER = new CoralineAdvancementTrigger(new ResourceLocation(CoralineSystems.MOD_ID, "scare_monster"));
    public static final CoralineAdvancementTrigger ZIPLINE = new CoralineAdvancementTrigger(new ResourceLocation(CoralineSystems.MOD_ID, "zipline"));
    public static final CoralineAdvancementTrigger FLY_PIG = new CoralineAdvancementTrigger(new ResourceLocation(CoralineSystems.MOD_ID, "fly_pig"));
    public static final CoralineAdvancementTrigger OPEN_INVENTORY = new CoralineAdvancementTrigger(new ResourceLocation(CoralineSystems.MOD_ID, "open_inventory"));
    public static final FullSacksTrigger FULL_SACKS = new FullSacksTrigger();
    public static final PancakeToppingTrigger PANCAKE_TOPPING = new PancakeToppingTrigger();
    public static final FullPancakeStackTrigger FULL_PANCAKE_STACK = new FullPancakeStackTrigger();
    public static final PlayerSmeltedTrigger PLAYER_SMELTED = new PlayerSmeltedTrigger();
    public static final PlayerCraftedTrigger PLAYER_CRAFTED = new PlayerCraftedTrigger();

    public static void init() {
        CriteriaTriggers.register(CORRUPT_PORTAL);
        CriteriaTriggers.register(COBALT_PANTS);
        CriteriaTriggers.register(HELPER_DANCING);
        CriteriaTriggers.register(MINESHAFT_DISCOVERED);
        CriteriaTriggers.register(SCARE_MONSTER);
        CriteriaTriggers.register(ZIPLINE);
        CriteriaTriggers.register(FLY_PIG);
        CriteriaTriggers.register(FULL_SACKS);
        CriteriaTriggers.register(OPEN_INVENTORY);
        CriteriaTriggers.register(PANCAKE_TOPPING);
        CriteriaTriggers.register(FULL_PANCAKE_STACK);
        CriteriaTriggers.register(PLAYER_SMELTED);
        CriteriaTriggers.register(PLAYER_CRAFTED);
    }
}