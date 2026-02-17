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
                StringBuilder available = new StringBuilder();
                for (String name : NationTemplate.getAvailableTemplates()) {
                    NationTemplate t = NationTemplate.getTemplate(name);
                    if (t != null) {
                        available.append("\n§8§l┃ §7  • §e").append(name)
                                 .append(" §8(§f").append(t.getNationName())
                                 .append("§8, §e").append(t.getTotalChunks()).append(" §7чанков§8)");
                    }
                }
                source.sendFailure(Component.literal(
                    "§8§l┃ §c✘ §fШаблон '§e" + templateKey + "§f' не найден!\n" +
                    "§8§l┃ §7Доступные шаблоны:" + available));
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
                StringBuilder townList = new StringBuilder();
                for (NationTemplate.TownTemplate tt : template.getTowns()) {
                    townList.append("\n§8§l┃ §7  • §f").append(tt.name)
                            .append(" §8(").append(tt.getChunkCount()).append(" чанков)");
                }
                source.sendFailure(Component.literal(
                    "§8§l┃ §c✘ §fГород '§e" + capitalName + "§f' не найден в шаблоне!\n" +
                    "§8§l┃ §7Города в шаблоне '§f" + template.getNationName() + "§7':" + townList + "\n" +
                    "§8§l┃ §7Оборачивайте название в кавычки: §f\"Название\""));
                return 0;
            }

            if (NationsData.nationExists(template.getNationName())) {
                source.sendFailure(Component.literal(
                    "§8§l┃ §c✘ §fНация '§e" + template.getNationName() + "§f' уже существует!"));
                return 0;
            }

            if (NationsData.isColorTaken(template.getColor())) {
                source.sendFailure(Component.literal(
                    "§8§l┃ §c✘ §fЦвет §e" + template.getColor().getDisplayName() + " §fуже занят другой нацией!"));
                return 0;
            }

            for (NationTemplate.TownTemplate tt : template.getTowns()) {
                if (NationsData.townExists(tt.name)) {
                    source.sendFailure(Component.literal(
                        "§8§l┃ §c✘ §fГород '§e" + tt.name + "§f' уже существует!"));
                    return 0;
                }
            }

            ServerPlayer player = source.getPlayerOrException();
            ChunkPos playerChunk = new ChunkPos(player.blockPosition());

            List<ChunkCheckResult> conflicts = checkAllChunks(template, playerChunk);
            if (!conflicts.isEmpty()) {
                StringBuilder conflictMsg = new StringBuilder();
                int shown = 0;
                for (ChunkCheckResult conflict : conflicts) {
                    if (shown >= 5) {
                        conflictMsg.append("\n§8§l┃ §7  ... и ещё ")
                                   .append(conflicts.size() - 5).append(" конфликтов");
                        break;
                    }
                    conflictMsg.append("\n§8§l┃ §7  • Чанк §f[")
                               .append(conflict.chunk.x).append(", ").append(conflict.chunk.z)
                               .append("] §7занят городом §f").append(conflict.existingTown);
                    shown++;
                }
                source.sendFailure(Component.literal(
                    "§8§l┃ §c✘ §fНекоторые чанки уже заняты!" + conflictMsg));
                return 0;
            }

            return createNationWithTowns(source, player, template, playerChunk, capitalName);

        } catch (Exception e) {
            source.sendFailure(Component.literal("§8§l┃ §c✘ §fОшибка: " + e.getMessage()));
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

            town.addLog("Город создан оператором (шаблон: " + template.getNationName() + ")");
            town.addLog("Заприватено " + claimed + " чанков");

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
        for (Town town : createdTowns) {
            Economy.createTownBalance(town.getName());
        }

        NationsData.save();

        final int totalChunksUsed;
        {
            int c = 0;
            for (Town t : createdTowns) c += t.getClaimedChunks().size();
            totalChunksUsed = c;
        }

        final StringBuilder townsList = new StringBuilder();
        for (Town town : createdTowns) {
            int chunks = town.getClaimedChunks().size();
            String marker = town.getName().equals(capitalName) ? "§e👑 " : "§7🏠 ";
            townsList.append("\n§8§l║ §f  ").append(marker).append("§f").append(town.getName())
                     .append(" §8— §e").append(chunks).append(" §7чанков");
        }

        final String colorName = template.getColor().getDisplayName();
        final String nf = template.getNationName();
        final String cf = capitalName;
        final int ttc = template.getTotalChunks();
        final int cx = centerChunk.x, cz = centerChunk.z;

        source.sendSuccess(() -> Component.literal(
            "\n§8§l╔══════════════════════════════════════╗\n" +
            "§8§l║ §a✔ §fНация §e" + nf + " §fсоздана!\n" +
            "§8§l║ §7Цвет: §f" + colorName + "\n" +
            "§8§l║ §7Столица: §e" + cf + "\n" +
            "§8§l║ §7Размер: §e" + ttc + " §7чанков\n" +
            "§8§l║ §7Заприватено: §e" + totalChunksUsed + "§7/§e" + ttc + "\n" +
            "§8§l║\n§8§l║ §7Города:" + townsList + "\n" +
            "§8§l║\n§8§l║ §7Центр: §fчанк [" + cx + ", " + cz + "]\n" +
            "§8§l╚══════════════════════════════════════╝"
        ), true);

        return 1;
    }

    private static int deleteNation(CommandSourceStack source, String templateKey) {
        NationTemplate template = NationTemplate.getTemplate(templateKey);
        if (template == null) {
            source.sendFailure(Component.literal("§8§l┃ §c✘ §fШаблон '§e" + templateKey + "§f' не найден!"));
            return 0;
        }
        if (!NationsData.nationExists(template.getNationName())) {
            source.sendFailure(Component.literal("§8§l┃ §c✘ §fНация '§e" + template.getNationName() + "§f' не существует!"));
            return 0;
        }
        for (NationTemplate.TownTemplate tt : template.getTowns()) {
            if (NationsData.townExists(tt.name)) NationsData.removeTown(tt.name);
        }
        NationsData.removeNation(template.getNationName());
        NationsData.save();
        source.sendSuccess(() -> Component.literal(
            "§8§l┃ §a✔ §fНация §e" + template.getNationName() + " §fи все её города удалены!"), true);
        return 1;
    }

    private static int deleteAllNations(CommandSourceStack source) {
        List<String> townNames = new ArrayList<>();
        for (Town t : NationsData.getAllTowns()) townNames.add(t.getName());
        List<String> nationNames = new ArrayList<>();
        for (Nation n : NationsData.getAllNations()) nationNames.add(n.getName());

        for (String tn : townNames) NationsData.removeTown(tn);
        for (String nn : nationNames) NationsData.removeNation(nn);
        NationsData.save();

        final int tc = townNames.size(), nc = nationNames.size();
        source.sendSuccess(() -> Component.literal(
            "\n§8§l╔══════════════════════════════════════╗\n" +
            "§8§l║ §a✔ §fВсе нации и города удалены!\n" +
            "§8§l║ §7Удалено наций: §e" + nc + "\n" +
            "§8§l║ §7Удалено городов: §e" + tc + "\n" +
            "§8§l╚══════════════════════════════════════╝"
        ), true);
        return 1;
    }

    private static List<ChunkCheckResult> checkAllChunks(NationTemplate template, ChunkPos center) {
        List<ChunkCheckResult> conflicts = new ArrayList<>();
        for (NationTemplate.TownTemplate tt : template.getTowns()) {
            for (int[] offset : tt.chunks) {
                ChunkPos cp = new ChunkPos(center.x + offset[0], center.z + offset[1]);
                Town existing = NationsData.getTownByChunk(cp);
                if (existing != null) conflicts.add(new ChunkCheckResult(cp, existing.getName()));
            }
        }
        return conflicts;
    }

    private static int listTemplates(CommandSourceStack source) {
        StringBuilder msg = new StringBuilder();
        msg.append("\n§8§l╔══════════════════════════════════════╗\n");
        msg.append("§8§l║ §e📋 §fДоступные шаблоны наций:\n§8§l║\n");
        for (String key : NationTemplate.getAvailableTemplates()) {
            NationTemplate t = NationTemplate.getTemplate(key);
            if (t == null) continue;
            msg.append("§8§l║ §e▸ §f").append(key).append(" §8-> §f").append(t.getNationName())
               .append(" §8(§7").append(t.getColor().getDisplayName())
               .append("§8, §e").append(t.getTotalChunks()).append(" §7чанков§8)\n");
            for (NationTemplate.TownTemplate tt : t.getTowns()) {
                msg.append("§8§l║   §7• ").append(tt.name)
                   .append(" §8(§f").append(tt.getChunkCount()).append("§8)\n");
            }
            msg.append("§8§l║\n");
        }
        msg.append("§8§l║ §7Создать: §f/op_create_nation <шаблон> \"<столица>\"\n");
        msg.append("§8§l║ §7Удалить все: §f/op_delete_all_nations\n");
        msg.append("§8§l╚══════════════════════════════════════╝");
        source.sendSuccess(() -> Component.literal(msg.toString()), false);
        return 1;
    }

    private static class ChunkCheckResult {
        ChunkPos chunk;
        String existingTown;
        ChunkCheckResult(ChunkPos chunk, String existingTown) {
            this.chunk = chunk;
            this.existingTown = existingTown;
        }
    }
}
