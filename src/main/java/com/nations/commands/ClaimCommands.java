package com.nations.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.nations.data.NationsData;
import com.nations.data.Town;
import com.nations.data.TownRole;
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
                source.sendFailure(Component.literal("§8§l┃ §c✘ §fВы не в городе!"));
                return 0;
            }
            if (!town.hasPermission(player.getUUID(), TownRole.BUILDER)) {
                source.sendFailure(Component.literal("§8§l┃ §c✘ §fНужна роль §a🔨 Строитель §fили выше!"));
                return 0;
            }
            if (!town.canClaimMore()) {
                source.sendFailure(Component.literal(
                    "§8§l┃ §c✘ §fЛимит территории! §e" + town.getClaimedChunks().size() +
                    "§f/§e" + town.getMaxChunks() + " §fчанков\n" +
                    "§8§l┃ §7Привлеките больше жителей для расширения"));
                return 0;
            }
            if (!NationsData.canClaim(player.getUUID())) {
                source.sendFailure(Component.literal(
                    "§8§l┃ §c✘ §fЛимит скорости! Можно приватить §e5 чанков §fв минуту"));
                return 0;
            }

            ChunkPos cp = new ChunkPos(player.blockPosition());
            if (NationsData.getTownByChunk(cp) != null) {
                source.sendFailure(Component.literal("§8§l┃ §c✘ §fЭтот чанк уже занят!"));
                return 0;
            }

            town.claimChunk(cp);
            town.addLog(player.getName().getString() + " заприватил чанк [" + cp.x + "," + cp.z + "]");
            NationsData.incrementClaim(player.getUUID());
            NationsData.save();

            source.sendSuccess(() -> Component.literal(
                "§8§l┃ §a✔ §fЧанк §e[" + cp.x + ", " + cp.z + "] §fзаприватен для города §e" +
                town.getName() + " §8(§f" + town.getClaimedChunks().size() + "§8/§f" + town.getMaxChunks() + "§8)"), true);
            return 1;
        } catch (Exception e) {
            source.sendFailure(Component.literal("§8§l┃ §c✘ §fОшибка: " + e.getMessage()));
            return 0;
        }
    }

    private static int unclaimChunk(CommandSourceStack source) {
        try {
            ServerPlayer player = source.getPlayerOrException();
            Town town = NationsData.getTownByPlayer(player.getUUID());
            if (town == null || !town.hasPermission(player.getUUID(), TownRole.VICE_RULER)) {
                source.sendFailure(Component.literal("§8§l┃ §c✘ §fНужна роль §e⚜ Зам. Правителя §fили выше!"));
                return 0;
            }

            ChunkPos cp = new ChunkPos(player.blockPosition());
            if (!town.ownsChunk(cp)) {
                source.sendFailure(Component.literal("§8§l┃ §c✘ §fЭтот чанк не ваш!"));
                return 0;
            }

            town.unclaimChunk(cp);
            town.addLog(player.getName().getString() + " освободил чанк [" + cp.x + "," + cp.z + "]");
            NationsData.save();
            source.sendSuccess(() -> Component.literal(
                "§8§l┃ §a✔ §fЧанк §e[" + cp.x + ", " + cp.z + "] §fосвобождён"), true);
            return 1;
        } catch (Exception e) {
            source.sendFailure(Component.literal("§8§l┃ §c✘ §fОшибка: " + e.getMessage()));
            return 0;
        }
    }

    private static int openClaimMap(CommandSourceStack source) {
        try {
            ServerPlayer player = source.getPlayerOrException();
            NetworkHandler.sendToPlayer(player, ClaimMapPacket.create(player));
            return 1;
        } catch (Exception e) {
            source.sendFailure(Component.literal("§8§l┃ §c✘ §fОшибка: " + e.getMessage()));
            return 0;
        }
    }
}
