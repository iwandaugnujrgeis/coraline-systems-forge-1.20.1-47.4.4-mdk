package net.zharok01.coralinesystems.event.static_portal;

import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.saveddata.SavedData;
import net.zharok01.coralinesystems.registry.CoralineBlocks;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class StaticPortalLinkData extends SavedData {

    private static final String DATA_NAME = "coraline_portal_links";
    private static final String TAG_DIMENSION = "Dim";

    private final Map<GlobalPos, BlockPos> links = new HashMap<>();
    // ASSUMPTION: Legacy coordinate-only links are claimed by the first dimension with a portal at that position.
    private final Map<BlockPos, BlockPos> coraline$legacyLinks = new HashMap<>();

    public BlockPos getLinkedDestination(ServerLevel level, BlockPos portalBlock) {
        BlockPos destination = links.get(GlobalPos.of(level.dimension(), portalBlock));
        if (destination == null
                && coraline$legacyLinks.containsKey(portalBlock)
                && level.getBlockState(portalBlock).is(CoralineBlocks.STATIC_PORTAL_BLOCK.get())) {
            destination = coraline$claimLegacy(level.dimension(), portalBlock);
        }
        return destination;
    }

    public void linkPortals(ResourceKey<Level> dimension, Collection<BlockPos> groupA, BlockPos spawnForA, Collection<BlockPos> groupB, BlockPos spawnForB) {
        for (BlockPos pos : groupA) unlinkPair(dimension, pos);
        for (BlockPos pos : groupB) unlinkPair(dimension, pos);

        for (BlockPos pos : groupA) links.put(GlobalPos.of(dimension, pos.immutable()), spawnForB.immutable());
        for (BlockPos pos : groupB) links.put(GlobalPos.of(dimension, pos.immutable()), spawnForA.immutable());
        setDirty();
    }

    public void unlinkPair(ResourceKey<Level> dimension, BlockPos portalBlock) {
        BlockPos destination = links.get(GlobalPos.of(dimension, portalBlock));
        if (destination == null) {
            destination = coraline$claimLegacy(dimension, portalBlock);
        }
        if (destination == null) return;

        BlockPos returnSpawn = links.get(GlobalPos.of(dimension, destination));
        if (returnSpawn == null) {
            returnSpawn = coraline$claimLegacy(dimension, destination);
        }

        coraline$unlinkAllPointingTo(dimension, destination);
        if (returnSpawn != null) {
            coraline$unlinkAllPointingTo(dimension, returnSpawn);
        }
        setDirty();
    }

    private void coraline$unlinkAllPointingTo(ResourceKey<Level> dimension, BlockPos destination) {
        links.entrySet().removeIf(entry -> entry.getKey().dimension().equals(dimension) && entry.getValue().equals(destination));
    }

    private BlockPos coraline$claimLegacy(ResourceKey<Level> dimension, BlockPos portalBlock) {
        BlockPos destination = coraline$legacyLinks.remove(portalBlock);
        if (destination != null) {
            links.put(GlobalPos.of(dimension, portalBlock.immutable()), destination);
            setDirty();
        }
        return destination;
    }

    @Override
    public @NotNull CompoundTag save(@NotNull CompoundTag tag) {
        ListTag list = new ListTag();
        for (Map.Entry<GlobalPos, BlockPos> entry : links.entrySet()) {
            CompoundTag pair = coraline$writePair(entry.getKey().pos(), entry.getValue());
            pair.putString(TAG_DIMENSION, entry.getKey().dimension().location().toString());
            list.add(pair);
        }
        for (Map.Entry<BlockPos, BlockPos> entry : coraline$legacyLinks.entrySet()) {
            list.add(coraline$writePair(entry.getKey(), entry.getValue()));
        }
        tag.put("Links", list);
        return tag;
    }

    private static CompoundTag coraline$writePair(BlockPos source, BlockPos destination) {
        CompoundTag pair = new CompoundTag();
        pair.putInt("SX", source.getX());
        pair.putInt("SY", source.getY());
        pair.putInt("SZ", source.getZ());
        pair.putInt("DX", destination.getX());
        pair.putInt("DY", destination.getY());
        pair.putInt("DZ", destination.getZ());
        return pair;
    }

    public static StaticPortalLinkData load(CompoundTag tag) {
        StaticPortalLinkData data = new StaticPortalLinkData();
        ListTag list = tag.getList("Links", Tag.TAG_COMPOUND);
        for (int i = 0; i < list.size(); i++) {
            CompoundTag pair = list.getCompound(i);
            BlockPos source = new BlockPos(pair.getInt("SX"), pair.getInt("SY"), pair.getInt("SZ"));
            BlockPos destination = new BlockPos(pair.getInt("DX"), pair.getInt("DY"), pair.getInt("DZ"));
            if (pair.contains(TAG_DIMENSION, Tag.TAG_STRING)) {
                ResourceKey<Level> dimension = ResourceKey.create(Registries.DIMENSION, new ResourceLocation(pair.getString(TAG_DIMENSION)));
                data.links.put(GlobalPos.of(dimension, source), destination);
            } else {
                data.coraline$legacyLinks.put(source, destination);
            }
        }
        return data;
    }

    public static StaticPortalLinkData get(ServerLevel level) {
        return level.getServer().overworld().getDataStorage().computeIfAbsent(StaticPortalLinkData::load, StaticPortalLinkData::new, DATA_NAME);
    }
}
