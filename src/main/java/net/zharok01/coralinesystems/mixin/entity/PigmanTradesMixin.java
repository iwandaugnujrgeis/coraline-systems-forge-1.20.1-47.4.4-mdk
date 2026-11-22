package net.zharok01.coralinesystems.mixin.entity;

import com.github.alexthe666.alexsmobs.item.AMItemRegistry;
import com.legacy.rediscovered.entity.pigman.data.PigmanTrades;
import com.legacy.rediscovered.registry.RediscoveredBlocks;
import com.legacy.rediscovered.registry.RediscoveredItems;
import com.ninni.etcetera.registry.EtceteraItems;
import com.teamabnormals.caverns_and_chasms.core.registry.CCItems;
import net.mehvahdjukaar.supplementaries.reg.ModRegistry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.npc.VillagerTrades;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.registries.ForgeRegistries;
import net.zharok01.coralinesystems.registry.CoralineItems;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = PigmanTrades.class, remap = false)
public class PigmanTradesMixin {

    @Inject(method = "<clinit>", at = @At("TAIL"))
    private static void coraline$overridePigmanTrades(CallbackInfo ci) {
        overrideMetalworker();
        overrideBowyer();
        overrideTechnician();
        overrideTailor();
        overrideDoctor();
    }

    @Unique
    @SuppressWarnings("removal")
    private static Item coraline$item(String namespace, String path) {
        Item found = ForgeRegistries.ITEMS.getValue(new ResourceLocation(namespace, path));
        return found != null ? found : Items.AIR;
    }

    @Unique
    private static Item coraline$cs(String path) {
        return coraline$item("coraline_systems", path);
    }

    // Metalworker
    @Unique
    private static void overrideMetalworker() {
        PigmanTrades.METALWORKER_OFFERS.clear();

        // Level 1: Novice
        PigmanTrades.METALWORKER_OFFERS.put(1, new VillagerTrades.ItemListing[]{
                new PigmanTrades.Trade.Builder()
                        .firstItem(Items.RAW_COPPER, 12).result(RediscoveredItems.ruby, 1)
                        .priceMultiplier(0.05F * 4).build(),
                new PigmanTrades.Trade.Builder()
                        .firstItem(Items.RAW_IRON, 8).result(RediscoveredItems.ruby, 1)
                        .priceMultiplier(0.05F * 4).build(),
                new PigmanTrades.Trade.Builder()
                        .firstItem(RediscoveredItems.ruby, 2)
                        .result(coraline$item("kubejs", "copper_pickaxe"), 1)
                        .givenXP(4).build(),
                new PigmanTrades.Trade.Builder()
                        .firstItem(RediscoveredItems.ruby, 1)
                        .result(coraline$item("kubejs", "copper_shovel"), 1)
                        .givenXP(3).build()
        });

        // Level 2: Apprentice
        PigmanTrades.METALWORKER_OFFERS.put(2, new VillagerTrades.ItemListing[]{
                new PigmanTrades.Trade.Builder()
                        .firstItem(Items.IRON_INGOT, 5).result(RediscoveredItems.ruby, 1)
                        .priceMultiplier(0.05F * 3).build(),
                new PigmanTrades.Trade.Builder()
                        .firstItem(RediscoveredItems.ruby, 3)
                        .result(Items.IRON_PICKAXE.getDefaultInstance())
                        .givenXP(10).build(),
                new PigmanTrades.Trade.Builder()
                        .firstItem(RediscoveredItems.ruby, 4)
                        .result(Items.IRON_SWORD.getDefaultInstance())
                        .givenXP(12).build(),
                new PigmanTrades.Trade.Builder()
                        .firstItem(RediscoveredItems.ruby, 3)
                        .result(Items.CHAINMAIL_BOOTS.getDefaultInstance())
                        .givenXP(10).build(),
                new PigmanTrades.Trade.Builder()
                        .firstItem(RediscoveredItems.ruby, 4)
                        .result(Items.CHAINMAIL_LEGGINGS.getDefaultInstance())
                        .givenXP(10).build()
        });

        // Level 3: Journeyman
        PigmanTrades.METALWORKER_OFFERS.put(3, new VillagerTrades.ItemListing[]{
                new PigmanTrades.Trade.Builder()
                        .firstItem(Items.GOLD_INGOT, 4).result(RediscoveredItems.ruby, 1)
                        .priceMultiplier(0.05F * 3).build(),
                new PigmanTrades.Trade.Builder()
                        .firstItem(RediscoveredItems.ruby, 2)
                        .result(Items.CHAINMAIL_HELMET.getDefaultInstance())
                        .givenXP(11).build(),
                new PigmanTrades.Trade.Builder()
                        .firstItem(RediscoveredItems.ruby, 5)
                        .result(Items.CHAINMAIL_CHESTPLATE.getDefaultInstance())
                        .givenXP(11).build(),
                new PigmanTrades.Trade.Builder()
                        .firstItem(RediscoveredItems.ruby, 6)
                        .result(coraline$item("kubejs", "cobalt_sword"), 1)
                        .givenXP(14).maxUses(6).build()
        });

        // Level 4: Expert
        PigmanTrades.METALWORKER_OFFERS.put(4, new VillagerTrades.ItemListing[]{
                new PigmanTrades.Trade.Builder()
                        .firstItem(RediscoveredItems.ruby, 4)
                        .result(RediscoveredItems.plate_helmet, 1)
                        .givenXP(13).maxUses(8).build(),
                new PigmanTrades.Trade.Builder()
                        .firstItem(RediscoveredItems.ruby, 5)
                        .result(RediscoveredItems.plate_boots, 1)
                        .givenXP(13).maxUses(8).build(),
                new PigmanTrades.Trade.Builder()
                        .firstItem(RediscoveredItems.ruby, 7)
                        .result(coraline$item("kubejs", "bismuth_pickaxe"), 1)
                        .givenXP(16).maxUses(5).build()
        });

        // Level 5: Master
        PigmanTrades.METALWORKER_OFFERS.put(5, new VillagerTrades.ItemListing[]{
                new PigmanTrades.Trade.Builder()
                        .firstItem(RediscoveredItems.ruby, 8)
                        .result(RediscoveredItems.plate_chestplate, 1)
                        .givenXP(20).maxUses(4).build(),
                new PigmanTrades.Trade.Builder()
                        .firstItem(RediscoveredItems.ruby, 6)
                        .result(RediscoveredItems.plate_leggings, 1)
                        .givenXP(18).maxUses(4).build(),
                new PigmanTrades.Trade.Builder()
                        .firstItem(RediscoveredItems.ruby, 3)
                        .result(coraline$item("kubejs", "cobalt"), 4)
                        .givenXP(15).maxUses(8).priceMultiplier(0.05F * 2).build()
        });
    }

