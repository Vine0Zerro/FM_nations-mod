package com.nations.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.nations.data.NationsData;
import com.nations.data.Town;
import com.nations.network.ClaimMapPacket;
import com.nations.network.NetworkHandler;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.ChunkPos;

public class ClaimCommands {

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("claim")
            .executes(ctx -> claimChunk(ctx.getSource()))
        );

        dispatcher.register(Commands.literal("unclaim")
            .executes(ctx -> unclaimChunk(ctx.getSource()))
        );

        dispatcher.register(Commands.literal("claimmap")
            .executes(ctx -> openClaimMap(ctx.getSource()))
        );
    }

    private static int claimChunk(CommandSourceStack source) {
        try {
            ServerPlayer player = source.getPlayerOrException();
            Town town = NationsData.getTownByPlayer(player.getUUID());
            if (town == null) {
                source.sendFailure(Component.literal("§cВы не в городе!"));
                return 0;
            }
            if (!town.getMayor().equals(player.getUUID())) {
                source.sendFailure(Component.literal("§cТолько мэр может приватить!"));
                return 0;
            }
            if (!NationsData.canClaim(player.getUUID())) {
                source.sendFailure(Component.literal(
                    "§cЛимит! Можно приватить 5 чанков в минуту."));
                return 0;
            }

            ChunkPos cp = new ChunkPos(player.blockPosition());
            if (NationsData.getTownByChunk(cp) != null) {
                source.sendFailure(Component.literal("§cЭтот чанк уже занят!"));
                return 0;
            }

            town.claimChunk(cp);
            NationsData.incrementClaim(player.getUUID());
            NationsData.save();

            source.sendSuccess(() -> Component.literal(
                "§aЧанк [" + cp.x + ", " + cp.z + "] запривачен для города §e" +
                town.getName()), true);
            return 1;
        } catch (Exception e) {
            source.sendFailure(Component.literal("§cОшибка: " + e.getMessage()));
            return 0;
        }
    }

    private static int unclaimChunk(CommandSourceStack source) {
        try {
            ServerPlayer player = source.getPlayerOrException();
            Town town = NationsData.getTownByPlayer(player.getUUID());
            if (town == null || !town.getMayor().equals(player.getUUID())) {
                source.sendFailure(Component.literal("§cВы не мэр!"));
                return 0;
            }

            ChunkPos cp = new ChunkPos(player.blockPosition());
            if (!town.ownsChunk(cp)) {
                source.sendFailure(Component.literal("§cЭтот чанк не ваш!"));
                return 0;
            }

            town.unclaimChunk(cp);
            NationsData.save();
            source.sendSuccess(() -> Component.literal(
                "§aЧанк [" + cp.x + ", " + cp.z + "] освобождён."), true);
            return 1;
        } catch (Exception e) {
            source.sendFailure(Component.literal("§cОшибка: " + e.getMessage()));
            return 0;
        }
    }

    private static int openClaimMap(CommandSourceStack source) {
        try {
            ServerPlayer player = source.getPlayerOrException();
            NetworkHandler.sendToPlayer(player, ClaimMapPacket.create(player));
            return 1;
        } catch (Exception e) {
            source.sendFailure(Component.literal("§cОшибка: " + e.getMessage()));
            return 0;
        }
    }
}