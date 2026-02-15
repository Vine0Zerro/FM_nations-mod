package com.nations;

import com.nations.commands.*;
import com.nations.data.*;
import com.nations.events.ProtectionHandler;
import com.nations.events.TerritoryHandler;
import com.nations.network.NetworkHandler;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.server.ServerStartedEvent;
import net.minecraftforge.event.server.ServerStoppingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod("nations")
public class NationsMod {
    public static final String MODID = "nations";
    public static final Logger LOGGER = LogManager.getLogger();
    private int tickCounter = 0;
    private static final int TAX_INTERVAL_TICKS = 20 * 60 * 60; // 1 час

    public NationsMod() {
        FMLJavaModLoadingContext.get().getModEventBus()
            .addListener(this::setup);
        MinecraftForge.EVENT_BUS.register(this);
        MinecraftForge.EVENT_BUS.register(new ProtectionHandler());
        MinecraftForge.EVENT_BUS.register(new TerritoryHandler());
    }

    private void setup(final FMLCommonSetupEvent event) {
        NetworkHandler.init();
    }

    @SubscribeEvent
    public void onServerStarted(ServerStartedEvent event) {
        NationsData.load(event.getServer());
    }

    @SubscribeEvent
    public void onServerStopping(ServerStoppingEvent event) {
        NationsData.save();
    }

    @SubscribeEvent
    public void onRegisterCommands(RegisterCommandsEvent event) {
        TownCommands.register(event.getDispatcher());
        NationCommands.register(event.getDispatcher());
        ClaimCommands.register(event.getDispatcher());
        EconomyCommands.register(event.getDispatcher());
        AllianceCommands.register(event.getDispatcher());
        RankingCommands.register(event.getDispatcher());
    }

    // === Автоматический сбор налогов каждый час ===
    @SubscribeEvent
    public void onServerTick(TickEvent.ServerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        tickCounter++;
        if (tickCounter < TAX_INTERVAL_TICKS) return;
        tickCounter = 0;

        long now = System.currentTimeMillis();

        for (Town town : NationsData.getAllTowns()) {
            if (now - town.getLastTaxCollection() < 3600000) continue; // 1 час
            if (town.getTaxRate() <= 0) continue;

            double collected = Economy.collectTax(town, town.getTaxRate());
            town.setLastTaxCollection(now);
            town.addLog("Автосбор налогов: " + Economy.format(collected));

            // Налог нации
            if (town.getNationName() != null) {
                Nation nation = NationsData.getNation(town.getNationName());
                if (nation != null && nation.getNationTaxRate() > 0) {
                    double nationTax = collected * nation.getNationTaxRate();
                    Economy.withdrawFromTown(town.getName(), nationTax);
                    Economy.depositToNation(nation.getName(), nationTax);
                    town.addLog("Налог нации: " + Economy.format(nationTax));
                }
            }

            // Уведомить онлайн жителей
            if (NationsData.getServer() != null) {
                for (var memberId : town.getMembers()) {
                    ServerPlayer p = NationsData.getServer().getPlayerList().getPlayer(memberId);
                    if (p != null) {
                        p.sendSystemMessage(Component.literal(
                            "§8§l┃ §e⚡ §7Автосбор налогов города §f" + town.getName() +
                            " §8(§e" + String.format("%.1f%%", town.getTaxRate() * 100) + "§8)"));
                    }
                }
            }
        }
        NationsData.save();
    }
}
