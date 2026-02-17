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

    private static final String ICON_CROWN_BASE64 = "data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAACAAAAAgCAYAAABzenr0AAAABmJLR0QA/wD/AP+gvaeTAAAACXBIWXMAAAsTAAALEwEAmpwYAAAAB3RJTUUH6gIQFR0z0/48VwAAAqVJREFUWMPtl89rE0EUx7+zs7vbFk0tVv+A4kERrJdePCh48CDiQfBisp68e/GsB717F/wD/AfEg3gQBC2ItyJ6Ew/WgxQramO32d3ZmRkP2zbZpGk3iXjwcWbe2/fNfN+ZefMGRF+W9X8D0C6V90d9LyPzQ4g0L0GZpwD4E+D6wN/16fN7I0Qh0FqZAtA+d+6o+30nhDYEASLhQGtlhghk/2ATADxpJigzTSa8IhqNQiMRCGBCeaSMjUZgCBCLte8SpV7o2M/JpA0AsrD+EwB0GGEALqb3A8gRsblo9yfZF0qy10w0fK21+9SLdWtO9cp4/b5QHTryHCdiawFk36oDAGSNMAD68JcA5OfDGV6p3b54r7FohfIZ8ypdz7a0nnpl8pr/1sajg9+b+A+vrfgA385dPfBYtPGlW15x6KwzOuVoBULs3BtJfbfZCO3sdwuvR4Ux/NfqCHdUedO+mhUvnymNVX+Q7Lnx+8ooM8e/wotnX33qiYbmLw8W5gdmVGnmsegXAPkAwCiK7bNng4o8n7LsL4o3f2h5yqdn8g2ZENbRdhE/HsI40CkTDLtFrrpzA5dWVIb6Mv7QgDPXP+IV6ZRJbIwD7SJ+vFZYRx6d/pv0lUL86GWTsvrCZlHzfHNNdM+OxtSiA/eem/Old8i53qt/rrvztNVVs2bVBTtlGGLsbTsRDQ/8K6xU7pBXdkf2hPMkpjFkIbTrkSOZrJCGfm3g9q5Iwj7dFxoibxn5M1K5GR7nlwfjdUHbbtlWba4orZdr/zIx+qeyaf5uwCwuAiw6EJcm2S49PegtXNyxNdG979HWkzccJ+Qnc3g1pcydTdX6bZ36mZ/eUPb50ZnY9l3tL/RubYj09jRGd/Z2vPCv29Oxn13X9vYO9UzPpkq9X1PK6GSB645jP74rc3L3zuSGfnf2QrS5ON0QCj2YgSklsKlQrajok/8B+le5c6l0OCgAAAAASUVORK5CYII=";
    private static final String ICON_TOWN_BASE64 = "data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAABAAAAAQCAMAAAAoLQ9TAAAAA3NCSVQICAjb4U/gAAAACXBIWXMAAAsTAAALEwEAmpwYAAAAB3RJTUUH6gIQFR4s+v8+3QAAABl0RVh0Q29tbWVudABDcmVhdGVkIHdpdGggR0lNUFeBDhcAAABLUExURQAAAM/P0dDQ0svLzczMzvfz9vv7+/z7++Tm5tzd2/r5+fj49/n19/79/fX19P/+/v38/P39/f///+rq6cfHxt7e3qenpt/f3qiop9XU1KalpcC+vqKhocG/v8/PzquqqqGfn6OiocG7v8bFxMLAwNTU07q6uaWjpKqpqaaipa6qrLOusamlp6yoqv///9/h4dja2f////j49u3t7NLU1MbHxevr6vr6+erq6e3t7N3d3MbGxt7e3uDf37e3tqempt/f3t/f37a2tainp9TU09ra2bKxsaWlpL68vMvKydTT09XV1Lu7u7Kysainp5+enry6usHAv7Cwr5yam+np6NjY2NLS0ubm5eXl5d3d3MnJyN/f39vb27+/v7q6ud7e3tva2r+/vv///+VauVgAAABUdFJOUwAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAKU1MITbP29rNNOe3tOUD29kBA9vZAOe3tOAhNs/f2s0wIClxcCsucjc0AAAABYktHRBJ7vGwAAAAAC4lEQVQY02NgIBIwMjGzIHFZ2dg5OLm4eWB8dl4+PX0DPT5+AaiAoKGRsYmpmbmhEFRA2MIyJDQs3MpaBCogamMbERIZZWcvBhUQd3CMjomNc3KWgApIurjGJyTGublLQQWkPTy9vH18/fxloAKy0nIBgUHB8gqKMIcopaioqqlraCK5VUtbR5dYbwIAoBMTO+1B/sUAAAAASUVORK5CYII=";

    private static Class<?> clsBlueMapAPI, clsBlueMapMap, clsMarkerSet, clsShapeMarker, clsPOIMarker, clsShape, clsVector2d, clsColor;
    private static Class<?> clsVector3d;
    private static Method mGetInstance, mGetMaps, mGetId, mGetMarkerSets;
    private static Method mMarkerSetBuilder, mMarkerSetLabel, mMarkerSetBuild, mMarkerSetGetMarkers;
    private static Method mShapeMarkerBuilder, mShapeMarkerLabel, mShapeMarkerShape, mShapeMarkerDepthTest, mShapeMarkerFillColor, mShapeMarkerLineColor, mShapeMarkerLineWidth, mShapeMarkerDetail, mShapeMarkerBuild;
    private static Method mPOIMarkerBuilder, mPOIMarkerLabel, mPOIMarkerDetail, mPOIMarkerIcon, mPOIMarkerBuild;
    private static Method mPOIMarkerPosition;
    private static Constructor<?> cVector2d, cShape, cColor;
    private static Constructor<?> cVector3d;
    private static Method mOnEnable;

    public static void init() {
        if (!ModList.get().isLoaded("bluemap")) {
            NationsMod.LOGGER.warn("BlueMap mod not found! Integration disabled.");
            return;
        }
        try {
            loadClasses();
            registerOnEnable();
            NationsMod.LOGGER.info("BlueMap integration registered — waiting for BlueMap API...");
        } catch (Exception e) {
            NationsMod.LOGGER.error("Failed to initialize BlueMap integration: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static void loadClasses() throws Exception {
        ClassLoader cl = BlueMapIntegration.class.getClassLoader();

        clsBlueMapAPI = Class.forName("de.bluecolored.bluemap.api.BlueMapAPI", true, cl);
        clsBlueMapMap = Class.forName("de.bluecolored.bluemap.api.BlueMapMap", true, cl);
        clsMarkerSet = Class.forName("de.bluecolored.bluemap.api.markers.MarkerSet", true, cl);
        clsShapeMarker = Class.forName("de.bluecolored.bluemap.api.markers.ShapeMarker", true, cl);
        clsPOIMarker = Class.forName("de.bluecolored.bluemap.api.markers.POIMarker", true, cl);
        clsShape = Class.forName("de.bluecolored.bluemap.api.math.Shape", true, cl);
        clsColor = Class.forName("de.bluecolored.bluemap.api.math.Color", true, cl);

        // Пробуем разные пакеты для Vector2d и Vector3d
        try {
            clsVector2d = Class.forName("com.flowpowered.math.vector.Vector2d", true, cl);
            clsVector3d = Class.forName("com.flowpowered.math.vector.Vector3d", true, cl);
        } catch (ClassNotFoundException e) {
            clsVector2d = Class.forName("de.bluecolored.bluemap.api.math.Vector2d", true, cl);
            clsVector3d = Class.forName("de.bluecolored.bluemap.api.math.Vector3d", true, cl);
        }

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

        // POI — пробуем position с Vector3d, потом с 3 double
        mPOIMarkerBuilder = clsPOIMarker.getMethod("builder");
        Class<?> clsPOIMarkerBuilder = mPOIMarkerBuilder.getReturnType();
        mPOIMarkerLabel = clsPOIMarkerBuilder.getMethod("label", String.class);
        mPOIMarkerDetail = clsPOIMarkerBuilder.getMethod("detail", String.class);

        // Пробуем position(Vector3d)
        try {
            mPOIMarkerPosition = clsPOIMarkerBuilder.getMethod("position", clsVector3d);
        } catch (NoSuchMethodException e) {
            // Пробуем position(double, double, double)
            mPOIMarkerPosition = clsPOIMarkerBuilder.getMethod("position", double.class, double.class, double.class);
        }

        // icon — пробуем разные сигнатуры
        try {
            mPOIMarkerIcon = clsPOIMarkerBuilder.getMethod("icon", String.class, int.class, int.class);
        } catch (NoSuchMethodException e) {
            try {
                mPOIMarkerIcon = clsPOIMarkerBuilder.getMethod("icon", String.class, clsVector2d);
            } catch (NoSuchMethodException e2) {
                mPOIMarkerIcon = null;
                NationsMod.LOGGER.warn("BlueMap POI icon method not found — icons disabled");
            }
        }

        mPOIMarkerBuild = clsPOIMarkerBuilder.getMethod("build");

        cVector2d = clsVector2d.getConstructor(double.class, double.class);
        cVector3d = clsVector3d.getConstructor(double.class, double.class, double.class);
        cShape = clsShape.getConstructor(clsVector2d.arrayType());
        cColor = clsColor.getConstructor(int.class, int.class, int.class, float.class);

        mOnEnable = clsBlueMapAPI.getMethod("onEnable", Consumer.class);
    }

    @SuppressWarnings("unchecked")
    private static void registerOnEnable() throws Exception {
        Consumer<Object> callback = api -> {
            blueMapAPI = api;
            enabled = true;
            NationsMod.LOGGER.info("BlueMap API is now available! Rendering all markers...");
            try {
                updateAllMarkers();
            } catch (Exception e) {
                NationsMod.LOGGER.error("Error during initial BlueMap marker render: " + e.getMessage());
                e.printStackTrace();
            }
        };
        mOnEnable.invoke(null, callback);

        try {
            Optional<?> opt = (Optional<?>) mGetInstance.invoke(null);
            if (opt.isPresent()) {
                blueMapAPI = opt.get();
                enabled = true;
            }
        } catch (Exception ignored) {}
    }

    public static boolean isEnabled() {
        return enabled && blueMapAPI != null;
    }

    public static void updateAllMarkers() {
        if (!enabled || blueMapAPI == null) {
            try {
                Optional<?> opt = (Optional<?>) mGetInstance.invoke(null);
                if (opt.isPresent()) {
                    blueMapAPI = opt.get();
                    enabled = true;
                } else {
                    return;
                }
            } catch (Exception e) {
                return;
            }
        }

        try {
            Collection<?> maps = (Collection<?>) mGetMaps.invoke(blueMapAPI);
            if (maps.isEmpty()) return;

            for (Object map : maps) {
                String mapId = (String) mGetId.invoke(map);
                if (!mapId.toLowerCase().contains("overworld") && !mapId.equals("world")) continue;

                Map<String, Object> markerSets = (Map<String, Object>) mGetMarkerSets.invoke(map);
                Object markerSet = markerSets.get(MARKER_SET_ID);

                if (markerSet == null) {
                    Object builder = mMarkerSetBuilder.invoke(null);
                    mMarkerSetLabel.invoke(builder, "Нации и Города");
                    markerSet = mMarkerSetBuild.invoke(builder);
                    markerSets.put(MARKER_SET_ID, markerSet);
                }

                Map<String, Object> markers = (Map<String, Object>) mMarkerSetGetMarkers.invoke(markerSet);
                markers.clear();

                // 1. Внешние границы НАЦИЙ (толстые)
                for (Nation nation : NationsData.getAllNations()) {
                    try {
                        drawNationBorder(nation, markers);
                    } catch (Exception e) {
                        NationsMod.LOGGER.error("Error drawing nation border for " + nation.getName() + ": " + e.getMessage());
                    }
                }

                // 2. Внутренние границы городов внутри наций (тонкие, пунктирные)
                for (Nation nation : NationsData.getAllNations()) {
                    try {
                        drawInnerTownBorders(nation, markers);
                    } catch (Exception e) {
                        NationsMod.LOGGER.error("Error drawing inner borders for " + nation.getName() + ": " + e.getMessage());
                    }
                }

                // 3. Города без нации
                for (Town town : NationsData.getAllTowns()) {
                    if (town.getNationName() == null) {
                        try {
                            drawStandaloneTown(town, markers);
                        } catch (Exception e) {
                            NationsMod.LOGGER.error("Error drawing standalone town " + town.getName() + ": " + e.getMessage());
                        }
                    }
                }

                // 4. Иконки городов
                for (Town town : NationsData.getAllTowns()) {
                    try {
                        drawTownPOI(town, markers);
                    } catch (Exception e) {
                        NationsMod.LOGGER.error("Error drawing POI for " + town.getName() + ": " + e.getMessage());
                    }
                }

                NationsMod.LOGGER.info("BlueMap markers updated: " + markers.size() + " markers on " + mapId);
            }
        } catch (Exception e) {
            NationsMod.LOGGER.error("Error updating BlueMap markers: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // === ОТРИСОВКА ===

    private static void drawNationBorder(Nation nation, Map<String, Object> markers) throws Exception {
        // Собираем ВСЕ чанки нации
        Set<ChunkPos> allChunks = new HashSet<>();
        for (String tn : nation.getTowns()) {
            Town t = NationsData.getTown(tn);
            if (t != null) allChunks.addAll(t.getClaimedChunks());
        }
        if (allChunks.isEmpty()) return;

        // Только ВНЕШНИЕ рёбра (граница нации с пустотой или чужой территорией)
        Set<String> outerEdges = calculateOuterEdges(allChunks);
        List<List<Point>> polygons = tracePolygons(outerEdges);

        int hex = nation.getColor().getHex();
        int r = (hex >> 16) & 0xFF;
        int g = (hex >> 8) & 0xFF;
        int b = (hex) & 0xFF;

        Object fillColor = cColor.newInstance(r, g, b, 0.25f);
        Object lineColor = cColor.newInstance(r, g, b, 1.0f);

        String popup = buildNationPopup(nation, r, g, b);
        int i = 0;

        for (List<Point> poly : polygons) {
            if (poly.size() < 3) continue;
            Object shape = createShape(poly);
            Object marker = createShapeMarker(nation.getName(), shape, fillColor, lineColor, 4, popup);
            markers.put("n_" + nation.getName() + "_" + (i++), marker);
        }
    }

    private static void drawInnerTownBorders(Nation nation, Map<String, Object> markers) throws Exception {
        // Собираем какой чанк какому городу принадлежит
        Map<ChunkPos, String> chunkOwner = new HashMap<>();
        for (String tn : nation.getTowns()) {
            Town t = NationsData.getTown(tn);
            if (t != null) {
                for (ChunkPos cp : t.getClaimedChunks()) chunkOwner.put(cp, tn);
            }
        }

        // Находим рёбра МЕЖДУ разными городами одной нации
        Set<String> innerEdges = new HashSet<>();
        for (Map.Entry<ChunkPos, String> entry : chunkOwner.entrySet()) {
            ChunkPos cp = entry.getKey();
            String myTown = entry.getValue();

            checkInnerEdge(innerEdges, chunkOwner, myTown, new ChunkPos(cp.x, cp.z - 1), cp.x * 16, cp.z * 16, (cp.x + 1) * 16, cp.z * 16);
            checkInnerEdge(innerEdges, chunkOwner, myTown, new ChunkPos(cp.x, cp.z + 1), cp.x * 16, (cp.z + 1) * 16, (cp.x + 1) * 16, (cp.z + 1) * 16);
            checkInnerEdge(innerEdges, chunkOwner, myTown, new ChunkPos(cp.x + 1, cp.z), (cp.x + 1) * 16, cp.z * 16, (cp.x + 1) * 16, (cp.z + 1) * 16);
            checkInnerEdge(innerEdges, chunkOwner, myTown, new ChunkPos(cp.x - 1, cp.z), cp.x * 16, cp.z * 16, cp.x * 16, (cp.z + 1) * 16);
        }

        if (innerEdges.isEmpty()) return;

        int hex = nation.getColor().getHex();
        int r = (hex >> 16) & 0xFF;
        int g = (hex >> 8) & 0xFF;
        int b = (hex) & 0xFF;

        // Полупрозрачная тонкая линия для внутренних границ
        Object lineColor = cColor.newInstance(r, g, b, 0.4f);
        Object fillColor = cColor.newInstance(r, g, b, 0.05f);

        int i = 0;
        for (String edge : innerEdges) {
            String[] pts = edge.split(">");
            double x1 = Double.parseDouble(pts[0].split(",")[0]);
            double z1 = Double.parseDouble(pts[0].split(",")[1]);
            double x2 = Double.parseDouble(pts[1].split(",")[0]);
            double z2 = Double.parseDouble(pts[1].split(",")[1]);

            // Тонкий полигон-линия шириной 0.5 блока
            List<Point> linePoly = new ArrayList<>();
            double w = 0.5;
            if (Math.abs(x1 - x2) < 0.1) {
                linePoly.add(new Point(x1 - w, z1));
                linePoly.add(new Point(x1 + w, z1));
                linePoly.add(new Point(x2 + w, z2));
                linePoly.add(new Point(x2 - w, z2));
            } else {
                linePoly.add(new Point(x1, z1 - w));
                linePoly.add(new Point(x2, z2 - w));
                linePoly.add(new Point(x2, z2 + w));
                linePoly.add(new Point(x1, z1 + w));
            }

            Object shape = createShape(linePoly);
            // Толщина линии 1 (не 3!) и полупрозрачный цвет
            Object marker = createShapeMarker("", shape, fillColor, lineColor, 1, "");
            markers.put("in_" + nation.getName() + "_" + (i++), marker);
        }
    }

    private static void checkInnerEdge(Set<String> edges, Map<ChunkPos, String> owners, String myTown, ChunkPos neighbor, int x1, int z1, int x2, int z2) {
        String otherTown = owners.get(neighbor);
        if (otherTown != null && !otherTown.equals(myTown)) {
            String p1 = x1 + "," + z1;
            String p2 = x2 + "," + z2;
            if (p1.compareTo(p2) > 0) { String t = p1; p1 = p2; p2 = t; }
            edges.add(p1 + ">" + p2);
        }
    }

    private static void drawStandaloneTown(Town town, Map<String, Object> markers) throws Exception {
        if (town.getClaimedChunks().isEmpty()) return;

        Set<String> edges = calculateOuterEdges(town.getClaimedChunks());
        List<List<Point>> polygons = tracePolygons(edges);

        int r = 136, g = 136, b = 136;
        Object fillColor = cColor.newInstance(r, g, b, 0.35f);
        Object lineColor = cColor.newInstance(r, g, b, 1.0f);

        if (town.isAtWar()) {
            fillColor = cColor.newInstance(255, 0, 0, 0.4f);
            lineColor = cColor.newInstance(255, 0, 0, 1.0f);
        } else if (town.isCaptured()) {
            fillColor = cColor.newInstance(255, 140, 0, 0.4f);
            lineColor = cColor.newInstance(255, 140, 0, 1.0f);
        }

        String popup = buildPopup(town, "Без нации", r, g, b);
        int i = 0;
        for (List<Point> poly : polygons) {
            if (poly.size() < 3) continue;
            Object marker = createShapeMarker(town.getName(), createShape(poly), fillColor, lineColor, 3, popup);
            markers.put("t_" + town.getName() + "_" + (i++), marker);
        }
    }

    private static void drawTownPOI(Town town, Map<String, Object> markers) throws Exception {
        if (town.getSpawnPos() == null) return;

        boolean isCapital = false;
        String nationName = "Без нации";
        int r = 136, g = 136, b = 136;

        if (town.getNationName() != null) {
            Nation nation = NationsData.getNation(town.getNationName());
            if (nation != null) {
                int hex = nation.getColor().getHex();
                r = (hex >> 16) & 0xFF;
                g = (hex >> 8) & 0xFF;
                b = (hex) & 0xFF;
                nationName = nation.getName();
                isCapital = nation.isCapital(town.getName());
            }
        }

        String popup = buildPopup(town, nationName, r, g, b);
        Object builder = mPOIMarkerBuilder.invoke(null);
        mPOIMarkerLabel.invoke(builder, town.getName());

        // Position — поддержка разных API
        double px = town.getSpawnPos().getX() + 0.5;
        double py = town.getSpawnPos().getY() + 2.0;
        double pz = town.getSpawnPos().getZ() + 0.5;

        if (mPOIMarkerPosition.getParameterCount() == 1) {
            // position(Vector3d)
            Object vec3 = cVector3d.newInstance(px, py, pz);
            mPOIMarkerPosition.invoke(builder, vec3);
        } else {
            // position(double, double, double)
            mPOIMarkerPosition.invoke(builder, px, py, pz);
        }

        mPOIMarkerDetail.invoke(builder, popup);

        // Иконка
        if (mPOIMarkerIcon != null) {
            try {
                String iconData = isCapital ? ICON_CROWN_BASE64 : ICON_TOWN_BASE64;
                int iconSize = isCapital ? 32 : 16;

                if (mPOIMarkerIcon.getParameterCount() == 3) {
                    // icon(String, int, int) — anchor
                    mPOIMarkerIcon.invoke(builder, iconData, iconSize / 2, iconSize / 2);
                } else if (mPOIMarkerIcon.getParameterCount() == 2) {
                    // icon(String, Vector2d) — anchor as vector
                    Object anchor = cVector2d.newInstance((double)(iconSize / 2), (double)(iconSize / 2));
                    mPOIMarkerIcon.invoke(builder, iconData, anchor);
                }
            } catch (Exception e) {
                NationsMod.LOGGER.warn("Failed to set POI icon for " + town.getName() + ": " + e.getMessage());
            }
        }

        markers.put("poi_" + town.getName(), mPOIMarkerBuild.invoke(builder));
    }

    // === ВСПОМОГАТЕЛЬНЫЕ МЕТОДЫ ===

    private static Set<String> calculateOuterEdges(Set<ChunkPos> chunks) {
        Set<String> edges = new HashSet<>();
        for (ChunkPos cp : chunks) {
            double x1 = cp.x * 16.0;
            double z1 = cp.z * 16.0;
            double x2 = x1 + 16.0;
            double z2 = z1 + 16.0;
            toggleEdge(edges, x1, z1, x2, z1);
            toggleEdge(edges, x2, z1, x2, z2);
            toggleEdge(edges, x2, z2, x1, z2);
            toggleEdge(edges, x1, z2, x1, z1);
        }
        return edges;
    }

    private static void toggleEdge(Set<String> edges, double x1, double z1, double x2, double z2) {
        String f = x1 + "," + z1 + ">" + x2 + "," + z2;
        String b = x2 + "," + z2 + ">" + x1 + "," + z1;
        if (edges.contains(b)) edges.remove(b);
        else edges.add(f);
    }

    private static List<List<Point>> tracePolygons(Set<String> edges) {
        List<List<Point>> polygons = new ArrayList<>();
        Map<Point, Point> map = new HashMap<>();
        for (String e : edges) {
            String[] p = e.split(">");
            String[] a = p[0].split(",");
            String[] b = p[1].split(",");
            map.put(new Point(Double.parseDouble(a[0]), Double.parseDouble(a[1])),
                    new Point(Double.parseDouble(b[0]), Double.parseDouble(b[1])));
        }
        while (!map.isEmpty()) {
            List<Point> poly = new ArrayList<>();
            Point start = map.keySet().iterator().next();
            Point curr = start;
            int safety = 0;
            while (curr != null && safety++ < 100000) {
                poly.add(curr);
                Point next = map.remove(curr);
                if (next == null || next.equals(start)) break;
                curr = next;
            }
            if (poly.size() >= 3) polygons.add(poly);
        }
        return polygons;
    }

    private static Object createShape(List<Point> points) throws Exception {
        Object vectorArray = java.lang.reflect.Array.newInstance(clsVector2d, points.size());
        for (int i = 0; i < points.size(); i++) {
            java.lang.reflect.Array.set(vectorArray, i, cVector2d.newInstance(points.get(i).x, points.get(i).z));
        }
        return cShape.newInstance(vectorArray);
    }

    private static Object createShapeMarker(String label, Object shape, Object fill, Object line, int width, String detail) throws Exception {
        Object bd = mShapeMarkerBuilder.invoke(null);
        mShapeMarkerLabel.invoke(bd, label);
        mShapeMarkerShape.invoke(bd, shape, 64f);
        mShapeMarkerDepthTest.invoke(bd, false);
        mShapeMarkerFillColor.invoke(bd, fill);
        mShapeMarkerLineColor.invoke(bd, line);
        mShapeMarkerLineWidth.invoke(bd, width);
        mShapeMarkerDetail.invoke(bd, detail);
        return mShapeMarkerBuild.invoke(bd);
    }

    private static class Point {
        double x, z;
        Point(double x, double z) { this.x = x; this.z = z; }
        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Point p = (Point) o;
            return Double.compare(p.x, x) == 0 && Double.compare(p.z, z) == 0;
        }
        @Override
        public int hashCode() { return Objects.hash(x, z); }
    }

    private static String buildNationPopup(Nation nation, int r, int g, int b) {
        StringBuilder sb = new StringBuilder();
        sb.append("<div style=\"font-family:'Segoe UI';padding:12px;color:white;background:rgba(10,10,15,0.92);border-radius:8px;border:2px solid rgb(").append(r).append(",").append(g).append(",").append(b).append(")\">");
        sb.append("<div style=\"font-size:18px;font-weight:bold;color:rgb(").append(r).append(",").append(g).append(",").append(b).append(");margin-bottom:6px\">🏛 ").append(nation.getName()).append("</div>");
        sb.append("<hr style=\"border:0;border-top:1px solid #444;margin:6px 0\">");
        sb.append("<div style=\"margin:3px 0\">🏰 Городов: <b style=\"color:#FFD700\">").append(nation.getTowns().size()).append("</b></div>");
        sb.append("<div style=\"margin:3px 0\">👥 Жителей: <b style=\"color:#AAA\">").append(nation.getTotalMembers()).append("</b></div>");
        sb.append("<div style=\"margin:3px 0\">📍 Территория: <b style=\"color:#AAA\">").append(nation.getTotalChunks()).append("</b> чанков</div>");
        if (nation.getCapitalTown() != null) {
            sb.append("<div style=\"margin:3px 0\">👑 Столица: <b style=\"color:#FFD700\">").append(nation.getCapitalTown()).append("</b></div>");
        }
        if (!nation.getWarTargets().isEmpty()) {
            sb.append("<div style=\"margin:3px 0;color:#FF4444\">⚔ Война: <b>").append(String.join(", ", nation.getWarTargets())).append("</b></div>");
        }
        sb.append("</div>");
        return sb.toString();
    }

    private static String buildPopup(Town town, String nName, int r, int g, int b) {
        StringBuilder sb = new StringBuilder();
        sb.append("<div style=\"font-family:'Segoe UI';padding:10px;color:white;background:rgba(10,10,15,0.92);border-radius:6px;border:1px solid rgb(").append(r).append(",").append(g).append(",").append(b).append(")\">");
        sb.append("<div style=\"font-size:15px;font-weight:bold;margin-bottom:4px\">🏰 ").append(town.getName()).append("</div>");
        sb.append("<div style=\"color:rgb(").append(r).append(",").append(g).append(",").append(b).append(");margin-bottom:4px\">").append(nName).append("</div>");
        sb.append("<div style=\"font-size:12px;color:#999\">👥 ").append(town.getMembers().size()).append(" жителей | 📍 ").append(town.getClaimedChunks().size()).append(" чанков</div>");
        if (town.isAtWar()) sb.append("<div style=\"color:#FF4444;margin-top:4px\">⚔ В СОСТОЯНИИ ВОЙНЫ</div>");
        if (town.isCaptured()) sb.append("<div style=\"color:#FFA500;margin-top:4px\">🏴 Захвачен: ").append(town.getCapturedBy()).append("</div>");
        sb.append("</div>");
        return sb.toString();
    }
}
