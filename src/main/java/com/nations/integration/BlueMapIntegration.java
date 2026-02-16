package com.nations.integration;

import com.nations.NationsMod;
import com.nations.data.*;
import net.minecraft.world.level.ChunkPos;
import net.minecraftforge.fml.ModList;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.*;
import java.util.function.Consumer;

public class BlueMapIntegration {

    private static boolean enabled = false;
    private static Object blueMapAPI = null;
    private static final String MARKER_SET_ID = "nations_towns";

    // Кэшированные классы и методы
    private static Class<?> clsBlueMapAPI;
    private static Class<?> clsBlueMapMap;
    private static Class<?> clsMarkerSet;
    private static Class<?> clsShapeMarker;
    private static Class<?> clsPOIMarker;
    private static Class<?> clsShape;
    private static Class<?> clsVector2d;
    private static Class<?> clsColor;

    private static Method mGetInstance;
    private static Method mGetMaps;
    private static Method mGetId;
    private static Method mGetMarkerSets;
    private static Method mMarkerSetBuilder;
    private static Method mMarkerSetLabel;
    private static Method mMarkerSetDefaultHidden;
    private static Method mMarkerSetBuild;
    private static Method mMarkerSetGetMarkers;
    
    private static Method mShapeMarkerBuilder;
    private static Method mShapeMarkerLabel;
    private static Method mShapeMarkerShape;
    private static Method mShapeMarkerDepthTest;
    private static Method mShapeMarkerFillColor;
    private static Method mShapeMarkerLineColor;
    private static Method mShapeMarkerLineWidth;
    private static Method mShapeMarkerDetail;
    private static Method mShapeMarkerBuild;

    private static Method mPOIMarkerToBuilder;
    private static Method mPOIMarkerLabel;
    private static Method mPOIMarkerPosition;
    private static Method mPOIMarkerDetail;
    private static Method mPOIMarkerBuild;

    private static Constructor<?> cVector2d;
    private static Constructor<?> cShape;
    private static Constructor<?> cColor;

    public static void init() {
        if (!ModList.get().isLoaded("bluemap")) {
            return;
        }

        try {
            loadClasses();
            checkApi();
            NationsMod.LOGGER.info("BlueMap обнаружен, интеграция готова.");
            enabled = true;
        } catch (Exception e) {
            NationsMod.LOGGER.error("Ошибка инициализации BlueMap интеграции: " + e.getMessage());
            // e.printStackTrace(); // Можно включить для отладки
        }
    }

