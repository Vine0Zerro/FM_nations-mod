package com.nations.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.nations.data.*;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.ChunkPos;

import java.util.UUID;

public class NationCommands {

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("nation")
            .then(Commands.literal("create")
                .then(Commands.argument("name", StringArgumentType.word())
                    .then(Commands.argument("color", StringArgumentType.word())
                        .executes(ctx -> createNation(ctx.getSource(),
                            StringArgumentType.getString(ctx, "name"),
                            StringArgumentType.getString(ctx, "color"))))))
            .then(Commands.literal("delete")
                .executes(ctx -> deleteNation(ctx.getSource())))
            .then(Commands.literal("invite")
                .then(Commands.argument("town", StringArgumentType.word())
                    .executes(ctx -> inviteTown(ctx.getSource(),
                        StringArgumentType.getString(ctx, "town")))))
            .then(Commands.literal("accept")
                .then(Commands.argument("nation", StringArgumentType.word())
                    .executes(ctx -> acceptInvite(ctx.getSource(),
                        StringArgumentType.getString(ctx, "nation")))))
            .then(Commands.literal("leave")
                .executes(ctx -> leaveNation(ctx.getSource())))
            .then(Commands.literal("kick")
                .then(Commands.argument("town", StringArgumentType.word())
                    .executes(ctx -> kickTown(ctx.getSource(),
                        StringArgumentType.getString(ctx, "town")))))
            .then(Commands.literal("color")
                .then(Commands.argument("color", StringArgumentType.word())
                    .executes(ctx -> changeColor(ctx.getSource(),
                        StringArgumentType.getString(ctx, "color")))))
            .then(Commands.literal("info")
                .executes(ctx -> nationInfo(ctx.getSource()))
                .then(Commands.argument("name", StringArgumentType.word())
                    .executes(ctx -> nationInfoByName(ctx.getSource(),
                        StringArgumentType.getString(ctx, "name")))))
            .then(Commands.literal("list")
                .executes(ctx -> listNations(ctx.getSource())))
            .then(Commands.literal("colors")
                .executes(ctx -> listColors(ctx.getSource())))
            .then(Commands.literal("tax")
                .then(Commands.argument("rate", DoubleArgumentType.doubleArg(0, 30))
                    .executes(ctx -> setNationTax(ctx.getSource(),
                        DoubleArgumentType.getDouble(ctx, "rate")))))
            // ВОЙНА — ТОЛЬКО ДЛЯ ОПЕРАТОРОВ (permission level 2)
            .then(Commands.literal("war")
                .then(Commands.literal("declare")
                    .requires(src -> src.hasPermission(2))
                    .then(Commands.argument("attacker", StringArgumentType.word())
                        .then(Commands.argument("defender", StringArgumentType.word())
                            .executes(ctx -> declareWar(ctx.getSource(),
                                StringArgumentType.getString(ctx, "attacker"),
                                StringArgumentType.getString(ctx, "defender"))))))
                .then(Commands.literal("end")
                    .requires(src -> src.hasPermission(2))
                    .then(Commands.argument("nation1", StringArgumentType.word())
                        .then(Commands.argument("nation2", StringArgumentType.word())
                            .executes(ctx -> endWar(ctx.getSource(),
                                StringArgumentType.getString(ctx, "nation1"),
                                StringArgumentType.getString(ctx, "nation2"))))))
                .then(Commands.literal("capture")
                    .then(Commands.argument("town", StringArgumentType.word())
                        .executes(ctx -> captureTown(ctx.getSource(),
                            StringArgumentType.getString(ctx, "town")))))
                .then(Commands.literal("surrender")
                    .then(Commands.argument("nation", StringArgumentType.word())
                        .executes(ctx -> surrender(ctx.getSource(),
                            StringArgumentType.getString(ctx, "nation"))))))
        );
    }

    private static int createNation(CommandSourceStack source, String name, String colorId) {
        try {
            ServerPlayer player = source.getPlayerOrException();
            UUID uuid = player.getUUID();
            Town town = NationsData.getTownByPlayer(uuid);

            if (town == null || !town.hasPermission(uuid, TownRole.RULER)) {
                source.sendFailure(Component.literal(
                    "§8§l┃ §c✘ §fВы должны быть §6👑 Правителем §fгорода!"));
                return 0;
            }
            if (town.getNationName() != null) {
                source.sendFailure(Component.literal("§8§l┃ §c✘ §fВаш город уже в нации!"));
                return 0;
            }
            if (NationsData.nationExists(name)) {
                source.sendFailure(Component.literal("§8§l┃ §c✘ §fНация с таким именем уже существует!"));
                return 0;
            }
            NationColor color = NationColor.fromId(colorId);
            if (color == null) {
                source.sendFailure(Component.literal(
                    "§8§l┃ §c✘ §fНеизвестный цвет! Используйте §e/nation colors"));
                return 0;
            }
            if (NationsData.isColorTaken(color)) {
                source.sendFailure(Component.literal("§8§l┃ §c✘ §fЭтот цвет уже занят другой нацией!"));
                return 0;
            }

            Nation nation = new Nation(name, uuid, color);
            nation.addTown(town.getName());
            town.setNationName(name);
            town.addLog("Город вступил в нацию " + name);
            NationsData.addNation(nation);
            NationsData.save();

            source.getServer().getPlayerList().broadcastSystemMessage(
                Component.literal(
                    "\n§8§l╔══════════════════════════════════╗\n" +
                    "§8§l║  §6§l🏛 НОВАЯ НАЦИЯ!                 §8§l║\n" +
                    "§8§l║                                    §8§l║\n" +
                    "§8§l║  §fНазвание: §e§l" + name + "                  §8§l║\n" +
                    "§8§l║  §fЦвет: §e" + color.getDisplayName() + "                     §8§l║\n" +
                    "§8§l║  §fОснователь: §f" + player.getName().getString() + "           §8§l║\n" +
                    "§8§l╚══════════════════════════════════╝\n"), false);
            return 1;
        } catch (Exception e) {
            source.sendFailure(Component.literal("§8§l┃ §c✘ §fОшибка: " + e.getMessage()));
            return 0;
        }
    }

    private static int deleteNation(CommandSourceStack source) {
        try {
            ServerPlayer player = source.getPlayerOrException();
            Nation nation = NationsData.getNationByPlayer(player.getUUID());
            if (nation == null || !nation.getLeader().equals(player.getUUID())) {
                source.sendFailure(Component.literal("§8§l┃ §c✘ §fВы не лидер нации!"));
                return 0;
            }
            String nationName = nation.getName();
            NationsData.removeNation(nationName);

            source.getServer().getPlayerList().broadcastSystemMessage(
                Component.literal(
                    "\n§8§l╔══════════════════════════════════╗\n" +
                    "§8§l║  §c§l🏛 НАЦИЯ РАСПУЩЕНА!              §8§l║\n" +
                    "§8§l║                                    §8§l║\n" +
                    "§8§l║  §fНация §e" + nationName + " §fбольше не существует  §8§l║\n" +
                    "§8§l╚══════════════════════════════════╝\n"), false);
            return 1;
        } catch (Exception e) {
            source.sendFailure(Component.literal("§8§l┃ §c✘ §fОшибка: " + e.getMessage()));
            return 0;
        }
    }

    private static int inviteTown(CommandSourceStack source, String townName) {
        try {
            ServerPlayer player = source.getPlayerOrException();
            Nation nation = NationsData.getNationByPlayer(player.getUUID());
            if (nation == null || !nation.getLeader().equals(player.getUUID())) {
                source.sendFailure(Component.literal("§8§l┃ §c✘ §fВы не лидер нации!"));
                return 0;
            }
            Town town = NationsData.getTown(townName);
            if (town == null) {
                source.sendFailure(Component.literal("§8§l┃ §c✘ §fГород не найден!"));
                return 0;
            }
            if (town.getNationName() != null) {
                source.sendFailure(Component.literal("§8§l┃ §c✘ §fЭтот город уже в нации!"));
                return 0;
            }
            nation.getPendingInvites().add(town.getMayor());
            NationsData.save();

            ServerPlayer mayor = source.getServer().getPlayerList().getPlayer(town.getMayor());
            if (mayor != null) {
                mayor.sendSystemMessage(Component.literal(
                    "\n§8§l╔══════════════════════════════════╗\n" +
                    "§8§l║  §a§l📩 ПРИГЛАШЕНИЕ В НАЦИЮ          §8§l║\n" +
                    "§8§l║                                    §8§l║\n" +
                    "§8§l║  §fНация §e§l" + nation.getName() + " §fприглашает       §8§l║\n" +
                    "§8§l║  §fваш город присоединиться!       §8§l║\n" +
                    "§8§l║                                    §8§l║\n" +
                    "§8§l║  §fВведите: §a/nation accept " + nation.getName() + " §8§l║\n" +
                    "§8§l╚══════════════════════════════════╝\n"));
            }
            source.sendSuccess(() -> Component.literal(
                "§8§l┃ §a✔ §fПриглашение отправлено городу §e" + townName), true);
            return 1;
        } catch (Exception e) {
            source.sendFailure(Component.literal("§8§l┃ §c✘ §fОшибка: " + e.getMessage()));
            return 0;
        }
    }

    private static int acceptInvite(CommandSourceStack source, String nationName) {
        try {
            ServerPlayer player = source.getPlayerOrException();
            Town town = NationsData.getTownByPlayer(player.getUUID());
            if (town == null || !town.hasPermission(player.getUUID(), TownRole.RULER)) {
                source.sendFailure(Component.literal("§8§l┃ §c✘ §fВы не §6👑 Правитель §fгорода!"));
                return 0;
            }
            Nation nation = NationsData.getNation(nationName);
            if (nation == null) {
                source.sendFailure(Component.literal("§8§l┃ §c✘ §fНация не найдена!"));
                return 0;
            }
            if (!nation.getPendingInvites().contains(player.getUUID())) {
                source.sendFailure(Component.literal("§8§l┃ §c✘ §fУ вас нет приглашения!"));
                return 0;
            }
            nation.getPendingInvites().remove(player.getUUID());
            nation.addTown(town.getName());
            town.setNationName(nation.getName());
            town.addLog("Город вступил в нацию " + nation.getName());
            NationsData.save();

            source.getServer().getPlayerList().broadcastSystemMessage(
                Component.literal(
                    "§8§l┃ §a🏛 §fГород §e" + town.getName() +
                    " §fвступил в нацию §e" + nation.getName()), false);
            return 1;
        } catch (Exception e) {
            source.sendFailure(Component.literal("§8§l┃ §c✘ §fОшибка: " + e.getMessage()));
            return 0;
        }
    }

    private static int leaveNation(CommandSourceStack source) {
        try {
            ServerPlayer player = source.getPlayerOrException();
            Town town = NationsData.getTownByPlayer(player.getUUID());
            if (town == null || !town.hasPermission(player.getUUID(), TownRole.RULER)) {
                source.sendFailure(Component.literal("§8§l┃ §c✘ §fВы не §6👑 Правитель!"));
                return 0;
            }
            if (town.getNationName() == null) {
                source.sendFailure(Component.literal("§8§l┃ §c✘ §fВаш город не в нации!"));
                return 0;
            }
            Nation nation = NationsData.getNation(town.getNationName());
            if (nation != null) {
                if (nation.getLeader().equals(player.getUUID())) {
                    source.sendFailure(Component.literal(
                        "§8§l┃ §c✘ §fЛидер нации не может выйти!\n" +
                        "§8§l┃ §7Используйте §f/nation delete"));
                    return 0;
                }
                nation.removeTown(town.getName());
            }
            town.setNationName(null);
            town.addLog("Город покинул нацию");
            NationsData.save();
            source.sendSuccess(() -> Component.literal("§8§l┃ §a✔ §fВаш город покинул нацию."), true);
            return 1;
        } catch (Exception e) {
            source.sendFailure(Component.literal("§8§l┃ §c✘ §fОшибка: " + e.getMessage()));
            return 0;
        }
    }

    private static int kickTown(CommandSourceStack source, String townName) {
        try {
            ServerPlayer player = source.getPlayerOrException();
            Nation nation = NationsData.getNationByPlayer(player.getUUID());
            if (nation == null || !nation.getLeader().equals(player.getUUID())) {
                source.sendFailure(Component.literal("§8§l┃ §c✘ §fВы не лидер нации!"));
                return 0;
            }
            Town town = NationsData.getTown(townName);
            if (town == null || !nation.hasTown(townName)) {
                source.sendFailure(Component.literal("§8§l┃ §c✘ §fГород не в вашей нации!"));
                return 0;
            }
            nation.removeTown(townName);
            town.setNationName(null);
            NationsData.save();
            source.sendSuccess(() -> Component.literal(
                "§8§l┃ §a✔ §fГород §e" + townName + " §fисключён из нации."), true);
            return 1;
        } catch (Exception e) {
            source.sendFailure(Component.literal("§8§l┃ §c✘ §fОшибка: " + e.getMessage()));
            return 0;
        }
    }

    private static int changeColor(CommandSourceStack source, String colorId) {
        try {
            ServerPlayer player = source.getPlayerOrException();
            Nation nation = NationsData.getNationByPlayer(player.getUUID());
            if (nation == null || !nation.getLeader().equals(player.getUUID())) {
                source.sendFailure(Component.literal("§8§l┃ §c✘ §fВы не лидер нации!"));
                return 0;
            }
            NationColor color = NationColor.fromId(colorId);
            if (color == null) {
                source.sendFailure(Component.literal("§8§l┃ §c✘ §fНеизвестный цвет! Используйте §e/nation colors"));
                return 0;
            }
            if (NationsData.isColorTaken(color) && nation.getColor() != color) {
                source.sendFailure(Component.literal("§8§l┃ §c✘ §fЭтот цвет уже занят!"));
                return 0;
            }
            nation.setColor(color);
            NationsData.save();
            source.sendSuccess(() -> Component.literal(
                "§8§l┃ §a✔ §fЦвет нации изменён на §e" + color.getDisplayName()), true);
            return 1;
        } catch (Exception e) {
            source.sendFailure(Component.literal("§8§l┃ §c✘ §fОшибка: " + e.getMessage()));
            return 0;
        }
    }

    private static int setNationTax(CommandSourceStack source, double rate) {
        try {
            ServerPlayer player = source.getPlayerOrException();
            Nation nation = NationsData.getNationByPlayer(player.getUUID());
            if (nation == null || !nation.getLeader().equals(player.getUUID())) {
                source.sendFailure(Component.literal("§8§l┃ §c✘ §fВы не лидер нации!"));
                return 0;
            }
            nation.setNationTaxRate(rate / 100.0);
            NationsData.save();
            source.sendSuccess(() -> Component.literal(
                "§8§l┃ §a✔ §fНалог нации установлен: §e" + rate + "%"), true);
            return 1;
        } catch (Exception e) {
            source.sendFailure(Component.literal("§8§l┃ §c✘ §fОшибка: " + e.getMessage()));
            return 0;
        }
    }

    // === ВОЙНА — ТОЛЬКО ОПЕРАТОР ===
    private static int declareWar(CommandSourceStack source, String attackerName, String defenderName) {
        try {
            Nation attacker = NationsData.getNation(attackerName);
            Nation defender = NationsData.getNation(defenderName);
            if (attacker == null) {
                source.sendFailure(Component.literal("§8§l┃ §c✘ §fНация §e" + attackerName + " §fне найдена!"));
                return 0;
            }
            if (defender == null) {
                source.sendFailure(Component.literal("§8§l┃ §c✘ §fНация §e" + defenderName + " §fне найдена!"));
                return 0;
            }
            if (attacker.getName().equalsIgnoreCase(defender.getName())) {
                source.sendFailure(Component.literal("§8§l┃ §c✘ §fНельзя объявить войну себе!"));
                return 0;
            }
            if (NationsData.areAllied(attacker.getName(), defender.getName())) {
                source.sendFailure(Component.literal("§8§l┃ §c✘ §fЭти нации в альянсе!"));
                return 0;
            }
            if (attacker.isAtWarWith(defender.getName())) {
                source.sendFailure(Component.literal("§8§l┃ §c✘ §fЭти нации уже воюют!"));
                return 0;
            }

            attacker.declareWar(defender.getName());
            defender.declareWar(attacker.getName());

            for (String townName : attacker.getTowns()) {
                Town t = NationsData.getTown(townName);
                if (t != null) {
                    t.setAtWar(true);
                    t.setPvpEnabled(true);
                    t.setDestructionEnabled(true);
                    t.addLog("ВОЙНА объявлена против " + defender.getName());
                }
            }
            for (String townName : defender.getTowns()) {
                Town t = NationsData.getTown(townName);
                if (t != null) {
                    t.setAtWar(true);
                    t.setPvpEnabled(true);
                    t.setDestructionEnabled(true);
                    t.addLog("ВОЙНА объявлена со стороны " + attacker.getName());
                }
            }
            NationsData.save();

            source.getServer().getPlayerList().broadcastSystemMessage(
                Component.literal(
                    "\n§4§l╔══════════════════════════════════╗\n" +
                    "§4§l║                                    ║\n" +
                    "§4§l║     §c§l⚔⚔⚔ ОБЪЯВЛЕНА ВОЙНА! ⚔⚔⚔     §4§l║\n" +
                    "§4§l║                                    ║\n" +
                    "§4§l║  §e§l" + attacker.getName() + "  §c§lпротив  §e§l" + defender.getName() + "        §4§l║\n" +
                    "§4§l║                                    ║\n" +
                    "§4§l║  §fPvP и разрушение включены!       §4§l║\n" +
                    "§4§l║  §fНа территориях обеих наций       §4§l║\n" +
                    "§4§l║                                    ║\n" +
                    "§4§l║  §7Невраждующие игроки не могут     §4§l║\n" +
                    "§4§l║  §7приближаться к зоне боевых       §4§l║\n" +
                    "§4§l║  §7действий ближе чем на 50 блоков  §4§l║\n" +
                    "§4§l║                                    ║\n" +
                    "§4§l╚══════════════════════════════════╝\n"), false);
            return 1;
        } catch (Exception e) {
            source.sendFailure(Component.literal("§8§l┃ §c✘ §fОшибка: " + e.getMessage()));
            return 0;
        }
    }

    private static int endWar(CommandSourceStack source, String nation1Name, String nation2Name) {
        try {
            Nation nation1 = NationsData.getNation(nation1Name);
            Nation nation2 = NationsData.getNation(nation2Name);
            if (nation1 == null || nation2 == null) {
                source.sendFailure(Component.literal("§8§l┃ §c✘ §fОдна из наций не найдена!"));
                return 0;
            }
            if (!nation1.isAtWarWith(nation2.getName())) {
                source.sendFailure(Component.literal("§8§l┃ §c✘ §fЭти нации не воюют!"));
                return 0;
            }

            nation1.endWar(nation2.getName());
            nation2.endWar(nation1.getName());

            for (String townName : nation1.getTowns()) {
                Town t = NationsData.getTown(townName);
                if (t != null) {
                    t.setAtWar(false);
                    t.setPvpEnabled(false);
                    t.setDestructionEnabled(false);
                    t.addLog("МИР заключён с " + nation2.getName());
                }
            }
            for (String townName : nation2.getTowns()) {
                Town t = NationsData.getTown(townName);
                if (t != null) {
                    t.setAtWar(false);
                    t.setPvpEnabled(false);
                    t.setDestructionEnabled(false);
                    t.addLog("МИР заключён с " + nation1.getName());
                }
            }
            NationsData.save();

            source.getServer().getPlayerList().broadcastSystemMessage(
                Component.literal(
                    "\n§a§l╔══════════════════════════════════╗\n" +
                    "§a§l║                                    ║\n" +
                    "§a§l║      §f§l☮☮☮ ЗАКЛЮЧЁН МИР! ☮☮☮       §a§l║\n" +
                    "§a§l║                                    ║\n" +
                    "§a§l║  §e§l" + nation1.getName() + "  §a§lи  §e§l" + nation2.getName() + "              §a§l║\n" +
                    "§a§l║                                    ║\n" +
                    "§a§l║  §fPvP и разрушение отключены!      §a§l║\n" +
                    "§a§l║  §fТерритории снова защищены        §a§l║\n" +
                    "§a§l║                                    ║\n" +
                    "§a§l╚══════════════════════════════════╝\n"), false);
            return 1;
        } catch (Exception e) {
            source.sendFailure(Component.literal("§8§l┃ §c✘ §fОшибка: " + e.getMessage()));
            return 0;
        }
    }

    private static int captureTown(CommandSourceStack source, String townName) {
        try {
            ServerPlayer player = source.getPlayerOrException();
            Nation nation = NationsData.getNationByPlayer(player.getUUID());
            if (nation == null || !nation.getLeader().equals(player.getUUID())) {
                source.sendFailure(Component.literal("§8§l┃ §c✘ §fВы не лидер нации!"));
                return 0;
            }
            Town town = NationsData.getTown(townName);
            if (town == null) {
                source.sendFailure(Component.literal("§8§l┃ §c✘ §fГород не найден!"));
                return 0;
            }
            if (town.getNationName() == null) {
                source.sendFailure(Component.literal("§8§l┃ §c✘ §fГород не принадлежит нации!"));
                return 0;
            }
            if (town.getNationName().equalsIgnoreCase(nation.getName())) {
                source.sendFailure(Component.literal("§8§l┃ §c✘ §fЭто ваш город!"));
                return 0;
            }
            Nation targetNation = NationsData.getNation(town.getNationName());
            if (targetNation == null || !nation.isAtWarWith(targetNation.getName())) {
                source.sendFailure(Component.literal("§8§l┃ §c✘ §fВы не воюете с нацией этого города!"));
                return 0;
            }

            ChunkPos playerChunk = new ChunkPos(player.blockPosition());
            if (!town.ownsChunk(playerChunk)) {
                source.sendFailure(Component.literal(
                    "§8§l┃ §c✘ §fВы должны находиться на территории этого города!"));
                return 0;
            }

            town.setCaptured(true);
            town.setCapturedBy(nation.getName());
            town.addLog("Город ЗАХВАЧЕН нацией " + nation.getName());
            nation.addTownCaptured();
            NationsData.save();

            source.getServer().getPlayerList().broadcastSystemMessage(
                Component.literal(
                    "\n§6§l╔══════════════════════════════════╗\n" +
                    "§6§l║                                    ║\n" +
                    "§6§l║      §c§l🏴 ГОРОД ЗАХВАЧЕН! 🏴         §6§l║\n" +
                    "§6§l║                                    ║\n" +
                    "§6§l║  §fГород §e§l" + town.getName() + "                      §6§l║\n" +
                    "§6§l║  §fзахвачен нацией §e§l" + nation.getName() + "            §6§l║\n" +
                    "§6§l║                                    ║\n" +
                    "§6§l╚══════════════════════════════════╝\n"), false);
            return 1;
        } catch (Exception e) {
            source.sendFailure(Component.literal("§8§l┃ §c✘ §fОшибка: " + e.getMessage()));
            return 0;
        }
    }

    private static int surrender(CommandSourceStack source, String targetName) {
        try {
            ServerPlayer player = source.getPlayerOrException();
            Nation nation = NationsData.getNationByPlayer(player.getUUID());
            if (nation == null || !nation.getLeader().equals(player.getUUID())) {
                source.sendFailure(Component.literal("§8§l┃ §c✘ §fВы не лидер нации!"));
                return 0;
            }
            Nation target = NationsData.getNation(targetName);
            if (target == null || !nation.isAtWarWith(target.getName())) {
                source.sendFailure(Component.literal("§8§l┃ §c✘ §fВы не воюете с этой нацией!"));
                return 0;
            }

            nation.addWarLost();
            target.addWarWon();

            double lostTreasury = Economy.getNationBalance(nation.getName()) * 0.5;
            Economy.withdrawFromNation(nation.getName(), lostTreasury);
            Economy.depositToNation(target.getName(), lostTreasury);

            nation.endWar(target.getName());
            target.endWar(nation.getName());

            for (String tn : nation.getTowns()) {
                Town t = NationsData.getTown(tn);
                if (t != null) {
                    t.setAtWar(false);
                    t.setPvpEnabled(false);
                    t.setDestructionEnabled(false);
                    t.addLog("КАПИТУЛЯЦИЯ перед " + target.getName());
                }
            }
            for (String tn : target.getTowns()) {
                Town t = NationsData.getTown(tn);
                if (t != null) {
                    t.setAtWar(false);
                    t.setPvpEnabled(false);
                    t.setDestructionEnabled(false);
                    t.addLog("ПОБЕДА над " + nation.getName());
                }
            }
            NationsData.save();

            source.getServer().getPlayerList().broadcastSystemMessage(
                Component.literal(
                    "\n§c§l╔══════════════════════════════════╗\n" +
                    "§c§l║                                    ║\n" +
                    "§c§l║    §f§l🏳 КАПИТУЛЯЦИЯ! 🏳              §c§l║\n" +
                    "§c§l║                                    ║\n" +
                    "§c§l║  §fНация §e" + nation.getName() + " §fсдалась!            §c§l║\n" +
                    "§c§l║  §fПобедитель: §e" + target.getName() + "                §c§l║\n" +
                    "§c§l║  §fПередано: §6" + Economy.format(lostTreasury) + "         §c§l║\n" +
                    "§c§l║                                    ║\n" +
                    "§c§l╚══════════════════════════════════╝\n"), false);
            return 1;
        } catch (Exception e) {
            source.sendFailure(Component.literal("§8§l┃ §c✘ §fОшибка: " + e.getMessage()));
            return 0;
        }
    }

    private static int nationInfo(CommandSourceStack source) {
        try {
            ServerPlayer player = source.getPlayerOrException();
            Nation nation = NationsData.getNationByPlayer(player.getUUID());
            if (nation == null) {
                source.sendFailure(Component.literal("§8§l┃ §c✘ §fВы не в нации!"));
                return 0;
            }
            sendNationInfo(source, nation);
            return 1;
        } catch (Exception e) {
            source.sendFailure(Component.literal("§8§l┃ §c✘ §fОшибка: " + e.getMessage()));
            return 0;
        }
    }

    private static int nationInfoByName(CommandSourceStack source, String name) {
        Nation nation = NationsData.getNation(name);
        if (nation == null) {
            source.sendFailure(Component.literal("§8§l┃ §c✘ §fНация не найдена!"));
            return 0;
        }
        sendNationInfo(source, nation);
        return 1;
    }

    private static void sendNationInfo(CommandSourceStack source, Nation nation) {
        StringBuilder sb = new StringBuilder();
        sb.append("\n§8§l╔══════════════════════════════════╗\n");
        sb.append("§8§l║  §6§l🏛 ").append(nation.getName()).append("\n");
        sb.append("§8§l╠══════════════════════════════════╣\n");
        sb.append("§8§l║  §7Цвет: §f").append(nation.getColor().getDisplayName()).append("\n");
        sb.append("§8§l║  §7Рейтинг: §e⭐ ").append(nation.getRating()).append("\n");
        sb.append("§8§l║  §7Города: §f").append(String.join(", ", nation.getTowns())).append("\n");
        sb.append("§8§l║  §7Жителей: §f").append(nation.getTotalMembers()).append("\n");
        sb.append("§8§l║  §7Территория: §f").append(nation.getTotalChunks()).append(" §7чанков\n");
        sb.append("§8§l║  §7Налог: §f").append(String.format("%.1f%%", nation.getNationTaxRate() * 100)).append("\n");
        sb.append("§8§l║  §7Казна: §6").append(Economy.format(Economy.getNationBalance(nation.getName()))).append("\n");
        sb.append("§8§l║  §7Побед: §a").append(nation.getWarsWon());
        sb.append(" §8| §7Поражений: §c").append(nation.getWarsLost());
        sb.append(" §8| §7Захватов: §e").append(nation.getTownsCaptured()).append("\n");
        if (nation.getAllianceName() != null)
            sb.append("§8§l║  §7Альянс: §d").append(nation.getAllianceName()).append("\n");
        sb.append("§8§l║  §7Войны: ");
        if (nation.getWarTargets().isEmpty()) sb.append("§a☮ нет");
        else sb.append("§c⚔ ").append(String.join(", ", nation.getWarTargets()));
        sb.append("\n");

        // Дипломатия
        if (!nation.getAllDiplomacy().isEmpty()) {
            sb.append("§8§l╠══ §7§lДИПЛОМАТИЯ §8§l══╣\n");
            for (var e : nation.getAllDiplomacy().entrySet()) {
                String status;
                switch (e.getValue()) {
                    case "friendly": status = "§a🤝 Дружественный"; break;
                    case "hostile": status = "§c⚔ Враждебный"; break;
                    default: status = "§7◆ Нейтральный"; break;
                }
                sb.append("§8§l║  §e").append(e.getKey()).append(" §8— ").append(status).append("\n");
            }
        }

        sb.append("§8§l╚══════════════════════════════════╝\n");
        source.sendSuccess(() -> Component.literal(sb.toString()), false);
    }

    private static int listNations(CommandSourceStack source) {
        var all = NationsData.getAllNations();
        if (all.isEmpty()) {
            source.sendSuccess(() -> Component.literal("§8§l┃ §7Наций пока нет."), false);
            return 1;
        }
        StringBuilder sb = new StringBuilder("\n§8§l╔══ §6§l🏛 НАЦИИ §8§l══╗\n");
        for (Nation n : all) {
            sb.append("§8§l║ §e").append(n.getName());
            sb.append(" §8[§f").append(n.getColor().getDisplayName()).append("§8]");
            sb.append(" §7⭐").append(n.getRating());
            if (!n.getWarTargets().isEmpty()) sb.append(" §c⚔");
            sb.append("\n");
        }
        sb.append("§8§l╚═══════════════╝\n");
        source.sendSuccess(() -> Component.literal(sb.toString()), false);
        return 1;
    }

    private static int listColors(CommandSourceStack source) {
        StringBuilder sb = new StringBuilder("\n§8§l╔══ §6§l🎨 ЦВЕТА §8§l══╗\n");
        for (NationColor c : NationColor.values()) {
            boolean taken = NationsData.isColorTaken(c);
            sb.append("§8§l║ ").append(taken ? "§c✘ " : "§a✔ ");
            sb.append("§e").append(c.getId());
            sb.append(" §8(§f").append(c.getDisplayName()).append("§8)");
            sb.append(taken ? " §c[занят]" : " §a[свободен]");
            sb.append("\n");
        }
        sb.append("§8§l╚═══════════════╝\n");
        source.sendSuccess(() -> Component.literal(sb.toString()), false);
        return 1;
    }
}