    // Bowyer
    private static void overrideBowyer() {
        PigmanTrades.BOWYER_OFFERS.clear();

        // Level 1: Novice
        PigmanTrades.BOWYER_OFFERS.put(1, new VillagerTrades.ItemListing[]{
                new PigmanTrades.Trade.Builder()
                        .firstItem(Items.FLINT, 8).result(RediscoveredItems.ruby, 1)
                        .priceMultiplier(0.05F * 2).build(),
                new PigmanTrades.Trade.Builder()
                        .firstItem(Items.FEATHER, 20).result(RediscoveredItems.ruby, 3)
                        .priceMultiplier(0.05F * 2).build(),
                new PigmanTrades.Trade.Builder()
                        .firstItem(RediscoveredItems.ruby, 3)
                        .result(Items.ARROW, 24)
                        .priceMultiplier(0.05F * 2).build(),
                new PigmanTrades.Trade.Builder()
                        .firstItem(RediscoveredItems.ruby, 2)
                        .result(Items.BOW.getDefaultInstance())
                        .build()
        });

        // Level 2: Apprentice
        PigmanTrades.BOWYER_OFFERS.put(2, new VillagerTrades.ItemListing[]{
                new PigmanTrades.Trade.Builder()
                        .firstItem(RediscoveredItems.ruby, 2)
                        .result(CCItems.LARGE_ARROW.get(), 8)
                        .priceMultiplier(0.05F * 2).build(),
                new PigmanTrades.Trade.Builder()
                        .firstItem(RediscoveredItems.ruby, 3)
                        .result(Items.SPECTRAL_ARROW, 12)
                        .priceMultiplier(0.05F * 2).build(),
                new PigmanTrades.Trade.Builder()
                        .firstItem(RediscoveredItems.ruby, 3)
                        .result(coraline$item("supplementaries", "rope_arrow"), 8)
                        .priceMultiplier(0.05F * 2).build(),
                new PigmanTrades.Trade.Builder()
                        .firstItem(RediscoveredItems.ruby, 4)
                        .result(RediscoveredItems.quiver, 1)
                        .maxUses(6).build()
        });

        // Level 3: Journeyman
        PigmanTrades.BOWYER_OFFERS.put(3, new VillagerTrades.ItemListing[]{
                new PigmanTrades.Trade.Builder()
                        .firstItem(Items.STRING, 12).result(RediscoveredItems.ruby, 2)
                        .priceMultiplier(0.05F * 3).build(),
                new PigmanTrades.Trade.Builder()
                        .firstItem(RediscoveredItems.ruby, 4)
                        .result(Items.CROSSBOW.getDefaultInstance())
                        .build(),
                new PigmanTrades.Trade.Builder()
                        .firstItem(RediscoveredItems.ruby, 4)
                        .result(RediscoveredItems.purple_arrow, 12)
                        .priceMultiplier(0.05F * 2).build()
        });

        // Level 4: Expert
        PigmanTrades.BOWYER_OFFERS.put(4, new VillagerTrades.ItemListing[]{
                new PigmanTrades.Trade.Builder()
                        .firstItem(RediscoveredItems.ruby, 2)
                        .result(CCItems.LARGE_ARROW.get(), 16)
                        .priceMultiplier(0.05F * 2).build(),
                new PigmanTrades.Trade.Builder()
                        .firstItem(RediscoveredItems.ruby, 2)
                        .result(Items.ARROW, 32)
                        .priceMultiplier(0.05F * 2).build(),
                new PigmanTrades.Trade.Builder()
                        .firstItem(RediscoveredItems.ruby, 3)
                        .result(coraline$item("supplementaries", "slingshot"), 1)
                        .maxUses(6).build()
        });

        // Level 5: Master
        PigmanTrades.BOWYER_OFFERS.put(5, new VillagerTrades.ItemListing[]{
                new PigmanTrades.Trade.Builder()
                        .firstItem(RediscoveredItems.ruby, 2)
                        .result(CoralineItems.ORB.get(), 8)
                        .priceMultiplier(0.05F * 4).build(),
                new PigmanTrades.Trade.Builder()
                        .firstItem(RediscoveredItems.ruby, 5)
                        .result(coraline$item("rediscovered", "ruby_eye"), 1)
                        .maxUses(4).build(),
                new PigmanTrades.Trade.Builder()
                        .firstItem(RediscoveredItems.ruby, 3)
                        .result(coraline$item("supplementaries", "bomb"), 4)
                        .maxUses(8).priceMultiplier(0.05F * 2).build()
        });
    }