    private static void loadClasses() throws ClassNotFoundException, NoSuchMethodException {
        // Загружаем классы по имени. 
        // ВАЖНО: Vector2d берем из flow-math, который есть в BlueMap
        clsBlueMapAPI = Class.forName("de.bluecolored.bluemap.api.BlueMapAPI");
        clsBlueMapMap = Class.forName("de.bluecolored.bluemap.api.BlueMapMap");
        clsMarkerSet = Class.forName("de.bluecolored.bluemap.api.markers.MarkerSet");
        clsShapeMarker = Class.forName("de.bluecolored.bluemap.api.markers.ShapeMarker");
        clsPOIMarker = Class.forName("de.bluecolored.bluemap.api.markers.POIMarker");
        clsShape = Class.forName("de.bluecolored.bluemap.api.math.Shape");
        clsVector2d = Class.forName("com.flowpowered.math.vector.Vector2d");
        clsColor = Class.forName("de.bluecolored.bluemap.api.math.Color");

        // Методы BlueMapAPI
        mGetInstance = clsBlueMapAPI.getMethod("getInstance");
        mGetMaps = clsBlueMapAPI.getMethod("getMaps");
        
        // Методы BlueMapMap
        mGetId = clsBlueMapMap.getMethod("getId");
        mGetMarkerSets = clsBlueMapMap.getMethod("getMarkerSets");

        // Методы MarkerSet
        mMarkerSetBuilder = clsMarkerSet.getMethod("builder");
        Class<?> clsMarkerSetBuilder = mMarkerSetBuilder.getReturnType();
        mMarkerSetLabel = clsMarkerSetBuilder.getMethod("label", String.class);
        mMarkerSetDefaultHidden = clsMarkerSetBuilder.getMethod("defaultHidden", boolean.class);
        mMarkerSetBuild = clsMarkerSetBuilder.getMethod("build");
        mMarkerSetGetMarkers = clsMarkerSet.getMethod("getMarkers");

        // Методы ShapeMarker
        mShapeMarkerBuilder = clsShapeMarker.getMethod("builder");
        Class<?> clsShapeMarkerBuilder = mShapeMarkerBuilder.getReturnType();
        mShapeMarkerLabel = clsShapeMarkerBuilder.getMethod("label", String.class);
        mShapeMarkerShape = clsShapeMarkerBuilder.getMethod("shape", clsShape, float.class);
        mShapeMarkerDepthTest = clsShapeMarkerBuilder.getMethod("depthTestEnabled", boolean.class);
        mShapeMarkerFillColor = clsShapeMarkerBuilder.getMethod("fillColor", clsColor);
        mShapeMarkerLineColor = clsShapeMarkerBuilder.getMethod("lineColor", clsColor);
        mShapeMarkerLineWidth = clsShapeMarkerBuilder.getMethod("lineWidth", int.class);
        mShapeMarkerDetail = clsShapeMarkerBuilder.getMethod("detail", String.class);
        mShapeMarkerBuild = clsShapeMarkerBuilder.getMethod("build");

        // Методы POIMarker
        mPOIMarkerToBuilder = clsPOIMarker.getMethod("toBuilder");
        Class<?> clsPOIMarkerBuilder = mPOIMarkerToBuilder.getReturnType();
        mPOIMarkerLabel = clsPOIMarkerBuilder.getMethod("label", String.class);
        mPOIMarkerPosition = clsPOIMarkerBuilder.getMethod("position", double.class, double.class, double.class);
        mPOIMarkerDetail = clsPOIMarkerBuilder.getMethod("detail", String.class);
        mPOIMarkerBuild = clsPOIMarkerBuilder.getMethod("build");

        // Конструкторы
        cVector2d = clsVector2d.getConstructor(double.class, double.class);
        cShape = clsShape.getConstructor(clsVector2d.arrayType());
        cColor = clsColor.getConstructor(int.class, int.class, int.class, float.class);
    }

    private static void checkApi() {
        try {
            Optional<?> opt = (Optional<?>) mGetInstance.invoke(null);
            if (opt.isPresent()) {
                blueMapAPI = opt.get();
            }
        } catch (Exception ignored) {}
    }

    public static void updateAllMarkers() {
        if (!enabled) return;
        
        if (blueMapAPI == null) {
            checkApi();
            if (blueMapAPI == null) return;
        }

        try {
            Collection<?> maps = (Collection<?>) mGetMaps.invoke(blueMapAPI);
            
            for (Object map : maps) {
                String mapId = (String) mGetId.invoke(map);
                
                // Фильтр миров
                if (!mapId.toLowerCase().contains("overworld") && !mapId.equals("world")) continue;

                Map<String, Object> markerSets = (Map<String, Object>) mGetMarkerSets.invoke(map);
                
                Object markerSet = markerSets.get(MARKER_SET_ID);
                
                if (markerSet == null) {
                    Object builder = mMarkerSetBuilder.invoke(null);
                    mMarkerSetLabel.invoke(builder, "Города и Нации");
                    mMarkerSetDefaultHidden.invoke(builder, false);
                    markerSet = mMarkerSetBuild.invoke(builder);
                    markerSets.put(MARKER_SET_ID, markerSet);
                }

                Map<String, Object> markers = (Map<String, Object>) mMarkerSetGetMarkers.invoke(markerSet);
                markers.clear();

                for (Town town : NationsData.getAllTowns()) {
                    drawTown(town, markers);
                }
            }
        } catch (Exception e) {
            NationsMod.LOGGER.error("Ошибка обновления маркеров BlueMap: " + e.getMessage());
        }
    }

