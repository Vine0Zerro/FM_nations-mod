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
        }
    }

    private static void loadClasses() throws ClassNotFoundException, NoSuchMethodException {
        clsBlueMapAPI = Class.forName("de.bluecolored.bluemap.api.BlueMapAPI");
        clsBlueMapMap = Class.forName("de.bluecolored.bluemap.api.BlueMapMap");
        clsMarkerSet = Class.forName("de.bluecolored.bluemap.api.markers.MarkerSet");
        clsShapeMarker = Class.forName("de.bluecolored.bluemap.api.markers.ShapeMarker");
        clsPOIMarker = Class.forName("de.bluecolored.bluemap.api.markers.POIMarker");
        clsShape = Class.forName("de.bluecolored.bluemap.api.math.Shape");
        clsVector2d = Class.forName("com.flowpowered.math.vector.Vector2d");
        clsColor = Class.forName("de.bluecolored.bluemap.api.math.Color");

        mGetInstance = clsBlueMapAPI.getMethod("getInstance");
        mGetMaps = clsBlueMapAPI.getMethod("getMaps");
        mGetId = clsBlueMapMap.getMethod("getId");
        mGetMarkerSets = clsBlueMapMap.getMethod("getMarkerSets");

        mMarkerSetBuilder = clsMarkerSet.getMethod("builder");
        Class<?> clsMarkerSetBuilder = mMarkerSetBuilder.getReturnType();
        mMarkerSetLabel = clsMarkerSetBuilder.getMethod("label", String.class);
        mMarkerSetBuild = clsMarkerSetBuilder.getMethod("build");
        mMarkerSetGetMarkers = clsMarkerSet.getMethod("getMarkers");

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

        mPOIMarkerToBuilder = clsPOIMarker.getMethod("toBuilder");
        Class<?> clsPOIMarkerBuilder = mPOIMarkerToBuilder.getReturnType();
        mPOIMarkerLabel = clsPOIMarkerBuilder.getMethod("label", String.class);
        mPOIMarkerPosition = clsPOIMarkerBuilder.getMethod("position", double.class, double.class, double.class);
        mPOIMarkerDetail = clsPOIMarkerBuilder.getMethod("detail", String.class);
        mPOIMarkerBuild = clsPOIMarkerBuilder.getMethod("build");

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
                
                if (!mapId.toLowerCase().contains("overworld") && !mapId.equals("world")) continue;

                Map<String, Object> markerSets = (Map<String, Object>) mGetMarkerSets.invoke(map);
                Object markerSet = markerSets.get(MARKER_SET_ID);
                
                if (markerSet == null) {
                    Object builder = mMarkerSetBuilder.invoke(null);
                    mMarkerSetLabel.invoke(builder, "Города и Нации");
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
        String nationName = "Без нации";

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

        // Цвета
        Object fillColor;
        Object lineColor;
        // lineWidth = 0 убирает сетку, делая заливку сплошной
        int lineWidth = 0; 

        if (town.isAtWar()) {
            fillColor = cColor.newInstance(255, 0, 0, 0.4f);
            lineColor = cColor.newInstance(255, 0, 0, 0.0f);
        } else if (town.isCaptured()) {
            fillColor = cColor.newInstance(255, 140, 0, 0.4f);
            lineColor = cColor.newInstance(255, 140, 0, 0.0f);
        } else {
            fillColor = cColor.newInstance(r, g, b, 0.4f);
            // Прозрачная линия, чтобы не было границ между чанками
            lineColor = cColor.newInstance(r, g, b, 0.0f); 
        }

        String popup = buildPopup(town, nationName);

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
            mShapeMarkerLineWidth.invoke(builder, lineWidth);
            mShapeMarkerDetail.invoke(builder, popup);
            
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
            mPOIMarkerDetail.invoke(builder, popup);
            
            Object spawnMarker = mPOIMarkerBuild.invoke(builder);
            markers.put(spawnId, spawnMarker);
        }
    }

    private static String buildPopup(Town town, String nationName) {
        StringBuilder sb = new StringBuilder();
        
        // Стили CSS
        // Шрифт Segoe UI, жирность 600, закругленные углы 12px
        String boxStyle = "font-family: 'Segoe UI', sans-serif; padding: 15px; background: rgba(20, 20, 30, 0.95); " +
                          "border-radius: 12px; border: 1px solid rgba(255, 255, 255, 0.15); " + 
                          "width: 240px; text-align: center; color: white; box-shadow: 0 5px 15px rgba(0,0,0,0.5);";
        
        String labelStyle = "font-size: 13px; font-weight: 600; color: #AAAAAA; margin-bottom: 2px;";
        String valueStyle = "font-size: 16px; font-weight: 700; margin-bottom: 12px;";
        
        sb.append("<div style=\"").append(boxStyle).append("\">");

        // 1. Нация
        sb.append("<div style=\"").append(labelStyle).append("\">Нация:</div>");
        
        String nColor = "#FFFFFF";
        if (town.getNationName() != null) {
             Nation nation = NationsData.getNation(town.getNationName());
             if (nation != null) {
                 int hex = nation.getColor().getHex();
                 nColor = String.format("#%06X", (0xFFFFFF & hex));
             }
        }
        sb.append("<div style=\"").append(valueStyle).append("color:").append(nColor).append(";\">")
          .append(nationName).append("</div>");

        // 2. Город
        sb.append("<div style=\"").append(labelStyle).append("\">Город:</div>");
        sb.append("<div style=\"").append(valueStyle).append("color: #FFD700;\">")
          .append(town.getName()).append("</div>");

        // 3. Разделитель
        sb.append("<hr style=\"border: 0; border-top: 1px solid rgba(255,255,255,0.2); margin: 10px 0;\">");

        // 4. Мэр
        String mayorName = "Неизвестно";
        if (NationsData.getServer() != null) {
            var p = NationsData.getServer().getPlayerList().getPlayer(town.getMayor());
            if (p != null) mayorName = p.getName().getString();
        }
        sb.append("<div style=\"").append(labelStyle).append("\">Мэр:</div>");
        sb.append("<div style=\"").append(valueStyle).append("\">").append(mayorName).append("</div>");

        // 5. Жители (списком)
        sb.append("<div style=\"").append(labelStyle).append("\">Жители:</div>");
        sb.append("<div style=\"font-size: 13px; color: #DDDDDD; line-height: 1.4;\">");
        
        List<String> names = new ArrayList<>();
        int limit = 0;
        for (UUID id : town.getMembers()) {
            if (limit >= 12) {
                names.add("и др.");
                break;
            }
            if (NationsData.getServer() != null) {
                var p = NationsData.getServer().getPlayerList().getPlayer(id);
                if (p != null) names.add(p.getName().getString());
                else names.add("оффлайн");
            } else {
                names.add("?");
            }
            limit++;
        }
        sb.append(String.join(", ", names));
        
        sb.append("</div>"); // residents
        sb.append("</div>"); // box

        return sb.toString();
    }
}
