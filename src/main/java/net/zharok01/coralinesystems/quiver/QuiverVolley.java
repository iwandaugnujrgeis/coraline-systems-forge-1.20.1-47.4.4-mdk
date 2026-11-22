package net.zharok01.coralinesystems.quiver;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Skeleton;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.ArrowItem;
import net.minecraft.world.item.BowItem;
import net.minecraft.world.item.CrossbowItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.ProjectileWeaponItem;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;

public final class QuiverVolley {

    private static final float[] SIDE_ARROW_YAW_DEGREES = {-10.0F, 10.0F};
    private static final int coraline$SKELETON_SIDE_ARROW_COUNT = 2;
    private static final int DURABILITY_PER_SHOT = 1;
    private static final String QUIVER_LOADED_KEY = "CoralineQuiverLoaded";
    private static final String CHARGED_PROJECTILES_KEY = "ChargedProjectiles";

    private static LivingEntity activeShooter;
    private static ItemStack quiverArrow = ItemStack.EMPTY;
    private static AbstractArrow mainArrow;

    private QuiverVolley() {
    }

    public static void begin(Level level, LivingEntity shooter) {
        if (!level.isClientSide) {
            activeShooter = shooter;
            quiverArrow = ItemStack.EMPTY;
            mainArrow = null;
        }
    }

    public static void recordQuiverArrow(Player player, ItemStack arrow) {
        if (player == activeShooter) {
            quiverArrow = arrow.copy();
        }
    }

    public static void captureMainArrow(AbstractArrow arrow) {
        if (activeShooter != null && mainArrow == null && arrow.getOwner() == activeShooter) {
            mainArrow = arrow;
        }
    }

    public static void coraline$beginSkeletonVolley(Skeleton shooter) {
        begin(shooter.level(), shooter);
    }

    public static void coraline$finishSkeletonVolley(Skeleton shooter) {
        Level level = shooter.level();
        if (level.isClientSide) {
            return;
        }
        AbstractArrow main = mainArrow;
        activeShooter = null;
        quiverArrow = ItemStack.EMPTY;
        mainArrow = null;

        ItemStack quiver = shooter.getItemBySlot(EquipmentSlot.CHEST);
        if (main == null || !QuiverContents.isQuiver(quiver)) {
            return;
        }

        if (!(shooter.getMainHandItem().getItem() instanceof BowItem bowItem)) {
            return;
        }

        NonNullList<ItemStack> contents = QuiverContents.read(quiver);
        List<ItemStack> sideArrows = new ArrayList<>();
        while (sideArrows.size() < coraline$SKELETON_SIDE_ARROW_COUNT) {
            int slot = QuiverContents.pickRandomAmmoSlot(contents, bowItem, shooter.getRandom());
            if (slot == -1) {
                break;
            }
            ItemStack arrow = contents.get(slot).copy();
            arrow.setCount(1);
            contents.get(slot).shrink(1);
            sideArrows.add(arrow);
        }

        if (!sideArrows.isEmpty()) {
            QuiverContents.write(quiver, contents);
        }

        for (int i = 0; i < sideArrows.size(); i++) {
            ItemStack arrowStack = sideArrows.get(i);
            ArrowItem arrowItem = arrowStack.getItem() instanceof ArrowItem item ? item : (ArrowItem) Items.ARROW;
            AbstractArrow sideArrow = arrowItem.createArrow(level, arrowStack, shooter);
            sideArrow = bowItem.customArrow(sideArrow);
            sideArrow.setDeltaMovement(main.getDeltaMovement().yRot(SIDE_ARROW_YAW_DEGREES[i] * Mth.DEG_TO_RAD));
            sideArrow.setCritArrow(main.isCritArrow());
            sideArrow.pickup = main.pickup;
            level.addFreshEntity(sideArrow);
        }
    }

    public static void finish(ItemStack weapon, Level level, LivingEntity shooter, ProjectileWeaponItem weaponItem) {
        if (level.isClientSide) {
            return;
        }
        AbstractArrow main = mainArrow;
        ItemStack centerArrow = quiverArrow;
        activeShooter = null;
        quiverArrow = ItemStack.EMPTY;
        mainArrow = null;

        if (main != null && !centerArrow.isEmpty() && shooter instanceof ServerPlayer player && QuiverContents.isWornBy(player)) {
            fireSideArrows(weapon, level, player, weaponItem, main, centerArrow);
            QuiverContents.damage(player, player.getItemBySlot(EquipmentSlot.CHEST), DURABILITY_PER_SHOT);
        }
    }

