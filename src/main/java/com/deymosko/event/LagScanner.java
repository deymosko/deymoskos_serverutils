package com.deymosko.event;

import com.deymosko.d_serverutils.D_serverurils;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.phys.AABB;

import java.util.Collection;
import java.util.List;

public class LagScanner {

    private static final int TOP_N = 5;
    private static final long SCAN_COOLDOWN_MS = 5000L;
    private static long lastScanTime = 0L;

    private LagScanner() {
    }

    public static void maybeScanLevel(ServerLevel level, double currentTps) {
        long now = System.currentTimeMillis();
        if (now - lastScanTime < SCAN_COOLDOWN_MS) {
            return;
        }

        lastScanTime = now;
        scanLevel(level, currentTps);
    }

    private static void scanLevel(ServerLevel level, double currentTps) {
        Collection<LevelChunk> chunks = ChunkTracker.getLoadedChunksSnapshot();

        LevelChunk[] bestChunks = new LevelChunk[TOP_N];
        int[] bestScore = new int[TOP_N];
        int[] bestEntities = new int[TOP_N];
        int[] bestBlockEntities = new int[TOP_N];

        for (LevelChunk chunk : chunks) {
            if (chunk.getLevel() != level) {
                continue;
            }

            ChunkPos pos = chunk.getPos();
            int minX = pos.getMinBlockX();
            int minZ = pos.getMinBlockZ();
            int maxX = minX + 15;
            int maxZ = minZ + 15;

            AABB box = new AABB(
                    minX, level.getMinBuildHeight(), minZ,
                    maxX + 1, level.getMaxBuildHeight(), maxZ + 1
            );

            int entities = level.getEntities(null, box).size();
            int blockEntities = chunk.getBlockEntities().size();
            int score = entities + blockEntities * 3;

            for (int i = 0; i < TOP_N; i++) {
                if (score > bestScore[i]) {
                    // shift lower entries down
                    for (int j = TOP_N - 1; j > i; j--) {
                        bestChunks[j] = bestChunks[j - 1];
                        bestScore[j] = bestScore[j - 1];
                        bestEntities[j] = bestEntities[j - 1];
                        bestBlockEntities[j] = bestBlockEntities[j - 1];
                    }
                    bestChunks[i] = chunk;
                    bestScore[i] = score;
                    bestEntities[i] = entities;
                    bestBlockEntities[i] = blockEntities;
                    break;
                }
            }
        }

        D_serverurils.LOGGER.warn(
                "[LagScanner] TPS={} -> analyzed {} chunks in world {}",
                String.format("%.2f", currentTps),
                chunks.size(),
                level.dimension().location()
        );

        for (int i = 0; i < TOP_N; i++) {
            if (bestChunks[i] == null) {
                continue;
            }

            ChunkPos pos = bestChunks[i].getPos();
            D_serverurils.LOGGER.warn(
                    "[LagScanner] #{} chunk=({}, {}), score={}, entities={}, blockEntities={}",
                    i + 1,
                    pos.x,
                    pos.z,
                    bestScore[i],
                    bestEntities[i],
                    bestBlockEntities[i]
            );
        }

        sendMessageToOps(level, currentTps, bestChunks, bestScore, bestEntities, bestBlockEntities);
    }

    private static void sendMessageToOps(ServerLevel level, double currentTps, LevelChunk[] bestChunks, int[] bestScore, int[] bestEntities, int[] bestBlockEntities) {
        List<ServerPlayer> players = level.getServer().getPlayerList().getPlayers();
        if (players.isEmpty()) {
            return;
        }

        Component header = Component.literal(String.format("[LagScanner] TPS %.2f — possible problematic chunks:", currentTps))
                .withStyle(ChatFormatting.GOLD);

        for (ServerPlayer player : players) {
            if (!player.hasPermissions(2)) {
                continue;
            }

            player.sendSystemMessage(header);
            for (int i = 0; i < TOP_N; i++) {
                if (bestChunks[i] == null) {
                    continue;
                }

                ChunkPos pos = bestChunks[i].getPos();
                Component line = Component.literal(String.format(
                        "[LagScanner] #%d chunk=(%d, %d) score=%d entities=%d blockEntities=%d",
                        i + 1,
                        pos.x,
                        pos.z,
                        bestScore[i],
                        bestEntities[i],
                        bestBlockEntities[i]
                )).withStyle(ChatFormatting.YELLOW);
                player.sendSystemMessage(line);
            }
        }
    }
}