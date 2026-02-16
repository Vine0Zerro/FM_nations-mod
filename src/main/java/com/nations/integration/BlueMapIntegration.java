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

    // --- Кэш рефлексии (классы и методы) ---
    private static Class<?> clsBlueMapAPI, clsBlueMapMap, clsMarkerSet, clsShapeMarker, clsPOIMarker, clsShape, clsVector2d, clsColor;
    private static Method mGetInstance, mGetMaps, mGetId, mGetMarkerSets;
    private static Method mMarkerSetBuilder, mMarkerSetLabel, mMarkerSetBuild, mMarkerSetGetMarkers;
    private static Method mShapeMarkerBuilder, mShapeMarkerLabel, mShapeMarkerShape, mShapeMarkerDepthTest, mShapeMarkerFillColor, mShapeMarkerLineColor, mShapeMarkerLineWidth, mShapeMarkerDetail, mShapeMarkerBuild;
    private static Method mPOIMarkerToBuilder, mPOIMarkerLabel, mPOIMarkerPosition, mPOIMarkerDetail, mPOIMarkerBuild;
    private static Constructor<?> cVector2d, cShape, cColor;

    public static void init() {
        if (!ModList.get().isLoaded("bluemap")) return;

        try {
            loadClasses();
            
            // 1. Подписываемся на событие включения (если BlueMap загрузится позже)
            Method mOnEnable = clsBlueMapAPI.getMethod("onEnable", Consumer.class);
            // Сложно сделать Consumer через рефлексию, поэтому используем стратегию поллинга и прямой проверки
            
            // 2. Проверяем прямо сейчас (если BlueMap уже загружен)
            checkApi();
            
            if (blueMapAPI != null) {
                enabled = true;
                updateAllMarkers(); // Рисуем сразу при старте!
                NationsMod.LOGGER.info("BlueMap подключен и обновлен.");
            } else {
                NationsMod.LOGGER.info("BlueMap API не готов, ожидание...");
                enabled = true; // Разрешаем попытки обновления в тиках
            }
        } catch (Exception e) {
            NationsMod.LOGGER.error("BlueMap init error: " + e.getMessage());
        }
    }

    private static void loadClasses() throws ClassNotFoundException, NoSuchMethodException {
        ClassLoader cl = BlueMapIntegration.class.getClassLoader();
        clsBlueMapAPI = Class.forName("de.bluecolored.bluemap.api.BlueMapAPI", true, cl);
        clsBlueMapMap = Class.forName("de.bluecolored.bluemap.api.BlueMapMap", true, cl);
        clsMarkerSet = Class.forName("de.bluecolored.bluemap.api.markers.MarkerSet", true, cl);
        clsShapeMarker = Class.forName("de.bluecolored.bluemap.api.markers.ShapeMarker", true, cl);
        clsPOIMarker = Class.forName("de.bluecolored.bluemap.api.markers.POIMarker", true, cl);
        clsShape = Class.forName("de.bluecolored.bluemap.api.math.Shape", true, cl);
        clsVector2d = Class.forName("com.flowpowered.math.vector.Vector2d", true, cl);
        clsColor = Class.forName("de.bluecolored.bluemap.api.math.Color", true, cl);

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
            if (opt.isPresent()) blueMapAPI = opt.get();
        } catch (Exception ignored) {}
    }

    public static void updateAllMarkers() {
        if (!enabled) return;
        if (blueMapAPI == null) { checkApi(); if (blueMapAPI == null) return; }

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
                    drawTownMerged(town, markers);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // === Рисование объединенного контура ===
    private static void drawTownMerged(Town town, Map<String, Object> markers) throws Exception {
        Set<ChunkPos> chunks = town.getClaimedChunks();
        if (chunks.isEmpty()) return;

        // 1. Собираем грани (алгоритм объединения)
        Set<String> edges = new HashSet<>();
        for (ChunkPos cp : chunks) {
            double x1 = cp.x * 16;
            double z1 = cp.z * 16;
            double x2 = x1 + 16;
            double z2 = z1 + 16;
            toggleEdge(edges, x1, z1, x2, z1);
            toggleEdge(edges, x2, z1, x2, z2);
            toggleEdge(edges, x2, z2, x1, z2);
            toggleEdge(edges, x1, z2, x1, z1);
        }

        // 2. Строим полигоны
        List<List<Point>> polygons = tracePolygons(edges);

        // 3. Цвета
        int r = 136, g = 136, b = 136;
        String nationName = "Без нации";
        if (town.getNationName() != null) {
            Nation nation = NationsData.getNation(town.getNationName());
            if (nation != null) {
                int hex = nation.getColor().getHex();
                r = (hex >> 16) & 0xFF; g = (hex >> 8) & 0xFF; b = (hex) & 0xFF;
                nationName = nation.getName();
            }
        }

        Object fillColor = cColor.newInstance(r, g, b, 0.4f);
        Object lineColor = cColor.newInstance(r, g, b, 0.9f);

        if (town.isAtWar()) {
            fillColor = cColor.newInstance(255, 0, 0, 0.4f);
            lineColor = cColor.newInstance(255, 0, 0, 1.0f);
        } else if (town.isCaptured()) {
            fillColor = cColor.newInstance(255, 140, 0, 0.4f);
            lineColor = cColor.newInstance(255, 140, 0, 1.0f);
        }

        String popup = buildPopup(town, nationName, r, g, b);

        int polyIndex = 0;
        for (List<Point> polyPoints : polygons) {
            Object vectorArray = java.lang.reflect.Array.newInstance(clsVector2d, polyPoints.size());
            for (int i = 0; i < polyPoints.size(); i++) {
                Point p = polyPoints.get(i);
                Object vec = cVector2d.newInstance(p.x, p.z);
                java.lang.reflect.Array.set(vectorArray, i, vec);
            }

            Object shape = cShape.newInstance(vectorArray);
            
            Object builder = mShapeMarkerBuilder.invoke(null);
            mShapeMarkerLabel.invoke(builder, town.getName());
            mShapeMarkerShape.invoke(builder, shape, 64f);
            mShapeMarkerDepthTest.invoke(builder, false);
            mShapeMarkerFillColor.invoke(builder, fillColor);
            mShapeMarkerLineColor.invoke(builder, lineColor);
            mShapeMarkerLineWidth.invoke(builder, 3); // Жирная граница по краю
            mShapeMarkerDetail.invoke(builder, popup);

            Object marker = mShapeMarkerBuild.invoke(builder);
            markers.put("p_" + town.getName() + "_" + (polyIndex++), marker);
        }

        if (town.getSpawnPos() != null) {
            String spawnId = "spawn_" + town.getName();
            Object builder = mPOIMarkerToBuilder.invoke(null);
            mPOIMarkerLabel.invoke(builder, town.getName());
            mPOIMarkerPosition.invoke(builder, (double)town.getSpawnPos().getX(), (double)town.getSpawnPos().getY() + 2, (double)town.getSpawnPos().getZ());
            mPOIMarkerDetail.invoke(builder, popup);
            Object spawnMarker = mPOIMarkerBuild.invoke(builder);
            markers.put(spawnId, spawnMarker);
        }
    }

    private static void toggleEdge(Set<String> edges, double x1, double z1, double x2, double z2) {
        String forward = x1 + "," + z1 + ">" + x2 + "," + z2;
        String backward = x2 + "," + z2 + ">" + x1 + "," + z1;
        if (edges.contains(backward)) edges.remove(backward);
        else edges.add(forward);
    }

    private static List<List<Point>> tracePolygons(Set<String> edges) {
        List<List<Point>> polygons = new ArrayList<>();
        Map<Point, Point> pathMap = new HashMap<>();
        for (String edge : edges) {
            String[] parts = edge.split(">");
            String[] p1 = parts[0].split(",");
            String[] p2 = parts[1].split(",");
            pathMap.put(new Point(Double.parseDouble(p1[0]), Double.parseDouble(p1[1])),
                        new Point(Double.parseDouble(p2[0]), Double.parseDouble(p2[1])));
        }
        while (!pathMap.isEmpty()) {
            List<Point> poly = new ArrayList<>();
            Point start = pathMap.keySet().iterator().next();
            Point current = start;
            while (current != null) {
                poly.add(current);
                Point next = pathMap.remove(current);
                if (next == null || next.equals(start)) break;
                current = next;
            }
            polygons.add(poly);
        }
        return polygons;
    }

    private static class Point {
        double x, z;
        Point(double x, double z) { this.x = x; this.z = z; }
        @Override public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Point p = (Point) o; return Double.compare(p.x, x) == 0 && Double.compare(p.z, z) == 0;
        }
        @Override public int hashCode() { return Objects.hash(x, z); }
    }

    private static String buildPopup(Town town, String nationName, int r, int g, int b) {
        StringBuilder sb = new StringBuilder();
        
        // CSS
        // margin: -10px и width: 100% заставляют наш блок заполнить стандартный пузырь полностью
        String containerStyle = "font-family: 'Segoe UI', sans-serif; background: rgba(15, 15, 20, 0.95); " +
                                "padding: 15px; border-radius: 8px; color: #fff; min-width: 260px; " +
                                "margin: -10px; border: 1px solid rgba(255,255,255,0.1);";
        
        String centerStyle = "text-align: center; margin-bottom: 10px;";
        String leftStyle = "text-align: left;";
        String rowStyle = "margin-bottom: 4px; font-size: 14px; line-height: 1.4;";
        String labelStyle = "color: #bbb; font-weight: 600; margin-right: 6px;";
        String valStyle = "color: #fff; font-weight: 500;";
        
        String titleColor = String.format("rgb(%d, %d, %d)", r, g, b);
        if (town.isAtWar()) titleColor = "#FF4444";

        sb.append("<div style=\"").append(containerStyle).append("\">");

        // --- ВЕРХ (Центр) ---
        sb.append("<div style=\"").append(centerStyle).append("\">");
        
        // Нация
        String natColor = town.getNationName() != null ? titleColor : "#fff";
        sb.append("<div style=\"font-size: 12px; color: #aaa; text-transform: uppercase;\">Нация</div>");
        sb.append("<div style=\"font-size: 16px; font-weight: bold; color: ").append(natColor).append(";\">")
          .append(nationName).append("</div>");
        
        // Город
        sb.append("<div style=\"font-size: 12px; color: #aaa; text-transform: uppercase; margin-top: 5px;\">Город</div>");
        sb.append("<div style=\"font-size: 20px; font-weight: 800; color: #FFD700;\">")
          .append(town.getName()).append("</div>");
        
        sb.append("</div>"); // Конец центра

        // --- РАЗДЕЛИТЕЛЬ ---
        sb.append("<hr style=\"border: 0; border-top: 1px solid rgba(255,255,255,0.4); margin: 10px 0;\">");

        // --- НИЗ (Слева) ---
        sb.append("<div style=\"").append(leftStyle).append("\">");

        // Мэр
        String mayorName = "Неизвестно";
        if (NationsData.getServer() != null) {
            var p = NationsData.getServer().getPlayerList().getPlayer(town.getMayor());
            if (p != null) mayorName = p.getName().getString();
        }
        sb.append("<div style=\"").append(rowStyle).append("\">")
          .append("<span style=\"").append(labelStyle).append("\">Мэр:</span>")
          .append("<span style=\"").append(valStyle).append("\">").append(mayorName).append("</span>")
          .append("</div>");

        // Жители (в одну строку)
        sb.append("<div style=\"").append(rowStyle).append("\">")
          .append("<span style=\"").append(labelStyle).append("\">Жители:</span>")
          .append("<span style=\"color: #ddd; font-size: 13px;\">");
        
        List<String> names = new ArrayList<>();
        int limit = 0;
        for (UUID id : town.getMembers()) {
            if (limit >= 15) { names.add("..."); break; }
            if (NationsData.getServer() != null) {
                var p = NationsData.getServer().getPlayerList().getPlayer(id);
                names.add(p != null ? p.getName().getString() : "оффлайн");
            } else {
                names.add("?");
            }
            limit++;
        }
        sb.append(String.join(", ", names));
        sb.append("</span></div>"); // Конец жителей

        // Статусы
        if (town.isAtWar()) {
            sb.append("<div style=\"margin-top:10px; color:#ff5555; font-weight:bold; text-align:center;\">⚠ ИДЕТ ВОЙНА</div>");
        } else if (town.isCaptured()) {
            sb.append("<div style=\"margin-top:10px; color:#ffaa00; font-weight:bold; text-align:center;\">🏴 ЗАХВАЧЕН</div>");
        }

        sb.append("</div>"); // Конец низа
        sb.append("</div>"); // Конец контейнера

        return sb.toString();
    }
}
