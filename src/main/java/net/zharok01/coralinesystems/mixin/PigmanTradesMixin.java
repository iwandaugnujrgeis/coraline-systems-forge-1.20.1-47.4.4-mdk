package net.zharok01.coralinesystems.mixin;

import com.github.alexthe666.alexsmobs.item.AMItemRegistry;
import com.legacy.rediscovered.data.RediscoveredTags;
import com.legacy.rediscovered.entity.pigman.data.PigmanTrades;
import com.legacy.rediscovered.registry.RediscoveredBlocks;
import com.legacy.rediscovered.registry.RediscoveredItems;
import com.ninni.etcetera.registry.EtceteraItems;
import com.teamabnormals.caverns_and_chasms.core.registry.CCBlocks;
import com.teamabnormals.caverns_and_chasms.core.registry.CCItems;
import net.mehvahdjukaar.supplementaries.reg.ModRegistry;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.entity.npc.VillagerTrades;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.level.block.Blocks;
import net.zharok01.coralinesystems.registry.CoralineItems;
import org.spongepowered.asm.mixin.Mixin;
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

    // Metalworker:
    
    private static void overrideMetalworker() {
        PigmanTrades.METALWORKER_OFFERS.clear();
        //TODO: Figure out how to change the POI-type block for Metalworker!
        /*
        // Level 1: Novice
        PigmanTrades.METALWORKER_OFFERS.put(1, new VillagerTrades.ItemListing[]{
                new PigmanTrades.Trade.Builder().firstItem(RediscoveredItems.ruby, 1).result(Items.CHARCOAL, 16).build(),
                new PigmanTrades.Trade.Builder().firstItem(RediscoveredItems.ruby, 3).result(Items.IRON_HOE.getDefaultInstance()).givenXP(6).build(),
                new PigmanTrades.Trade.Builder().firstItem(RediscoveredItems.ruby, 2).result(Items.IRON_SHOVEL.getDefaultInstance()).givenXP(4).build(),
                new PigmanTrades.Trade.Builder().firstItem(Items.IRON_INGOT, 4).result(RediscoveredItems.ruby, 1).givenXP(5).build()
        });

        // Level 2: Apprentice
        PigmanTrades.METALWORKER_OFFERS.put(2, new VillagerTrades.ItemListing[]{
                new PigmanTrades.Trade.Builder().firstItem(Items.GOLD_INGOT, 5).result(RediscoveredItems.ruby, 1).givenXP(13).priceMultiplier(0.05F * 2).build(),
                new PigmanTrades.Trade.Builder().firstItem(RediscoveredItems.ruby, 4).result(Items.IRON_AXE.getDefaultInstance()).givenXP(12).build(),
                new PigmanTrades.Trade.Builder().firstItem(RediscoveredItems.ruby, 5).result(Items.IRON_SWORD.getDefaultInstance()).givenXP(15).build(),
                new PigmanTrades.Trade.Builder().firstItem(RediscoveredItems.ruby, 2).result(Items.IRON_PICKAXE.getDefaultInstance()).givenXP(11).build(),
                new PigmanTrades.Trade.Builder().firstItem(RediscoveredItems.ruby, 1).result(Items.CHAINMAIL_LEGGINGS.getDefaultInstance()).givenXP(10).build(),
                new PigmanTrades.Trade.Builder().firstItem(RediscoveredItems.ruby, 3).result(Items.CHAINMAIL_BOOTS.getDefaultInstance()).givenXP(10).build()
        });

        // Level 3: Journeyman
        PigmanTrades.METALWORKER_OFFERS.put(3, new VillagerTrades.ItemListing[]{
                new PigmanTrades.Trade.Builder().firstItem(RediscoveredItems.ruby, 1).result(Items.CHAINMAIL_HELMET.getDefaultInstance()).givenXP(11).build(),
                new PigmanTrades.Trade.Builder().firstItem(RediscoveredItems.ruby, 4).result(Items.CHAINMAIL_CHESTPLATE.getDefaultInstance()).givenXP(11).build()
        });

        // Level 4: Expert
        PigmanTrades.METALWORKER_OFFERS.put(4, new VillagerTrades.ItemListing[]{
                new PigmanTrades.Trade.Builder().firstItem(RediscoveredItems.ruby, 3).result(RediscoveredItems.plate_helmet.getDefaultInstance()).givenXP(13).build(),
                new PigmanTrades.Trade.Builder().firstItem(RediscoveredItems.ruby, 5).result(RediscoveredItems.plate_boots.getDefaultInstance()).givenXP(15).build(),
        });

        // Level 5: Master
        PigmanTrades.METALWORKER_OFFERS.put(5, new VillagerTrades.ItemListing[]{
                new PigmanTrades.Trade.Builder().firstItem(RediscoveredItems.ruby, 2).result(RediscoveredItems.plate_boots.getDefaultInstance()).givenXP(15).build(),
        });
        */
    }

    // Bowyer:
    
    private static void overrideBowyer() {
        PigmanTrades.BOWYER_OFFERS.clear();

        // Level 1: Novice
        PigmanTrades.BOWYER_OFFERS.put(1, new VillagerTrades.ItemListing[]{
                new PigmanTrades.Trade.Builder().firstItem(RediscoveredItems.ruby, 4).result(Items.ARROW, 24).priceMultiplier(0.05F * 2).build(),
                new PigmanTrades.Trade.Builder().firstItem(RediscoveredItems.ruby, 2).result(Items.BOW.getDefaultInstance()).build(),
                new PigmanTrades.Trade.Builder().firstItem(Items.FLINT, 8).result(RediscoveredItems.ruby, 1).priceMultiplier(0.05F * 2).build(),
                new PigmanTrades.Trade.Builder().firstItem(Items.FEATHER, 24).result(RediscoveredItems.ruby, 4).priceMultiplier(0.05F * 2).build()
        });

        // Level 2: Apprentice
        PigmanTrades.BOWYER_OFFERS.put(2, new VillagerTrades.ItemListing[]{
                new PigmanTrades.Trade.Builder().firstItem(RediscoveredItems.ruby, 2).result(CCItems.LARGE_ARROW.get(), 8).priceMultiplier(0.05F * 2).build(),
                new PigmanTrades.Trade.Builder().firstItem(RediscoveredItems.ruby, 3).result(Items.SPECTRAL_ARROW, 16).priceMultiplier(0.05F * 2).build()
        });

        // Level 3: Journeyman
        PigmanTrades.BOWYER_OFFERS.put(3, new VillagerTrades.ItemListing[]{
                new PigmanTrades.Trade.Builder().firstItem(RediscoveredItems.ruby, 4).result(RediscoveredItems.purple_arrow, 16).priceMultiplier(0.05F * 2).build(),
                new PigmanTrades.Trade.Builder().firstItem(Items.STRING, 12).result(RediscoveredItems.ruby, 2).priceMultiplier(0.05F * 4).build(),
                new PigmanTrades.Trade.Builder().firstItem(RediscoveredItems.ruby, 4).result(Items.CROSSBOW.getDefaultInstance()).build()
        });

        // Level 4: Expert
        PigmanTrades.BOWYER_OFFERS.put(4, new VillagerTrades.ItemListing[]{
                new PigmanTrades.Trade.Builder().firstItem(RediscoveredItems.ruby, 2).result(CCItems.LARGE_ARROW.get(), 12).priceMultiplier(0.05F * 2).build(),
                new PigmanTrades.Trade.Builder().firstItem(RediscoveredItems.ruby, 2).result(Items.ARROW, 32).priceMultiplier(0.05F * 2).build(),
        });

        // Level 5: Master
        PigmanTrades.BOWYER_OFFERS.put(5, new VillagerTrades.ItemListing[]{
                new PigmanTrades.Trade.Builder().firstItem(RediscoveredItems.ruby, 2).result(Items.PHANTOM_MEMBRANE, 8).priceMultiplier(0.05F * 4).build(),
        });
    }

    // Technician:
    
    private static void overrideTechnician() {
        PigmanTrades.TECHNICIAN_OFFERS.clear();

        // Level 1: Novice
        PigmanTrades.TECHNICIAN_OFFERS.put(1, new VillagerTrades.ItemListing[]{
                new PigmanTrades.Trade.Builder().firstItem(Items.REDSTONE, 32).result(RediscoveredItems.ruby, 1).priceMultiplier(0.05F * 8).build(),
                new PigmanTrades.Trade.Builder().firstItem(RediscoveredItems.ruby, 2).secondItem(Items.IRON_INGOT, 4).result(RediscoveredBlocks.spikes, 16).build(),
                new PigmanTrades.Trade.Builder().firstItem(RediscoveredItems.ruby, 12).result(CCBlocks.STORAGE_DUCT.get(), 6).priceMultiplier(0.05F * 4).build()
        });

        // Level 2: Apprentice
        PigmanTrades.TECHNICIAN_OFFERS.put(2, new VillagerTrades.ItemListing[]{
                new PigmanTrades.Trade.Builder().firstItem(RediscoveredItems.ruby, 2).result(EtceteraItems.DICE.get(), 1).priceMultiplier(0.05F * 8).build(),
                new PigmanTrades.Trade.Builder().firstItem(RediscoveredItems.ruby, 10).result(ModRegistry.COG_BLOCK.get(), 32).build(),
                new PigmanTrades.Trade.Builder().firstItem(RediscoveredItems.ruby, 10).result(Blocks.PISTON, 16).build(),
                new PigmanTrades.Trade.Builder().firstItem(Items.IRON_INGOT, 4).result(RediscoveredItems.ruby, 1).build()
        });

        // Level 3: Journeyman
        PigmanTrades.TECHNICIAN_OFFERS.put(3, new VillagerTrades.ItemListing[]{
                new PigmanTrades.Trade.Builder().firstItem(Items.QUARTZ, 16).result(RediscoveredItems.ruby, 4).priceMultiplier(0.05F * 2).build()
        });

        // Level 4: Expert
        PigmanTrades.TECHNICIAN_OFFERS.put(4, new VillagerTrades.ItemListing[]{
                new PigmanTrades.Trade.Builder().firstItem(RediscoveredItems.ruby, 1).secondItem(Items.REDSTONE, 2).result(Blocks.DISPENSER, 6).build(),
                new PigmanTrades.Trade.Builder().firstItem(Items.GLOWSTONE_DUST, 24).result(RediscoveredItems.ruby, 1).priceMultiplier(0.05F * 6).build()
        });

        // Level 5: Master
        PigmanTrades.TECHNICIAN_OFFERS.put(5, new VillagerTrades.ItemListing[]{
                new PigmanTrades.Trade.Builder().firstItem(RediscoveredItems.ruby, 1).result(Items.SLIME_BALL, 8).maxUses(10).build()
        });
    }

    // Tailor:
    
    private static void overrideTailor() {
        PigmanTrades.TAILOR_OFFERS.clear();

        // Level 1: Novice
        PigmanTrades.TAILOR_OFFERS.put(1, new VillagerTrades.ItemListing[]{
                new PigmanTrades.Trade.Builder().firstItem(RediscoveredItems.ruby, 6).result(Blocks.WHITE_WOOL, 32).build(),
                new PigmanTrades.Trade.Builder().firstItem(RediscoveredItems.ruby, 2).result(RediscoveredBlocks.cyan_rose, 6).build(),
                new PigmanTrades.Trade.Builder().firstItem(Items.STRING, 12).result(RediscoveredItems.ruby, 1).build(),
                new PigmanTrades.Trade.Builder().firstItem(EtceteraItems.COTTON_FLOWER.get(), 16).result(RediscoveredItems.ruby, 3).build(),
                new PigmanTrades.Trade.Builder().firstItem(Items.LEATHER, 8).result(RediscoveredItems.ruby, 2).build()
        });

        // Level 2: Apprentice
        PigmanTrades.TAILOR_OFFERS.put(2, new VillagerTrades.ItemListing[]{
                new PigmanTrades.Trade.Builder().firstItem(RediscoveredItems.ruby, 1).secondItem(Blocks.WHITE_WOOL, 8).result(Items.STRING, 8 * 4).build(),
                new PigmanTrades.Trade.Builder().firstItem(RediscoveredItems.ruby, 2).result(Items.SHEARS.getDefaultInstance()).build(),
                new PigmanTrades.Trade.Builder().firstItem(RediscoveredItems.ruby, 8).result(RediscoveredBlocks.spring_green_wool, 48).build(),
                new PigmanTrades.Trade.Builder().firstItem(RediscoveredItems.ruby, 8).result(RediscoveredBlocks.bright_green_wool, 48).build()
        });

        // Level 3: Journeyman
        PigmanTrades.TAILOR_OFFERS.put(3, new VillagerTrades.ItemListing[]{
                new PigmanTrades.Trade.Builder().firstItem(RediscoveredItems.ruby, 8).result(RediscoveredBlocks.lavender_wool, 48).build(),
                new PigmanTrades.Trade.Builder().firstItem(RediscoveredItems.ruby, 8).result(RediscoveredBlocks.rose_wool, 48).build(),
                new PigmanTrades.Trade.Builder().firstItem(RediscoveredItems.ruby, 7).result(RediscoveredItems.studded_boots, 1).priceMultiplier(0.05F * 3).build(),
                new PigmanTrades.Trade.Builder().firstItem(RediscoveredItems.ruby, 10).result(RediscoveredItems.studded_leggings, 1).priceMultiplier(0.05F * 3).build()
        });

        // Level 4: Expert
        PigmanTrades.TAILOR_OFFERS.put(4, new VillagerTrades.ItemListing[]{
                new PigmanTrades.Trade.Builder().firstItem(RediscoveredItems.ruby, 1).secondItem(Blocks.WHITE_WOOL, 8).result(RediscoveredBlocks.sky_blue_wool, 8).build(),
                new PigmanTrades.Trade.Builder().firstItem(RediscoveredItems.ruby, 1).secondItem(Blocks.WHITE_WOOL, 8).result(RediscoveredBlocks.slate_blue_wool, 8).build(),
                new PigmanTrades.Trade.Builder().firstItem(RediscoveredItems.ruby, 12).result(RediscoveredItems.studded_chestplate, 1).priceMultiplier(0.05F * 3).build(),
                new PigmanTrades.Trade.Builder().firstItem(RediscoveredItems.ruby, 8).result(RediscoveredItems.studded_helmet, 1).priceMultiplier(0.05F * 3).build()
        });

        // Level 5: Master
        PigmanTrades.TAILOR_OFFERS.put(5, new VillagerTrades.ItemListing[]{
                new PigmanTrades.Trade.Builder().firstItem(Items.BONE_MEAL, 16).result(RediscoveredItems.ruby, 2).build()
        });
    }

    // Doctor:
    
    private static void overrideDoctor() {
        PigmanTrades.DOCTOR_OFFERS.clear();

        // Level 1: Novice
        PigmanTrades.DOCTOR_OFFERS.put(1, new VillagerTrades.ItemListing[]{
                new PigmanTrades.Trade.Builder().firstItem(RediscoveredItems.ruby, 2).result(PotionUtils.setPotion(Items.POTION.getDefaultInstance(), Potions.HEALING)).maxUses(24).build(),
                new PigmanTrades.Trade.Builder().firstItem(RediscoveredItems.ruby, 3).result(PotionUtils.setPotion(Items.POTION.getDefaultInstance(), Potions.LONG_REGENERATION)).maxUses(24).build(),
                new PigmanTrades.Trade.Builder().firstItem(Items.NETHER_WART, 16).result(RediscoveredItems.ruby, 4).priceMultiplier(0.05F * 3).build(),
                new PigmanTrades.Trade.Builder().firstItem(Items.HONEYCOMB, 8).result(RediscoveredItems.ruby, 1).priceMultiplier(0.05F * 2).build()
        });

        // Level 2: Apprentice
        PigmanTrades.DOCTOR_OFFERS.put(2, new VillagerTrades.ItemListing[]{
                new PigmanTrades.Trade.Builder().firstItem(RediscoveredItems.ruby, 12).result(Items.GHAST_TEAR, 6).build(),
                new PigmanTrades.Trade.Builder().firstItem(Items.SPIDER_EYE, 8).result(RediscoveredItems.ruby, 3).priceMultiplier(0.05F * 2).build(),
                new PigmanTrades.Trade.Builder().firstItem(Items.GOLD_INGOT, 8).result(RediscoveredItems.ruby, 3).priceMultiplier(0.05F * 2).build()
        });

        // Level 3: Journeyman
        PigmanTrades.DOCTOR_OFFERS.put(3, new VillagerTrades.ItemListing[]{
                new PigmanTrades.Trade.Builder().firstItem(RediscoveredItems.ruby, 3).secondItem(Items.GUNPOWDER, 1).result(PotionUtils.setPotion(Items.SPLASH_POTION.getDefaultInstance(), Potions.LUCK)).maxUses(16).build(),
                new PigmanTrades.Trade.Builder().firstItem(RediscoveredItems.ruby, 12).result(Items.DRAGON_BREATH, 1).priceMultiplier(0.05F * 2).build()
        });

        // Level 4: Expert
        PigmanTrades.DOCTOR_OFFERS.put(4, new VillagerTrades.ItemListing[]{
                new PigmanTrades.Trade.Builder().firstItem(RediscoveredItems.ruby, 10).result(Items.BLAZE_ROD, 16).priceMultiplier(0.05F * 2).build(),
                new PigmanTrades.Trade.Builder().firstItem(RediscoveredItems.ruby, 3).secondItem(Items.GUNPOWDER, 1).result(PotionUtils.setPotion(Items.SPLASH_POTION.getDefaultInstance(), Potions.STRONG_STRENGTH)).maxUses(12).build(),
                new PigmanTrades.Trade.Builder().firstItem(RediscoveredItems.ruby, 3).secondItem(Items.GUNPOWDER, 1).result(PotionUtils.setPotion(Items.SPLASH_POTION.getDefaultInstance(), Potions.LONG_SWIFTNESS)).maxUses(12).build(),
                new PigmanTrades.Trade.Builder().firstItem(RediscoveredItems.ruby, 2).secondItem(Items.GUNPOWDER, 1).result(PotionUtils.setPotion(Items.SPLASH_POTION.getDefaultInstance(), Potions.LONG_NIGHT_VISION)).maxUses(16).build()
        });

        // Level 5: Master
        PigmanTrades.DOCTOR_OFFERS.put(5, new VillagerTrades.ItemListing[]{
                new PigmanTrades.Trade.Builder().firstItem(RediscoveredItems.ruby, 16).result(CoralineItems.DIMENSIONAL_SHARD.get(), 8).build(),
                new PigmanTrades.Trade.Builder().firstItem(RediscoveredItems.ruby, 8).result(AMItemRegistry.BEAR_DUST.get(), 4).build()
        });
    }
}