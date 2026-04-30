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

    public static final RegistryObject<SoundEvent> STATIC_BUZZ = createSoundEvent("static_buzz");
    public static final RegistryObject<SoundEvent> HELPER_HURT = createSoundEvent("helper_hurt");
    public static final RegistryObject<SoundEvent> HELPER_IDLE = createSoundEvent("helper_idle");
    public static final RegistryObject<SoundEvent> HELPER_DEATH = createSoundEvent("helper_death");

    public static final RegistryObject<SoundEvent> MONSTER_STARE = createSoundEvent("monster_stare");
    public static final RegistryObject<SoundEvent> MONSTER_IDLE = createSoundEvent("monster_idle");
    public static final RegistryObject<SoundEvent> MONSTER_DEATH = createSoundEvent("monster_death");
    public static final RegistryObject<SoundEvent> MONSTER_HURT = createSoundEvent("monster_hurt");
    public static final RegistryObject<SoundEvent> MONSTER_TELEPORT = createSoundEvent("monster_teleport");
    public static final RegistryObject<SoundEvent> MONSTER_VANISH = createSoundEvent("monster_vanish");
    public static final RegistryObject<SoundEvent> MONSTER_SCREAM = createSoundEvent("monster_scream");

    public static final RegistryObject<SoundEvent> STATIC_PORTAL_AMBIENT = createSoundEvent("static_portal_ambient");
    public static final RegistryObject<SoundEvent> STATIC_PORTAL_OPEN = createSoundEvent("static_portal_open");

    public static final RegistryObject<SoundEvent> GIANT_STOMP = createSoundEvent("giant_stomp");
    public static final RegistryObject<SoundEvent> GIANT_AMBIENT = createSoundEvent("giant_ambient");
    public static final RegistryObject<SoundEvent> GIANT_HURT = createSoundEvent("giant_hurt");
    public static final RegistryObject<SoundEvent> GIANT_DEATH = createSoundEvent("giant_death");

    public static final RegistryObject<SoundEvent> VIBRATION_SENSOR_PING = createSoundEvent("vibration_sensor_ping");
    public static final RegistryObject<SoundEvent> DETECTOR_ACTIVATED = createSoundEvent("detector_activated");

    public static final RegistryObject<SoundEvent> MINESHAFT_SPOOK = createSoundEvent("mineshaft_spook");

    private static RegistryObject<SoundEvent> createSoundEvent(final String soundName) {
        return SOUND_EVENTS.register(soundName, () -> SoundEvent.createVariableRangeEvent(new ResourceLocation(CoralineSystems.MOD_ID, soundName)));
    }

    public static void register(IEventBus eventBus) {
        SOUND_EVENTS.register(eventBus);
    }
}