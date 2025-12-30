package com.majorbonghits.moderncompanions.world;

import com.majorbonghits.moderncompanions.Constants;
import com.majorbonghits.moderncompanions.core.ModEntityTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.levelgen.structure.StructureStart;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.event.level.ChunkEvent;

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
@EventBusSubscriber(modid = Constants.MOD_ID, bus = EventBusSubscriber.Bus.FORGE)
public final class StructureCompanionSpawner {
    private StructureCompanionSpawner() {}

    /** Map structure id -> companion entity choices (supports multiple per structure). */
    private static final Map<ResourceLocation, List<Supplier<? extends EntityType<? extends PathfinderMob>>>> STRUCTURE_TO_ENTITIES = Map.ofEntries(
            Map.entry(Constants.id("alchemist_house"), List.of(ModEntityTypes.ALCHEMIST)),
            Map.entry(Constants.id("beastmaster_house"), List.of(ModEntityTypes.BEASTMASTER)),
            Map.entry(Constants.id("berserker_house"), List.of(ModEntityTypes.BERSERKER)),
            Map.entry(Constants.id("cleric_house"), List.of(ModEntityTypes.CLERIC)),
            Map.entry(Constants.id("scout_house"), List.of(ModEntityTypes.SCOUT)),
            Map.entry(Constants.id("stormcaller_house"), List.of(ModEntityTypes.STORMCALLER)),
            Map.entry(Constants.id("vanguard_house"), List.of(ModEntityTypes.VANGUARD)),
            Map.entry(Constants.id("smith"), List.of(ModEntityTypes.VANGUARD)),
            Map.entry(Constants.id("house"), List.of(ModEntityTypes.KNIGHT)),
            Map.entry(Constants.id("largehouse"), List.of(ModEntityTypes.ARCHER)),
            Map.entry(Constants.id("largehouse2"), List.of(ModEntityTypes.AXEGUARD)),
            Map.entry(Constants.id("largehouse3"), List.of(ModEntityTypes.BERSERKER)),
            Map.entry(Constants.id("lumber"), List.of(ModEntityTypes.ARBALIST)),
            // Towers can roll different mage variants
            Map.entry(Constants.id("tower1"), List.of(ModEntityTypes.FIRE_MAGE, ModEntityTypes.LIGHTNING_MAGE)),
            Map.entry(Constants.id("tower2"), List.of(ModEntityTypes.NECROMANCER)),
            Map.entry(Constants.id("watermill"), List.of(ModEntityTypes.BEASTMASTER)),
            Map.entry(Constants.id("windmill"), List.of(ModEntityTypes.STORMCALLER)),
            Map.entry(Constants.id("church"), List.of(ModEntityTypes.CLERIC)),
            // Biome-themed house variants (default to Knight so every house gets a resident)
            Map.entry(Constants.id("oak_house"), List.of(ModEntityTypes.KNIGHT)),
            Map.entry(Constants.id("oak_birch_house"), List.of(ModEntityTypes.SCOUT)),
            Map.entry(Constants.id("birch_house"), List.of(ModEntityTypes.KNIGHT)),
            Map.entry(Constants.id("acacia_house"), List.of(ModEntityTypes.ARCHER)),
            Map.entry(Constants.id("spruce_house"), List.of(ModEntityTypes.BEASTMASTER)),
            Map.entry(Constants.id("dark_oak_house"), List.of(ModEntityTypes.AXEGUARD)),
            Map.entry(Constants.id("sandstone_house"), List.of(ModEntityTypes.KNIGHT)),
            Map.entry(Constants.id("terracotta_house"), List.of(ModEntityTypes.ARBALIST))
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
            if (id == null || !STRUCTURE_TO_ENTITIES.containsKey(id)) return;

            BlockPos center = start.getBoundingBox().getCenter();
            String key = id + "|" + center.getX() + "," + center.getY() + "," + center.getZ();
            pending.add(new SpawnRequest(center, key, STRUCTURE_TO_ENTITIES.get(id)));
        });

        if (pending.isEmpty()) return;

        serverLevel.getServer().execute(() -> {
            StructureSpawnTracker tracker = StructureSpawnTracker.get(serverLevel);
            for (SpawnRequest req : pending) {
                if (!tracker.markIfNew(req.key())) continue;
                EntityType<? extends PathfinderMob> type = pickEntityFor(serverLevel.random, req.typeSuppliers());
                type.spawn(serverLevel, req.center(), MobSpawnType.STRUCTURE);
            }
        });
    }

    private static EntityType<? extends PathfinderMob> pickEntityFor(RandomSource random, List<Supplier<? extends EntityType<? extends PathfinderMob>>> choices) {
        if (choices.isEmpty()) throw new IllegalStateException("No entity choices for structure spawn");
        Supplier<? extends EntityType<? extends PathfinderMob>> supplier = choices.size() == 1
                ? choices.get(0)
                : choices.get(random.nextInt(choices.size()));
        return supplier.get();
    }

    private record SpawnRequest(BlockPos center, String key,
                                List<Supplier<? extends EntityType<? extends PathfinderMob>>> typeSuppliers) {}

    /**
     * SavedData to remember which structure placements already spawned a companion.
     */
    private static final class StructureSpawnTracker extends SavedData {
        private static final String DATA_NAME = Constants.MOD_ID + "_structure_spawns";
        private final Set<String> seenKeys = new HashSet<>();

        StructureSpawnTracker() {}

        static StructureSpawnTracker get(ServerLevel level) {
            return level.getDataStorage().computeIfAbsent(StructureSpawnTracker::load, StructureSpawnTracker::new, DATA_NAME);
        }

        boolean markIfNew(String key) {
            boolean added = seenKeys.add(key);
            if (added) setDirty();
            return added;
        }

        @Override
        public CompoundTag save(CompoundTag tag) {
            ListTag list = new ListTag();
            for (String key : seenKeys) {
                list.add(StringTag.valueOf(key));
            }
            tag.put("keys", list);
            return tag;
        }

        private static StructureSpawnTracker load(CompoundTag tag) {
            StructureSpawnTracker tracker = new StructureSpawnTracker();
            ListTag list = tag.getList("keys", ListTag.TAG_STRING);
            for (int i = 0; i < list.size(); i++) {
                tracker.seenKeys.add(list.getString(i));
            }
            return tracker;
        }
    }
}
