package com.nations.integration;

import com.nations.NationsMod;
import com.nations.data.*;
import net.minecraft.world.level.ChunkPos;
import net.minecraftforge.fml.ModList;
import org.dynmap.DynmapCommonAPI;
import org.dynmap.DynmapCommonAPIListener;
import org.dynmap.markers.AreaMarker;
import org.dynmap.markers.Marker;
import org.dynmap.markers.MarkerAPI;
import org.dynmap.markers.MarkerSet;

import java.util.*;

public class DynmapIntegration {

    private static MarkerAPI markerAPI = null;
    private static MarkerSet townMarkerSet = null;
    private static boolean enabled = false;

    public static void init() {
        if (!ModList.get().isLoaded("dynmap")) {
            return;
        }

        // Регистрируем слушатель — это официальный способ получить API
        DynmapCommonAPIListener.register(new DynmapCommonAPIListener() {
            @Override
            public void apiEnabled(DynmapCommonAPI api) {
                markerAPI = api.getMarkerAPI();
                if (markerAPI != null) {
                    enabled = true;
                    try {
                        setupMarkerSets();
                        updateAllMarkers();
                        NationsMod.LOGGER.info("DynMap API успешно подключен!");
                    } catch (Exception e) {
                        NationsMod.LOGGER.error("Ошибка настройки маркеров: " + e.getMessage());
                    }
                }
            }
        });
    }

    private static void setupMarkerSets() {
        if (markerAPI == null) return;

        // Удаляем старый сет, если есть
        MarkerSet existing = markerAPI.getMarkerSet("nations.towns");
        if (existing != null) {
            existing.deleteMarkerSet();
        }

        // Создаем новый слой
        townMarkerSet = markerAPI.createMarkerSet(
            "nations.towns", 
            "Города и Нации", 
            null, 
            false
        );
        
        if (townMarkerSet != null) {
            townMarkerSet.setLayerPriority(10);
            townMarkerSet.setHideByDefault(false);
        }
    }

    public static void updateAllMarkers() {
        if (!enabled || townMarkerSet == null) return;

        // Очищаем старые маркеры
        for (AreaMarker area : townMarkerSet.getAreaMarkers()) {
            area.deleteMarker();
        }
        for (Marker marker : townMarkerSet.getMarkers()) {
            marker.deleteMarker();
        }

        // Рисуем новые
        for (Town town : NationsData.getAllTowns()) {
            drawTown(town);
        }
    }

    private static void drawTown(Town town) {
        int color = 0x888888;
        String nationName = "";
        
        if (town.getNationName() != null) {
            Nation nation = NationsData.getNation(town.getNationName());
            if (nation != null) {
                color = nation.getColor().getHex();
                nationName = nation.getName();
            }
        }

        int strokeColor = color;
        if (town.isAtWar()) strokeColor = 0xFF0000;
        else if (town.isCaptured()) strokeColor = 0xFF6600;

        for (ChunkPos cp : town.getClaimedChunks()) {
            double x1 = cp.x * 16;
            double z1 = cp.z * 16;
            double x2 = x1 + 16;
            double z2 = z1 + 16;

            String markerId = "n_" + town.getName() + "_" + cp.x + "_" + cp.z;
            
            // Создаем квадрат на карте
            AreaMarker area = townMarkerSet.createAreaMarker(
                markerId, 
                town.getName(), 
                true, 
                "world", 
                new double[]{x1, x2, x2, x1}, 
                new double[]{z1, z1, z2, z2}, 
                false
            );

            if (area != null) {
                // Настраиваем вид
                area.setFillStyle(0.35, color);
                area.setLineStyle(2, 0.8, strokeColor);
                
                // Устанавливаем HTML описание
                area.setDescription(buildLabel(town, nationName));
            }
        }
    }

    private static String buildLabel(Town town, String nationName) {
        String borderColor = town.isAtWar() ? "#F00" : "#FFD700";
        StringBuilder sb = new StringBuilder();
        sb.append("<div style='padding:10px; background:rgba(0,0,0,0.9); border:2px solid ").append(borderColor).append("; border-radius:10px; color:white; font-family:sans-serif;'>");
        sb.append("<div style='font-size:16px; font-weight:bold; color:#FFD700; margin-bottom:5px;'>🏰 ").append(town.getName()).append("</div>");
        
        if (!nationName.isEmpty()) {
            sb.append("<div style='color:#55AAFF; font-weight:bold; margin-bottom:5px;'>🏛 Нация: ").append(nationName).append("</div>");
        }

        if (town.isAtWar()) sb.append("<div style='color:#FF4444; font-weight:bold;'>⚠️ СОСТОЯНИЕ ВОЙНЫ</div>");
        if (town.isCaptured()) sb.append("<div style='color:#FFAA00;'>🏴 Захвачен нацией: ").append(town.getCapturedBy()).append("</div>");

        sb.append("<hr style='border:0; border-top:1px solid #444; margin:8px 0;'>");
        sb.append("<div style='font-size:12px;'>");
        sb.append("👥 Жителей: <span style='color:#FFF;'>").append(town.getMembers().size()).append("</span><br>");
        sb.append("📍 Чанков: <span style='color:#FFF;'>").append(town.getClaimedChunks().size()).append("</span><br>");
        
        String mayorName = "неизвестно";
        if (NationsData.getServer() != null) {
            var p = NationsData.getServer().getPlayerList().getPlayer(town.getMayor());
            if (p != null) mayorName = p.getName().getString();
        }
        sb.append("👑 Правитель: <span style='color:#FFD700;'>").append(mayorName).append("</span>");
        sb.append("</div></div>");
        return sb.toString();
    }
}
