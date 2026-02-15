package com.nations.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.nations.data.NationsData;
import com.nations.data.Town;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.ChunkPos;

import java.util.UUID;

public class TownCommands {

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("town")
            .then(Commands.literal("create")
                .then(Commands.argument("name", StringArgumentType.word())
                    .executes(ctx -> createTown(ctx.getSource(),
                        StringArgumentType.getString(ctx, "name")))))
            .then(Commands.literal("delete")
                .executes(ctx -> deleteTown(ctx.getSource())))
            .then(Commands.literal("invite")
                .then(Commands.argument("player", StringArgumentType.word())
                    .executes(ctx -> invitePlayer(ctx.getSource(),
                        StringArgumentType.getString(ctx, "player")))))
            .then(Commands.literal("join")
                .then(Commands.argument("town", StringArgumentType.word())
                    .executes(ctx -> joinTown(ctx.getSource(),
                        StringArgumentType.getString(ctx, "town")))))
            .then(Commands.literal("leave")
                .executes(ctx -> leaveTown(ctx.getSource())))
            .then(Commands.literal("kick")
                .then(Commands.argument("player", StringArgumentType.word())
                    .executes(ctx -> kickPlayer(ctx.getSource(),
                        StringArgumentType.getString(ctx, "player")))))
            .then(Commands.literal("info")
                .executes(ctx -> townInfo(ctx.getSource()))
                .then(Commands.argument("name", StringArgumentType.word())
                    .executes(ctx -> townInfoByName(ctx.getSource(),
                        StringArgumentType.getString(ctx, "name")))))
            .then(Commands.literal("list")
                .executes(ctx -> listTowns(ctx.getSource())))
            .then(Commands.literal("pvp")
                .then(Commands.literal("on")
                    .executes(ctx -> setPvp(ctx.getSource(), true)))
                .then(Commands.literal("off")
                    .executes(ctx -> setPvp(ctx.getSource(), false))))
            .then(Commands.literal("destruction")
                .then(Commands.literal("on")
                    .executes(ctx -> setDestruction(ctx.getSource(), true)))
                .then(Commands.literal("off")
                    .executes(ctx -> setDestruction(ctx.getSource(), false))))
        );
    }

    private static int createTown(CommandSourceStack source, String name) {
        try {
            ServerPlayer player = source.getPlayerOrException();
            UUID uuid = player.getUUID();

            if (NationsData.getTownByPlayer(uuid) != null) {
                source.sendFailure(Component.literal("§cВы уже состоите в городе!"));
                return 0;
            }
            if (NationsData.townExists(name)) {
                source.sendFailure(Component.literal("§cГород с таким именем уже существует!"));
                return 0;
            }

            Town town = new Town(name, uuid);
            // Приватим чанк где стоит игрок как первый чанк города
            ChunkPos cp = new ChunkPos(player.blockPosition());
            if (NationsData.getTownByChunk(cp) != null) {
                source.sendFailure(Component.literal("§cЭтот чанк уже занят другим городом!"));
                return 0;
            }
            town.claimChunk(cp);
            NationsData.addTown(town);
            source.sendSuccess(() -> Component.literal(
                "§aГород §e" + name + "§a успешно создан! Первый чанк запривачен."), true);
            return 1;
        } catch (Exception e) {
            source.sendFailure(Component.literal("§cОшибка: " + e.getMessage()));
            return 0;
        }
    }

    private static int deleteTown(CommandSourceStack source) {
        try {
            ServerPlayer player = source.getPlayerOrException();
            Town town = NationsData.getTownByPlayer(player.getUUID());
            if (town == null) {
                source.sendFailure(Component.literal("§cВы не состоите в городе!"));
                return 0;
            }
            if (!town.getMayor().equals(player.getUUID())) {
                source.sendFailure(Component.literal("§cТолько мэр может удалить город!"));
                return 0;
            }
            // Убрать из нации
            if (town.getNationName() != null) {
                var nation = NationsData.getNation(town.getNationName());
                if (nation != null) {
                    nation.removeTown(town.getName());
                    NationsData.save();
                }
            }
            NationsData.removeTown(town.getName());
            source.sendSuccess(() -> Component.literal(
                "§aГород §e" + town.getName() + "§a удалён!"), true);
            return 1;
        } catch (Exception e) {
            source.sendFailure(Component.literal("§cОшибка: " + e.getMessage()));
            return 0;
        }
    }

    private static int invitePlayer(CommandSourceStack source, String playerName) {
        try {
            ServerPlayer sender = source.getPlayerOrException();
            Town town = NationsData.getTownByPlayer(sender.getUUID());
            if (town == null || !town.getMayor().equals(sender.getUUID())) {
                source.sendFailure(Component.literal("§cВы не мэр города!"));
                return 0;
            }
            ServerPlayer target = source.getServer().getPlayerList()
                .getPlayerByName(playerName);
            if (target == null) {
                source.sendFailure(Component.literal("§cИгрок не найден!"));
                return 0;
            }
            if (NationsData.getTownByPlayer(target.getUUID()) != null) {
                source.sendFailure(Component.literal("§cИгрок уже в городе!"));
                return 0;
            }
            // Сохраняем инвайт в самом городе через members (простой подход)
            target.sendSystemMessage(Component.literal(
                "§aВас пригласили в город §e" + town.getName() +
                "§a! Напишите §e/town join " + town.getName() + "§a чтобы принять."));
            source.sendSuccess(() -> Component.literal(
                "§aПриглашение отправлено игроку §e" + playerName), true);
            return 1;
        } catch (Exception e) {
            source.sendFailure(Component.literal("§cОшибка: " + e.getMessage()));
            return 0;
        }
    }

    private static int joinTown(CommandSourceStack source, String townName) {
        try {
            ServerPlayer player = source.getPlayerOrException();
            if (NationsData.getTownByPlayer(player.getUUID()) != null) {
                source.sendFailure(Component.literal("§cВы уже в городе!"));
                return 0;
            }
            Town town = NationsData.getTown(townName);
            if (town == null) {
                source.sendFailure(Component.literal("§cГород не найден!"));
                return 0;
            }
            town.addMember(player.getUUID());
            NationsData.save();
            source.sendSuccess(() -> Component.literal(
                "§aВы присоединились к городу §e" + town.getName()), true);
            return 1;
        } catch (Exception e) {
            source.sendFailure(Component.literal("§cОшибка: " + e.getMessage()));
            return 0;
        }
    }

    private static int leaveTown(CommandSourceStack source) {
        try {
            ServerPlayer player = source.getPlayerOrException();
            Town town = NationsData.getTownByPlayer(player.getUUID());
            if (town == null) {
                source.sendFailure(Component.literal("§cВы не в городе!"));
                return 0;
            }
            if (town.getMayor().equals(player.getUUID())) {
                source.sendFailure(Component.literal(
                    "§cМэр не может покинуть город! Удалите город командой /town delete"));
                return 0;
            }
            town.removeMember(player.getUUID());
            NationsData.save();
            source.sendSuccess(() -> Component.literal("§aВы покинули город."), true);
            return 1;
        } catch (Exception e) {
            source.sendFailure(Component.literal("§cОшибка: " + e.getMessage()));
            return 0;
        }
    }

    private static int kickPlayer(CommandSourceStack source, String playerName) {
        try {
            ServerPlayer sender = source.getPlayerOrException();
            Town town = NationsData.getTownByPlayer(sender.getUUID());
            if (town == null || !town.getMayor().equals(sender.getUUID())) {
                source.sendFailure(Component.literal("§cВы не мэр!"));
                return 0;
            }
            ServerPlayer target = source.getServer().getPlayerList()
                .getPlayerByName(playerName);
            if (target == null) {
                source.sendFailure(Component.literal("§cИгрок не найден!"));
                return 0;
            }
            if (!town.isMember(target.getUUID())) {
                source.sendFailure(Component.literal("§cИгрок не в вашем городе!"));
                return 0;
            }
            town.removeMember(target.getUUID());
            NationsData.save();
            target.sendSystemMessage(Component.literal(
                "§cВас выгнали из города " + town.getName()));
            source.sendSuccess(() -> Component.literal(
                "§aИгрок §e" + playerName + "§a выгнан из города."), true);
            return 1;
        } catch (Exception e) {
            source.sendFailure(Component.literal("§cОшибка: " + e.getMessage()));
            return 0;
        }
    }

    private static int townInfo(CommandSourceStack source) {
        try {
            ServerPlayer player = source.getPlayerOrException();
            Town town = NationsData.getTownByPlayer(player.getUUID());
            if (town == null) {
                source.sendFailure(Component.literal("§cВы не в городе!"));
                return 0;
            }
            sendTownInfo(source, town);
            return 1;
        } catch (Exception e) {
            source.sendFailure(Component.literal("§cОшибка: " + e.getMessage()));
            return 0;
        }
    }

    private static int townInfoByName(CommandSourceStack source, String name) {
        Town town = NationsData.getTown(name);
        if (town == null) {
            source.sendFailure(Component.literal("§cГород не найден!"));
            return 0;
        }
        sendTownInfo(source, town);
        return 1;
    }

    private static void sendTownInfo(CommandSourceStack source, Town town) {
        source.sendSuccess(() -> Component.literal(
            "§6=== Город: §e" + town.getName() + " §6===\n" +
            "§7Нация: §f" + (town.getNationName() != null ? town.getNationName() : "нет") + "\n" +
            "§7Участников: §f" + town.getMembers().size() + "\n" +
            "§7Чанков: §f" + town.getClaimedChunks().size() + "\n" +
            "§7PvP: " + (town.isPvpEnabled() ? "§aВКЛ" : "§cВЫКЛ") + "\n" +
            "§7Разрушение: " + (town.isDestructionEnabled() ? "§aВКЛ" : "§cВЫКЛ") + "\n" +
            "§7Война: " + (town.isAtWar() ? "§cДА" : "§aНЕТ")
        ), false);
    }

    private static int listTowns(CommandSourceStack source) {
        var allTowns = NationsData.getAllTowns();
        if (allTowns.isEmpty()) {
            source.sendSuccess(() -> Component.literal("§7Городов пока нет."), false);
            return 1;
        }
        StringBuilder sb = new StringBuilder("§6=== Города ===\n");
        for (Town t : allTowns) {
            sb.append("§e").append(t.getName())
              .append(" §7[").append(t.getMembers().size()).append(" чел.] ")
              .append(t.getNationName() != null ? "§9" + t.getNationName() : "§8без нации")
              .append("\n");
        }
        source.sendSuccess(() -> Component.literal(sb.toString()), false);
        return 1;
    }

    private static int setPvp(CommandSourceStack source, boolean enabled) {
        try {
            ServerPlayer player = source.getPlayerOrException();
            Town town = NationsData.getTownByPlayer(player.getUUID());
            if (town == null || !town.getMayor().equals(player.getUUID())) {
                source.sendFailure(Component.literal("§cВы не мэр!"));
                return 0;
            }
            town.setPvpEnabled(enabled);
            NationsData.save();
            source.sendSuccess(() -> Component.literal(
                "§aPvP в городе " + (enabled ? "§aвключён" : "§cвыключен")), true);
            return 1;
        } catch (Exception e) {
            source.sendFailure(Component.literal("§cОшибка: " + e.getMessage()));
            return 0;
        }
    }

    private static int setDestruction(CommandSourceStack source, boolean enabled) {
        try {
            ServerPlayer player = source.getPlayerOrException();
            Town town = NationsData.getTownByPlayer(player.getUUID());
            if (town == null || !town.getMayor().equals(player.getUUID())) {
                source.sendFailure(Component.literal("§cВы не мэр!"));
                return 0;
            }
            town.setDestructionEnabled(enabled);
            NationsData.save();
            source.sendSuccess(() -> Component.literal(
                "§aРазрушение в городе " + (enabled ? "§aвключено" : "§cвыключено")), true);
            return 1;
        } catch (Exception e) {
            source.sendFailure(Component.literal("§cОшибка: " + e.getMessage()));
            return 0;
        }
    }
}