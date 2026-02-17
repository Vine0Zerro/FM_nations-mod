package com.nations.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.nations.data.*;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.ChunkPos;

import java.util.*;

public class OpCreateTownNationCommand {

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("op_create_town_nation")
            .requires(source -> source.hasPermission(4)) // только операторы
            .then(Commands.argument("столица", StringArgumentType.word())
                .then(Commands.argument("шаблон", StringArgumentType.greedyString())
                    .executes(ctx -> {
                        String capital = StringArgumentType.getString(ctx, "столица");
                        String fullArgs = StringArgumentType.getString(ctx, "шаблон");
                        return execute(ctx.getSource(), capital, fullArgs);
                    })
                )
            )
        );

        // Команда для просмотра доступных шаблонов
        dispatcher.register(Commands.literal("op_templates")
            .requires(source -> source.hasPermission(4))
            .executes(ctx -> listTemplates(ctx.getSource()))
        );
    }

    private static int execute(CommandSourceStack source, String capitalName, String fullArgs) {
        try {
            // Парсим аргументы: "Российская Федерация 100" или "США 100"
            // Последнее слово — лимит чанков, всё остальное — название шаблона
            String[] parts = fullArgs.trim().split("\\s+");
            if (parts.length < 2) {
                source.sendFailure(Component.literal(
                    "§8§l┃ §c✘ §fИспользование: §e/op_create_town_nation <столица> <шаблон> <лимит>\n" +
                    "§8§l┃ §7Пример: §f/op_create_town_nation Москва Российская Федерация 100"));
                return 0;
            }

            // Последний элемент — лимит
            int chunkLimit;
            try {
                chunkLimit = Integer.parseInt(parts[parts.length - 1]);
            } catch (NumberFormatException e) {
                source.sendFailure(Component.literal(
                    "§8§l┃ §c✘ §fПоследний аргумент должен быть числом (лимит чанков)!\n" +
                    "§8§l┃ §7Пример: §f/op_create_town_nation Москва Российская Федерация 100"));
                return 0;
            }

            // Всё кроме последнего — название шаблона
            StringBuilder templateNameBuilder = new StringBuilder();
            for (int i = 0; i < parts.length - 1; i++) {
                if (i > 0) templateNameBuilder.append(" ");
                templateNameBuilder.append(parts[i]);
            }
            String templateName = templateNameBuilder.toString();

            // Ищем шаблон
            NationTemplate template = NationTemplate.getTemplate(templateName);
            if (template == null) {
                StringBuilder available = new StringBuilder();
                for (String name : NationTemplate.getAvailableTemplates()) {
                    available.append("\n§8§l┃ §7  • §f").append(name);
                }
                source.sendFailure(Component.literal(
                    "§8§l┃ §c✘ §fШаблон '§e" + templateName + "§f' не найден!\n" +
                    "§8§l┃ §7Доступные шаблоны:" + available));
                return 0;
            }

            // Проверяем что столица есть в шаблоне
            boolean capitalFound = false;
            for (NationTemplate.TownTemplate tt : template.getTowns()) {
                if (tt.name.equalsIgnoreCase(capitalName)) {
                    capitalFound = true;
                    break;
                }
            }
            if (!capitalFound) {
                StringBuilder townList = new StringBuilder();
                for (NationTemplate.TownTemplate tt : template.getTowns()) {
                    townList.append("\n§8§l┃ §7  • §f").append(tt.name)
                            .append(" §8(").append(tt.chunksX * tt.chunksZ).append(" чанков)");
                }
                source.sendFailure(Component.literal(
                    "§8§l┃ §c✘ §fГород '§e" + capitalName + "§f' не найден в шаблоне!\n" +
                    "§8§l┃ §7Города в шаблоне '§f" + templateName + "§7':" + townList));
                return 0;
            }

            // Проверяем лимит
            int totalNeeded = template.getTotalChunks();
            if (chunkLimit < totalNeeded) {
                source.sendFailure(Component.literal(
                    "§8§l┃ §c✘ §fЛимит §e" + chunkLimit + " §fчанков недостаточен!\n" +
                    "§8§l┃ §7Шаблон '§f" + templateName + "§7' требует §e" + totalNeeded + " §7чанков"));
                return 0;
            }

            // Проверяем что нация с таким именем не существует
            if (NationsData.nationExists(template.getNationName())) {
                source.sendFailure(Component.literal(
                    "§8§l┃ §c✘ §fНация '§e" + template.getNationName() + "§f' уже существует!"));
                return 0;
            }

            // Проверяем что цвет не занят
            if (NationsData.isColorTaken(template.getColor())) {
                source.sendFailure(Component.literal(
                    "§8§l┃ §c✘ §fЦвет §e" + template.getColor().getDisplayName() + " §fуже занят другой нацией!"));
                return 0;
            }

            // Проверяем что города не существуют
            for (NationTemplate.TownTemplate tt : template.getTowns()) {
                if (NationsData.townExists(tt.name)) {
                    source.sendFailure(Component.literal(
                        "§8§l┃ §c✘ §fГород '§e" + tt.name + "§f' уже существует!"));
                    return 0;
                }
            }

            // Получаем позицию игрока
            ServerPlayer player = source.getPlayerOrException();
            ChunkPos playerChunk = new ChunkPos(player.blockPosition());

            // Проверяем что все чанки свободны
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

            // === СОЗДАЁМ ВСЁ ===
            return createNationWithTowns(source, player, template, playerChunk, capitalName, chunkLimit);

        } catch (Exception e) {
            source.sendFailure(Component.literal("§8§l┃ §c✘ §fОшибка: " + e.getMessage()));
            e.printStackTrace();
            return 0;
        }
    }

    private static int createNationWithTowns(
            CommandSourceStack source,
            ServerPlayer player,
            NationTemplate template,
            ChunkPos centerChunk,
            String capitalName,
            int chunkLimit
    ) {
        UUID playerId = player.getUUID();

        // 1. Создаём все города
        List<Town> createdTowns = new ArrayList<>();
        Town capitalTown = null;

        for (NationTemplate.TownTemplate tt : template.getTowns()) {
            Town town = new Town(tt.name, playerId);
            town.setTaxRate(0.05);

            // Устанавливаем лимит чанков (переопределяем стандартный)
            // Будем хранить максимальное кол-во в самом городе через количество приваченных

            // Вычисляем начальный чанк для этого города
            int startX = centerChunk.x + tt.offsetX;
            int startZ = centerChunk.z + tt.offsetZ;

            // Приватим чанки прямоугольником
            int claimed = 0;
            for (int x = startX; x < startX + tt.chunksX; x++) {
                for (int z = startZ; z < startZ + tt.chunksZ; z++) {
                    ChunkPos cp = new ChunkPos(x, z);
                    if (NationsData.getTownByChunk(cp) == null) {
                        town.claimChunk(cp);
                        claimed++;
                    }
                }
            }

            // Устанавливаем спавн в центре города
            int spawnX = (startX + tt.chunksX / 2) * 16 + 8;
            int spawnZ = (startZ + tt.chunksZ / 2) * 16 + 8;
            town.setSpawnPos(new net.minecraft.core.BlockPos(spawnX, 64, spawnZ));

            town.addLog("Город создан оператором (шаблон: " + template.getNationName() + ")");
            town.addLog("Заприватено " + claimed + " чанков");

            NationsData.addTown(town);
            createdTowns.add(town);

            if (tt.name.equalsIgnoreCase(capitalName)) {
                capitalTown = town;
            }
        }

        // 2. Создаём нацию
        Nation nation = new Nation(template.getNationName(), playerId, template.getColor());

        // 3. Привязываем все города к нации
        for (Town town : createdTowns) {
            town.setNationName(template.getNationName());
            nation.addTown(town.getName());
        }

        NationsData.addNation(nation);

        // 4. Создаём баланс нации в экономике
        Economy.createNationBalance(template.getNationName());

        // 5. Создаём балансы городов
        for (Town town : createdTowns) {
            Economy.createTownBalance(town.getName());
        }

        NationsData.save();

        // === Красивый вывод ===
        int totalChunks = 0;
        StringBuilder townsList = new StringBuilder();
        for (Town town : createdTowns) {
            int chunks = town.getClaimedChunks().size();
            totalChunks += chunks;
            String marker = town.getName().equalsIgnoreCase(capitalName) ? "§e👑 " : "§7🏠 ";
            townsList.append("\n§8§l┃ §f  ")
                     .append(marker).append("§f").append(town.getName())
                     .append(" §8— §e").append(chunks).append(" §7чанков");
        }

        int hexColor = template.getColor().getHex();
        String colorName = template.getColor().getDisplayName();

        source.sendSuccess(() -> Component.literal(
            "\n§8§l╔══════════════════════════════════════╗\n" +
            "§8§l║ §a✔ §fНация §e" + template.getNationName() + " §fсоздана!\n" +
            "§8§l║ §7Цвет: §f" + colorName + "\n" +
            "§8§l║ §7Столица: §e" + capitalName + "\n" +
            "§8§l║ §7Лимит чанков: §e" + chunkLimit + "\n" +
            "§8§l║ §7Использовано: §e" + totalChunks + "§7/§e" + chunkLimit + "\n" +
            "§8§l║\n" +
            "§8§l║ §7Города:" + townsList + "\n" +
            "§8§l║\n" +
            "§8§l║ §7Центр: §fчанк [" + (new ChunkPos(player.blockPosition())).x + ", " +
                (new ChunkPos(player.blockPosition())).z + "]\n" +
            "§8§l╚══════════════════════════════════════╝"
        ), true);

        return 1;
    }

    private static List<ChunkCheckResult> checkAllChunks(NationTemplate template, ChunkPos center) {
        List<ChunkCheckResult> conflicts = new ArrayList<>();

        for (NationTemplate.TownTemplate tt : template.getTowns()) {
            int startX = center.x + tt.offsetX;
            int startZ = center.z + tt.offsetZ;

            for (int x = startX; x < startX + tt.chunksX; x++) {
                for (int z = startZ; z < startZ + tt.chunksZ; z++) {
                    ChunkPos cp = new ChunkPos(x, z);
                    Town existing = NationsData.getTownByChunk(cp);
                    if (existing != null) {
                        conflicts.add(new ChunkCheckResult(cp, existing.getName()));
                    }
                }
            }
        }
        return conflicts;
    }

    private static int listTemplates(CommandSourceStack source) {
        StringBuilder msg = new StringBuilder();
        msg.append("\n§8§l╔══════════════════════════════════════╗\n");
        msg.append("§8§l║ §e📋 §fДоступные шаблоны наций:\n");
        msg.append("§8§l║\n");

        for (String templateName : NationTemplate.getAvailableTemplates()) {
            NationTemplate t = NationTemplate.getTemplate(templateName);
            if (t == null) continue;

            msg.append("§8§l║ §e▸ §f").append(t.getNationName())
               .append(" §8(§7").append(t.getColor().getDisplayName())
               .append("§8, §e").append(t.getTotalChunks()).append(" §7чанков§8)\n");

            for (NationTemplate.TownTemplate tt : t.getTowns()) {
                msg.append("§8§l║   §7• ").append(tt.name)
                   .append(" §8(§f").append(tt.chunksX * tt.chunksZ).append("§8)\n");
            }
            msg.append("§8§l║\n");
        }

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
