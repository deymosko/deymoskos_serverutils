package com.deymosko.command;

import com.deymosko.event.ServerEvents;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.ChatType;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;

public class TpsCommand
{
    public static LiteralArgumentBuilder<CommandSourceStack> register()
    {
        return Commands.literal("tps")
                .requires(cs -> cs.hasPermission(3))
                .executes(ctx ->
                {
                    ServerPlayer player = ctx.getSource().getPlayerOrException();
                    ServerLevel world = ctx.getSource().getLevel();
                    player.sendSystemMessage(Component.literal("TPS: \n" +
                            "☰☰☰☰☰☰☰☰☰☰☰☰☰☰☰☰☰☰☰☰☰☰☰☰☰☰☰☰☰☰\n" +
                            "☰☰☰☰☰☰☰☰☰☰☰☰☰☰☰☰☰☰☰☰☰☰☰☰☰☰☰☰☰☰\n" +
                            "☰☰☰☰☰☰☰☰☰☰☰☰☰☰☰☰☰☰☰☰☰☰☰☰☰☰☰☰☰☰\n" +
                            "☰☰☰☰☰☰☰☰☰☰☰☰☰☰☰☰☰☰☰☰☰☰☰☰☰☰☰☰☰☰\n" +
                            "☰☰☰☰☰☰☰☰☰☰☰☰☰☰☰☰☰☰☰☰☰☰☰☰☰☰☰☰☰☰\n" +
                            "☰☰☰☰☰☰☰☰☰☰☰☰☰☰☰☰☰☰☰☰☰☰☰☰☰☰☰☰☰☰\n" + ServerEvents.TPSTracker.getCurrentTps()));
                    return 1;
                });
    }


    private Component getFormattedGraphChunk(double tps)
    {
        return null;
    }
}
