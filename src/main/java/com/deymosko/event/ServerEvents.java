package com.deymosko.event;

import com.deymosko.d_serverutils.D_serverurils;
import net.minecraft.server.level.ServerLevel;
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
        if (event.phase == TickEvent.Phase.END) return;
        TPSTracker.recordTick();
        ServerLevel level = event.getServer().overworld();
        double currentTps = TPSTracker.getCurrentTps();
        if (currentTps <= 15.0d) {
            LagScanner.maybeScanLevel(level, currentTps);
        }

    }
    public static class TPSTracker
    {
        private static final int SAMPLE_SIZE = 20;
        private static final float[] tickTimesMillis = new float[SAMPLE_SIZE];
        private static int nextIndex = 0;
        private static long lastTickTime = -1L;
        private static long startTime = -1L;

        public static void recordTick()
        {
            long now = System.currentTimeMillis();
            if (startTime == -1L)
            {
                startTime = now;
            }

            if (lastTickTime != -1L)
            {
                float elapsed = (float) (now - lastTickTime);
                tickTimesMillis[nextIndex] = elapsed;
                nextIndex = (nextIndex + 1) % SAMPLE_SIZE;
            }

            lastTickTime = now;
        }

        public static double getCurrentTps() {
            if (startTime == -1L)
            {
                return 20.0d;
            }

            if (System.currentTimeMillis() - startTime < 4000L)
            {
                return 20.0d;
            }

            int samples = 0;
            float totalMs = 0.0f;

            for (float tickTime : tickTimesMillis)
            {
                if (tickTime > 0.0f)
                {
                    totalMs += tickTime;
                    samples++;
                }
            }

            if (samples == 0)
            {
                return 20.0d;
            }

            double averageMspt = totalMs / samples;
            if (averageMspt <= 0.0d)
            {
                return 20.0d;
            }

            double calculatedTps = 1000.0d / averageMspt;
            return Math.min(calculatedTps, 20.0d);
        }
    }
}
