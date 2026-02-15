package com.nations.events;

import com.nations.data.*;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.ChunkPos;
import net.minecraftforge.event.entity.player.AttackEntityEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.event.level.ExplosionEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class ProtectionHandler {

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onBlockBreak(BlockEvent.BreakEvent event) {
        if (!(event.getPlayer() instanceof ServerPlayer player)) return;
        ChunkPos cp = new ChunkPos(event.getPos());
        Town town = NationsData.getTownByChunk(cp);
        if (town == null) return;
        if (town.isMember(player.getUUID())) return;
        if (town.isPlotOwner(cp, player.getUUID())) return;
        if (canInteractDuringWar(player, town)) return;

        event.setCanceled(true);
        player.sendSystemMessage(Component.literal(
            "§8§l┃ §c🛡 §fЗащищённая территория города §e" + town.getName()));
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onBlockPlace(BlockEvent.EntityPlaceEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        ChunkPos cp = new ChunkPos(event.getPos());
        Town town = NationsData.getTownByChunk(cp);
        if (town == null) return;
        if (town.isMember(player.getUUID())) return;
        if (town.isPlotOwner(cp, player.getUUID())) return;
        if (canInteractDuringWar(player, town)) return;

        event.setCanceled(true);
        player.sendSystemMessage(Component.literal(
            "§8§l┃ §c🛡 §fЗащищённая территория города §e" + town.getName()));
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onPlayerInteract(PlayerInteractEvent.RightClickBlock event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        ChunkPos cp = new ChunkPos(event.getPos());
        Town town = NationsData.getTownByChunk(cp);
        if (town == null) return;
        if (town.isMember(player.getUUID())) return;
        if (town.isPlotOwner(cp, player.getUUID())) return;
        if (isAlly(player, town)) return;
        if (canInteractDuringWar(player, town)) return;

        event.setCanceled(true);
        player.sendSystemMessage(Component.literal(
            "§8§l┃ §c🛡 §fЗащищённая территория города §e" + town.getName()));
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onAttackEntity(AttackEntityEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer attacker)) return;
        if (!(event.getTarget() instanceof ServerPlayer victim)) return;

        ChunkPos cp = new ChunkPos(victim.blockPosition());
        Town town = NationsData.getTownByChunk(cp);

        // На незаприваченной территории — PvP по серверным правилам
        if (town == null) return;

        if (!town.isPvpEnabled()) {
            event.setCanceled(true);
            attacker.sendSystemMessage(Component.literal(
                "§8§l┃ §c⚔ §fPvP выключен на территории §e" + town.getName()));
            return;
        }

        // Если PvP включен (война) — проверяем что оба участника враждующих наций
        if (town.isAtWar()) {
            Nation attackerNation = NationsData.getNationByPlayer(attacker.getUUID());
            Nation victimNation = NationsData.getNationByPlayer(victim.getUUID());

            if (attackerNation == null || victimNation == null) {
                event.setCanceled(true);
                attacker.sendSystemMessage(Component.literal(
                    "§8§l┃ §c⚔ §fТолько участники враждующих наций могут сражаться!"));
                return;
            }

            if (!NationsData.areNationsAtWar(attackerNation.getName(), victimNation.getName())) {
                event.setCanceled(true);
                attacker.sendSystemMessage(Component.literal(
                    "§8§l┃ §c⚔ §fВаши нации не воюют друг с другом!"));
                return;
            }
            // Враждующие нации — урон разрешён
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onExplosion(ExplosionEvent.Detonate event) {
        event.getAffectedBlocks().removeIf(pos -> {
            ChunkPos cp = new ChunkPos(pos);
            Town town = NationsData.getTownByChunk(cp);
            if (town == null) return false;
            return !(town.isAtWar() && town.isDestructionEnabled());
        });
    }

    private boolean canInteractDuringWar(ServerPlayer player, Town targetTown) {
        if (!targetTown.isAtWar() || !targetTown.isDestructionEnabled()) return false;
        if (targetTown.getNationName() == null) return false;

        Nation playerNation = NationsData.getNationByPlayer(player.getUUID());
        if (playerNation == null) return false;

        return NationsData.areNationsAtWar(playerNation.getName(), targetTown.getNationName());
    }

    private boolean isAlly(ServerPlayer player, Town targetTown) {
        if (targetTown.getNationName() == null) return false;
        Nation playerNation = NationsData.getNationByPlayer(player.getUUID());
        if (playerNation == null) return false;
        return NationsData.areAllied(playerNation.getName(), targetTown.getNationName());
    }
}
