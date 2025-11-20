package com.deymosko.event;

import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class TPSTracker
{
    private static long lastTime = System.currentTimeMillis();
    private static int ticks_this_second = 0;
    private static double tps = 20.0d;


    @SubscribeEvent
    public static void onServerTick(TickEvent.ServerTickEvent event)
    {
        if(event.phase == TickEvent.Phase.END) return;

        ticks_this_second++;

        long now = System.currentTimeMillis();
        long diff = now - lastTime;

        if(diff >= 1000L)
        {
            tps = ticks_this_second * (1000 / diff);
            if(tps > 20.0d) tps = 20.0d;

            ticks_this_second = 0;
            lastTime = now;
        }

    }

    public static double getCurrentTps() {
        return tps;
    }
}