    private static void drawTown(Town town, Map<String, Object> markers) throws Exception {
        int r = 136, g = 136, b = 136;
        String nationName = "";

        if (town.getNationName() != null) {
            Nation nation = NationsData.getNation(town.getNationName());
            if (nation != null) {
                int hex = nation.getColor().getHex();
                r = (hex >> 16) & 0xFF;
                g = (hex >> 8) & 0xFF;
                b = (hex) & 0xFF;
                nationName = nation.getName();
            }
        }

        Object fillColor = cColor.newInstance(r, g, b, 0.4f);
        Object lineColor = cColor.newInstance(r, g, b, 0.9f);

        if (town.isAtWar()) {
            lineColor = cColor.newInstance(255, 0, 0, 1.0f);
        } else if (town.isCaptured()) {
            lineColor = cColor.newInstance(255, 100, 0, 1.0f);
        }

        for (ChunkPos cp : town.getClaimedChunks()) {
            double x1 = cp.x * 16;
            double z1 = cp.z * 16;
            double x2 = x1 + 16;
            double z2 = z1 + 16;

            Object v1 = cVector2d.newInstance(x1, z1);
            Object v2 = cVector2d.newInstance(x2, z1);
            Object v3 = cVector2d.newInstance(x2, z2);
            Object v4 = cVector2d.newInstance(x1, z2);
            
            Object pointsArray = java.lang.reflect.Array.newInstance(clsVector2d, 4);
            java.lang.reflect.Array.set(pointsArray, 0, v1);
            java.lang.reflect.Array.set(pointsArray, 1, v2);
            java.lang.reflect.Array.set(pointsArray, 2, v3);
            java.lang.reflect.Array.set(pointsArray, 3, v4);

            Object shape = cShape.newInstance(pointsArray);

            String markerId = "town_" + town.getName() + "_" + cp.x + "_" + cp.z;
            
            Object builder = mShapeMarkerBuilder.invoke(null);
            mShapeMarkerLabel.invoke(builder, town.getName());
            mShapeMarkerShape.invoke(builder, shape, 64f);
            mShapeMarkerDepthTest.invoke(builder, false);
            mShapeMarkerFillColor.invoke(builder, fillColor);
            mShapeMarkerLineColor.invoke(builder, lineColor);
            mShapeMarkerLineWidth.invoke(builder, 2);
            mShapeMarkerDetail.invoke(builder, buildPopup(town, nationName));
            
            Object chunkMarker = mShapeMarkerBuild.invoke(builder);
            markers.put(markerId, chunkMarker);
        }

        if (town.getSpawnPos() != null) {
            String spawnId = "spawn_" + town.getName();
            
            Object builder = mPOIMarkerToBuilder.invoke(null);
            mPOIMarkerLabel.invoke(builder, town.getName());
            mPOIMarkerPosition.invoke(builder, 
                (double)town.getSpawnPos().getX(), 
                (double)town.getSpawnPos().getY() + 2, 
                (double)town.getSpawnPos().getZ());
            mPOIMarkerDetail.invoke(builder, buildPopup(town, nationName));
            
            Object spawnMarker = mPOIMarkerBuild.invoke(builder);
            markers.put(spawnId, spawnMarker);
        }
    }

    private static String buildPopup(Town town, String nationName) {
        StringBuilder sb = new StringBuilder();
        sb.append("<div style='text-align:center; font-family: Minecraft, sans-serif;'>");
        sb.append("<h3 style='color:#FFD700; margin:0;'>🏰 ").append(town.getName()).append("</h3>");
        
        if (!nationName.isEmpty()) {
            sb.append("<div style='color:#55AAFF; font-weight:bold;'>🏛 ").append(nationName).append("</div>");
        }

        if (town.isAtWar()) sb.append("<div style='color:#FF0000; font-weight:bold;'>⚔ ВОЙНА</div>");
        if (town.isCaptured()) sb.append("<div style='color:#FFAA00; font-weight:bold;'>🏴 ЗАХВАЧЕН</div>");

        sb.append("<hr>");
        sb.append("<div>👥 Жителей: <b>").append(town.getMembers().size()).append("</b></div>");
        sb.append("<div>📍 Чанков: <b>").append(town.getClaimedChunks().size()).append("</b></div>");
        sb.append("<div>⚔ PvP: <b>").append(town.isPvpEnabled() ? "<span style='color:red'>ON</span>" : "<span style='color:green'>OFF</span>").append("</b></div>");
        
        String mayorName = "Неизвестно";
        if (NationsData.getServer() != null) {
            var p = NationsData.getServer().getPlayerList().getPlayer(town.getMayor());
            if (p != null) mayorName = p.getName().getString();
        }
        sb.append("<div style='margin-top:5px;'>👑 Правитель: <span style='color:gold;'>").append(mayorName).append("</span></div>");
        sb.append("</div>");
        
        return sb.toString();
    }
}
