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

    private static final String ICON_CROWN = "data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAACAAAAAgCAYAAABzenr0AAAABmJLR0QA/wD/AP+gvaeTAAAACXBIWXMAAAsTAAALEwEAmpwYAAAAB3RJTUUH6gIQFR0z0/48VwAAAqVJREFUWMPtl89rE0EUx7+zs7vbFk0tVv+A4kERrJdePCh48CDiQfBisp68e/GsB717F/wD/AfEg3gQBC2ItyJ6Ew/WgxQramO32d3ZmRkP2zbZpGk3iXjwcWbe2/fNfN+ZefMGRF+W9X8D0C6V90d9LyPzQ4g0L0GZpwD4E+D6wN/16fN7I0Qh0FqZAtA+d+6o+30nhDYEASLhQGtlhghk/2ATADxpJigzTSa8IhqNQiMRCGBCeaSMjUZgCBCLte8SpV7o2M/JpA0AsrD+EwB0GGEALqb3A8gRsblo9yfZF0qy10w0fK21+9SLdWtO9cp4/b5QHTryHCdiawFk36oDAGSNMAD68JcA5OfDGV6p3b54r7FohfIZ8ypdz7a0nnpl8pr/1cajg9+b+A+vrfgA385dPfBYtPGlW15x6KwzOuVoBULs3BtJfbfZCO3sdwuvR4Ux/NfqCHdUedO+mhUvnymNVX+Q7Lnx+8ooM8e/wotnX33qiYbmLw8W5gdmVGnmsegXAPkAwCiK7bNng4o8n7LsL4o3f2h5yqdn8g2ZENbRdhE/HsI40CkTDLtFrrpzA5dWVIb6Mv7QgDPXP+IV6ZRJbIwD7SJ+vFZYRx6d/pv0lUL86GWTsvrCZlHzfHNNdM+OxtSiA/eem/Old8i53qt/rrvztNVVs2bVBTtlGGLsbTsRDQ/8K6xU7pBXdkf2hPMkpjFkIbTrkSOZrJCGfm3g9q5Iwj7dFxoibxn5M1K5GR7nlwfjdUHbbtlWba4orZdr/zIx+qeyaf5uwCwuAiw6EJcm2S49PegtXNyxNdG979HWkzccJ+Qnc3g1pcydTdX6bZ36mZ/eUPb50ZnY9l3tL/RubYj09jRGd/Z2vPCv29Oxn13X9vYO9UzPpkq9X1PK6GSB645jP74rc3L3zuSGfnf2QrS5ON0QCj2YgSklsKlQrajok/8B+le5c6l0OCgAAAAASUVORK5CYII=";
    private static final String ICON_TOWN = "data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAABAAAAAQCAMAAAAoLQ9TAAAAA3NCSVQICAjb4U/gAAAACXBIWXMAAAsTAAALEwEAmpwYAAAAB3RJTUUH6gIQFR4s+v8+3QAAABl0RVh0Q29tbWVudABDcmVhdGVkIHdpdGggR0lNUFeBDhcAAABLUExURQAAAM/P0dDQ0svLzczMzvfz9vv7+/z7++Tm5tzd2/r5+fj49/n19/79/fX19P/+/v38/P39/f///+rq6cfHxt7e3qenpt/f3qiop9XU1KalpcC+vqKhocG/v8/PzquqqqGfn6OiocG7v8bFxMLAwNTU07q6uaWjpKqpqaaipa6qrLOusamlp6yoqv///9/h4dja2f////j49u3t7NLU1MbHxevr6vr6+erq6e3t7N3d3MbGxt7e3uDf37e3tqempt/f3t/f37a2tainp9TU09ra2bKxsaWlpL68vMvKydTT09XV1Lu7u7Kysainp5+enry6usHAv7Cwr5yam+np6NjY2NLS0ubm5eXl5d3d3MnJyN/f39vb27+/v7q6ud7e3tva2r+/vv///+VauVgAAABUdFJOUwAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAKU1MITbP29rNNOe3tOUD29kBA9vZAOe3tOAhNs/f2s0wIClxcCsucjc0AAAABYktHRBJ7vGwAAAAAC4lEQVQY02NgIBIwMjGzIHFZ2dg5OLm4eWB8dl4+PX0DPT5+AaiAoKGRsYmpmbmhEFRA2MIyJDQs3MpaBCogamMbERIZZWcvBhUQd3CMjomNc3KWgApIurjGJyTGublLQQWkPTy9vH18/fxloAKy0nIBgUHB8gqKMIcopaioqqlraCK5VUtbR5dYbwIAoBMTO+1B/sUAAAAASUVORK5CYII=";

    private static Class<?> clsBlueMapAPI, clsBlueMapMap, clsMarkerSet;
    private static Class<?> clsShapeMarker, clsShape, clsVector2d, clsVector3d, clsColor;
    private static Class<?> clsHtmlMarker;
    private static Method mGetInstance, mGetMaps, mGetId, mGetMarkerSets;
    private static Method mMarkerSetBuilder, mMarkerSetLabel, mMarkerSetBuild, mMarkerSetGetMarkers;
    private static Method mShapeMarkerBuilder, mShapeMarkerLabel, mShapeMarkerShape;
    private static Method mShapeMarkerDepthTest, mShapeMarkerFillColor, mShapeMarkerLineColor;
    private static Method mShapeMarkerLineWidth, mShapeMarkerDetail, mShapeMarkerBuild;
    private static Method mHtmlMarkerBuilder, mHtmlMarkerLabel, mHtmlMarkerPosition, mHtmlMarkerHtml, mHtmlMarkerBuild;
    private static boolean htmlUseVec3 = false;
    private static Constructor<?> cVector2d, cVector3d, cShape, cColor;
    private static Method mOnEnable;

    public static void init() {
        if (!ModList.get().isLoaded("bluemap")) {
            NationsMod.LOGGER.warn("BlueMap not found, integration disabled.");
            return;
        }
        try {
            loadClasses();
            registerOnEnable();
            NationsMod.LOGGER.info("BlueMap integration registered.");
        } catch (Exception e) {
            NationsMod.LOGGER.error("Failed to init BlueMap: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static void loadClasses() throws Exception {
        ClassLoader cl = BlueMapIntegration.class.getClassLoader();

        clsBlueMapAPI = Class.forName("de.bluecolored.bluemap.api.BlueMapAPI", true, cl);
        clsBlueMapMap = Class.forName("de.bluecolored.bluemap.api.BlueMapMap", true, cl);
        clsMarkerSet = Class.forName("de.bluecolored.bluemap.api.markers.MarkerSet", true, cl);
        clsShapeMarker = Class.forName("de.bluecolored.bluemap.api.markers.ShapeMarker", true, cl);
        clsHtmlMarker = Class.forName("de.bluecolored.bluemap.api.markers.HtmlMarker", true, cl);
        clsShape = Class.forName("de.bluecolored.bluemap.api.math.Shape", true, cl);
        clsColor = Class.forName("de.bluecolored.bluemap.api.math.Color", true, cl);

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
        Class<?> msb = mMarkerSetBuilder.getReturnType();
        mMarkerSetLabel = msb.getMethod("label", String.class);
        mMarkerSetBuild = msb.getMethod("build");
        mMarkerSetGetMarkers = clsMarkerSet.getMethod("getMarkers");

        mShapeMarkerBuilder = clsShapeMarker.getMethod("builder");
        Class<?> smb = mShapeMarkerBuilder.getReturnType();
        mShapeMarkerLabel = smb.getMethod("label", String.class);
        mShapeMarkerShape = smb.getMethod("shape", clsShape, float.class);
        mShapeMarkerDepthTest = smb.getMethod("depthTestEnabled", boolean.class);
        mShapeMarkerFillColor = smb.getMethod("fillColor", clsColor);
        mShapeMarkerLineColor = smb.getMethod("lineColor", clsColor);
        mShapeMarkerLineWidth = smb.getMethod("lineWidth", int.class);
        mShapeMarkerDetail = smb.getMethod("detail", String.class);
        mShapeMarkerBuild = smb.getMethod("build");

        mHtmlMarkerBuilder = clsHtmlMarker.getMethod("builder");
        Class<?> hmb = mHtmlMarkerBuilder.getReturnType();
        mHtmlMarkerLabel = hmb.getMethod("label", String.class);
        mHtmlMarkerHtml = hmb.getMethod("html", String.class);
        mHtmlMarkerBuild = hmb.getMethod("build");
        try {
            mHtmlMarkerPosition = hmb.getMethod("position", double.class, double.class, double.class);
            htmlUseVec3 = false;
        } catch (NoSuchMethodException e) {
            mHtmlMarkerPosition = hmb.getMethod("position", clsVector3d);
            htmlUseVec3 = true;
        }

        cVector2d = clsVector2d.getConstructor(double.class, double.class);
        cVector3d = clsVector3d.getConstructor(double.class, double.class, double.class);
        cShape = clsShape.getConstructor(clsVector2d.arrayType());
        cColor = clsColor.getConstructor(int.class, int.class, int.class, float.class);
        mOnEnable = clsBlueMapAPI.getMethod("onEnable", Consumer.class);
    }

    private static void registerOnEnable() throws Exception {
        Consumer<Object> callback = api -> {
            blueMapAPI = api;
            enabled = true;
            NationsMod.LOGGER.info("BlueMap API available! Rendering markers...");
            try { updateAllMarkers(); } catch (Exception e) {
                NationsMod.LOGGER.error("Initial render error: " + e.getMessage());
            }
        };
        mOnEnable.invoke(null, callback);
        try {
            Optional<?> opt = (Optional<?>) mGetInstance.invoke(null);
            if (opt.isPresent()) { blueMapAPI = opt.get(); enabled = true; }
        } catch (Exception ignored) {}
    }

    public static boolean isEnabled() { return enabled && blueMapAPI != null; }

    @SuppressWarnings("unchecked")
    public static void updateAllMarkers() {
        if (!enabled || blueMapAPI == null) {
            try {
                Optional<?> opt = (Optional<?>) mGetInstance.invoke(null);
                if (opt.isPresent()) { blueMapAPI = opt.get(); enabled = true; }
                else return;
            } catch (Exception e) { return; }
        }

        try {
            Collection<?> maps = (Collection<?>) mGetMaps.invoke(blueMapAPI);
            if (maps.isEmpty()) return;

            for (Object map : maps) {
                String mapId = (String) mGetId.invoke(map);
                String lower = mapId.toLowerCase();
                if (!lower.contains("overworld") && !lower.equals("world") && !lower.contains("мир")) continue;

                Map<String, Object> markerSets = (Map<String, Object>) mGetMarkerSets.invoke(map);
                Object markerSet = markerSets.get(MARKER_SET_ID);
                if (markerSet == null) {
                    Object b = mMarkerSetBuilder.invoke(null);
                    mMarkerSetLabel.invoke(b, "Нации и Города");
                    markerSet = mMarkerSetBuild.invoke(b);
                    markerSets.put(MARKER_SET_ID, markerSet);
                }

                Map<String, Object> markers = (Map<String, Object>) mMarkerSetGetMarkers.invoke(markerSet);
                markers.clear();

                // 1. Заливка + внешняя граница наций (толщина 4)
                for (Nation nation : NationsData.getAllNations()) {
                    try { drawNationFill(nation, markers); } catch (Exception e) {
                        NationsMod.LOGGER.error("Nation error " + nation.getName() + ": " + e.getMessage());
                    }
                }

                // 2. Внутренние границы между городами одной нации (толщина 2)
                for (Nation nation : NationsData.getAllNations()) {
                    try { drawInnerBorders(nation, markers); } catch (Exception e) {
                        NationsMod.LOGGER.error("Inner border error " + nation.getName() + ": " + e.getMessage());
                    }
                }

                // 3. Города без нации
                for (Town town : NationsData.getAllTowns()) {
                    if (town.getNationName() == null) {
                        try { drawStandaloneTown(town, markers); } catch (Exception e) {
                            NationsMod.LOGGER.error("Standalone error " + town.getName() + ": " + e.getMessage());
                        }
                    }
                }

                // 4. Иконки городов (HtmlMarker с base64 img)
                for (Town town : NationsData.getAllTowns()) {
                    try { drawTownIcon(town, markers); } catch (Exception e) {
                        NationsMod.LOGGER.error("Icon error " + town.getName() + ": " + e.getMessage());
                    }
                }

                NationsMod.LOGGER.info("BlueMap markers updated: " + markers.size() + " on " + mapId);
            }
        } catch (Exception e) {
            NationsMod.LOGGER.error("BlueMap update error: " + e.getMessage());
        }
    }

    private static void drawNationFill(Nation nation, Map<String, Object> markers) throws Exception {
        Set<ChunkPos> allChunks = new HashSet<>();
        for (String tn : nation.getTowns()) {
            Town t = NationsData.getTown(tn);
            if (t != null) allChunks.addAll(t.getClaimedChunks());
        }
        if (allChunks.isEmpty()) return;

        Set<String> outerEdges = calcEdges(allChunks);
        List<List<Point>> polygons = tracePolygons(outerEdges);

        int hex = nation.getColor().getHex();
        int r = (hex >> 16) & 0xFF, g = (hex >> 8) & 0xFF, b = hex & 0xFF;
        Object fill = cColor.newInstance(r, g, b, 0.25f);
        Object line = cColor.newInstance(r, g, b, 1.0f);
        String popup = buildNationPopup(nation, r, g, b);

        int i = 0;
        for (List<Point> poly : polygons) {
            if (poly.size() < 3) continue;
            markers.put("nation_" + nation.getName() + "_" + (i++),
                createShapeMarker(nation.getName(), createShape(poly), fill, line, 4, popup));
        }
    }

    private static void drawInnerBorders(Nation nation, Map<String, Object> markers) throws Exception {
        // Маппинг: чанк -> имя города
        Map<ChunkPos, String> chunkToTown = new HashMap<>();
        for (String tn : nation.getTowns()) {
            Town t = NationsData.getTown(tn);
            if (t != null) for (ChunkPos cp : t.getClaimedChunks()) chunkToTown.put(cp, tn);
        }
        if (chunkToTown.size() < 2) return;

        int hex = nation.getColor().getHex();
        int r = (hex >> 16) & 0xFF, g = (hex >> 8) & 0xFF, b = hex & 0xFF;
        int dr = Math.min(255, r + 40), dg = Math.min(255, g + 40), db = Math.min(255, b + 40);
        Object lineCol = cColor.newInstance(dr, dg, db, 0.6f);
        Object noFill = cColor.newInstance(0, 0, 0, 0.0f);

        // Находим рёбра между разными городами одной нации
        Set<String> innerEdges = new HashSet<>();
        for (Map.Entry<ChunkPos, String> entry : chunkToTown.entrySet()) {
            ChunkPos cp = entry.getKey();
            String myTown = entry.getValue();
            double x1 = cp.x * 16.0, z1 = cp.z * 16.0;
            double x2 = x1 + 16.0, z2 = z1 + 16.0;

            addInnerEdge(innerEdges, chunkToTown, myTown, new ChunkPos(cp.x, cp.z - 1), x1, z1, x2, z1);
            addInnerEdge(innerEdges, chunkToTown, myTown, new ChunkPos(cp.x, cp.z + 1), x1, z2, x2, z2);
            addInnerEdge(innerEdges, chunkToTown, myTown, new ChunkPos(cp.x + 1, cp.z), x2, z1, x2, z2);
            addInnerEdge(innerEdges, chunkToTown, myTown, new ChunkPos(cp.x - 1, cp.z), x1, z1, x1, z2);
        }

        int j = 0;
        for (String edge : innerEdges) {
            String[] pts = edge.split(">");
            String[] a = pts[0].split(","), b = pts[1].split(",");
            double ax = Double.parseDouble(a[0]), az = Double.parseDouble(a[1]);
            double bx = Double.parseDouble(b[0]), bz = Double.parseDouble(b[1]);

            List<Point> linePoly = new ArrayList<>();
            double w = 0.3;
            if (Math.abs(ax - bx) < 0.01) {
                linePoly.add(new Point(ax - w, Math.min(az, bz)));
                linePoly.add(new Point(ax + w, Math.min(az, bz)));
                linePoly.add(new Point(ax + w, Math.max(az, bz)));
                linePoly.add(new Point(ax - w, Math.max(az, bz)));
            } else {
                linePoly.add(new Point(Math.min(ax, bx), az - w));
                linePoly.add(new Point(Math.max(ax, bx), az - w));
                linePoly.add(new Point(Math.max(ax, bx), az + w));
                linePoly.add(new Point(Math.min(ax, bx), az + w));
            }
            markers.put("inner_" + nation.getName() + "_" + (j++),
                createShapeMarker("", createShape(linePoly), noFill, lineCol, 2, ""));
        }
    }

    private static void addInnerEdge(Set<String> edges, Map<ChunkPos, String> map, String myTown, ChunkPos neighbor,
                                      double x1, double z1, double x2, double z2) {
        String other = map.get(neighbor);
        if (other != null && !other.equals(myTown)) {
            String p1 = x1 + "," + z1, p2 = x2 + "," + z2;
            if (p1.compareTo(p2) > 0) { String t = p1; p1 = p2; p2 = t; }
            edges.add(p1 + ">" + p2);
        }
    }

    private static void drawStandaloneTown(Town town, Map<String, Object> markers) throws Exception {
        if (town.getClaimedChunks().isEmpty()) return;
        Set<String> edges = calcEdges(town.getClaimedChunks());
        List<List<Point>> polygons = tracePolygons(edges);
        int r = 150, g = 150, b = 150;
        Object fill = cColor.newInstance(r, g, b, 0.30f);
        Object line = cColor.newInstance(r, g, b, 1.0f);
        if (town.isAtWar()) { fill = cColor.newInstance(255, 50, 50, 0.4f); line = cColor.newInstance(255, 50, 50, 1.0f); }
        else if (town.isCaptured()) { fill = cColor.newInstance(255, 140, 0, 0.4f); line = cColor.newInstance(255, 140, 0, 1.0f); }
        String popup = buildPopup(town, "Без нации", r, g, b);
        int i = 0;
        for (List<Point> poly : polygons) {
            if (poly.size() < 3) continue;
            markers.put("standalone_" + town.getName() + "_" + (i++),
                createShapeMarker(town.getName(), createShape(poly), fill, line, 3, popup));
        }
    }

    private static void drawTownIcon(Town town, Map<String, Object> markers) throws Exception {
        if (town.getSpawnPos() == null) return;

        boolean isCapital = false;
        String nationName = "Без нации";
        String colorHex = "#888888";

        if (town.getNationName() != null) {
            Nation nation = NationsData.getNation(town.getNationName());
            if (nation != null) {
                colorHex = String.format("#%06X", nation.getColor().getHex());
                nationName = nation.getName();
                isCapital = nation.isCapital(town.getName());
            }
        }

        double px = town.getSpawnPos().getX() + 0.5;
        double py = town.getSpawnPos().getY() + 2.0;
        double pz = town.getSpawnPos().getZ() + 0.5;

        String icon = isCapital ? ICON_CROWN : ICON_TOWN;
        int size = isCapital ? 32 : 16;
        int half = size / 2;

        // Используем HtmlMarker с <img> тегом
        String html = "<div style=\"transform:translate(-" + half + "px,-" + half + "px);\">" +
            "<img src=\"" + icon + "\" width=\"" + size + "\" height=\"" + size + "\" style=\"display:block;\" />" +
            "</div>";

        String popup = buildPopup(town, nationName,
            Integer.parseInt(colorHex.substring(1, 3), 16),
            Integer.parseInt(colorHex.substring(3, 5), 16),
            Integer.parseInt(colorHex.substring(5, 7), 16));

        Object builder = mHtmlMarkerBuilder.invoke(null);
        mHtmlMarkerLabel.invoke(builder, town.getName());
        mHtmlMarkerHtml.invoke(builder, html);

        if (htmlUseVec3) {
            mHtmlMarkerPosition.invoke(builder, cVector3d.newInstance(px, py, pz));
        } else {
            mHtmlMarkerPosition.invoke(builder, px, py, pz);
        }

        markers.put("icon_" + town.getName(), mHtmlMarkerBuild.invoke(builder));
    }

    // === УТИЛИТЫ ===

    private static Set<String> calcEdges(Set<ChunkPos> chunks) {
        Set<String> edges = new HashSet<>();
        for (ChunkPos cp : chunks) {
            double x1 = cp.x * 16.0, z1 = cp.z * 16.0, x2 = x1 + 16, z2 = z1 + 16;
            toggleEdge(edges, x1, z1, x2, z1);
            toggleEdge(edges, x2, z1, x2, z2);
            toggleEdge(edges, x2, z2, x1, z2);
            toggleEdge(edges, x1, z2, x1, z1);
        }
        return edges;
    }

    private static void toggleEdge(Set<String> e, double x1, double z1, double x2, double z2) {
        String f = x1+","+z1+">"+x2+","+z2, b = x2+","+z2+">"+x1+","+z1;
        if (e.contains(b)) e.remove(b); else e.add(f);
    }

    private static List<List<Point>> tracePolygons(Set<String> edges) {
        List<List<Point>> polys = new ArrayList<>();
        Map<Point, Point> map = new HashMap<>();
        for (String e : edges) {
            String[] p = e.split(">"); String[] a = p[0].split(","), b = p[1].split(",");
            map.put(new Point(Double.parseDouble(a[0]), Double.parseDouble(a[1])),
                    new Point(Double.parseDouble(b[0]), Double.parseDouble(b[1])));
        }
        while (!map.isEmpty()) {
            List<Point> poly = new ArrayList<>();
            Point start = map.keySet().iterator().next(), curr = start;
            int s = 0;
            while (curr != null && s++ < 200000) {
                poly.add(curr);
                Point next = map.remove(curr);
                if (next == null || next.equals(start)) break;
                curr = next;
            }
            if (poly.size() >= 3) polys.add(poly);
        }
        return polys;
    }

    private static Object createShape(List<Point> pts) throws Exception {
        Object arr = java.lang.reflect.Array.newInstance(clsVector2d, pts.size());
        for (int i = 0; i < pts.size(); i++)
            java.lang.reflect.Array.set(arr, i, cVector2d.newInstance(pts.get(i).x, pts.get(i).z));
        return cShape.newInstance(arr);
    }

    private static Object createShapeMarker(String label, Object shape, Object fill, Object line, int w, String detail) throws Exception {
        Object bd = mShapeMarkerBuilder.invoke(null);
        mShapeMarkerLabel.invoke(bd, label);
        mShapeMarkerShape.invoke(bd, shape, 64f);
        mShapeMarkerDepthTest.invoke(bd, false);
        mShapeMarkerFillColor.invoke(bd, fill);
        mShapeMarkerLineColor.invoke(bd, line);
        mShapeMarkerLineWidth.invoke(bd, w);
        mShapeMarkerDetail.invoke(bd, detail);
        return mShapeMarkerBuild.invoke(bd);
    }

    static class Point {
        double x, z;
        Point(double x, double z) { this.x = x; this.z = z; }
        @Override public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof Point)) return false;
            Point p = (Point) o;
            return Double.compare(p.x, x) == 0 && Double.compare(p.z, z) == 0;
        }
        @Override public int hashCode() { return Objects.hash(x, z); }
    }

    private static String buildNationPopup(Nation n, int r, int g, int b) {
        String c = "rgb("+r+","+g+","+b+")";
        return "<div style=\"font-family:'Segoe UI',sans-serif;padding:12px;color:#fff;background:rgba(10,10,15,0.92);border-radius:8px;border:2px solid "+c+"\">" +
            "<div style=\"font-size:18px;font-weight:bold;color:"+c+"\">🏛 "+n.getName()+"</div>" +
            "<hr style=\"border:0;border-top:1px solid #444;margin:6px 0\">" +
            "<div>🏰 Городов: <b style=\"color:#FFD700\">"+n.getTowns().size()+"</b></div>" +
            "<div>👥 Жителей: <b>"+n.getTotalMembers()+"</b></div>" +
            "<div>📍 Территория: <b>"+n.getTotalChunks()+"</b> чанков</div>" +
            (n.getCapitalTown()!=null?"<div>👑 Столица: <b style=\"color:#FFD700\">"+n.getCapitalTown()+"</b></div>":"") +
            (!n.getWarTargets().isEmpty()?"<div style=\"color:#F44\">⚔ Война: "+String.join(", ",n.getWarTargets())+"</div>":"") +
            "</div>";
    }

    private static String buildPopup(Town t, String nName, int r, int g, int b) {
        String c = "rgb("+r+","+g+","+b+")";
        return "<div style=\"font-family:'Segoe UI',sans-serif;padding:10px;color:#fff;background:rgba(10,10,15,0.92);border-radius:6px;border:1px solid "+c+"\">" +
            "<div style=\"font-size:15px;font-weight:bold\">🏰 "+t.getName()+"</div>" +
            "<div style=\"color:"+c+"\">"+nName+"</div>" +
            "<div style=\"font-size:12px;color:#999\">👥 "+t.getMembers().size()+" | 📍 "+t.getClaimedChunks().size()+" чанков</div>" +
            (t.isAtWar()?"<div style=\"color:#F44\">⚔ ВОЙНА</div>":"") +
            (t.isCaptured()?"<div style=\"color:#FA0\">🏴 Захвачен: "+t.getCapturedBy()+"</div>":"") +
            "</div>";
    }
}
