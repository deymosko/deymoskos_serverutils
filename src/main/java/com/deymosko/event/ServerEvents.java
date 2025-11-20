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
        TPSTracker.recordTick(event.getServer());
        ServerLevel level = event.getServer().overworld();
        double currentTps = TPSTracker.getCurrentTps();
        if (currentTps <= 15.0d) {
            LagScanner.maybeScanLevel(level, currentTps);
        }

    }
    public static class TPSTracker
    {
        private static double tps = 20.0d;

        public static void recordTick(MinecraftServer server)
        {
            double averageTickMillis = server.getAverageTickTime();
            if (averageTickMillis <= 0.0d)
            {
                tps = 20.0d;
                return;
            }

            double calculatedTps = 1000.0d / averageTickMillis;
            tps = Math.min(calculatedTps, 20.0d);
        }

        public static double getCurrentTps() {
            return tps;
        }
    }
}
