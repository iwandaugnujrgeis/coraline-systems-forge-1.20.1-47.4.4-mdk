package net.zharok01.coralinesystems.block;

import net.minecraft.world.Container;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * Shared interface for Basket block entities.
 *
 * <p>Ported directly from Farmer's Delight. Provides the six directional
 * collection-area shapes used for item capture, plus the world-position
 * accessors required by the item-capture helper methods.</p>
 */
public interface Basket extends Container {

    /** One collection-area shape per facing direction (indexed by {@code Direction.get3DDataValue()}). */
    VoxelShape[] COLLECTION_AREA_SHAPES = {
            Block.box(0.0D, -16.0D, 0.0D, 16.0D, 16.0D, 16.0D),  // DOWN  (0)
            Block.box(0.0D,   0.0D, 0.0D, 16.0D, 32.0D, 16.0D),  // UP    (1)
            Block.box(0.0D,   0.0D, -16.0D, 16.0D, 16.0D, 16.0D), // NORTH (2)
            Block.box(0.0D,   0.0D,  0.0D, 16.0D, 16.0D, 32.0D),  // SOUTH (3)
            Block.box(-16.0D, 0.0D,  0.0D, 16.0D, 16.0D, 16.0D),  // WEST  (4)
            Block.box(0.0D,   0.0D,  0.0D, 32.0D, 16.0D, 16.0D),  // EAST  (5)
    };

    default VoxelShape getFacingCollectionArea(int facingIndex) {
        return COLLECTION_AREA_SHAPES[facingIndex];
    }

    /** World X centre of this basket (block centre, i.e. {@code blockPos.getX() + 0.5}). */
    double getLevelX();

    /** World Y centre of this basket (block centre, i.e. {@code blockPos.getY() + 0.5}). */
    double getLevelY();

    /** World Z centre of this basket (block centre, i.e. {@code blockPos.getZ() + 0.5}). */
    double getLevelZ();
}