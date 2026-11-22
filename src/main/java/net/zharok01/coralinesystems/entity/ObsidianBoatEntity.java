package net.zharok01.coralinesystems.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.vehicle.Boat;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.fluids.FluidType;
import net.zharok01.coralinesystems.mixin.accessor.BoatAccessor;
import net.zharok01.coralinesystems.registry.CoralineItems;
import net.zharok01.coralinesystems.registry.IsotopicEntities;
import org.jetbrains.annotations.NotNull;

public class ObsidianBoatEntity extends Boat {

    public ObsidianBoatEntity(EntityType<? extends Boat> entityType, Level level) {
        super(entityType, level);
        this.blocksBuilding = true;
    }

    public ObsidianBoatEntity(Level level, double x, double y, double z) {
        this(IsotopicEntities.OBSIDIAN_BOAT.get(), level);
        this.setPos(x, y, z);
        this.setDeltaMovement(Vec3.ZERO);
        this.xo = x;
        this.yo = y;
        this.zo = z;
    }

    @Override
    public boolean canBoatInFluid(FluidState state) {
        return state.is(FluidTags.LAVA);
    }

    @Override
    public boolean canBoatInFluid(FluidType type) {
        return type == net.minecraftforge.common.ForgeMod.LAVA_TYPE.get();
    }

    @Override
    protected void checkFallDamage(double y, boolean onGround, @NotNull BlockState state, @NotNull BlockPos pos) {
        ((BoatAccessor) this).coraline$setLastYd(this.getDeltaMovement().y);
    }

    @Override
    public boolean isOnFire() {
        return false;
    }

    @Override
    public boolean fireImmune() {
        return true;
    }

    @Override
    public @NotNull Item getDropItem() {
        return CoralineItems.OBSIDIAN_BOAT.get();
    }

    @Override
    public @NotNull ItemStack getPickResult() {
        return new ItemStack(this.getDropItem());
    }

    @Override
    public @NotNull Type getVariant() {
        return Type.OAK;
    }
}