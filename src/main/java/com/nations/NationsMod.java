package com.nations;

import com.nations.commands.ClaimCommands;
import com.nations.commands.NationCommands;
import com.nations.commands.TownCommands;
import com.nations.data.NationsData;
import com.nations.events.ProtectionHandler;
import com.nations.network.NetworkHandler;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegisterCommandsEvent;
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

    public NationsMod() {
        FMLJavaModLoadingContext.get().getModEventBus()
            .addListener(this::setup);
        MinecraftForge.EVENT_BUS.register(this);
        MinecraftForge.EVENT_BUS.register(new ProtectionHandler());
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
    }
}