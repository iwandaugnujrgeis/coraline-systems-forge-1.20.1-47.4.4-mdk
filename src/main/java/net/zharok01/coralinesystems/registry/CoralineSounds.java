package net.zharok01.coralinesystems.registry;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import net.zharok01.coralinesystems.CoralineSystems;

public class CoralineSounds {
    public static final DeferredRegister<SoundEvent> SOUND_EVENTS =
            DeferredRegister.create(ForgeRegistries.SOUND_EVENTS, CoralineSystems.MOD_ID);

    //Helper:
    public static final RegistryObject<SoundEvent> STATIC_BUZZ = createSoundEvent("static_buzz");
    public static final RegistryObject<SoundEvent> HELPER_HURT = createSoundEvent("helper_hurt");
    public static final RegistryObject<SoundEvent> HELPER_IDLE = createSoundEvent("helper_idle");
    public static final RegistryObject<SoundEvent> HELPER_DEATH = createSoundEvent("helper_death");

    //Monster:
    public static final RegistryObject<SoundEvent> MONSTER_STARE = createSoundEvent("monster_stare");
    public static final RegistryObject<SoundEvent> MONSTER_IDLE = createSoundEvent("monster_idle");
    public static final RegistryObject<SoundEvent> MONSTER_DEATH = createSoundEvent("monster_death");
    public static final RegistryObject<SoundEvent> MONSTER_HURT = createSoundEvent("monster_hurt");

    private static RegistryObject<SoundEvent> createSoundEvent(final String soundName) {
        return SOUND_EVENTS.register(soundName, () -> SoundEvent.createVariableRangeEvent(new ResourceLocation(CoralineSystems.MOD_ID, soundName)));
    }

    public static void register(IEventBus eventBus) {
        SOUND_EVENTS.register(eventBus);
    }
}