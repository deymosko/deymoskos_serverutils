package com.deymosko.event;

import com.deymosko.d_serverutils.D_serverurils;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraftforge.event.level.ChunkEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Mod.EventBusSubscriber(modid = D_serverurils.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class ChunkTracker {

    private static final Set<LevelChunk> LOADED_CHUNKS = Collections.newSetFromMap(new ConcurrentHashMap<>());

    private ChunkTracker() {
        // Utility class
    }

    @SubscribeEvent
    public static void onChunkLoad(ChunkEvent.Load event) {
        LevelAccessor level = event.getLevel();
        if (!(level instanceof ServerLevel)) {
            return;
        }

        if (event.getChunk() instanceof LevelChunk levelChunk) {
            LOADED_CHUNKS.add(levelChunk);
        }
    }

    @SubscribeEvent
    public static void onChunkUnload(ChunkEvent.Unload event) {
        if (event.getChunk() instanceof LevelChunk levelChunk) {
            LOADED_CHUNKS.remove(levelChunk);
        }
    }

    public static Collection<LevelChunk> getLoadedChunksSnapshot() {
        return List.copyOf(LOADED_CHUNKS);
    }
}