    // Technician
    private static void overrideTechnician() {
        PigmanTrades.TECHNICIAN_OFFERS.clear();

        // Level 1: Novice
        PigmanTrades.TECHNICIAN_OFFERS.put(1, new VillagerTrades.ItemListing[]{
                new PigmanTrades.Trade.Builder()
                        .firstItem(Items.REDSTONE, 32).result(RediscoveredItems.ruby, 1)
                        .priceMultiplier(0.05F * 6).build(),
                new PigmanTrades.Trade.Builder()
                        .firstItem(Items.QUARTZ, 16).result(RediscoveredItems.ruby, 2)
                        .priceMultiplier(0.05F * 3).build(),
                new PigmanTrades.Trade.Builder()
                        .firstItem(RediscoveredItems.ruby, 2).secondItem(Items.IRON_INGOT, 4)
                        .result(RediscoveredBlocks.spikes, 16)
                        .build()
        });

        // Level 2: Apprentice
        PigmanTrades.TECHNICIAN_OFFERS.put(2, new VillagerTrades.ItemListing[]{
                new PigmanTrades.Trade.Builder()
                        .firstItem(Items.IRON_INGOT, 4).result(RediscoveredItems.ruby, 1)
                        .build(),
                new PigmanTrades.Trade.Builder()
                        .firstItem(RediscoveredItems.ruby, 5)
                        .result(Blocks.PISTON, 8)
                        .build(),
                new PigmanTrades.Trade.Builder()
                        .firstItem(RediscoveredItems.ruby, 6)
                        .result(ModRegistry.COG_BLOCK.get(), 16)
                        .build(),
                new PigmanTrades.Trade.Builder()
                        .firstItem(RediscoveredItems.ruby, 4)
                        .result(ModRegistry.PULLEY_BLOCK.get(), 8)
                        .build()
        });

        // Level 3: Journeyman
        PigmanTrades.TECHNICIAN_OFFERS.put(3, new VillagerTrades.ItemListing[]{
                new PigmanTrades.Trade.Builder()
                        .firstItem(Items.GLOWSTONE_DUST, 24).result(RediscoveredItems.ruby, 1)
                        .priceMultiplier(0.05F * 5).build(),
                new PigmanTrades.Trade.Builder()
                        .firstItem(RediscoveredItems.ruby, 8)
                        .result(ModRegistry.REDSTONE_ILLUMINATOR.get(), 4)
                        .priceMultiplier(0.05F * 3).build(),
                new PigmanTrades.Trade.Builder()
                        .firstItem(RediscoveredItems.ruby, 6)
                        .result(coraline$item("quark", "crafter"), 2)
                        .maxUses(6).build()
        });

        // Level 4: Expert
        PigmanTrades.TECHNICIAN_OFFERS.put(4, new VillagerTrades.ItemListing[]{
                new PigmanTrades.Trade.Builder()
                        .firstItem(RediscoveredItems.ruby, 1).secondItem(Items.REDSTONE, 2)
                        .result(Blocks.DISPENSER, 6)
                        .build(),
                new PigmanTrades.Trade.Builder()
                        .firstItem(RediscoveredItems.ruby, 4)
                        .result(Blocks.OBSERVER, 6)
                        .build(),
                new PigmanTrades.Trade.Builder()
                        .firstItem(RediscoveredItems.ruby, 5)
                        .result(ModRegistry.SPRING_LAUNCHER.get(), 4)
                        .maxUses(8).build(),
                new PigmanTrades.Trade.Builder()
                        .firstItem(RediscoveredItems.ruby, 3)
                        .result(ModRegistry.LOCK_BLOCK.get(), 4)
                        .maxUses(8).build()
        });

        // Level 5: Master
        PigmanTrades.TECHNICIAN_OFFERS.put(5, new VillagerTrades.ItemListing[]{
                new PigmanTrades.Trade.Builder()
                        .firstItem(RediscoveredItems.ruby, 3)
                        .result(EtceteraItems.DICE.get(), 1)
                        .priceMultiplier(0.05F * 6).build(),
                new PigmanTrades.Trade.Builder()
                        .firstItem(RediscoveredItems.ruby, 2)
                        .result(coraline$item("kubejs", "gear"), 4)
                        .maxUses(8).priceMultiplier(0.05F * 2).build(),
                new PigmanTrades.Trade.Builder()
                        .firstItem(RediscoveredItems.ruby, 6)
                        .result(CoralineItems.TIMEDUST.get(), 2)
                        .maxUses(4).priceMultiplier(0.05F * 4).build()
        });
    }

