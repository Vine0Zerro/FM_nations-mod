package com.nations.network;

import com.nations.NationsMod;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;

public class NetworkHandler {
    private static final String PROTOCOL = "1";
    public static SimpleChannel CHANNEL;

    private static int id = 0;

    public static void init() {
        CHANNEL = NetworkRegistry.newSimpleChannel(
            new ResourceLocation(NationsMod.MODID, "main"),
            () -> PROTOCOL,
            PROTOCOL::equals,
            PROTOCOL::equals
        );

        CHANNEL.messageBuilder(ClaimMapPacket.class, id++, NetworkDirection.PLAY_TO_CLIENT)
            .encoder(ClaimMapPacket::encode)
            .decoder(ClaimMapPacket::decode)
            .consumerMainThread(ClaimMapPacket::handle)
            .add();

        CHANNEL.messageBuilder(ClaimChunksPacket.class, id++, NetworkDirection.PLAY_TO_SERVER)
            .encoder(ClaimChunksPacket::encode)
            .decoder(ClaimChunksPacket::decode)
            .consumerMainThread(ClaimChunksPacket::handle)
            .add();
    }

    public static void sendToPlayer(ServerPlayer player, Object msg) {
        CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), msg);
    }

    public static void sendToServer(Object msg) {
        CHANNEL.sendToServer(msg);
    }
}