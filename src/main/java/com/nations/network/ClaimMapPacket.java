package com.nations.network;

import com.nations.data.Nation;
import com.nations.data.NationsData;
import com.nations.data.Town;
import com.nations.gui.ClaimMapScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.*;
import java.util.function.Supplier;

public class ClaimMapPacket {

    // Данные чанков: chunkX, chunkZ, townName, nationName, color (hex)
    private final List<ChunkEntry> entries;
    private final int playerChunkX;
    private final int playerChunkZ;

    public static class ChunkEntry {
        public int x, z;
        public String townName;
        public String nationName;
        public int color;

        public ChunkEntry(int x, int z, String townName, String nationName, int color) {
            this.x = x; this.z = z;
            this.townName = townName;
            this.nationName = nationName;
            this.color = color;
        }
    }

    public ClaimMapPacket(List<ChunkEntry> entries, int playerChunkX, int playerChunkZ) {
        this.entries = entries;
        this.playerChunkX = playerChunkX;
        this.playerChunkZ = playerChunkZ;
    }

    public static ClaimMapPacket create(ServerPlayer player) {
        int pcx = player.blockPosition().getX() >> 4;
        int pcz = player.blockPosition().getZ() >> 4;
        int radius = 16; // радиус карты в чанках

        List<ChunkEntry> entries = new ArrayList<>();
        for (int dx = -radius; dx <= radius; dx++) {
            for (int dz = -radius; dz <= radius; dz++) {
                int cx = pcx + dx;
                int cz = pcz + dz;
                var cp = new net.minecraft.world.level.ChunkPos(cx, cz);
                Town town = NationsData.getTownByChunk(cp);
                if (town != null) {
                    String nationName = town.getNationName() != null ?
                        town.getNationName() : "";
                    int color = 0xAAAAAA; // серый по умолчанию
                    if (town.getNationName() != null) {
                        Nation nation = NationsData.getNation(town.getNationName());
                        if (nation != null) color = nation.getColor().getHex();
                    }
                    entries.add(new ChunkEntry(cx, cz, town.getName(), nationName, color));
                }
            }
        }
        return new ClaimMapPacket(entries, pcx, pcz);
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeInt(playerChunkX);
        buf.writeInt(playerChunkZ);
        buf.writeInt(entries.size());
        for (ChunkEntry e : entries) {
            buf.writeInt(e.x);
            buf.writeInt(e.z);
            buf.writeUtf(e.townName);
            buf.writeUtf(e.nationName);
            buf.writeInt(e.color);
        }
    }

    public static ClaimMapPacket decode(FriendlyByteBuf buf) {
        int pcx = buf.readInt();
        int pcz = buf.readInt();
        int size = buf.readInt();
        List<ChunkEntry> entries = new ArrayList<>();
        for (int i = 0; i < size; i++) {
            entries.add(new ChunkEntry(
                buf.readInt(), buf.readInt(),
                buf.readUtf(), buf.readUtf(),
                buf.readInt()
            ));
        }
        return new ClaimMapPacket(entries, pcx, pcz);
    }

    public void handle(Supplier<NetworkEvent.Context> ctxSupplier) {
        NetworkEvent.Context ctx = ctxSupplier.get();
        ctx.enqueueWork(() -> {
            DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> {
                ClientHandler.openMap(this);
            });
        });
        ctx.setPacketHandled(true);
    }

    public List<ChunkEntry> getEntries() { return entries; }
    public int getPlayerChunkX() { return playerChunkX; }
    public int getPlayerChunkZ() { return playerChunkZ; }

    // Отдельный класс чтобы избежать загрузки клиентских классов на сервере
    public static class ClientHandler {
        public static void openMap(ClaimMapPacket packet) {
            Minecraft.getInstance().setScreen(new ClaimMapScreen(packet));
        }
    }
}