    // Tailor
    private static void overrideTailor() {
        PigmanTrades.TAILOR_OFFERS.clear();

        // Level 1: Novice
        PigmanTrades.TAILOR_OFFERS.put(1, new VillagerTrades.ItemListing[]{
                new PigmanTrades.Trade.Builder()
                        .firstItem(Items.STRING, 12).result(RediscoveredItems.ruby, 1)
                        .build(),
                new PigmanTrades.Trade.Builder()
                        .firstItem(Items.LEATHER, 8).result(RediscoveredItems.ruby, 2)
                        .build(),
                new PigmanTrades.Trade.Builder()
                        .firstItem(RediscoveredItems.ruby, 5)
                        .result(Blocks.WHITE_WOOL, 24)
                        .build(),
                new PigmanTrades.Trade.Builder()
                        .firstItem(RediscoveredItems.ruby, 2)
                        .result(RediscoveredBlocks.cyan_rose, 4)
                        .build()
        });

        // Level 2: Apprentice
        PigmanTrades.TAILOR_OFFERS.put(2, new VillagerTrades.ItemListing[]{
                new PigmanTrades.Trade.Builder()
                        .firstItem(RediscoveredItems.ruby, 1).secondItem(Blocks.WHITE_WOOL, 8)
                        .result(Items.STRING, 24)
                        .build(),
                new PigmanTrades.Trade.Builder()
                        .firstItem(RediscoveredItems.ruby, 2)
                        .result(Items.SHEARS.getDefaultInstance())
                        .build(),
                new PigmanTrades.Trade.Builder()
                        .firstItem(RediscoveredItems.ruby, 4)
                        .result(coraline$cs("sky_blue_wool"), 16)
                        .build(),
                new PigmanTrades.Trade.Builder()
                        .firstItem(RediscoveredItems.ruby, 4)
                        .result(coraline$cs("rose_wool"), 16)
                        .build()
        });

        // Level 3: Journeyman
        PigmanTrades.TAILOR_OFFERS.put(3, new VillagerTrades.ItemListing[]{
                new PigmanTrades.Trade.Builder()
                        .firstItem(RediscoveredItems.ruby, 7)
                        .result(RediscoveredItems.studded_boots, 1)
                        .priceMultiplier(0.05F * 3).build(),
                new PigmanTrades.Trade.Builder()
                        .firstItem(RediscoveredItems.ruby, 10)
                        .result(RediscoveredItems.studded_leggings, 1)
                        .priceMultiplier(0.05F * 3).build(),
                new PigmanTrades.Trade.Builder()
                        .firstItem(RediscoveredItems.ruby, 2)
                        .result(coraline$cs("lavender_dye"), 8)
                        .build(),
                new PigmanTrades.Trade.Builder()
                        .firstItem(RediscoveredItems.ruby, 2)
                        .result(coraline$cs("navy_blue_dye"), 8)
                        .build()
        });

        // Level 4: Expert
        PigmanTrades.TAILOR_OFFERS.put(4, new VillagerTrades.ItemListing[]{
                new PigmanTrades.Trade.Builder()
                        .firstItem(RediscoveredItems.ruby, 8)
                        .result(RediscoveredItems.studded_helmet, 1)
                        .priceMultiplier(0.05F * 3).build(),
                new PigmanTrades.Trade.Builder()
                        .firstItem(RediscoveredItems.ruby, 12)
                        .result(RediscoveredItems.studded_chestplate, 1)
                        .priceMultiplier(0.05F * 3).build(),
                new PigmanTrades.Trade.Builder()
                        .firstItem(RediscoveredItems.ruby, 10)
                        .result(CoralineItems.COBALT_PANTS.get(), 1)
                        .givenXP(16).maxUses(5).build(),
                new PigmanTrades.Trade.Builder()
                        .firstItem(RediscoveredItems.ruby, 3)
                        .result(coraline$cs("phtalo_stained_glass"), 8)
                        .build()
        });

        // Level 5: Master
        PigmanTrades.TAILOR_OFFERS.put(5, new VillagerTrades.ItemListing[]{
                new PigmanTrades.Trade.Builder()
                        .firstItem(Items.BONE_MEAL, 16).result(RediscoveredItems.ruby, 2)
                        .build(),
                new PigmanTrades.Trade.Builder()
                        .firstItem(RediscoveredItems.ruby, 5)
                        .result(coraline$cs("wine_wool"), 16)
                        .build(),
                new PigmanTrades.Trade.Builder()
                        .firstItem(RediscoveredItems.ruby, 5)
                        .result(coraline$cs("plum_wool"), 16)
                        .build(),
                new PigmanTrades.Trade.Builder()
                        .firstItem(RediscoveredItems.ruby, 4)
                        .result(coraline$item("etcetera", "bouquet"), 1)
                        .maxUses(6).build()
        });
    }

