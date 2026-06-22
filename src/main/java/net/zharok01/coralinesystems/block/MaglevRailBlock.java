package net.zharok01.coralinesystems.block;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.vehicle.AbstractMinecart;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseRailBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.block.state.properties.RailShape;
import org.jetbrains.annotations.NotNull;

/**
 * Maglev Rail — a straight-only rail that allows carts to travel at twice
 * vanilla's maximum speed (0.8 m/tick vs 0.4 m/tick).
 *
 * The speed is advertised via {@link #getRailMaxSpeed}, which is the Forge hook
 * consumed by {@code AbstractMinecart#getMaxSpeedWithRail()}. A companion Mixin
 * on {@code AbstractMinecart} raises the per-cart ceiling to match whenever the
 * cart sits on a maglev rail; without that, {@code setCurrentCartSpeedCapOnRail}
 * would silently clamp the value back to 0.4.
 *
 * Like {@code PoweredRailBlock} this uses {@code isStraight = true}, meaning it
 * only accepts the six straight/ascending shapes — no curves.
 */
public class MaglevRailBlock extends BaseRailBlock {

    /** Straight-only shape set, matching PoweredRailBlock / CopperRailBlock. */
    public static final EnumProperty<RailShape> SHAPE = BlockStateProperties.RAIL_SHAPE_STRAIGHT;

    /** Double vanilla's 0.4 f/tick rail cap. */
    public static final float MAGLEV_MAX_SPEED = 0.8f;

    public MaglevRailBlock(BlockBehaviour.Properties properties) {
        super(true, properties);
        this.registerDefaultState(
                this.stateDefinition.any()
                        .setValue(SHAPE, RailShape.NORTH_SOUTH)
                        .setValue(WATERLOGGED, false)
        );
    }

    // -------------------------------------------------------------------------
    // Forge speed hook
    // -------------------------------------------------------------------------

    @Override
    public float getRailMaxSpeed(BlockState state, Level level, BlockPos pos, AbstractMinecart cart) {
        return MAGLEV_MAX_SPEED;
    }

    // -------------------------------------------------------------------------
    // Shape property (required abstract)
    // -------------------------------------------------------------------------

    @Override
    public @NotNull Property<RailShape> getShapeProperty() {
        return SHAPE;
    }

    // -------------------------------------------------------------------------
    // State definition
    // -------------------------------------------------------------------------

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(SHAPE, WATERLOGGED);
    }

    // -------------------------------------------------------------------------
    // Rotate / mirror  (straight-only shapes only — same mapping as PoweredRailBlock)
    // -------------------------------------------------------------------------

    @Override
    public @NotNull BlockState rotate(@NotNull BlockState state, Rotation rotation) {
        return switch (rotation) {
            case CLOCKWISE_180 -> switch (state.getValue(SHAPE)) {
                case ASCENDING_EAST  -> state.setValue(SHAPE, RailShape.ASCENDING_WEST);
                case ASCENDING_WEST  -> state.setValue(SHAPE, RailShape.ASCENDING_EAST);
                case ASCENDING_NORTH -> state.setValue(SHAPE, RailShape.ASCENDING_SOUTH);
                case ASCENDING_SOUTH -> state.setValue(SHAPE, RailShape.ASCENDING_NORTH);
                default              -> state; // NORTH_SOUTH, EAST_WEST unchanged
            };
            case COUNTERCLOCKWISE_90 -> switch (state.getValue(SHAPE)) {
                case NORTH_SOUTH     -> state.setValue(SHAPE, RailShape.EAST_WEST);
                case EAST_WEST       -> state.setValue(SHAPE, RailShape.NORTH_SOUTH);
                case ASCENDING_EAST  -> state.setValue(SHAPE, RailShape.ASCENDING_NORTH);
                case ASCENDING_WEST  -> state.setValue(SHAPE, RailShape.ASCENDING_SOUTH);
                case ASCENDING_NORTH -> state.setValue(SHAPE, RailShape.ASCENDING_WEST);
                case ASCENDING_SOUTH -> state.setValue(SHAPE, RailShape.ASCENDING_EAST);
                default              -> state;
            };
            case CLOCKWISE_90 -> switch (state.getValue(SHAPE)) {
                case NORTH_SOUTH     -> state.setValue(SHAPE, RailShape.EAST_WEST);
                case EAST_WEST       -> state.setValue(SHAPE, RailShape.NORTH_SOUTH);
                case ASCENDING_EAST  -> state.setValue(SHAPE, RailShape.ASCENDING_SOUTH);
                case ASCENDING_WEST  -> state.setValue(SHAPE, RailShape.ASCENDING_NORTH);
                case ASCENDING_NORTH -> state.setValue(SHAPE, RailShape.ASCENDING_EAST);
                case ASCENDING_SOUTH -> state.setValue(SHAPE, RailShape.ASCENDING_WEST);
                default              -> state;
            };
            default -> state;
        };
    }

    @Override
    public @NotNull BlockState mirror(BlockState state, Mirror mirror) {
        RailShape shape = state.getValue(SHAPE);
        return switch (mirror) {
            case LEFT_RIGHT -> switch (shape) {
                case ASCENDING_NORTH -> state.setValue(SHAPE, RailShape.ASCENDING_SOUTH);
                case ASCENDING_SOUTH -> state.setValue(SHAPE, RailShape.ASCENDING_NORTH);
                default              -> super.mirror(state, mirror);
            };
            case FRONT_BACK -> switch (shape) {
                case ASCENDING_EAST -> state.setValue(SHAPE, RailShape.ASCENDING_WEST);
                case ASCENDING_WEST -> state.setValue(SHAPE, RailShape.ASCENDING_EAST);
                default             -> super.mirror(state, mirror);
            };
            default -> super.mirror(state, mirror);
        };
    }
}