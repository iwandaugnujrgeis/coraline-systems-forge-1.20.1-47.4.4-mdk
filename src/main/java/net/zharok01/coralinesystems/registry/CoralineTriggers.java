package net.zharok01.coralinesystems.registry;

import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.resources.ResourceLocation;
import net.zharok01.coralinesystems.CoralineSystems;
import net.zharok01.coralinesystems.util.triggers.*;
import net.zharok01.coralinesystems.util.triggers.FullSacksTrigger;
import net.zharok01.coralinesystems.util.helper.AdvancementTriggerHelper;

@SuppressWarnings("removal")
public class CoralineTriggers {

    public static final AdvancementTriggerHelper CORRUPT_PORTAL = new AdvancementTriggerHelper(new ResourceLocation(CoralineSystems.MOD_ID, "corrupt_portal"));
    public static final AdvancementTriggerHelper COBALT_PANTS = new AdvancementTriggerHelper(new ResourceLocation(CoralineSystems.MOD_ID, "cobalt_pants"));
    public static final AdvancementTriggerHelper HELPER_DANCING = new AdvancementTriggerHelper(new ResourceLocation(CoralineSystems.MOD_ID, "helper_dancing"));
    public static final AdvancementTriggerHelper MINESHAFT_DISCOVERED = new AdvancementTriggerHelper(new ResourceLocation(CoralineSystems.MOD_ID, "mineshaft_discovered"));
    public static final AdvancementTriggerHelper SCARE_MONSTER = new AdvancementTriggerHelper(new ResourceLocation(CoralineSystems.MOD_ID, "scare_monster"));
    public static final AdvancementTriggerHelper ZIPLINE = new AdvancementTriggerHelper(new ResourceLocation(CoralineSystems.MOD_ID, "zipline"));
    public static final AdvancementTriggerHelper FLY_PIG = new AdvancementTriggerHelper(new ResourceLocation(CoralineSystems.MOD_ID, "fly_pig"));
    public static final AdvancementTriggerHelper HARVEST_COMPOST = new AdvancementTriggerHelper(new ResourceLocation(CoralineSystems.MOD_ID, "harvest_compost"));
    public static final AdvancementTriggerHelper CENTRIFUGE_ORB_INSERT = new AdvancementTriggerHelper(new ResourceLocation(CoralineSystems.MOD_ID, "orb_insert"));
    public static final AdvancementTriggerHelper REACTOR_ORB_INSERT = new AdvancementTriggerHelper(new ResourceLocation(CoralineSystems.MOD_ID, "reactor_orb_insert"));
    public static final FullSacksTrigger FULL_SACKS = new FullSacksTrigger();
    public static final PancakeToppingTrigger PANCAKE_TOPPING = new PancakeToppingTrigger();
    public static final FullPancakeStackTrigger FULL_PANCAKE_STACK = new FullPancakeStackTrigger();
    public static final PlayerSmeltedTrigger PLAYER_SMELTED = new PlayerSmeltedTrigger();
    public static final PlayerCraftedTrigger PLAYER_CRAFTED = new PlayerCraftedTrigger();
    public static final RepeaterMaxDelayTrigger REPEATER_MAX_DELAY = new RepeaterMaxDelayTrigger();
    public static final AdvancementTriggerHelper HONEY_HARVEST_INVISIBLE = new AdvancementTriggerHelper(new ResourceLocation(CoralineSystems.MOD_ID, "honey_harvest_invisible"));

    public static final AdvancementTriggerHelper OPEN_INVENTORY = new AdvancementTriggerHelper(new ResourceLocation(CoralineSystems.MOD_ID, "open_inventory"));
    public static final AdvancementTriggerHelper OPEN_RECIPE_BOOK = new AdvancementTriggerHelper(new ResourceLocation(CoralineSystems.MOD_ID, "open_recipe_book"));
    public static final AdvancementTriggerHelper TIMEDUST_FUEL = new AdvancementTriggerHelper(new ResourceLocation(CoralineSystems.MOD_ID, "timedust_fuel"));
    public static final AdvancementTriggerHelper KOMBUCHA_CULTURE = new AdvancementTriggerHelper(new ResourceLocation(CoralineSystems.MOD_ID, "kombucha_culture"));
    public static final AdvancementTriggerHelper MULBERRY_WATER_CAULDRON = new AdvancementTriggerHelper(new ResourceLocation(CoralineSystems.MOD_ID, "mulberry_water_cauldron"));
    public static final MinecartDistanceTrigger MINECART_DISTANCE = new MinecartDistanceTrigger();
    public static final AdvancementTriggerHelper TUNE_NOTE_BLOCK = new AdvancementTriggerHelper(new ResourceLocation(CoralineSystems.MOD_ID, "tune_note_block"));
    public static final StatThresholdTrigger STAT_THRESHOLD = new StatThresholdTrigger();
    public static final AdvancementTriggerHelper FISH_REELED_OUT = new AdvancementTriggerHelper(new ResourceLocation(CoralineSystems.MOD_ID, "fish_reeled_out"));

    public static void init() {
        CriteriaTriggers.register(CORRUPT_PORTAL);
        CriteriaTriggers.register(COBALT_PANTS);
        CriteriaTriggers.register(HELPER_DANCING);
        CriteriaTriggers.register(MINESHAFT_DISCOVERED);
        CriteriaTriggers.register(SCARE_MONSTER);
        CriteriaTriggers.register(ZIPLINE);
        CriteriaTriggers.register(FLY_PIG);
        CriteriaTriggers.register(OPEN_INVENTORY);
        CriteriaTriggers.register(HARVEST_COMPOST);
        CriteriaTriggers.register(CENTRIFUGE_ORB_INSERT);
        CriteriaTriggers.register(REACTOR_ORB_INSERT);
        CriteriaTriggers.register(FULL_SACKS);
        CriteriaTriggers.register(PANCAKE_TOPPING);
        CriteriaTriggers.register(FULL_PANCAKE_STACK);
        CriteriaTriggers.register(PLAYER_SMELTED);
        CriteriaTriggers.register(PLAYER_CRAFTED);
        CriteriaTriggers.register(REPEATER_MAX_DELAY);
        CriteriaTriggers.register(OPEN_RECIPE_BOOK);
        CriteriaTriggers.register(TIMEDUST_FUEL);
        CriteriaTriggers.register(KOMBUCHA_CULTURE);
        CriteriaTriggers.register(MULBERRY_WATER_CAULDRON);
        CriteriaTriggers.register(MINECART_DISTANCE);
        CriteriaTriggers.register(TUNE_NOTE_BLOCK);
        CriteriaTriggers.register(STAT_THRESHOLD);
        CriteriaTriggers.register(HONEY_HARVEST_INVISIBLE);
        CriteriaTriggers.register(FISH_REELED_OUT);
    }
}