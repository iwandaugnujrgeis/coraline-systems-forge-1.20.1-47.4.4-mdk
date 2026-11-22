package net.zharok01.coralinesystems.quiver;

import com.legacy.rediscovered.data.RediscoveredTags;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ProjectileWeaponItem;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.zharok01.coralinesystems.util.helper.BagBreakHelper;

public final class QuiverContents {
    public static final int SLOT_COUNT = 9;
    private static final String DATA_KEY = "rediscovered_quiver_data";
    private static final String ITEMS_KEY = "Items";
    private static final String HIDE_FLAGS_KEY = "HideFlags";

    public static final int MAX_DURABILITY = 200;

    private QuiverContents() {
    }

    public static boolean isQuiver(ItemStack stack) {
        return stack.is(RediscoveredTags.Items.QUIVERS);
    }

    public static boolean isWornBy(Player player) {
        return isQuiver(player.getItemBySlot(EquipmentSlot.CHEST));
    }

    public static boolean hasArrows(ItemStack quiver) {
        CompoundTag data = quiver.getTagElement(DATA_KEY);
        return data != null && !data.getList(ITEMS_KEY, Tag.TAG_COMPOUND).isEmpty();
    }

    public static NonNullList<ItemStack> read(ItemStack quiver) {
        NonNullList<ItemStack> contents = NonNullList.withSize(SLOT_COUNT, ItemStack.EMPTY);
        CompoundTag data = quiver.getTagElement(DATA_KEY);
        if (data != null) {
            ContainerHelper.loadAllItems(data, contents);
        }
        return contents;
    }

    public static void write(ItemStack quiver, List<ItemStack> contents) {
        NonNullList<ItemStack> copy = NonNullList.withSize(SLOT_COUNT, ItemStack.EMPTY);
        for (int slot = 0; slot < SLOT_COUNT; slot++) {
            copy.set(slot, contents.get(slot));
        }
        ContainerHelper.saveAllItems(quiver.getOrCreateTagElement(DATA_KEY), copy);
        syncLock(quiver);
    }

    public static List<ItemStack> removeAll(ItemStack quiver) {
        List<ItemStack> arrows = new ArrayList<>();
        for (ItemStack stack : read(quiver)) {
            if (!stack.isEmpty()) {
                arrows.add(stack);
            }
        }
        write(quiver, NonNullList.withSize(SLOT_COUNT, ItemStack.EMPTY));
        return arrows;
    }

    public static void syncLock(ItemStack quiver) {
        Map<Enchantment, Integer> enchantments = EnchantmentHelper.getEnchantments(quiver);
        boolean locked = enchantments.containsKey(Enchantments.BINDING_CURSE);
        boolean hasArrows = hasArrows(quiver);
        if (hasArrows && !locked) {
            enchantments.put(Enchantments.BINDING_CURSE, 1);
            EnchantmentHelper.setEnchantments(enchantments, quiver);
            quiver.hideTooltipPart(ItemStack.TooltipPart.ENCHANTMENTS);
        } else if (!hasArrows && locked) {
            enchantments.remove(Enchantments.BINDING_CURSE);
            EnchantmentHelper.setEnchantments(enchantments, quiver);
            quiver.getOrCreateTag().remove(HIDE_FLAGS_KEY);
        }
    }

    public static void damage(ServerPlayer player, ItemStack quiver, int amount) {
        if (player.getAbilities().instabuild) {
            return;
        }
        int newDamage = quiver.getDamageValue() + amount;
        if (newDamage >= MAX_DURABILITY) {
            BagBreakHelper.destroy(player, quiver, removeAll(quiver));
        } else {
            quiver.setDamageValue(newDamage);
        }
    }

    public static int pickRandomAmmoSlot(NonNullList<ItemStack> contents, ProjectileWeaponItem weapon, RandomSource random) {
        List<Integer> candidates = new ArrayList<>();
        for (int slot = 0; slot < contents.size(); slot++) {
            ItemStack stack = contents.get(slot);
            if (stack.is(RediscoveredTags.Items.QUIVER_AMMO) && weapon.getAllSupportedProjectiles().test(stack)) {
                candidates.add(slot);
            }
        }
        return candidates.isEmpty() ? -1 : candidates.get(random.nextInt(candidates.size()));
    }
}