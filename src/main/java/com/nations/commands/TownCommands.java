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

import java.util.UUID;

public class TownCommands {

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("town")
            .then(Commands.literal("create")
                .then(Commands.argument("name", StringArgumentType.string())
                    .executes(ctx -> createTown(ctx.getSource(),
                        StringArgumentType.getString(ctx, "name")))))
            .then(Commands.literal("delete")
                .executes(ctx -> deleteTown(ctx.getSource())))
            .then(Commands.literal("invite")
                .then(Commands.argument("player", StringArgumentType.word())
                    .executes(ctx -> invitePlayer(ctx.getSource(),
                        StringArgumentType.getString(ctx, "player")))))
            .then(Commands.literal("join")
                .then(Commands.argument("town", StringArgumentType.string())
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
                .then(Commands.argument("name", StringArgumentType.string())
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
            .then(Commands.literal("role")
                .then(Commands.literal("set")
                    .then(Commands.argument("player", StringArgumentType.word())
                        .then(Commands.argument("role", StringArgumentType.word())
                            .executes(ctx -> setRole(ctx.getSource(),
                                StringArgumentType.getString(ctx, "player"),
                                StringArgumentType.getString(ctx, "role")))))))
            .then(Commands.literal("roles")
                .executes(ctx -> listRoles(ctx.getSource())))
            .then(Commands.literal("members")
                .executes(ctx -> listMembers(ctx.getSource())))
            .then(Commands.literal("spawn")
                .executes(ctx -> teleportSpawn(ctx.getSource())))
            .then(Commands.literal("setspawn")
                .executes(ctx -> setSpawn(ctx.getSource())))
            .then(Commands.literal("transfer")
                .then(Commands.argument("player", StringArgumentType.word())
                    .executes(ctx -> transferTown(ctx.getSource(),
                        StringArgumentType.getString(ctx, "player")))))
            .then(Commands.literal("plot")
                .then(Commands.literal("assign")
                    .then(Commands.argument("player", StringArgumentType.word())
                        .executes(ctx -> assignPlot(ctx.getSource(),
                            StringArgumentType.getString(ctx, "player")))))
                .then(Commands.literal("remove")
                    .executes(ctx -> removePlot(ctx.getSource())))
                .then(Commands.literal("info")
                    .executes(ctx -> plotInfo(ctx.getSource()))))
            .then(Commands.literal("log")
                .executes(ctx -> showLog(ctx.getSource())))
            .then(Commands.literal("diplomacy")
                .then(Commands.argument("nation", StringArgumentType.string())
                    .then(Commands.argument("status", StringArgumentType.word())
                        .executes(ctx -> setDiplomacy(ctx.getSource(),
                            StringArgumentType.getString(ctx, "nation"),
                            StringArgumentType.getString(ctx, "status"))))))
        );
    }

    private static int createTown(CommandSourceStack source, String name) {
        try {
            ServerPlayer player = source.getPlayerOrException();
            UUID uuid = player.getUUID();

            if (NationsData.getTownByPlayer(uuid) != null) {
                source.sendFailure(Component.literal("§8§l┃ §c✘ §fВы уже состоите в городе!"));
                return 0;
            }
            if (NationsData.townExists(name)) {
                source.sendFailure(Component.literal("§8§l┃ §c✘ §fГород с таким именем уже существует!"));
                return 0;
            }

            Town town = new Town(name, uuid);
            ChunkPos cp = new ChunkPos(player.blockPosition());
            if (NationsData.getTownByChunk(cp) != null) {
                source.sendFailure(Component.literal("§8§l┃ §c✘ §fЭтот чанк уже занят другим городом!"));
                return 0;
            }
            town.claimChunk(cp);
            town.setSpawnPos(player.blockPosition());
            NationsData.addTown(town);
            Economy.deposit(uuid, 0);

            source.sendSuccess(() -> Component.literal(
                "\n§8§l╔══════════════════════════════╗\n" +
                "§8§l║  §a§l✔ ГОРОД СОЗДАН!              §8§l║\n" +
                "§8§l║                                §8§l║\n" +
                "§8§l║  §fНазвание: §e" + name + "              §8§l║\n" +
                "§8§l║  §fВаша роль: §6👑 Правитель      §8§l║\n" +
                "§8§l║  §fПервый чанк запривачен!       §8§l║\n" +
                "§8§l║  §fСпавн установлен!             §8§l║\n" +
                "§8§l╚══════════════════════════════╝\n"), true);
            return 1;
        } catch (Exception e) {
            source.sendFailure(Component.literal("§8§l┃ §c✘ §fОшибка: " + e.getMessage()));
            return 0;
        }
    }

    private static int deleteTown(CommandSourceStack source) {
        try {
            ServerPlayer player = source.getPlayerOrException();
            Town town = NationsData.getTownByPlayer(player.getUUID());
            if (town == null) {
                source.sendFailure(Component.literal("§8§l┃ §c✘ §fВы не состоите в городе!"));
                return 0;
            }
            if (!town.hasPermission(player.getUUID(), TownRole.RULER)) {
                source.sendFailure(Component.literal("§8§l┃ §c✘ §fТолько Правитель может удалить город!"));
                return 0;
            }
            if (town.getNationName() != null) {
                var nation = NationsData.getNation(town.getNationName());
                if (nation != null) {
                    nation.removeTown(town.getName());
                    NationsData.save();
                }
            }
            String townName = town.getName();
            NationsData.removeTown(townName);
            source.sendSuccess(() -> Component.literal(
                "§8§l┃ §a✔ §fГород §e" + townName + " §fуспешно удалён!"), true);
            return 1;
        } catch (Exception e) {
            source.sendFailure(Component.literal("§8§l┃ §c✘ §fОшибка: " + e.getMessage()));
            return 0;
        }
    }

    private static int invitePlayer(CommandSourceStack source, String playerName) {
        try {
            ServerPlayer sender = source.getPlayerOrException();
            Town town = NationsData.getTownByPlayer(sender.getUUID());
            if (town == null || !town.hasPermission(sender.getUUID(), TownRole.OFFICER)) {
                source.sendFailure(Component.literal("§8§l┃ §c✘ §fНужна роль §9🛡 Офицер §fили выше!"));
                return 0;
            }
            ServerPlayer target = source.getServer().getPlayerList().getPlayerByName(playerName);
            if (target == null) {
                source.sendFailure(Component.literal("§8§l┃ §c✘ §fИгрок не найден!"));
                return 0;
            }
            if (NationsData.getTownByPlayer(target.getUUID()) != null) {
                source.sendFailure(Component.literal("§8§l┃ §c✘ §fИгрок уже в городе!"));
                return 0;
            }
            town.addLog(sender.getName().getString() + " пригласил " + playerName);
            target.sendSystemMessage(Component.literal(
                "\n§8§l╔══════════════════════════════╗\n" +
                "§8§l║  §a§l📩 ПРИГЛАШЕНИЕ              §8§l║\n" +
                "§8§l║                                §8§l║\n" +
                "§8§l║  §fВас пригласили в город        §8§l║\n" +
                "§8§l║  §e§l" + town.getName() + "                       §8§l║\n" +
                "§8§l║                                §8§l║\n" +
                "§8§l║  §fВведите: §a/town join " + town.getName() + "    §8§l║\n" +
                "§8§l╚══════════════════════════════╝\n"));
            source.sendSuccess(() -> Component.literal(
                "§8§l┃ §a✔ §fПриглашение отправлено игроку §e" + playerName), true);
            return 1;
        } catch (Exception e) {
            source.sendFailure(Component.literal("§8§l┃ §c✘ §fОшибка: " + e.getMessage()));
            return 0;
        }
    }

    private static int joinTown(CommandSourceStack source, String townName) {
        try {
            ServerPlayer player = source.getPlayerOrException();
            if (NationsData.getTownByPlayer(player.getUUID()) != null) {
                source.sendFailure(Component.literal("§8§l┃ §c✘ §fВы уже в городе!"));
                return 0;
            }
            Town town = NationsData.getTown(townName);
            if (town == null) {
                source.sendFailure(Component.literal("§8§l┃ §c✘ §fГород не найден!"));
                return 0;
            }
            town.addMember(player.getUUID());
            town.addLog(player.getName().getString() + " вступил в город");
            NationsData.save();
            source.sendSuccess(() -> Component.literal(
                "§8§l┃ §a✔ §fВы вступили в город §e" + town.getName() +
                " §fкак " + town.getRole(player.getUUID()).getDisplayName()), true);
            return 1;
        } catch (Exception e) {
            source.sendFailure(Component.literal("§8§l┃ §c✘ §fОшибка: " + e.getMessage()));
            return 0;
        }
    }

    private static int leaveTown(CommandSourceStack source) {
        try {
            ServerPlayer player = source.getPlayerOrException();
            Town town = NationsData.getTownByPlayer(player.getUUID());
            if (town == null) {
                source.sendFailure(Component.literal("§8§l┃ §c✘ §fВы не в городе!"));
                return 0;
            }
            if (town.hasPermission(player.getUUID(), TownRole.RULER)) {
                source.sendFailure(Component.literal(
                    "§8§l┃ §c✘ §fПравитель не может покинуть город!\n" +
                    "§8§l┃ §7Используйте §f/town delete §7или §f/town transfer <игрок>"));
                return 0;
            }
            town.removeMember(player.getUUID());
            town.addLog(player.getName().getString() + " покинул город");
            NationsData.save();
            source.sendSuccess(() -> Component.literal("§8§l┃ §a✔ §fВы покинули город."), true);
            return 1;
        } catch (Exception e) {
            source.sendFailure(Component.literal("§8§l┃ §c✘ §fОшибка: " + e.getMessage()));
            return 0;
        }
    }

    private static int kickPlayer(CommandSourceStack source, String playerName) {
        try {
            ServerPlayer sender = source.getPlayerOrException();
            Town town = NationsData.getTownByPlayer(sender.getUUID());
            if (town == null || !town.hasPermission(sender.getUUID(), TownRole.GENERAL)) {
                source.sendFailure(Component.literal("§8§l┃ §c✘ §fНужна роль §c⚔ Генерал §fили выше!"));
                return 0;
            }
            ServerPlayer target = source.getServer().getPlayerList().getPlayerByName(playerName);
            if (target == null) {
                source.sendFailure(Component.literal("§8§l┃ §c✘ §fИгрок не найден!"));
                return 0;
            }
            if (!town.isMember(target.getUUID())) {
                source.sendFailure(Component.literal("§8§l┃ §c✘ §fИгрок не в вашем городе!"));
                return 0;
            }
            if (town.getRole(target.getUUID()).getPower() >= town.getRole(sender.getUUID()).getPower()) {
                source.sendFailure(Component.literal("§8§l┃ §c✘ §fНельзя выгнать того, чья роль не ниже вашей!"));
                return 0;
            }
            town.removeMember(target.getUUID());
            town.addLog(sender.getName().getString() + " выгнал " + playerName);
            NationsData.save();
            target.sendSystemMessage(Component.literal(
                "§8§l┃ §c✘ §fВас выгнали из города §e" + town.getName()));
            source.sendSuccess(() -> Component.literal(
                "§8§l┃ §a✔ §fИгрок §e" + playerName + " §fвыгнан из города."), true);
            return 1;
        } catch (Exception e) {
            source.sendFailure(Component.literal("§8§l┃ §c✘ §fОшибка: " + e.getMessage()));
            return 0;
        }
    }

    private static int townInfo(CommandSourceStack source) {
        try {
            ServerPlayer player = source.getPlayerOrException();
            Town town = NationsData.getTownByPlayer(player.getUUID());
            if (town == null) {
                source.sendFailure(Component.literal("§8§l┃ §c✘ §fВы не в городе!"));
                return 0;
            }
            sendTownInfo(source, town);
            return 1;
        } catch (Exception e) {
            source.sendFailure(Component.literal("§8§l┃ §c✘ §fОшибка: " + e.getMessage()));
            return 0;
        }
    }

    private static int townInfoByName(CommandSourceStack source, String name) {
        Town town = NationsData.getTown(name);
        if (town == null) {
            source.sendFailure(Component.literal("§8§l┃ §c✘ §fГород не найден!"));
            return 0;
        }
        sendTownInfo(source, town);
        return 1;
    }

    private static void sendTownInfo(CommandSourceStack source, Town town) {
        StringBuilder sb = new StringBuilder();
        sb.append("\n§8§l╔══════════════════════════════════╗\n");
        sb.append("§8§l║  §e§l🏰 ").append(town.getName()).append("\n");
        sb.append("§8§l╠══════════════════════════════════╣\n");
        sb.append("§8§l║  §7Нация: §f").append(town.getNationName() != null ? town.getNationName() : "—").append("\n");
        sb.append("§8§l║  §7Жителей: §f").append(town.getMembers().size()).append("\n");
        sb.append("§8§l║  §7Территория: §f").append(town.getClaimedChunks().size())
          .append("§7/§f").append(town.getMaxChunks()).append(" §7чанков\n");
        sb.append("§8§l║  §7Сила: §e").append(town.getPower()).append("\n");
        sb.append("§8§l║  §7Налог: §f").append(String.format("%.1f%%", town.getTaxRate() * 100)).append("\n");
        sb.append("§8§l║  §7Казна: §6").append(Economy.format(Economy.getTownBalance(town.getName()))).append("\n");
        sb.append("§8§l║  §7PvP: ").append(town.isPvpEnabled() ? "§a✔ ВКЛ" : "§c✘ ВЫКЛ").append("\n");
        sb.append("§8§l║  §7Разрушение: ").append(town.isDestructionEnabled() ? "§a✔ ВКЛ" : "§c✘ ВЫКЛ").append("\n");
        if (town.isAtWar()) sb.append("§8§l║  §c§l⚔ В СОСТОЯНИИ ВОЙНЫ\n");
        if (town.isCaptured()) sb.append("§8§l║  §6§l🏴 Захвачен: §e").append(town.getCapturedBy()).append("\n");
        sb.append("§8§l╚══════════════════════════════════╝\n");

        source.sendSuccess(() -> Component.literal(sb.toString()), false);
    }

    private static int listTowns(CommandSourceStack source) {
        var allTowns = NationsData.getAllTowns();
        if (allTowns.isEmpty()) {
            source.sendSuccess(() -> Component.literal("§8§l┃ §7Городов пока нет."), false);
            return 1;
        }
        StringBuilder sb = new StringBuilder("\n§8§l╔══ §6§l🏰 ГОРОДА §8§l══╗\n");
        for (Town t : allTowns) {
            sb.append("§8§l║ §e").append(t.getName());
            sb.append(" §8[§f").append(t.getMembers().size()).append("§8] ");
            sb.append(t.getNationName() != null ? "§9" + t.getNationName() : "§8—");
            if (t.isAtWar()) sb.append(" §c⚔");
            if (t.isCaptured()) sb.append(" §6🏴");
            sb.append("\n");
        }
        sb.append("§8§l╚═══════════════╝\n");
        source.sendSuccess(() -> Component.literal(sb.toString()), false);
        return 1;
    }

    private static int setPvp(CommandSourceStack source, boolean enabled) {
        try {
            ServerPlayer player = source.getPlayerOrException();
            Town town = NationsData.getTownByPlayer(player.getUUID());
            if (town == null || !town.hasPermission(player.getUUID(), TownRole.VICE_RULER)) {
                source.sendFailure(Component.literal("§8§l┃ §c✘ §fНужна роль §e⚜ Зам. Правителя §fили выше!"));
                return 0;
            }
            town.setPvpEnabled(enabled);
            town.addLog(player.getName().getString() + " " + (enabled ? "включил" : "выключил") + " PvP");
            NationsData.save();
            source.sendSuccess(() -> Component.literal(
                "§8§l┃ " + (enabled ? "§a✔ §fPvP §aвключён" : "§c✘ §fPvP §cвыключен")), true);
            return 1;
        } catch (Exception e) {
            source.sendFailure(Component.literal("§8§l┃ §c✘ §fОшибка: " + e.getMessage()));
            return 0;
        }
    }

    private static int setDestruction(CommandSourceStack source, boolean enabled) {
        try {
            ServerPlayer player = source.getPlayerOrException();
            Town town = NationsData.getTownByPlayer(player.getUUID());
            if (town == null || !town.hasPermission(player.getUUID(), TownRole.VICE_RULER)) {
                source.sendFailure(Component.literal("§8§l┃ §c✘ §fНужна роль §e⚜ Зам. Правителя §fили выше!"));
                return 0;
            }
            town.setDestructionEnabled(enabled);
            town.addLog(player.getName().getString() + " " + (enabled ? "включил" : "выключил") + " разрушение");
            NationsData.save();
            source.sendSuccess(() -> Component.literal(
                "§8§l┃ " + (enabled ? "§a✔ §fРазрушение §aвключено" : "§c✘ §fРазрушение §cвыключено")), true);
            return 1;
        } catch (Exception e) {
            source.sendFailure(Component.literal("§8§l┃ §c✘ §fОшибка: " + e.getMessage()));
            return 0;
        }
    }

    private static int setRole(CommandSourceStack source, String playerName, String roleId) {
        try {
            ServerPlayer sender = source.getPlayerOrException();
            Town town = NationsData.getTownByPlayer(sender.getUUID());
            if (town == null || !town.hasPermission(sender.getUUID(), TownRole.RULER)) {
                source.sendFailure(Component.literal("§8§l┃ §c✘ §fТолько §6👑 Правитель §fможет назначать роли!"));
                return 0;
            }
            ServerPlayer target = source.getServer().getPlayerList().getPlayerByName(playerName);
            if (target == null) {
                source.sendFailure(Component.literal("§8§l┃ §c✘ §fИгрок не найден!"));
                return 0;
            }
            if (!town.isMember(target.getUUID())) {
                source.sendFailure(Component.literal("§8§l┃ §c✘ §fИгрок не в вашем городе!"));
                return 0;
            }
            TownRole role = TownRole.fromId(roleId);
            if (role == null) {
                source.sendFailure(Component.literal(
                    "§8§l┃ §c✘ §fНеизвестная роль! Используйте §e/town roles"));
                return 0;
            }
            if (role == TownRole.RULER) {
                source.sendFailure(Component.literal(
                    "§8§l┃ §c✘ §fИспользуйте §e/town transfer §fдля передачи города!"));
                return 0;
            }
            town.setRole(target.getUUID(), role);
            town.addLog(sender.getName().getString() + " назначил " + playerName + " роль " + role.getDisplayName());
            NationsData.save();
            target.sendSystemMessage(Component.literal(
                "§8§l┃ §a✔ §fВам назначена роль: " + role.getDisplayName() + " §fв городе §e" + town.getName()));
            source.sendSuccess(() -> Component.literal(
                "§8§l┃ §a✔ §fИгроку §e" + playerName + " §fназначена роль: " + role.getDisplayName()), true);
            return 1;
        } catch (Exception e) {
            source.sendFailure(Component.literal("§8§l┃ §c✘ §fОшибка: " + e.getMessage()));
            return 0;
        }
    }

    private static int listRoles(CommandSourceStack source) {
        StringBuilder sb = new StringBuilder("\n§8§l╔══ §6§l👑 РОЛИ §8§l══╗\n");
        for (TownRole r : TownRole.values()) {
            sb.append("§8§l║ §e").append(r.getId()).append(" §8— ").append(r.getDisplayName()).append("\n");
        }
        sb.append("§8§l╠══ §7§lПРАВА §8§l══╣\n");
        sb.append("§8§l║ §a🔨 Строитель§7+ — приватить чанки\n");
        sb.append("§8§l║ §9🛡 Офицер§7+ — приглашать игроков\n");
        sb.append("§8§l║ §c⚔ Генерал§7+ — выгонять игроков\n");
        sb.append("§8§l║ §e⚜ Зам. Правителя§7+ — налоги, PvP, казна\n");
        sb.append("§8§l║ §6👑 Правитель §7— полное управление\n");
        sb.append("§8§l╚═══════════════╝\n");
        source.sendSuccess(() -> Component.literal(sb.toString()), false);
        return 1;
    }

    private static int listMembers(CommandSourceStack source) {
        try {
            ServerPlayer player = source.getPlayerOrException();
            Town town = NationsData.getTownByPlayer(player.getUUID());
            if (town == null) {
                source.sendFailure(Component.literal("§8§l┃ §c✘ §fВы не в городе!"));
                return 0;
            }
            StringBuilder sb = new StringBuilder("\n§8§l╔══ §6§l👥 ЖИТЕЛИ " + town.getName() + " §8§l══╗\n");
            for (UUID memberId : town.getMembers()) {
                TownRole role = town.getRole(memberId);
                var p = source.getServer().getPlayerList().getPlayer(memberId);
                String name = p != null ? p.getName().getString() : memberId.toString().substring(0, 8) + "...";
                String online = p != null ? "§a●" : "§c●";
                sb.append("§8§l║ ").append(online).append(" §f").append(name).append(" §8— ").append(role.getDisplayName()).append("\n");
            }
            sb.append("§8§l╚══════════════════╝\n");
            source.sendSuccess(() -> Component.literal(sb.toString()), false);
            return 1;
        } catch (Exception e) {
            source.sendFailure(Component.literal("§8§l┃ §c✘ §fОшибка: " + e.getMessage()));
            return 0;
        }
    }

    private static int setSpawn(CommandSourceStack source) {
        try {
            ServerPlayer player = source.getPlayerOrException();
            Town town = NationsData.getTownByPlayer(player.getUUID());
            if (town == null || !town.hasPermission(player.getUUID(), TownRole.VICE_RULER)) {
                source.sendFailure(Component.literal("§8§l┃ §c✘ §fНужна роль §e⚜ Зам. Правителя §fили выше!"));
                return 0;
            }
            ChunkPos cp = new ChunkPos(player.blockPosition());
            if (!town.ownsChunk(cp)) {
                source.sendFailure(Component.literal("§8§l┃ §c✘ §fВы должны стоять на территории города!"));
                return 0;
            }
            town.setSpawnPos(player.blockPosition());
            town.addLog(player.getName().getString() + " установил спавн города");
            NationsData.save();
            source.sendSuccess(() -> Component.literal("§8§l┃ §a✔ §fСпавн города установлен!"), true);
            return 1;
        } catch (Exception e) {
            source.sendFailure(Component.literal("§8§l┃ §c✘ §fОшибка: " + e.getMessage()));
            return 0;
        }
    }

    private static int teleportSpawn(CommandSourceStack source) {
        try {
            ServerPlayer player = source.getPlayerOrException();
            Town town = NationsData.getTownByPlayer(player.getUUID());
            if (town == null) {
                source.sendFailure(Component.literal("§8§l┃ §c✘ §fВы не в городе!"));
                return 0;
            }
            if (town.getSpawnPos() == null) {
                source.sendFailure(Component.literal("§8§l┃ §c✘ §fСпавн города не установлен! Используйте §e/town setspawn"));
                return 0;
            }
            BlockPos spawn = town.getSpawnPos();
            player.teleportTo(spawn.getX() + 0.5, spawn.getY(), spawn.getZ() + 0.5);
            source.sendSuccess(() -> Component.literal(
                "§8§l┃ §a✔ §fВы телепортированы на спавн города §e" + town.getName()), true);
            return 1;
        } catch (Exception e) {
            source.sendFailure(Component.literal("§8§l┃ §c✘ §fОшибка: " + e.getMessage()));
            return 0;
        }
    }

    private static int transferTown(CommandSourceStack source, String playerName) {
        try {
            ServerPlayer sender = source.getPlayerOrException();
            Town town = NationsData.getTownByPlayer(sender.getUUID());
            if (town == null || !town.hasPermission(sender.getUUID(), TownRole.RULER)) {
                source.sendFailure(Component.literal("§8§l┃ §c✘ §fТолько §6👑 Правитель §fможет передать город!"));
                return 0;
            }
            ServerPlayer target = source.getServer().getPlayerList().getPlayerByName(playerName);
            if (target == null) {
                source.sendFailure(Component.literal("§8§l┃ §c✘ §fИгрок не найден!"));
                return 0;
            }
            if (!town.isMember(target.getUUID())) {
                source.sendFailure(Component.literal("§8§l┃ §c✘ §fИгрок не в вашем городе!"));
                return 0;
            }
            town.transferTo(target.getUUID());
            town.addLog(sender.getName().getString() + " передал город игроку " + playerName);
            NationsData.save();
            target.sendSystemMessage(Component.literal(
                "\n§8§l╔══════════════════════════════╗\n" +
                "§8§l║  §6§l👑 ВЫ СТАЛИ ПРАВИТЕЛЕМ!      §8§l║\n" +
                "§8§l║                                §8§l║\n" +
                "§8§l║  §fГород: §e" + town.getName() + "                   §8§l║\n" +
                "§8§l╚══════════════════════════════╝\n"));
            source.sendSuccess(() -> Component.literal(
                "§8§l┃ §a✔ §fГород передан игроку §e" + playerName), true);
            return 1;
        } catch (Exception e) {
            source.sendFailure(Component.literal("§8§l┃ §c✘ §fОшибка: " + e.getMessage()));
            return 0;
        }
    }

    private static int assignPlot(CommandSourceStack source, String playerName) {
        try {
            ServerPlayer sender = source.getPlayerOrException();
            Town town = NationsData.getTownByPlayer(sender.getUUID());
            if (town == null || !town.hasPermission(sender.getUUID(), TownRole.VICE_RULER)) {
                source.sendFailure(Component.literal("§8§l┃ §c✘ §fНужна роль §e⚜ Зам. Правителя §fили выше!"));
                return 0;
            }
            ServerPlayer target = source.getServer().getPlayerList().getPlayerByName(playerName);
            if (target == null) {
                source.sendFailure(Component.literal("§8§l┃ §c✘ §fИгрок не найден!"));
                return 0;
            }
            ChunkPos cp = new ChunkPos(sender.blockPosition());
            if (!town.ownsChunk(cp)) {
                source.sendFailure(Component.literal("§8§l┃ §c✘ §fВы должны стоять на территории города!"));
                return 0;
            }
            town.setPlotOwner(cp, target.getUUID());
            town.addLog(sender.getName().getString() + " выдал участок " + playerName + " [" + cp.x + "," + cp.z + "]");
            NationsData.save();
            source.sendSuccess(() -> Component.literal(
                "§8§l┃ §a✔ §fУчасток §e[" + cp.x + ", " + cp.z + "] §fвыдан игроку §e" + playerName), true);
            return 1;
        } catch (Exception e) {
            source.sendFailure(Component.literal("§8§l┃ §c✘ §fОшибка: " + e.getMessage()));
            return 0;
        }
    }

    private static int removePlot(CommandSourceStack source) {
        try {
            ServerPlayer sender = source.getPlayerOrException();
            Town town = NationsData.getTownByPlayer(sender.getUUID());
            if (town == null || !town.hasPermission(sender.getUUID(), TownRole.VICE_RULER)) {
                source.sendFailure(Component.literal("§8§l┃ §c✘ §fНужна роль §e⚜ Зам. Правителя §fили выше!"));
                return 0;
            }
            ChunkPos cp = new ChunkPos(sender.blockPosition());
            town.removePlot(cp);
            town.addLog(sender.getName().getString() + " удалил участок [" + cp.x + "," + cp.z + "]");
            NationsData.save();
            source.sendSuccess(() -> Component.literal(
                "§8§l┃ §a✔ §fУчасток §e[" + cp.x + ", " + cp.z + "] §fосвобождён"), true);
            return 1;
        } catch (Exception e) {
            source.sendFailure(Component.literal("§8§l┃ §c✘ §fОшибка: " + e.getMessage()));
            return 0;
        }
    }

    private static int plotInfo(CommandSourceStack source) {
        try {
            ServerPlayer player = source.getPlayerOrException();
            ChunkPos cp = new ChunkPos(player.blockPosition());
            Town town = NationsData.getTownByChunk(cp);
            if (town == null) {
                source.sendSuccess(() -> Component.literal("§8§l┃ §7Этот чанк не запривачен."), false);
                return 1;
            }
            UUID owner = town.getPlotOwner(cp);
            String ownerName = "—";
            if (owner != null) {
                var p = source.getServer().getPlayerList().getPlayer(owner);
                ownerName = p != null ? p.getName().getString() : owner.toString().substring(0, 8);
            }
            final String fn = ownerName;
            source.sendSuccess(() -> Component.literal(
                "§8§l┃ §7Участок §e[" + cp.x + ", " + cp.z + "]\n" +
                "§8§l┃ §7Город: §e" + town.getName() + "\n" +
                "§8§l┃ §7Владелец: §f" + fn), false);
            return 1;
        } catch (Exception e) {
            source.sendFailure(Component.literal("§8§l┃ §c✘ §fОшибка: " + e.getMessage()));
            return 0;
        }
    }

    private static int showLog(CommandSourceStack source) {
        try {
            ServerPlayer player = source.getPlayerOrException();
            Town town = NationsData.getTownByPlayer(player.getUUID());
            if (town == null) {
                source.sendFailure(Component.literal("§8§l┃ §c✘ §fВы не в городе!"));
                return 0;
            }
            if (!town.hasPermission(player.getUUID(), TownRole.OFFICER)) {
                source.sendFailure(Component.literal("§8§l┃ §c✘ §fНужна роль §9🛡 Офицер §fили выше!"));
                return 0;
            }
            var log = town.getActionLog();
            if (log.isEmpty()) {
                source.sendSuccess(() -> Component.literal("§8§l┃ §7Журнал пуст."), false);
                return 1;
            }
            StringBuilder sb = new StringBuilder("\n§8§l╔══ §6§l📋 ЖУРНАЛ " + town.getName() + " §8§l══╗\n");
            int start = Math.max(0, log.size() - 15);
            for (int i = start; i < log.size(); i++) {
                sb.append("§8§l║ §7").append(log.get(i)).append("\n");
            }
            sb.append("§8§l╚═══════════════════════╝\n");
            source.sendSuccess(() -> Component.literal(sb.toString()), false);
            return 1;
        } catch (Exception e) {
            source.sendFailure(Component.literal("§8§l┃ §c✘ §fОшибка: " + e.getMessage()));
            return 0;
        }
    }

    private static int setDiplomacy(CommandSourceStack source, String nationName, String status) {
        try {
            ServerPlayer player = source.getPlayerOrException();
            Nation nation = NationsData.getNationByPlayer(player.getUUID());
            if (nation == null || !nation.getLeader().equals(player.getUUID())) {
                source.sendFailure(Component.literal("§8§l┃ §c✘ §fВы не лидер нации!"));
                return 0;
            }
            Nation target = NationsData.getNation(nationName);
            if (target == null) {
                source.sendFailure(Component.literal("§8§l┃ §c✘ §fНация не найдена!"));
                return 0;
            }
            if (!status.equals("neutral") && !status.equals("hostile") && !status.equals("friendly")) {
                source.sendFailure(Component.literal(
                    "§8§l┃ §c✘ §fСтатус должен быть: §eneutral§f, §ehostile §fили §efriendly"));
                return 0;
            }
            String statusDisplay;
            switch (status) {
                case "friendly": statusDisplay = "§a🤝 Дружественный"; break;
                case "hostile": statusDisplay = "§c⚔ Враждебный"; break;
                default: statusDisplay = "§7◆ Нейтральный"; break;
            }
            nation.setDiplomacy(target.getName(), status);
            NationsData.save();
            final String sd = statusDisplay;
            source.sendSuccess(() -> Component.literal(
                "§8§l┃ §a✔ §fОтношение к §e" + target.getName() + "§f: " + sd), true);
            return 1;
        } catch (Exception e) {
            source.sendFailure(Component.literal("§8§l┃ §c✘ §fОшибка: " + e.getMessage()));
            return 0;
        }
    }
}
