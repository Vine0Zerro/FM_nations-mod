package com.nations.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.nations.data.*;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;
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
                    }))));

        dispatcher.register(Commands.literal("op_templates")
            .requires(source -> source.hasPermission(4))
            .executes(ctx -> listTemplates(ctx.getSource())));

        dispatcher.register(Commands.literal("op_delete_nation")
            .requires(source -> source.hasPermission(4))
            .then(Commands.argument("template", StringArgumentType.word())
                .executes(ctx -> deleteNation(ctx.getSource(), StringArgumentType.getString(ctx, "template")))));

        dispatcher.register(Commands.literal("op_delete_all_nations")
            .requires(source -> source.hasPermission(4))
            .executes(ctx -> deleteAllNations(ctx.getSource())));
    }

    private static int execute(CommandSourceStack source, String templateKey, String capitalName) {
        try {
            NationTemplate template = NationTemplate.getTemplate(templateKey);
            if (template == null) {
                source.sendFailure(Component.literal("§cШаблон не найден! Используйте §e/op_templates"));
                return 0;
            }

            boolean capitalFound = false;
            for (NationTemplate.TownTemplate tt : template.getTowns()) {
                if (tt.name.equalsIgnoreCase(capitalName)) {
                    capitalFound = true;
                    capitalName = tt.name;
                    break;
                }
            }
            if (!capitalFound) {
                StringBuilder towns = new StringBuilder("§cСтолица не найдена! Города в шаблоне:\n");
                for (NationTemplate.TownTemplate tt : template.getTowns())
                    towns.append("§7 - §e").append(tt.name).append(" §8(").append(tt.getChunkCount()).append(" чанков)\n");
                source.sendFailure(Component.literal(towns.toString()));
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
                source.sendFailure(Component.literal("§cТерритория занята! Конфликтов: " + conflicts.size()));
                return 0;
            }

            return createNationWithTowns(source, player, template, playerChunk, capitalName);
        } catch (Exception e) {
            source.sendFailure(Component.literal("§cОшибка: " + e.getMessage()));
            e.printStackTrace();
            return 0;
        }
    }

    private static int createNationWithTowns(CommandSourceStack source, ServerPlayer player,
            NationTemplate template, ChunkPos centerChunk, String capitalName) {
        UUID playerId = player.getUUID();
        List<Town> createdTowns = new ArrayList<>();

        for (NationTemplate.TownTemplate tt : template.getTowns()) {
            Town town = new Town(tt.name, playerId);
            town.setTaxRate(0.05);
            town.setCustomMaxChunks(tt.getChunkCount() + 50);

            for (int[] offset : tt.chunks) {
                ChunkPos cp = new ChunkPos(centerChunk.x + offset[0], centerChunk.z + offset[1]);
                if (NationsData.getTownByChunk(cp) == null) town.claimChunk(cp);
            }

            int[] center = tt.getCenter();
            town.setSpawnPos(new BlockPos((centerChunk.x + center[0]) * 16 + 8, 64, (centerChunk.z + center[1]) * 16 + 8));
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

        int hex = template.getColor().getHex();
        MutableComponent nationColored = Component.literal(template.getNationName())
            .setStyle(Style.EMPTY.withColor(TextColor.fromRgb(hex)).withBold(true));

        MutableComponent msg = Component.literal("§a✔ Нация ")
            .append(nationColored)
            .append(Component.literal(" §aсоздана! §7(" + createdTowns.size() + " городов, " + template.getTotalChunks() + " чанков)"));

        source.sendSuccess(() -> msg, true);
        return 1;
    }

    private static int deleteNation(CommandSourceStack source, String templateKey) {
        NationTemplate template = NationTemplate.getTemplate(templateKey);
        if (template == null || !NationsData.nationExists(template.getNationName())) {
            source.sendFailure(Component.literal("§cНация не найдена"));
            return 0;
        }
        int hex = template.getColor().getHex();
        String name = template.getNationName();
        for (NationTemplate.TownTemplate tt : template.getTowns())
            if (NationsData.townExists(tt.name)) NationsData.removeTown(tt.name);
        NationsData.removeNation(name);
        NationsData.save();

        MutableComponent nc = Component.literal(name).setStyle(Style.EMPTY.withColor(TextColor.fromRgb(hex)).withBold(true));
        source.sendSuccess(() -> Component.literal("§a✔ Нация ").append(nc).append(Component.literal(" §aудалена")), true);
        return 1;
    }

    private static int deleteAllNations(CommandSourceStack source) {
        List<String> towns = new ArrayList<>(), nations = new ArrayList<>();
        for (Town t : NationsData.getAllTowns()) towns.add(t.getName());
        for (String t : towns) NationsData.removeTown(t);
        for (Nation n : NationsData.getAllNations()) nations.add(n.getName());
        for (String n : nations) NationsData.removeNation(n);
        NationsData.save();
        source.sendSuccess(() -> Component.literal("§a✔ Все нации и города удалены"), true);
        return 1;
    }

    private static List<ChunkCheckResult> checkAllChunks(NationTemplate template, ChunkPos center) {
        List<ChunkCheckResult> conflicts = new ArrayList<>();
        for (NationTemplate.TownTemplate tt : template.getTowns())
            for (int[] offset : tt.chunks) {
                ChunkPos cp = new ChunkPos(center.x + offset[0], center.z + offset[1]);
                if (NationsData.getTownByChunk(cp) != null) conflicts.add(new ChunkCheckResult(cp));
            }
        return conflicts;
    }

    private static int listTemplates(CommandSourceStack source) {
        StringBuilder msg = new StringBuilder("\n§8§l╔══ §6§l🏛 ШАБЛОНЫ НАЦИЙ §8§l══╗\n");
        for (String key : NationTemplate.getAvailableTemplates()) {
            NationTemplate t = NationTemplate.getTemplate(key);
            if (t != null) msg.append("§8§l║ §e").append(key).append(" §8— §f").append(t.getNationName())
                .append(" §8(§7").append(t.getTotalChunks()).append(" чанков, ").append(t.getTowns().size()).append(" городов§8)\n");
        }
        msg.append("§8§l╚════════════════════╝\n§7/op_create_nation <шаблон> \"<столица>\"\n");
        source.sendSuccess(() -> Component.literal(msg.toString()), false);
        return 1;
    }

    private static class ChunkCheckResult {
        ChunkPos chunk;
        ChunkCheckResult(ChunkPos chunk) { this.chunk = chunk; }
    }
}
