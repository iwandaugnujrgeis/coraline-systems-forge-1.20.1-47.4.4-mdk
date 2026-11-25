package net.zharok01.coralinesystems.event;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.entity.living.LivingDropsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;
import net.zharok01.coralinesystems.CoralineSystems;
import net.zharok01.coralinesystems.registry.CoralineItems;
import net.zharok01.coralinesystems.registry.CoralineSounds;
import net.zharok01.coralinesystems.registry.CoralineTags;

@Mod.EventBusSubscriber(modid = CoralineSystems.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class SoulDropHandler {

    @SuppressWarnings("removal")
    private static final ResourceLocation COBALT_SWORD = new ResourceLocation("kubejs", "cobalt_sword");

    private static final float SOUL_DROP_CHANCE = 0.5F;
    private static final float FULL_RATE_MAX_HEALTH = 8.0F;

    @SubscribeEvent
    public static void onLivingDrops(LivingDropsEvent event) {
        LivingEntity entity = event.getEntity();

        if (!entity.getType().is(CoralineTags.BLEEDS)) return;

        DamageSource source = event.getSource();
        if (!(source.getEntity() instanceof Player player)) return;

        ItemStack heldItem = player.getMainHandItem();
        if (!COBALT_SWORD.equals(ForgeRegistries.ITEMS.getKey(heldItem.getItem()))) return;

        float healthRatio = Math.min(1.0F, entity.getMaxHealth() / FULL_RATE_MAX_HEALTH);
        float dropChance = SOUL_DROP_CHANCE * healthRatio * healthRatio * healthRatio;
        if (entity.getRandom().nextFloat() >= dropChance) return;

        event.setCanceled(true);

        ItemEntity soulDrop = new ItemEntity(
                entity.level(),
                entity.getX(),
                entity.getY(),
                entity.getZ(),
                new ItemStack(CoralineItems.SOUL_HEART.get())
        );
        soulDrop.setDefaultPickUpDelay();
        entity.level().addFreshEntity(soulDrop);

        entity.level().playSound(
                null,
                entity.getX(), entity.getY(), entity.getZ(),
                CoralineSounds.ESCAPE.get(),
                SoundSource.NEUTRAL,
                1.0F,
                0.9F + entity.getRandom().nextFloat() * 0.2F
        );
    }
}