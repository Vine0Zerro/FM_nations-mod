package com.nations.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.nations.data.*;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.ChunkPos;

import java.util.*;

public class OpCreateTownNationCommand {

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("op_create_nation")
            .requires(source -> source.hasPermission(4))
            .then(Commands.argument("template", StringArgumentType.word())
                .then(Commands.argument("capital", StringArgumentType.string())
                    .executes(ctx -> {
                        String template = StringArgumentType.getString(ctx, "template");
                        String capital = StringArgumentType.getString(ctx, "capital");
                        return execute(ctx.getSource(), template, capital);
                    })
                )
            )
        );

        dispatcher.register(Commands.literal("op_templates")
            .requires(source -> source.hasPermission(4))
            .executes(ctx -> listTemplates(ctx.getSource()))
        );

        dispatcher.register(Commands.literal("op_delete_nation")
            .requires(source -> source.hasPermission(4))
            .then(Commands.argument("template", StringArgumentType.word())
                .executes(ctx -> {
                    String template = StringArgumentType.getString(ctx, "template");
                    return deleteNation(ctx.getSource(), template);
                })
            )
        );

        dispatcher.register(Commands.literal("op_delete_all_nations")
            .requires(source -> source.hasPermission(4))
            .executes(ctx -> deleteAllNations(ctx.getSource()))
        );
    }

    private static int execute(CommandSourceStack source, String templateKey, String capitalName) {
        try {
            NationTemplate template = NationTemplate.getTemplate(templateKey);
            if (template == null) {
                source.sendFailure(Component.literal("§cШаблон не найден!"));
                return 0;
            }

            boolean capitalFound = false;
            for (NationTemplate.TownTemplate tt : template.getTowns()) {
                if (tt.name.equalsIgnoreCase(capitalName) || tt.name.equals(capitalName)) {
                    capitalFound = true;
                    capitalName = tt.name;
                    break;
                }
            }
            if (!capitalFound) {
                source.sendFailure(Component.literal("§cСтолица не найдена в шаблоне!"));
                return 0;
            }

            if (NationsData.nationExists(template.getNationName())) {
                source.sendFailure(Component.literal("§cНация уже существует!"));
                return 0;
            }

            if (NationsData.isColorTaken(template.getColor())) {
                source.sendFailure(Component.literal("§cЦвет уже занят!"));
                return 0;
            }

            for (NationTemplate.TownTemplate tt : template.getTowns()) {
                if (NationsData.townExists(tt.name)) {
                    source.sendFailure(Component.literal("§cГород " + tt.name + " уже существует!"));
                    return 0;
                }
            }

            ServerPlayer player = source.getPlayerOrException();
            ChunkPos playerChunk = new ChunkPos(player.blockPosition());

            List<ChunkCheckResult> conflicts = checkAllChunks(template, playerChunk);
            if (!conflicts.isEmpty()) {
                source.sendFailure(Component.literal("§cТерритория занята!"));
                return 0;
            }

            return createNationWithTowns(source, player, template, playerChunk, capitalName);

        } catch (Exception e) {
            source.sendFailure(Component.literal("§cОшибка: " + e.getMessage()));
            e.printStackTrace();
            return 0;
        }
    }

    private static int createNationWithTowns(
            CommandSourceStack source, ServerPlayer player,
            NationTemplate template, ChunkPos centerChunk, String capitalName
    ) {
        UUID playerId = player.getUUID();
        List<Town> createdTowns = new ArrayList<>();

        for (NationTemplate.TownTemplate tt : template.getTowns()) {
            Town town = new Town(tt.name, playerId);
            town.setTaxRate(0.05);
            town.setCustomMaxChunks(tt.getChunkCount());

            int claimed = 0;
            for (int[] offset : tt.chunks) {
                ChunkPos cp = new ChunkPos(centerChunk.x + offset[0], centerChunk.z + offset[1]);
                if (NationsData.getTownByChunk(cp) == null) {
                    town.claimChunk(cp);
                    claimed++;
                }
            }

            int[] center = tt.getCenter();
            int spawnX = (centerChunk.x + center[0]) * 16 + 8;
            int spawnZ = (centerChunk.z + center[1]) * 16 + 8;
            town.setSpawnPos(new BlockPos(spawnX, 64, spawnZ));

            NationsData.addTown(town);
            createdTowns.add(town);
        }

        Nation nation = new Nation(template.getNationName(), playerId, template.getColor());
        nation.setCapitalTown(capitalName);

        for (Town town : createdTowns) {
            town.setNationName(template.getNationName());
            nation.addTown(town.getName());
        }

        NationsData.addNation(nation);
        Economy.createNationBalance(template.getNationName());
        for (Town town : createdTowns) Economy.createTownBalance(town.getName());
        NationsData.save();

        source.sendSuccess(() -> Component.literal("§aНация " + template.getNationName() + " создана!"), true);
        return 1;
    }

    private static int deleteNation(CommandSourceStack source, String templateKey) {
        NationTemplate template = NationTemplate.getTemplate(templateKey);
        if (template == null || !NationsData.nationExists(template.getNationName())) {
            source.sendFailure(Component.literal("§cНация не найдена"));
            return 0;
        }
        for (NationTemplate.TownTemplate tt : template.getTowns()) {
            if (NationsData.townExists(tt.name)) NationsData.removeTown(tt.name);
        }
        NationsData.removeNation(template.getNationName());
        NationsData.save();
        source.sendSuccess(() -> Component.literal("§aНация удалена"), true);
        return 1;
    }

    private static int deleteAllNations(CommandSourceStack source) {
        List<String> towns = new ArrayList<>();
        for (Town t : NationsData.getAllTowns()) towns.add(t.getName());
        for (String t : towns) NationsData.removeTown(t);
        
        List<String> nations = new ArrayList<>();
        for (Nation n : NationsData.getAllNations()) nations.add(n.getName());
        for (String n : nations) NationsData.removeNation(n);
        
        NationsData.save();
        source.sendSuccess(() -> Component.literal("§aВсе данные удалены"), true);
        return 1;
    }

    private static List<ChunkCheckResult> checkAllChunks(NationTemplate template, ChunkPos center) {
        List<ChunkCheckResult> conflicts = new ArrayList<>();
        for (NationTemplate.TownTemplate tt : template.getTowns()) {
            for (int[] offset : tt.chunks) {
                ChunkPos cp = new ChunkPos(center.x + offset[0], center.z + offset[1]);
                if (NationsData.getTownByChunk(cp) != null) {
                    conflicts.add(new ChunkCheckResult(cp, "Занято"));
                }
            }
        }
        return conflicts;
    }

    private static int listTemplates(CommandSourceStack source) {
        StringBuilder msg = new StringBuilder("§eШаблоны:\n");
        for (String key : NationTemplate.getAvailableTemplates()) {
            msg.append(" - ").append(key).append("\n");
        }
        source.sendSuccess(() -> Component.literal(msg.toString()), false);
        return 1;
    }

    private static class ChunkCheckResult {
        ChunkPos chunk;
        String reason;
        ChunkCheckResult(ChunkPos chunk, String reason) { this.chunk = chunk; this.reason = reason; }
    }
}
