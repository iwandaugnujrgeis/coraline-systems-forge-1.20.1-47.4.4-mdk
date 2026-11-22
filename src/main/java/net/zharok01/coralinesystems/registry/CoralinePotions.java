package net.zharok01.coralinesystems.registry;

import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import net.zharok01.coralinesystems.CoralineSystems;

public class CoralinePotions {

    private static final int CLARITY_DURATION = 3 * 60 * 20;
    private static final int LONG_CLARITY_DURATION = 8 * 60 * 20;

    public static final DeferredRegister<Potion> POTIONS =
            DeferredRegister.create(ForgeRegistries.POTIONS, CoralineSystems.MOD_ID);

    public static final RegistryObject<Potion> CLARITY = POTIONS.register("clarity",
            () -> new Potion(new MobEffectInstance(CoralineEffects.CLARITY.get(), CLARITY_DURATION)));

    public static final RegistryObject<Potion> LONG_CLARITY = POTIONS.register("long_clarity",
            () -> new Potion(new MobEffectInstance(CoralineEffects.CLARITY.get(), LONG_CLARITY_DURATION)));

    public static void register(IEventBus eventBus) {
        POTIONS.register(eventBus);
    }
}
