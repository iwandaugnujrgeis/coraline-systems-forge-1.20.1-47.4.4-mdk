package net.zharok01.coralinesystems.registry;

import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.resources.ResourceLocation;
import net.zharok01.coralinesystems.CoralineSystems;
import com.github.alexthe666.alexsmobs.misc.AMAdvancementTrigger;

public class CoralineTriggers {

    public static final AMAdvancementTrigger CORRUPT_PORTAL = new AMAdvancementTrigger(new ResourceLocation(CoralineSystems.MOD_ID, "corrupt_portal"));
    public static final AMAdvancementTrigger COBALT_PANTS = new AMAdvancementTrigger(new ResourceLocation(CoralineSystems.MOD_ID, "cobalt_pants"));
    public static final AMAdvancementTrigger HELPER_DANCING = new AMAdvancementTrigger(new ResourceLocation(CoralineSystems.MOD_ID, "helper_dancing"));

    public static void init() {
        CriteriaTriggers.register(CORRUPT_PORTAL);
        CriteriaTriggers.register(COBALT_PANTS);
        CriteriaTriggers.register(HELPER_DANCING);
    }
}