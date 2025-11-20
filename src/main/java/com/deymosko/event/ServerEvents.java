package com.deymosko.event;

import com.deymosko.d_serverutils.D_serverurils;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ChunkHolder;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = D_serverurils.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.DEDICATED_SERVER)
public class ServerEvents
{
    @SubscribeEvent
    public static void onServerTick(TickEvent.ServerTickEvent event)
    {
        if(event.phase == TickEvent.Phase.END) return;
        TPSTracker.recordTick();
        ServerLevel level = event.getServer().overworld();
        double currentTps = TPSTracker.getCurrentTps();
        if (currentTps <= 15.0d) {
            LagScanner.maybeScanLevel(level, currentTps);
        }

    }
    public static class TPSTracker
    {
        private static final int SAMPLE_SIZE = 100;
        private static final long[] tickDurationsNanos = new long[SAMPLE_SIZE];
        private static int tickIndex = 0;
        private static int recordedTicks = 0;
        private static long lastTickTime = 0L;
        private static double tps = 20.0d;

        public static void recordTick()
        {
            long now = System.nanoTime();
            if (lastTickTime == 0L)
            {
                lastTickTime = now;
                return;
            }

            long duration = now - lastTickTime;
            lastTickTime = now;

            tickDurationsNanos[tickIndex] = duration;
            tickIndex = (tickIndex + 1) % SAMPLE_SIZE;
            if (recordedTicks < SAMPLE_SIZE)
            {
                recordedTicks++;
            }

            long totalDuration = 0L;
            for (int i = 0; i < recordedTicks; i++)
            {
                totalDuration += tickDurationsNanos[i];
            }

            double averageTickNanos = totalDuration / (double) recordedTicks;
            double calculatedTps = 1_000_000_000.0d / averageTickNanos;
            tps = Math.min(calculatedTps, 20.0d);

        }

        public static double getCurrentTps() {
            return tps;
        }
    }
}
