package net.zharok01.coralinesystems.registry;

import net.minecraft.world.effect.MobEffect;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import net.zharok01.coralinesystems.CoralineSystems;
import net.zharok01.coralinesystems.effect.ClarityEffect;
import net.zharok01.coralinesystems.effect.HaphazardEffect;
import net.zharok01.coralinesystems.effect.InstantStaminaEffect;
import net.zharok01.coralinesystems.effect.PersistenceEffect;

public class CoralineEffects {

    public static final DeferredRegister<MobEffect> MOB_EFFECTS =
            DeferredRegister.create(ForgeRegistries.MOB_EFFECTS, CoralineSystems.MOD_ID);

    public static final RegistryObject<MobEffect> HAPHAZARD =
            MOB_EFFECTS.register("haphazard", HaphazardEffect::new);

    public static final RegistryObject<MobEffect> PERSISTENCE =
            MOB_EFFECTS.register("persistence", PersistenceEffect::new);

    public static final RegistryObject<MobEffect> INSTANT_STAMINA =
            MOB_EFFECTS.register("instant_stamina", InstantStaminaEffect::new);

    public static final RegistryObject<MobEffect> CLARITY =
            MOB_EFFECTS.register("clarity", ClarityEffect::new);

    public static void register(IEventBus eventBus) {
        MOB_EFFECTS.register(eventBus);
    }
}