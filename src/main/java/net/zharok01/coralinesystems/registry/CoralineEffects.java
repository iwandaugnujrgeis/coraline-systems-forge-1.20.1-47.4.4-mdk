package net.zharok01.coralinesystems.registry;

import net.minecraft.world.effect.MobEffect;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import net.zharok01.coralinesystems.CoralineSystems;
import net.zharok01.coralinesystems.effect.HaphazardEffect;
import net.zharok01.coralinesystems.effect.InstantStaminaEffect;
import net.zharok01.coralinesystems.effect.PersistenceEffect;

public class CoralineEffects {

    public static final DeferredRegister<MobEffect> MOB_EFFECTS =
            DeferredRegister.create(ForgeRegistries.MOB_EFFECTS, CoralineSystems.MOD_ID);

    public static final RegistryObject<MobEffect> HAPHAZARD =
            MOB_EFFECTS.register("haphazard", HaphazardEffect::new);

    /** Applied by Wine (strength 1–5). Slows stamina drain to half speed. */
    public static final RegistryObject<MobEffect> PERSISTENCE =
            MOB_EFFECTS.register("persistence", PersistenceEffect::new);

    /**
     * Applied by Tea (amplifier = strength − 1, so 0–4) and Mulberry Juice
     * (fixed amplifier 1 → 4 points). Instantaneous — replenishes
     * {@code (amplifier + 1) * 2} stamina points immediately on drink.
     */
    public static final RegistryObject<MobEffect> INSTANT_STAMINA =
            MOB_EFFECTS.register("instant_stamina", InstantStaminaEffect::new);

    public static void register(IEventBus eventBus) {
        MOB_EFFECTS.register(eventBus);
    }
}
