package com.majorbonghits.moderncompanions.world;

import com.majorbonghits.moderncompanions.Constants;
import com.majorbonghits.moderncompanions.core.ModEntityTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.levelgen.structure.StructureStart;
import net.minecraft.world.level.saveddata.SavedData;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.level.ChunkEvent;

import java.util.HashSet;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;

/**
 * Spawns a matching companion when one of our structures is generated.
 * This avoids relying on NBT-embedded entities and prevents duplicates via a SavedData guard.
 */
@EventBusSubscriber(modid = Constants.MOD_ID, bus = EventBusSubscriber.Bus.GAME)
public final class StructureCompanionSpawner {
    private StructureCompanionSpawner() {}

    /** Map structure id -> companion entity type. */
    private static final Map<ResourceLocation, Supplier<? extends EntityType<? extends PathfinderMob>>> STRUCTURE_TO_ENTITY = Map.ofEntries(
            Map.entry(Constants.id("alchemist_house"), ModEntityTypes.ALCHEMIST),
            Map.entry(Constants.id("beastmaster_house"), ModEntityTypes.BEASTMASTER),
            Map.entry(Constants.id("berserker_house"), ModEntityTypes.BERSERKER),
            Map.entry(Constants.id("cleric_house"), ModEntityTypes.CLERIC),
            Map.entry(Constants.id("scout_house"), ModEntityTypes.SCOUT),
            Map.entry(Constants.id("stormcaller_house"), ModEntityTypes.STORMCALLER),
            Map.entry(Constants.id("vanguard_house"), ModEntityTypes.VANGUARD),
            Map.entry(Constants.id("smith"), ModEntityTypes.VANGUARD),
            Map.entry(Constants.id("house"), ModEntityTypes.KNIGHT),
            Map.entry(Constants.id("largehouse"), ModEntityTypes.ARCHER),
            Map.entry(Constants.id("largehouse2"), ModEntityTypes.AXEGUARD),
            Map.entry(Constants.id("largehouse3"), ModEntityTypes.BERSERKER),
            Map.entry(Constants.id("lumber"), ModEntityTypes.ARBALIST),
            Map.entry(Constants.id("tower1"), ModEntityTypes.ALCHEMIST),
            Map.entry(Constants.id("tower2"), ModEntityTypes.SCOUT),
            Map.entry(Constants.id("watermill"), ModEntityTypes.BEASTMASTER),
            Map.entry(Constants.id("windmill"), ModEntityTypes.STORMCALLER),
            Map.entry(Constants.id("church"), ModEntityTypes.CLERIC),
            // Biome-themed house variants (default to Knight so every house gets a resident)
            Map.entry(Constants.id("oak_house"), ModEntityTypes.KNIGHT),
            Map.entry(Constants.id("oak_birch_house"), ModEntityTypes.SCOUT),
            Map.entry(Constants.id("birch_house"), ModEntityTypes.KNIGHT),
            Map.entry(Constants.id("acacia_house"), ModEntityTypes.ARCHER),
            Map.entry(Constants.id("spruce_house"), ModEntityTypes.BEASTMASTER),
            Map.entry(Constants.id("dark_oak_house"), ModEntityTypes.AXEGUARD),
            Map.entry(Constants.id("sandstone_house"), ModEntityTypes.KNIGHT),
            Map.entry(Constants.id("terracotta_house"), ModEntityTypes.ARBALIST)
    );

    @SubscribeEvent
    public static void onChunkLoad(ChunkEvent.Load event) {
        if (!(event.getLevel() instanceof ServerLevel serverLevel)) return;
        ChunkAccess chunk = event.getChunk();

        // Collect work then run on the main server thread (chunk load may be off-thread).
        List<SpawnRequest> pending = new ArrayList<>();

        chunk.getAllStarts().forEach((structure, start) -> {
            if (!start.isValid()) return;
            ResourceLocation id = serverLevel.registryAccess()
                    .registryOrThrow(Registries.STRUCTURE)
                    .getKey(structure);
            if (id == null || !STRUCTURE_TO_ENTITY.containsKey(id)) return;

            BlockPos center = start.getBoundingBox().getCenter();
            String key = id + "|" + center.getX() + "," + center.getY() + "," + center.getZ();
            pending.add(new SpawnRequest(center, key, STRUCTURE_TO_ENTITY.get(id)));
        });

        if (pending.isEmpty()) return;

        serverLevel.getServer().execute(() -> {
            StructureSpawnTracker tracker = StructureSpawnTracker.get(serverLevel);
            for (SpawnRequest req : pending) {
                if (!tracker.markIfNew(req.key())) continue;
                EntityType<? extends PathfinderMob> type = req.typeSupplier().get();
                type.spawn(serverLevel, req.center(), MobSpawnType.STRUCTURE);
            }
        });
    }

    private record SpawnRequest(BlockPos center, String key,
                                Supplier<? extends EntityType<? extends PathfinderMob>> typeSupplier) {}

    /**
     * SavedData to remember which structure placements already spawned a companion.
     */
    private static final class StructureSpawnTracker extends SavedData {
        private static final String DATA_NAME = Constants.MOD_ID + "_structure_spawns";
        private final Set<String> seenKeys = new HashSet<>();

        StructureSpawnTracker() {}

        static StructureSpawnTracker get(ServerLevel level) {
            return level.getDataStorage().computeIfAbsent(
                    new SavedData.Factory<>(StructureSpawnTracker::new, StructureSpawnTracker::load),
                    DATA_NAME
            );
        }

        boolean markIfNew(String key) {
            boolean added = seenKeys.add(key);
            if (added) setDirty();
            return added;
        }

        @Override
        public CompoundTag save(CompoundTag tag, HolderLookup.Provider provider) {
            ListTag list = new ListTag();
            for (String key : seenKeys) {
                list.add(StringTag.valueOf(key));
            }
            tag.put("keys", list);
            return tag;
        }

        private static StructureSpawnTracker load(CompoundTag tag, HolderLookup.Provider provider) {
            StructureSpawnTracker tracker = new StructureSpawnTracker();
            ListTag list = tag.getList("keys", ListTag.TAG_STRING);
            for (int i = 0; i < list.size(); i++) {
                tracker.seenKeys.add(list.getString(i));
            }
            return tracker;
        }
    }
}
