package com.nations.events;

import com.nations.data.Nation;
import com.nations.data.NationsData;
import com.nations.data.Town;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
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
        if (town == null) return; // Не запривачено — можно ломать

        // Если игрок — член города, можно ломать
        if (town.isMember(player.getUUID())) return;

        // Если идёт война и разрушение включено
        if (town.isAtWar() && town.isDestructionEnabled()) {
            // Проверяем что ломающий из враждебной нации
            if (isEnemyNation(player, town)) return;
        }

        // Иначе — блокируем
        event.setCanceled(true);
        player.sendSystemMessage(Component.literal(
            "§cЭта территория принадлежит городу §e" + town.getName() + "§c!"));
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onBlockPlace(BlockEvent.EntityPlaceEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;

        ChunkPos cp = new ChunkPos(event.getPos());
        Town town = NationsData.getTownByChunk(cp);
        if (town == null) return;

        if (town.isMember(player.getUUID())) return;

        if (town.isAtWar() && town.isDestructionEnabled()) {
            if (isEnemyNation(player, town)) return;
        }

        event.setCanceled(true);
        player.sendSystemMessage(Component.literal(
            "§cЭта территория принадлежит городу §e" + town.getName() + "§c!"));
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onPlayerInteract(PlayerInteractEvent.RightClickBlock event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;

        ChunkPos cp = new ChunkPos(event.getPos());
        Town town = NationsData.getTownByChunk(cp);
        if (town == null) return;

        if (town.isMember(player.getUUID())) return;

        if (town.isAtWar() && town.isDestructionEnabled()) {
            if (isEnemyNation(player, town)) return;
        }

        event.setCanceled(true);
        player.sendSystemMessage(Component.literal(
            "§cНельзя взаимодействовать на территории города §e" + town.getName() + "§c!"));
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onAttackEntity(AttackEntityEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer attacker)) return;
        if (!(event.getTarget() instanceof ServerPlayer victim)) return;

        ChunkPos cp = new ChunkPos(victim.blockPosition());
        Town town = NationsData.getTownByChunk(cp);

        if (town == null) return; // Незапривачено — PvP по дефолту серверное

        if (!town.isPvpEnabled()) {
            event.setCanceled(true);
            attacker.sendSystemMessage(Component.literal(
                "§cPvP отключён на территории города §e" + town.getName() + "§c!"));
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onExplosion(ExplosionEvent.Detonate event) {
        // Убираем блоки из взрыва если они на защищённой территории
        event.getAffectedBlocks().removeIf(pos -> {
            ChunkPos cp = new ChunkPos(pos);
            Town town = NationsData.getTownByChunk(cp);
            if (town == null) return false;
            // Разрешаем взрывы только если война и destruction включён
            return !(town.isAtWar() && town.isDestructionEnabled());
        });
    }

    /**
     * Проверяет, является ли игрок членом нации, враждебной городу
     */
    private boolean isEnemyNation(ServerPlayer player, Town targetTown) {
        if (targetTown.getNationName() == null) return false;

        Nation targetNation = NationsData.getNation(targetTown.getNationName());
        if (targetNation == null) return false;

        Nation playerNation = NationsData.getNationByPlayer(player.getUUID());
        if (playerNation == null) return false;

        return targetNation.isAtWarWith(playerNation.getName());
    }
}