package com.nations.network;

import com.nations.data.NationsData;
import com.nations.data.Town;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.ChunkPos;
import net.minecraftforge.network.NetworkEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public class ClaimChunksPacket {

    private final List<int[]> chunks; // каждый элемент = {x, z}

    public ClaimChunksPacket(List<int[]> chunks) {
        this.chunks = chunks;
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeInt(chunks.size());
        for (int[] c : chunks) {
            buf.writeInt(c[0]);
            buf.writeInt(c[1]);
        }
    }

    public static ClaimChunksPacket decode(FriendlyByteBuf buf) {
        int size = buf.readInt();
        List<int[]> chunks = new ArrayList<>();
        for (int i = 0; i < size; i++) {
            chunks.add(new int[]{buf.readInt(), buf.readInt()});
        }
        return new ClaimChunksPacket(chunks);
    }

    public void handle(Supplier<NetworkEvent.Context> ctxSupplier) {
        NetworkEvent.Context ctx = ctxSupplier.get();
        ctx.enqueueWork(() -> {
            ServerPlayer player = ctx.getSender();
            if (player == null) return;

            Town town = NationsData.getTownByPlayer(player.getUUID());
            if (town == null) {
                player.sendSystemMessage(Component.literal("§cВы не в городе!"));
                return;
            }
            if (!town.getMayor().equals(player.getUUID())) {
                player.sendSystemMessage(Component.literal("§cТолько мэр может приватить!"));
                return;
            }

            int claimed = 0;
            int failed = 0;

            for (int[] c : chunks) {
                if (!NationsData.canClaim(player.getUUID())) {
                    player.sendSystemMessage(Component.literal(
                        "§cЛимит! 5 чанков в минуту. Запривачено: " + claimed +
                        ", пропущено: " + (chunks.size() - claimed)));
                    break;
                }

                ChunkPos cp = new ChunkPos(c[0], c[1]);
                if (NationsData.getTownByChunk(cp) != null) {
                    failed++;
                    continue;
                }

                town.claimChunk(cp);
                NationsData.incrementClaim(player.getUUID());
                claimed++;
            }

            NationsData.save();
            int finalClaimed = claimed;
            int finalFailed = failed;
            player.sendSystemMessage(Component.literal(
                "§aЗапривачено чанков: §e" + finalClaimed +
                (finalFailed > 0 ? " §c(пропущено занятых: " + finalFailed + ")" : "")));
        });
        ctx.setPacketHandled(true);
    }
}