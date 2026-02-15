package com.nations.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.nations.data.*;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

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
            .then(Commands.literal("war")
                .then(Commands.literal("declare")
                    .then(Commands.argument("nation", StringArgumentType.word())
                        .executes(ctx -> declareWar(ctx.getSource(),
                            StringArgumentType.getString(ctx, "nation")))))
                .then(Commands.literal("end")
                    .then(Commands.argument("nation", StringArgumentType.word())
                        .executes(ctx -> endWar(ctx.getSource(),
                            StringArgumentType.getString(ctx, "nation")))))
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
                    "§cВы должны быть Правителем города чтобы создать нацию!"));
                return 0;
            }
            if (town.getNationName() != null) {
                source.sendFailure(Component.literal("§cВаш город уже в нации!"));
                return 0;
            }
            if (NationsData.nationExists(name)) {
                source.sendFailure(Component.literal("§cНация с таким именем уже существует!"));
                return 0;
            }
            NationColor color = NationColor.fromId(colorId);
            if (color == null) {
                source.sendFailure(Component.literal(
                    "§cНеизвестный цвет! Используйте /nation colors"));
                return 0;
            }
            if (NationsData.isColorTaken(color)) {
                source.sendFailure(Component.literal("§cЭтот цвет уже занят!"));
                return 0;
            }

            Nation nation = new Nation(name, uuid, color);
            nation.addTown(town.getName());
            town.setNationName(name);
            NationsData.addNation(nation);
            NationsData.save();

            source.getServer().getPlayerList().broadcastSystemMessage(
                Component.literal("§6§l🏛 Создана нация: §e" + name +
                    " §6[" + color.getDisplayName() + "]"), false);
            return 1;
        } catch (Exception e) {
            source.sendFailure(Component.literal("§cОшибка: " + e.getMessage()));
            return 0;
        }
    }

    private static int deleteNation(CommandSourceStack source) {
        try {
            ServerPlayer player = source.getPlayerOrException();
            Nation nation = NationsData.getNationByPlayer(player.getUUID());
            if (nation == null || !nation.getLeader().equals(player.getUUID())) {
                source.sendFailure(Component.literal("§cВы не лидер нации!"));
                return 0;
            }
            for (String townName : nation.getTowns()) {
                Town t = NationsData.getTown(townName);
                if (t != null) {
                    t.setNationName(null);
                    t.setAtWar(false);
                    t.setCaptured(false);
                    t.setCapturedBy(null);
                }
            }
            // Удалить из альянса
            if (nation.getAllianceName() != null) {
                Alliance a = NationsData.getAlliance(nation.getAllianceName());
                if (a != null) a.removeMember(nation.getName());
            }
            String nationName = nation.getName();
            NationsData.removeNation(nationName);
            NationsData.save();

            source.getServer().getPlayerList().broadcastSystemMessage(
                Component.literal("§c§l🏛 Нация §e" + nationName + "§c была распущена!"), false);
            return 1;
        } catch (Exception e) {
            source.sendFailure(Component.literal("§cОшибка: " + e.getMessage()));
            return 0;
        }
    }

    private static int inviteTown(CommandSourceStack source, String townName) {
        try {
            ServerPlayer player = source.getPlayerOrException();
            Nation nation = NationsData.getNationByPlayer(player.getUUID());
            if (nation == null || !nation.getLeader().equals(player.getUUID())) {
                source.sendFailure(Component.literal("§cВы не лидер нации!"));
                return 0;
            }
            Town town = NationsData.getTown(townName);
            if (town == null) {
                source.sendFailure(Component.literal("§cГород не найден!"));
                return 0;
            }
            if (town.getNationName() != null) {
                source.sendFailure(Component.literal("§cЭтот город уже в нации!"));
                return 0;
            }
            nation.getPendingInvites().add(town.getMayor());
            NationsData.save();

            ServerPlayer mayor = source.getServer().getPlayerList().getPlayer(town.getMayor());
            if (mayor != null) {
                mayor.sendSystemMessage(Component.literal(
                    "§a🏛 Ваш город приглашён в нацию §e" + nation.getName() +
                    "§a! Напишите §e/nation accept " + nation.getName()));
            }
            source.sendSuccess(() -> Component.literal(
                "§aПриглашение отправлено городу §e" + townName), true);
            return 1;
        } catch (Exception e) {
            source.sendFailure(Component.literal("§cОшибка: " + e.getMessage()));
            return 0;
        }
    }

    private static int acceptInvite(CommandSourceStack source, String nationName) {
        try {
            ServerPlayer player = source.getPlayerOrException();
            Town town = NationsData.getTownByPlayer(player.getUUID());
            if (town == null || !town.hasPermission(player.getUUID(), TownRole.RULER)) {
                source.sendFailure(Component.literal("§cВы не Правитель города!"));
                return 0;
            }
            Nation nation = NationsData.getNation(nationName);
            if (nation == null) {
                source.sendFailure(Component.literal("§cНация не найдена!"));
                return 0;
            }
            if (!nation.getPendingInvites().contains(player.getUUID())) {
                source.sendFailure(Component.literal("§cУ вас нет приглашения!"));
                return 0;
            }
            nation.getPendingInvites().remove(player.getUUID());
            nation.addTown(town.getName());
            town.setNationName(nation.getName());
            NationsData.save();

            source.getServer().getPlayerList().broadcastSystemMessage(
                Component.literal("§a🏛 Город §e" + town.getName() +
                    "§a присоединился к нации §e" + nation.getName()), false);
            return 1;
        } catch (Exception e) {
            source.sendFailure(Component.literal("§cОшибка: " + e.getMessage()));
            return 0;
        }
    }

    private static int leaveNation(CommandSourceStack source) {
        try {
            ServerPlayer player = source.getPlayerOrException();
            Town town = NationsData.getTownByPlayer(player.getUUID());
            if (town == null || !town.hasPermission(player.getUUID(), TownRole.RULER)) {
                source.sendFailure(Component.literal("§cВы не Правитель!"));
                return 0;
            }
            if (town.getNationName() == null) {
                source.sendFailure(Component.literal("§cВаш город не в нации!"));
                return 0;
            }
            Nation nation = NationsData.getNation(town.getNationName());
            if (nation != null) {
                if (nation.getLeader().equals(player.getUUID())) {
                    source.sendFailure(Component.literal(
                        "§cЛидер нации не может выйти! Используйте /nation delete"));
                    return 0;
                }
                nation.removeTown(town.getName());
            }
            town.setNationName(null);
            NationsData.save();
            source.sendSuccess(() -> Component.literal("§aВаш город покинул нацию."), true);
            return 1;
        } catch (Exception e) {
            source.sendFailure(Component.literal("§cОшибка: " + e.getMessage()));
            return 0;
        }
    }

    private static int kickTown(CommandSourceStack source, String townName) {
        try {
            ServerPlayer player = source.getPlayerOrException();
            Nation nation = NationsData.getNationByPlayer(player.getUUID());
            if (nation == null || !nation.getLeader().equals(player.getUUID())) {
                source.sendFailure(Component.literal("§cВы не лидер нации!"));
                return 0;
            }
            Town town = NationsData.getTown(townName);
            if (town == null || !nation.hasTown(townName)) {
                source.sendFailure(Component.literal("§cГород не в вашей нации!"));
                return 0;
            }
            nation.removeTown(townName);
            town.setNationName(null);
            NationsData.save();
            source.sendSuccess(() -> Component.literal(
                "§aГород §e" + townName + "§a исключён из нации."), true);
            return 1;
        } catch (Exception e) {
            source.sendFailure(Component.literal("§cОшибка: " + e.getMessage()));
            return 0;
        }
    }

    private static int changeColor(CommandSourceStack source, String colorId) {
        try {
            ServerPlayer player = source.getPlayerOrException();
            Nation nation = NationsData.getNationByPlayer(player.getUUID());
            if (nation == null || !nation.getLeader().equals(player.getUUID())) {
                source.sendFailure(Component.literal("§cВы не лидер нации!"));
                return 0;
            }
            NationColor color = NationColor.fromId(colorId);
            if (color == null) {
                source.sendFailure(Component.literal("§cНеизвестный цвет!"));
                return 0;
            }
            if (NationsData.isColorTaken(color) && nation.getColor() != color) {
                source.sendFailure(Component.literal("§cЦвет занят!"));
                return 0;
            }
            nation.setColor(color);
            NationsData.save();
            source.sendSuccess(() -> Component.literal(
                "§aЦвет нации: §e" + color.getDisplayName()), true);
            return 1;
        } catch (Exception e) {
            source.sendFailure(Component.literal("§cОшибка: " + e.getMessage()));
            return 0;
        }
    }

    private static int setNationTax(CommandSourceStack source, double rate) {
        try {
            ServerPlayer player = source.getPlayerOrException();
            Nation nation = NationsData.getNationByPlayer(player.getUUID());
            if (nation == null || !nation.getLeader().equals(player.getUUID())) {
                source.sendFailure(Component.literal("§cВы не лидер нации!"));
                return 0;
            }
            nation.setNationTaxRate(rate / 100.0);
            NationsData.save();
            source.sendSuccess(() -> Component.literal(
                "§aНалог нации установлен: §e" + rate + "%"), true);
            return 1;
        } catch (Exception e) {
            source.sendFailure(Component.literal("§cОшибка: " + e.getMessage()));
            return 0;
        }
    }

    private static int declareWar(CommandSourceStack source, String targetName) {
        try {
            ServerPlayer player = source.getPlayerOrException();
            Nation nation = NationsData.getNationByPlayer(player.getUUID());
            if (nation == null || !nation.getLeader().equals(player.getUUID())) {
                source.sendFailure(Component.literal("§cВы не лидер нации!"));
                return 0;
            }
            Nation target = NationsData.getNation(targetName);
            if (target == null) {
                source.sendFailure(Component.literal("§cНация не найдена!"));
                return 0;
            }
            if (target.getName().equalsIgnoreCase(nation.getName())) {
                source.sendFailure(Component.literal("§cНельзя объявить войну себе!"));
                return 0;
            }
            if (NationsData.areAllied(nation.getName(), target.getName())) {
                source.sendFailure(Component.literal("§cНельзя объявить войну союзнику!"));
                return 0;
            }
            if (nation.isAtWarWith(target.getName())) {
                source.sendFailure(Component.literal("§cВы уже воюете с этой нацией!"));
                return 0;
            }

            nation.declareWar(target.getName());
            target.declareWar(nation.getName());

            for (String townName : nation.getTowns()) {
                Town t = NationsData.getTown(townName);
                if (t != null) { t.setAtWar(true); t.setPvpEnabled(true); t.setDestructionEnabled(true); }
            }
            for (String townName : target.getTowns()) {
                Town t = NationsData.getTown(townName);
                if (t != null) { t.setAtWar(true); t.setPvpEnabled(true); t.setDestructionEnabled(true); }
            }
            NationsData.save();

            source.getServer().getPlayerList().broadcastSystemMessage(
                Component.literal("§4§l⚔ ВОЙНА! §cНация §e" + nation.getName() +
                    " §cобъявила войну нации §e" + target.getName() + "§c!"), false);
            return 1;
        } catch (Exception e) {
            source.sendFailure(Component.literal("§cОшибка: " + e.getMessage()));
            return 0;
        }
    }

    private static int endWar(CommandSourceStack source, String targetName) {
        try {
            ServerPlayer player = source.getPlayerOrException();
            Nation nation = NationsData.getNationByPlayer(player.getUUID());
            if (nation == null || !nation.getLeader().equals(player.getUUID())) {
                source.sendFailure(Component.literal("§cВы не лидер нации!"));
                return 0;
            }
            Nation target = NationsData.getNation(targetName);
            if (target == null) {
                source.sendFailure(Component.literal("§cНация не найдена!"));
                return 0;
            }
            if (!nation.isAtWarWith(target.getName())) {
                source.sendFailure(Component.literal("§cВы не воюете с этой нацией!"));
                return 0;
            }

            nation.endWar(target.getName());
            target.endWar(nation.getName());

            for (String townName : nation.getTowns()) {
                Town t = NationsData.getTown(townName);
                if (t != null) {
                    t.setAtWar(false);
                    t.setPvpEnabled(false);
                    t.setDestructionEnabled(false);
                }
            }
            for (String townName : target.getTowns()) {
                Town t = NationsData.getTown(townName);
                if (t != null) {
                    t.setAtWar(false);
                    t.setPvpEnabled(false);
                    t.setDestructionEnabled(false);
                }
            }
            NationsData.save();

            source.getServer().getPlayerList().broadcastSystemMessage(
                Component.literal("§a§l☮ МИР! §aНации §e" + nation.getName() +
                    " §aи §e" + target.getName() + " §aзаключили мир!"), false);
            return 1;
        } catch (Exception e) {
            source.sendFailure(Component.literal("§cОшибка: " + e.getMessage()));
            return 0;
        }
    }

    private static int captureTown(CommandSourceStack source, String townName) {
        try {
            ServerPlayer player = source.getPlayerOrException();
            Nation nation = NationsData.getNationByPlayer(player.getUUID());
            if (nation == null || !nation.getLeader().equals(player.getUUID())) {
                source.sendFailure(Component.literal("§cВы не лидер нации!"));
                return 0;
            }
            Town town = NationsData.getTown(townName);
            if (town == null) {
                source.sendFailure(Component.literal("§cГород не найден!"));
                return 0;
            }
            if (town.getNationName() == null) {
                source.sendFailure(Component.literal("§cГород не принадлежит нации!"));
                return 0;
            }
            if (town.getNationName().equalsIgnoreCase(nation.getName())) {
                source.sendFailure(Component.literal("§cЭто ваш город!"));
                return 0;
            }
            Nation targetNation = NationsData.getNation(town.getNationName());
            if (targetNation == null || !nation.isAtWarWith(targetNation.getName())) {
                source.sendFailure(Component.literal("§cВы не воюете с нацией этого города!"));
                return 0;
            }

            // Игрок должен находиться на территории этого города
            ChunkPos playerChunk = new ChunkPos(player.blockPosition());
            if (!town.ownsChunk(playerChunk)) {
                source.sendFailure(Component.literal(
                    "§cВы должны находиться на территории этого города!"));
                return 0;
            }

            // Захват
            town.setCaptured(true);
            town.setCapturedBy(nation.getName());
            nation.addTownCaptured();
            NationsData.save();

            source.getServer().getPlayerList().broadcastSystemMessage(
                Component.literal("§4§l🏴 ЗАХВАТ! §cГород §e" + town.getName() +
                    " §cзахвачен нацией §e" + nation.getName() + "§c!"), false);
            return 1;
        } catch (Exception e) {
            source.sendFailure(Component.literal("§cОшибка: " + e.getMessage()));
            return 0;
        }
    }

    private static int surrender(CommandSourceStack source, String targetName) {
        try {
            ServerPlayer player = source.getPlayerOrException();
            Nation nation = NationsData.getNationByPlayer(player.getUUID());
            if (nation == null || !nation.getLeader().equals(player.getUUID())) {
                source.sendFailure(Component.literal("§cВы не лидер нации!"));
                return 0;
            }
            Nation target = NationsData.getNation(targetName);
            if (target == null || !nation.isAtWarWith(target.getName())) {
                source.sendFailure(Component.literal("§cВы не воюете с этой нацией!"));
                return 0;
            }

            // Капитуляция — враг побеждает
            nation.addWarLost();
            target.addWarWon();

            // Передать казну проигравшей нации
            double lostTreasury = Economy.getNationBalance(nation.getName()) * 0.5;
            Economy.withdrawFromNation(nation.getName(), lostTreasury);
            Economy.depositToNation(target.getName(), lostTreasury);

            // Завершить войну
            nation.endWar(target.getName());
            target.endWar(nation.getName());

            for (String tn : nation.getTowns()) {
                Town t = NationsData.getTown(tn);
                if (t != null) { t.setAtWar(false); t.setPvpEnabled(false); t.setDestructionEnabled(false); }
            }
            for (String tn : target.getTowns()) {
                Town t = NationsData.getTown(tn);
                if (t != null) { t.setAtWar(false); t.setPvpEnabled(false); t.setDestructionEnabled(false); }
            }
            NationsData.save();

            source.getServer().getPlayerList().broadcastSystemMessage(
                Component.literal("§c§l🏳 КАПИТУЛЯЦИЯ! §eНация " + nation.getName() +
                    " §cсдалась нации §e" + target.getName() +
                    "§c! Передано §e" + Economy.format(lostTreasury) + " §cказны!"), false);
            return 1;
        } catch (Exception e) {
            source.sendFailure(Component.literal("§cОшибка: " + e.getMessage()));
            return 0;
        }
    }

    private static int nationInfo(CommandSourceStack source) {
        try {
            ServerPlayer player = source.getPlayerOrException();
            Nation nation = NationsData.getNationByPlayer(player.getUUID());
            if (nation == null) {
                source.sendFailure(Component.literal("§cВы не в нации!"));
                return 0;
            }
            sendNationInfo(source, nation);
            return 1;
        } catch (Exception e) {
            source.sendFailure(Component.literal("§cОшибка: " + e.getMessage()));
            return 0;
        }
    }

    private static int nationInfoByName(CommandSourceStack source, String name) {
        Nation nation = NationsData.getNation(name);
        if (nation == null) {
            source.sendFailure(Component.literal("§cНация не найдена!"));
            return 0;
        }
        sendNationInfo(source, nation);
        return 1;
    }

    private static void sendNationInfo(CommandSourceStack source, Nation nation) {
        StringBuilder sb = new StringBuilder();
        sb.append("§6§l══════════════════════════\n");
        sb.append("§6§l  🏛 ").append(nation.getName()).append("\n");
        sb.append("§6§l══════════════════════════\n");
        sb.append("§7Цвет: §f").append(nation.getColor().getDisplayName()).append("\n");
        sb.append("§7Рейтинг: §e").append(nation.getRating()).append("\n");
        sb.append("§7Города: §f").append(String.join(", ", nation.getTowns())).append("\n");
        sb.append("§7Всего людей: §f").append(nation.getTotalMembers()).append("\n");
        sb.append("§7Всего чанков: §f").append(nation.getTotalChunks()).append("\n");
        sb.append("§7Налог нации: §f").append(String.format("%.1f%%", nation.getNationTaxRate() * 100)).append("\n");
        sb.append("§7Казна: §e").append(Economy.format(Economy.getNationBalance(nation.getName()))).append("\n");
        sb.append("§7Побед: §a").append(nation.getWarsWon());
        sb.append(" §7| Поражений: §c").append(nation.getWarsLost());
        sb.append(" §7| Захватов: §e").append(nation.getTownsCaptured()).append("\n");
        if (nation.getAllianceName() != null)
            sb.append("§7Альянс: §d").append(nation.getAllianceName()).append("\n");
        sb.append("§7Войны: §f");
        if (nation.getWarTargets().isEmpty()) sb.append("нет");
        else sb.append("§c").append(String.join(", ", nation.getWarTargets()));
        source.sendSuccess(() -> Component.literal(sb.toString()), false);
    }

    private static int listNations(CommandSourceStack source) {
        var all = NationsData.getAllNations();
        if (all.isEmpty()) {
            source.sendSuccess(() -> Component.literal("§7Наций пока нет."), false);
            return 1;
        }
        StringBuilder sb = new StringBuilder("§6=== 🏛 Нации ===\n");
        for (Nation n : all) {
            sb.append("§e").append(n.getName())
              .append(" §7[").append(n.getColor().getDisplayName())
              .append("] рейтинг: ").append(n.getRating())
              .append(" городов: ").append(n.getTowns().size());
            if (!n.getWarTargets().isEmpty()) sb.append(" §c⚔");
            sb.append("\n");
        }
        source.sendSuccess(() -> Component.literal(sb.toString()), false);
        return 1;
    }

    private static int listColors(CommandSourceStack source) {
        StringBuilder sb = new StringBuilder("§6=== 🎨 Цвета ===\n");
        for (NationColor c : NationColor.values()) {
            boolean taken = NationsData.isColorTaken(c);
            sb.append(taken ? "§c✘ " : "§a✔ ")
              .append("§e").append(c.getId())
              .append(" §7(").append(c.getDisplayName()).append(")")
              .append(taken ? " §c[занят]" : " §a[свободен]")
              .append("\n");
        }
        source.sendSuccess(() -> Component.literal(sb.toString()), false);
        return 1;
    }

    private static int ChunkPos(net.minecraft.core.BlockPos blockPosition) {
        return 0;
    }
}

// Вспомогательный класс для ChunkPos
class ChunkPos {
    public final int x, z;
    public ChunkPos(net.minecraft.core.BlockPos pos) {
        this.x = pos.getX() >> 4;
        this.z = pos.getZ() >> 4;
    }
}