    public static void finishCrossbowLoad(ItemStack crossbow, Level level, LivingEntity shooter) {
        if (level.isClientSide) {
            return;
        }
        ItemStack arrow = quiverArrow;
        activeShooter = null;
        quiverArrow = ItemStack.EMPTY;
        mainArrow = null;

        if (!CrossbowItem.isCharged(crossbow)) {
            return;
        }
        CompoundTag tag = crossbow.getTag();
        if (!arrow.isEmpty() && shooter instanceof Player player && QuiverContents.isWornBy(player)) {
            if (!player.getAbilities().instabuild) {
                ItemStack quiver = player.getItemBySlot(EquipmentSlot.CHEST);
                NonNullList<ItemStack> contents = QuiverContents.read(quiver);
                shrinkOne(contents, arrow);
                QuiverContents.write(quiver, contents);
            }
            tag.putBoolean(QUIVER_LOADED_KEY, true);
        } else {
            tag.remove(QUIVER_LOADED_KEY);
        }
    }

    public static void beginCrossbowShot(Level level, LivingEntity shooter, ItemStack crossbow) {
        begin(level, shooter);
        if (!level.isClientSide && shooter instanceof Player) {
            CompoundTag tag = crossbow.getOrCreateTag();
            if (tag.getBoolean(QUIVER_LOADED_KEY)) {
                quiverArrow = ItemStack.of(tag.getList(CHARGED_PROJECTILES_KEY, Tag.TAG_COMPOUND).getCompound(0));
            }
        }
    }

    public static void finishCrossbowShot(ItemStack crossbow, Level level, LivingEntity shooter) {
        if (level.isClientSide) {
            return;
        }
        if (shooter instanceof Player) {
            crossbow.getOrCreateTag().remove(QUIVER_LOADED_KEY);
        }
        if (EnchantmentHelper.getItemEnchantmentLevel(Enchantments.MULTISHOT, crossbow) > 0) {
            mainArrow = null;
        }
        finish(crossbow, level, shooter, (CrossbowItem) crossbow.getItem());
    }

    private static int fireSideArrows(ItemStack weapon, Level level, Player player, ProjectileWeaponItem weaponItem, AbstractArrow main, ItemStack centerArrow) {
        List<ItemStack> sideArrows = new ArrayList<>();

        if (player.getAbilities().instabuild) {
            for (int i = 0; i < SIDE_ARROW_YAW_DEGREES.length; i++) {
                sideArrows.add(centerArrow.copy());
            }
        } else {
            ItemStack quiver = player.getItemBySlot(EquipmentSlot.CHEST);
            NonNullList<ItemStack> contents = QuiverContents.read(quiver);

            if (weaponItem instanceof BowItem) {
                shrinkOne(contents, centerArrow);
            }

            while (sideArrows.size() < SIDE_ARROW_YAW_DEGREES.length) {
                int slot = QuiverContents.pickRandomAmmoSlot(contents, weaponItem, player.getRandom());
                if (slot == -1) {
                    break;
                }
                ItemStack arrow = contents.get(slot).copy();
                arrow.setCount(1);
                contents.get(slot).shrink(1);
                sideArrows.add(arrow);
            }
            QuiverContents.write(quiver, contents);
        }

        for (int i = 0; i < sideArrows.size(); i++) {
            ItemStack arrowStack = sideArrows.get(i);
            ArrowItem arrowItem = arrowStack.getItem() instanceof ArrowItem item ? item : (ArrowItem) Items.ARROW;
            AbstractArrow sideArrow = arrowItem.createArrow(level, arrowStack, player);
            if (weaponItem instanceof BowItem bowItem) {
                sideArrow = bowItem.customArrow(sideArrow);
            } else {
                sideArrow.setShotFromCrossbow(true);
                sideArrow.setSoundEvent(SoundEvents.CROSSBOW_HIT);
                sideArrow.setPierceLevel((byte) EnchantmentHelper.getItemEnchantmentLevel(Enchantments.PIERCING, weapon));
                sideArrow.pickup = main.pickup;
            }
            sideArrow.setDeltaMovement(main.getDeltaMovement().yRot(SIDE_ARROW_YAW_DEGREES[i] * Mth.DEG_TO_RAD));
            sideArrow.setCritArrow(main.isCritArrow());
            level.addFreshEntity(sideArrow);
        }
        return sideArrows.size();
    }

    private static void shrinkOne(NonNullList<ItemStack> contents, ItemStack arrow) {
        for (ItemStack stack : contents) {
            if (ItemStack.isSameItemSameTags(stack, arrow)) {
                stack.shrink(1);
                break;
            }
        }
    }
}