    // Doctor
    private static void overrideDoctor() {
        PigmanTrades.DOCTOR_OFFERS.clear();

        // Level 1: Novice
        PigmanTrades.DOCTOR_OFFERS.put(1, new VillagerTrades.ItemListing[]{
                new PigmanTrades.Trade.Builder()
                        .firstItem(Items.HONEYCOMB, 8).result(RediscoveredItems.ruby, 1)
                        .priceMultiplier(0.05F * 2).build(),
                new PigmanTrades.Trade.Builder()
                        .firstItem(RediscoveredItems.ruby, 2)
                        .result(PotionUtils.setPotion(Items.POTION.getDefaultInstance(), Potions.HEALING))
                        .maxUses(24).build(),
                new PigmanTrades.Trade.Builder()
                        .firstItem(RediscoveredItems.ruby, 3)
                        .result(PotionUtils.setPotion(Items.POTION.getDefaultInstance(), Potions.LONG_REGENERATION))
                        .maxUses(24).build(),
                new PigmanTrades.Trade.Builder()
                        .firstItem(RediscoveredItems.ruby, 1)
                        .result(coraline$item("kubejs", "glazed_steak"), 4)
                        .maxUses(16).build()
        });

        // Level 2: Apprentice
        PigmanTrades.DOCTOR_OFFERS.put(2, new VillagerTrades.ItemListing[]{
                new PigmanTrades.Trade.Builder()
                        .firstItem(Items.SPIDER_EYE, 8).result(RediscoveredItems.ruby, 3)
                        .priceMultiplier(0.05F * 2).build(),
                new PigmanTrades.Trade.Builder()
                        .firstItem(Items.GOLD_INGOT, 8).result(RediscoveredItems.ruby, 3)
                        .priceMultiplier(0.05F * 2).build(),
                new PigmanTrades.Trade.Builder()
                        .firstItem(RediscoveredItems.ruby, 10)
                        .result(Items.GHAST_TEAR, 4)
                        .build(),
                new PigmanTrades.Trade.Builder()
                        .firstItem(RediscoveredItems.ruby, 8)
                        .result(AMItemRegistry.SOUL_HEART.get(), 1)
                        .maxUses(6).build()
        });

        // Level 3: Journeyman
        PigmanTrades.DOCTOR_OFFERS.put(3, new VillagerTrades.ItemListing[]{
                new PigmanTrades.Trade.Builder()
                        .firstItem(RediscoveredItems.ruby, 3).secondItem(Items.GUNPOWDER, 1)
                        .result(PotionUtils.setPotion(Items.SPLASH_POTION.getDefaultInstance(), Potions.LUCK))
                        .maxUses(16).build(),
                new PigmanTrades.Trade.Builder()
                        .firstItem(RediscoveredItems.ruby, 2).secondItem(Items.GUNPOWDER, 1)
                        .result(PotionUtils.setPotion(Items.SPLASH_POTION.getDefaultInstance(), Potions.LONG_FIRE_RESISTANCE))
                        .maxUses(16).build(),
                new PigmanTrades.Trade.Builder()
                        .firstItem(Items.BONE, 8).result(RediscoveredItems.ruby, 1)
                        .priceMultiplier(0.05F * 2).build(),
                new PigmanTrades.Trade.Builder()
                        .firstItem(RediscoveredItems.ruby, 3)
                        .result(AMItemRegistry.BEAR_FUR.get(), 4)
                        .maxUses(10).build(),
                new PigmanTrades.Trade.Builder()
                        .firstItem(RediscoveredItems.ruby, 1)
                        .result(CoralineItems.MANGO.get(), 6)
                        .maxUses(20).build()
        });

        // Level 4: Expert
        PigmanTrades.DOCTOR_OFFERS.put(4, new VillagerTrades.ItemListing[]{
                new PigmanTrades.Trade.Builder()
                        .firstItem(RediscoveredItems.ruby, 3).secondItem(Items.GUNPOWDER, 1)
                        .result(PotionUtils.setPotion(Items.SPLASH_POTION.getDefaultInstance(), Potions.STRONG_STRENGTH))
                        .maxUses(12).build(),
                new PigmanTrades.Trade.Builder()
                        .firstItem(RediscoveredItems.ruby, 3).secondItem(Items.GUNPOWDER, 1)
                        .result(PotionUtils.setPotion(Items.SPLASH_POTION.getDefaultInstance(), Potions.LONG_SWIFTNESS))
                        .maxUses(12).build(),
                new PigmanTrades.Trade.Builder()
                        .firstItem(RediscoveredItems.ruby, 2).secondItem(Items.GUNPOWDER, 1)
                        .result(PotionUtils.setPotion(Items.SPLASH_POTION.getDefaultInstance(), Potions.LONG_NIGHT_VISION))
                        .maxUses(16).build(),
                new PigmanTrades.Trade.Builder()
                        .firstItem(RediscoveredItems.ruby, 2)
                        .result(CoralineItems.MILK_BOTTLE.get(), 3)
                        .maxUses(12).build()
        });

        // Level 5: Master
        PigmanTrades.DOCTOR_OFFERS.put(5, new VillagerTrades.ItemListing[]{
                new PigmanTrades.Trade.Builder()
                        .firstItem(RediscoveredItems.ruby, 16)
                        .result(CoralineItems.DIMENSIONAL_SHARD.get(), 8)
                        .build(),
                new PigmanTrades.Trade.Builder()
                        .firstItem(RediscoveredItems.ruby, 3)
                        .result(CoralineItems.KOMBUCHA_BOTTLE.get(), 1)
                        .maxUses(8).build(),
                new PigmanTrades.Trade.Builder()
                        .firstItem(RediscoveredItems.ruby, 2)
                        .result(CoralineItems.WALNUT.get(), 6)
                        .maxUses(12).build()
        });
    }
}