package com.nations.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.nations.data.*;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

public class EconomyCommands {

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("money")
            .executes(ctx -> balance(ctx.getSource()))
            .then(Commands.literal("pay")
                .then(Commands.argument("player", StringArgumentType.word())
                    .then(Commands.argument("amount", DoubleArgumentType.doubleArg(0.1))
                        .executes(ctx -> pay(ctx.getSource(),
                            StringArgumentType.getString(ctx, "player"),
                            DoubleArgumentType.getDouble(ctx, "amount"))))))
            .then(Commands.literal("top")
                .executes(ctx -> top(ctx.getSource())))
        );

        dispatcher.register(Commands.literal("tax")
            .then(Commands.literal("set")
                .then(Commands.argument("rate", DoubleArgumentType.doubleArg(0, 50))
                    .executes(ctx -> setTax(ctx.getSource(),
                        DoubleArgumentType.getDouble(ctx, "rate")))))
            .then(Commands.literal("collect")
                .executes(ctx -> collectTax(ctx.getSource())))
            .then(Commands.literal("info")
                .executes(ctx -> taxInfo(ctx.getSource())))
        );

        dispatcher.register(Commands.literal("treasury")
            .executes(ctx -> treasury(ctx.getSource()))
            .then(Commands.literal("deposit")
                .then(Commands.argument("amount", DoubleArgumentType.doubleArg(0.1))
                    .executes(ctx -> treasuryDeposit(ctx.getSource(),
                        DoubleArgumentType.getDouble(ctx, "amount")))))
            .then(Commands.literal("withdraw")
                .then(Commands.argument("amount", DoubleArgumentType.doubleArg(0.1))
                    .executes(ctx -> treasuryWithdraw(ctx.getSource(),
                        DoubleArgumentType.getDouble(ctx, "amount")))))
        );
    }

    private static int balance(CommandSourceStack source) {
        try {
            ServerPlayer player = source.getPlayerOrException();
            double bal = Economy.getBalance(player.getUUID());
            source.sendSuccess(() -> Component.literal(
                "§6💰 Ваш баланс: §e" + Economy.format(bal)), false);
            return 1;
        } catch (Exception e) {
            source.sendFailure(Component.literal("§cОшибка: " + e.getMessage()));
            return 0;
        }
    }

    private static int pay(CommandSourceStack source, String targetName, double amount) {
        try {
            ServerPlayer player = source.getPlayerOrException();
            ServerPlayer target = source.getServer().getPlayerList().getPlayerByName(targetName);
            if (target == null) {
                source.sendFailure(Component.literal("§cИгрок не найден!"));
                return 0;
            }
            if (target.getUUID().equals(player.getUUID())) {
                source.sendFailure(Component.literal("§cНельзя платить себе!"));
                return 0;
            }
            if (!Economy.transfer(player.getUUID(), target.getUUID(), amount)) {
                source.sendFailure(Component.literal("§cНедостаточно средств!"));
                return 0;
            }
            NationsData.save();
            source.sendSuccess(() -> Component.literal(
                "§aВы отправили §e" + Economy.format(amount) + " §aигроку §e" + targetName), true);
            target.sendSystemMessage(Component.literal(
                "§aВы получили §e" + Economy.format(amount) + " §aот §e" + player.getName().getString()));
            return 1;
        } catch (Exception e) {
            source.sendFailure(Component.literal("§cОшибка: " + e.getMessage()));
            return 0;
        }
    }

    private static int top(CommandSourceStack source) {
        source.sendSuccess(() -> Component.literal("§6=== 💰 Топ баланс ==="), false);
        // Показываем топ по нациям
        var ranking = NationsData.getNationRanking();
        int i = 1;
        for (Nation n : ranking) {
            double bal = Economy.getNationBalance(n.getName());
            final int pos = i;
            final String name = n.getName();
            source.sendSuccess(() -> Component.literal(
                "§e" + pos + ". §f" + name + " §7- §e" + Economy.format(bal)), false);
            i++;
            if (i > 10) break;
        }
        return 1;
    }

    private static int setTax(CommandSourceStack source, double rate) {
        try {
            ServerPlayer player = source.getPlayerOrException();
            Town town = NationsData.getTownByPlayer(player.getUUID());
            if (town == null || !town.hasPermission(player.getUUID(), TownRole.VICE_RULER)) {
                source.sendFailure(Component.literal("§cНужна роль Зам. Правителя или выше!"));
                return 0;
            }
            town.setTaxRate(rate / 100.0);
            NationsData.save();
            source.sendSuccess(() -> Component.literal(
                "§aНалог города установлен: §e" + rate + "%"), true);
            return 1;
        } catch (Exception e) {
            source.sendFailure(Component.literal("§cОшибка: " + e.getMessage()));
            return 0;
        }
    }

    private static int collectTax(CommandSourceStack source) {
        try {
            ServerPlayer player = source.getPlayerOrException();
            Town town = NationsData.getTownByPlayer(player.getUUID());
            if (town == null || !town.hasPermission(player.getUUID(), TownRole.VICE_RULER)) {
                source.sendFailure(Component.literal("§cНужна роль Зам. Правителя или выше!"));
                return 0;
            }
            double collected = Economy.collectTax(town, town.getTaxRate());
            NationsData.save();
            source.sendSuccess(() -> Component.literal(
                "§aСобрано налогов: §e" + Economy.format(collected)), true);

            // Уведомить жителей
            for (var member : town.getMembers()) {
                ServerPlayer p = source.getServer().getPlayerList().getPlayer(member);
                if (p != null && !p.getUUID().equals(player.getUUID())) {
                    p.sendSystemMessage(Component.literal(
                        "§eС вас собран налог города §f" + town.getName() +
                        " §e(" + (town.getTaxRate() * 100) + "%)"));
                }
            }
            return 1;
        } catch (Exception e) {
            source.sendFailure(Component.literal("§cОшибка: " + e.getMessage()));
            return 0;
        }
    }

    private static int taxInfo(CommandSourceStack source) {
        try {
            ServerPlayer player = source.getPlayerOrException();
            Town town = NationsData.getTownByPlayer(player.getUUID());
            if (town == null) {
                source.sendFailure(Component.literal("§cВы не в городе!"));
                return 0;
            }
            source.sendSuccess(() -> Component.literal(
                "§6=== Налоги ===\n" +
                "§7Налог города: §e" + (town.getTaxRate() * 100) + "%\n" +
                "§7Казна города: §e" + Economy.format(Economy.getTownBalance(town.getName())) + "\n" +
                (town.getNationName() != null ?
                    "§7Казна нации: §e" + Economy.format(Economy.getNationBalance(town.getNationName())) : "")
            ), false);
            return 1;
        } catch (Exception e) {
            source.sendFailure(Component.literal("§cОшибка: " + e.getMessage()));
            return 0;
        }
    }

    private static int treasury(CommandSourceStack source) {
        try {
            ServerPlayer player = source.getPlayerOrException();
            Town town = NationsData.getTownByPlayer(player.getUUID());
            if (town == null) {
                source.sendFailure(Component.literal("§cВы не в городе!"));
                return 0;
            }
            source.sendSuccess(() -> Component.literal(
                "§6=== 🏦 Казна ===\n" +
                "§7Город §e" + town.getName() + "§7: §e" +
                Economy.format(Economy.getTownBalance(town.getName())) + "\n" +
                "§7Ваш баланс: §e" + Economy.format(Economy.getBalance(player.getUUID()))
            ), false);
            return 1;
        } catch (Exception e) {
            source.sendFailure(Component.literal("§cОшибка: " + e.getMessage()));
            return 0;
        }
    }

    private static int treasuryDeposit(CommandSourceStack source, double amount) {
        try {
            ServerPlayer player = source.getPlayerOrException();
            Town town = NationsData.getTownByPlayer(player.getUUID());
            if (town == null) {
                source.sendFailure(Component.literal("§cВы не в городе!"));
                return 0;
            }
            if (!Economy.withdraw(player.getUUID(), amount)) {
                source.sendFailure(Component.literal("§cНедостаточно средств!"));
                return 0;
            }
            Economy.depositToTown(town.getName(), amount);
            NationsData.save();
            source.sendSuccess(() -> Component.literal(
                "§aВы внесли §e" + Economy.format(amount) + " §aв казну города"), true);
            return 1;
        } catch (Exception e) {
            source.sendFailure(Component.literal("§cОшибка: " + e.getMessage()));
            return 0;
        }
    }

    private static int treasuryWithdraw(CommandSourceStack source, double amount) {
        try {
            ServerPlayer player = source.getPlayerOrException();
            Town town = NationsData.getTownByPlayer(player.getUUID());
            if (town == null || !town.hasPermission(player.getUUID(), TownRole.VICE_RULER)) {
                source.sendFailure(Component.literal("§cНужна роль Зам. Правителя или выше!"));
                return 0;
            }
            if (!Economy.withdrawFromTown(town.getName(), amount)) {
                source.sendFailure(Component.literal("§cВ казне недостаточно средств!"));
                return 0;
            }
            Economy.deposit(player.getUUID(), amount);
            NationsData.save();
            source.sendSuccess(() -> Component.literal(
                "§aВы забрали §e" + Economy.format(amount) + " §aиз казны города"), true);
            return 1;
        } catch (Exception e) {
            source.sendFailure(Component.literal("§cОшибка: " + e.getMessage()));
            return 0;
        }
    }
}
