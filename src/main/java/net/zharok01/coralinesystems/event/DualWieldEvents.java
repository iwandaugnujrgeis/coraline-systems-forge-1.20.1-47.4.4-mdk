package net.zharok01.coralinesystems.event;

import java.util.UUID;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.AxeItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.PickaxeItem;
import net.minecraft.world.item.ShovelItem;
import net.minecraft.world.item.SwordItem;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.ForgeEventFactory;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.zharok01.coralinesystems.CoralineSystems;

@Mod.EventBusSubscriber(modid = CoralineSystems.MOD_ID)
public class DualWieldEvents {
    private static final UUID DUAL_WIELD_ATTACK_DAMAGE_ID = UUID.fromString("5b0e7c1a-3d7f-4e0b-9a52-6c1f8d2e4a90");

    public static boolean coraline$isDualWielding(Player player) {
        Item mainHand = player.getMainHandItem().getItem();
        Item offHand = player.getOffhandItem().getItem();
        return mainHand instanceof PickaxeItem && offHand instanceof PickaxeItem
                || mainHand instanceof AxeItem && offHand instanceof AxeItem
                || mainHand instanceof ShovelItem && offHand instanceof ShovelItem
                || mainHand instanceof SwordItem && offHand instanceof SwordItem;
    }

    public static boolean coraline$isDualWieldingSwords(Player player) {
        return player.getMainHandItem().getItem() instanceof SwordItem
                && player.getOffhandItem().getItem() instanceof SwordItem;
    }

    public static void coraline$damageOffhand(Player player, int amount) {
        coraline$damageItemInHand(player, InteractionHand.OFF_HAND, amount);
    }

    public static void coraline$damageItemInHand(Player player, InteractionHand hand, int amount) {
        ItemStack itemStack = player.getItemInHand(hand);
        ItemStack original = itemStack.copy();
        itemStack.hurtAndBreak(amount, player, broken -> broken.broadcastBreakEvent(hand));
        if (itemStack.isEmpty()) {
            ForgeEventFactory.onPlayerDestroyItem(player, original, hand);
            if (player.getUseItem() == itemStack) {
                player.stopUsingItem();
            }
            player.setItemInHand(hand, ItemStack.EMPTY);
        }
    }

    @SubscribeEvent
    public static void coraline$onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END || event.player.level().isClientSide) {
            return;
        }
        Player player = event.player;
        AttributeInstance attackDamage = player.getAttribute(Attributes.ATTACK_DAMAGE);
        double offhandAttackDamage = coraline$isDualWielding(player)
                ? coraline$getOffhandAttackDamage(player.getOffhandItem()) : 0.0D;
        AttributeModifier currentBonus = attackDamage.getModifier(DUAL_WIELD_ATTACK_DAMAGE_ID);
        if (offhandAttackDamage == 0.0D) {
            if (currentBonus != null) {
                attackDamage.removeModifier(DUAL_WIELD_ATTACK_DAMAGE_ID);
            }
        } else if (currentBonus == null || currentBonus.getAmount() != offhandAttackDamage) {
            if (currentBonus != null) {
                attackDamage.removeModifier(DUAL_WIELD_ATTACK_DAMAGE_ID);
            }
            attackDamage.addTransientModifier(new AttributeModifier(DUAL_WIELD_ATTACK_DAMAGE_ID,
                    "Dual wield attack damage", offhandAttackDamage, AttributeModifier.Operation.ADDITION));
        }
    }

    private static double coraline$getOffhandAttackDamage(ItemStack itemStack) {
        double attackDamage = 0.0D;
        for (AttributeModifier modifier : itemStack.getAttributeModifiers(EquipmentSlot.MAINHAND)
                .get(Attributes.ATTACK_DAMAGE)) {
            if (modifier.getOperation() == AttributeModifier.Operation.ADDITION) {
                attackDamage += modifier.getAmount();
            }
        }
        return attackDamage;
    }
}
