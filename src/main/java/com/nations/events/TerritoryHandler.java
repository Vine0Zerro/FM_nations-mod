package com.nations.events;

import com.nations.data.*;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.ChunkPos;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class TerritoryHandler {

    private final Map<UUID, String> lastTownName = new HashMap<>();
    private final Map<UUID, Long> warZoneCooldown = new HashMap<>();
    private int tickCounter = 0;

    // === Табло при входе на территорию ===
    @SubscribeEvent
    public void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        if (!(event.player instanceof ServerPlayer player)) return;

        tickCounter++;
        if (tickCounter % 20 != 0) return; // Проверяем раз в секунду

        ChunkPos cp = new ChunkPos(player.blockPosition());
        Town town = NationsData.getTownByChunk(cp);

        String currentTown = town != null ? town.getName() : null;
        String prevTown = lastTownName.get(player.getUUID());

        // Вошёл на новую территорию
        if (currentTown != null && !currentTown.equals(prevTown)) {
            showTerritoryEntry(player, town);
            lastTownName.put(player.getUUID(), currentTown);
        }
        // Покинул территорию
        else if (currentTown == null && prevTown != null) {
            player.sendSystemMessage(Component.literal(
                "\n§8§l┃ §7◆ §fДикая территория §7◆\n"));
            lastTownName.put(player.getUUID(), null);
        }

        // === Проверка зоны войны для невраждующих ===
        if (town != null && town.isAtWar()) {
            checkWarZoneProximity(player, town);
        }
    }

    private void showTerritoryEntry(ServerPlayer player, Town town) {
        StringBuilder sb = new StringBuilder();
        sb.append("\n§8§l╔══════════════════════════╗\n");

        if (town.isAtWar()) {
            sb.append("§8§l║  §4§l⚔ ЗОНА ВОЙНЫ ⚔          §8§l║\n");
        } else if (town.isCaptured()) {
            sb.append("§8§l║  §6§l🏴 ЗАХВАЧЕННАЯ ТЕРРИТОРИЯ §8§l║\n");
        }

        sb.append("§8§l║  §e§l🏰 ").append(town.getName());

        // Добавляем пробелы для выравнивания
        int padding = 24 - town.getName().length();
        for (int i = 0; i < padding; i++) sb.append(" ");
        sb.append("§8§l║\n");

        if (town.getNationName() != null) {
            Nation nation = NationsData.getNation(town.getNationName());
            String nationDisplay = "§9§l🏛 " + town.getNationName();
            sb.append("§8§l║  ").append(nationDisplay);
            int pad2 = 24 - town.getNationName().length();
            for (int i = 0; i < pad2; i++) sb.append(" ");
            sb.append("§8§l║\n");
        }

        // PvP статус
        String pvpStatus = town.isPvpEnabled() ? "§c⚔ PvP: ВКЛ" : "§a🛡 PvP: ВЫКЛ";
        sb.append("§8§l║  ").append(pvpStatus).append("              §8§l║\n");

        sb.append("§8§l╚══════════════════════════╝\n");

        player.sendSystemMessage(Component.literal(sb.toString()));
    }

    private void checkWarZoneProximity(ServerPlayer player, Town warTown) {
        // Проверяем что игрок не участник войны
        Nation playerNation = NationsData.getNationByPlayer(player.getUUID());
        if (playerNation == null) {
            teleportAway(player, warTown);
            return;
        }

        // Если игрок из враждующей нации — можно находиться
        if (warTown.getNationName() != null) {
            if (NationsData.areNationsAtWar(playerNation.getName(), warTown.getNationName())) {
                return; // Враждующий — может быть
            }
            // Если это его нация — может быть
            if (playerNation.getName().equalsIgnoreCase(warTown.getNationName())) {
                return;
            }
        }

        // Иначе — нельзя быть в зоне войны
        // Проверяем расстояние до ближайшего воюющего игрока
        for (ServerPlayer other : player.server.getPlayerList().getPlayers()) {
            if (other.getUUID().equals(player.getUUID())) continue;

            Nation otherNation = NationsData.getNationByPlayer(other.getUUID());
            if (otherNation == null) continue;

            // Если другой игрок участник войны
            boolean isWarParticipant = false;
            if (warTown.getNationName() != null) {
                if (otherNation.getName().equalsIgnoreCase(warTown.getNationName()) ||
                    NationsData.areNationsAtWar(otherNation.getName(), warTown.getNationName())) {
                    isWarParticipant = true;
                }
            }

            if (isWarParticipant) {
                double distance = player.distanceTo(other);
                if (distance < 50) {
                    teleportAway(player, warTown);
                    return;
                }
            }
        }
    }

    private void teleportAway(ServerPlayer player, Town warTown) {
        // Кулдаун чтобы не спамить
        Long lastTp = warZoneCooldown.get(player.getUUID());
        long now = System.currentTimeMillis();
        if (lastTp != null && now - lastTp < 5000) return; // 5 сек кулдаун
        warZoneCooldown.put(player.getUUID(), now);

        // Телепортируем на спавн города игрока
        Town playerTown = NationsData.getTownByPlayer(player.getUUID());
        if (playerTown != null && playerTown.getSpawnPos() != null) {
            BlockPos spawn = playerTown.getSpawnPos();
            player.teleportTo(spawn.getX() + 0.5, spawn.getY(), spawn.getZ() + 0.5);
        } else {
            // На мировой спавн
            BlockPos worldSpawn = player.server.overworld().getSharedSpawnPos();
            player.teleportTo(worldSpawn.getX() + 0.5, worldSpawn.getY(), worldSpawn.getZ() + 0.5);
        }

        player.sendSystemMessage(Component.literal(
            "\n§8§l╔══════════════════════════════════╗\n" +
            "§8§l║  §c§l⚠ ВНИМАНИЕ!                      §8§l║\n" +
            "§8§l║                                    §8§l║\n" +
            "§8§l║  §fВам запрещено приближаться к      §8§l║\n" +
            "§8§l║  §fигрокам во время войны!           §8§l║\n" +
            "§8§l║                                    §8§l║\n" +
            "§8§l║  §7Зона войны: §e" + warTown.getName() + "               §8§l║\n" +
            "§8§l║  §7Вы телепортированы в безопасность §8§l║\n" +
            "§8§l╚══════════════════════════════════╝\n"));
    }

    @SubscribeEvent
    public void onPlayerLogout(PlayerEvent.PlayerLoggedOutEvent event) {
        lastTownName.remove(event.getEntity().getUUID());
        warZoneCooldown.remove(event.getEntity().getUUID());
    }
}
