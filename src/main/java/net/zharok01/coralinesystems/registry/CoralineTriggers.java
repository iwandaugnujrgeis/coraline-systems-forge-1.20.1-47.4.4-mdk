package net.zharok01.coralinesystems.registry;

import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.resources.ResourceLocation;
import net.zharok01.coralinesystems.CoralineSystems;
import com.github.alexthe666.alexsmobs.misc.AMAdvancementTrigger;

public class CoralineTriggers {

    public static final AMAdvancementTrigger CORRUPT_PORTAL = new AMAdvancementTrigger(new ResourceLocation(CoralineSystems.MOD_ID, "corrupt_portal"));

    public static void init() {
        CriteriaTriggers.register(CORRUPT_PORTAL);
    }
}