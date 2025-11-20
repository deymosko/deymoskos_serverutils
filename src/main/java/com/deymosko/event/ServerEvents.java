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
        private static long lastTime = System.currentTimeMillis();
        private static int ticks_this_second = 0;
        private static double tps = 20.0d;


        public static void recordTick()
        {

            ticks_this_second++;

            long now = System.currentTimeMillis();
            long diff = now - lastTime;

            if(diff >= 1000L)
            {
                tps = ticks_this_second * (1000.0 / diff);
                if(tps > 20.0d) tps = 20.0d;

                ticks_this_second = 0;
                lastTime = now;
            }

        }

        public static double getCurrentTps() {
            return tps;
        }
    }
}
