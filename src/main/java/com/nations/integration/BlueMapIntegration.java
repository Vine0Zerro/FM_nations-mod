package com.nations.integration;

import com.nations.NationsMod;
import com.nations.data.*;
import net.minecraft.world.level.ChunkPos;
import net.minecraftforge.fml.ModList;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.*;

public class BlueMapIntegration {

    private static boolean enabled = false;
    private static Object blueMapAPI = null;
    private static final String MARKER_SET_ID = "nations_towns";
    private static final String ICON_CROWN_BASE64 = "data:image/png;base64,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";
    private static final String ICON_TOWN_BASE64 = "data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAABAAAAAQCAMAAAAoLQ9TAAAAIGNIUk0AAHomAACAhAAA+gAAAIDoAAB1MAAA6mAAADqYAAAXcJy6UTwAAAEpUExURQAAAM/P0dDQ0svLzczMzvfz9vv7+/z7++Tm5tzd2/r5+fj49/n19/79/fX19P/+/v38/P39/f///+rq6cfHxt7e3qenpt/f3qiop9XU1KalpcC+vqKhocG/v8/PzquqqqGfn6OiocG7v8bFxMLAwNTU07q6uaWjpKqpqaaipa6qrLOusamlp6yoqv///9/h4dja2f////j49u3t7NLU1MbHxevr6vr6+erq6e3t7N3d3MbGxt7e3uDf37e3tqempt/f3t/f37a2tainp9TU09ra2bKxsaWlpL68vMvKydTT09XV1Lu7u7Kysainp5+enry6usHAv7Cwr5yam+np6NjY2NLS0ubm5eXl5d3d3MnJyN/f39vb27+/v7q6ud7e3tva2r+/vv///+VauVgAAABUdFJOUwAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAKU1MITbP29rNNOe3tOUD29kBA9vZAOe3tOAhNs/f2s0wIClxcCsucjc0AAAABYktHRBJ7vGwAAAAAB3RJTUUH6gIQFR8CaGuYkgAAAIxJREFUGNNjYCASMDIxsyBxWdnYOTi5uHlgfHZePj19Az0+fgGogKChkbGJqZm5oRBUQNjCMiQ0LNzKWgQqIGpjGxESGWVnLwYVEHdwjI6JjXNyloAKSLq4xickxrm5S0EFpD08vbx9fP38ZaACstJyAYFBwfIKijCHKCmrqKqpa2giuVVLW0eXWG8CAKATE9MHZILXAAAAJXRFWHRkYXRlOmNyZWF0ZQAyMDI2LTAyLTE2VDIxOjMwOjU0KzAwOjAwx1vJegAAACV0RVh0ZGF0ZTptb2RpZnkAMjAyNi0wMi0xNlQyMTozMDo1NCswMDowMLYGccYAAAAodEVYdGRhdGU6dGltZXN0YW1wADIwMjYtMDItMTZUMjE6MzE6MDIrMDA6MDAl4QB5AAAAAElFTkSuQmCC";

    private static Class<?> clsBlueMapAPI, clsBlueMapMap, clsMarkerSet, clsShapeMarker, clsPOIMarker, clsShape, clsVector2d, clsColor;
    private static Method mGetInstance, mGetMaps, mGetId, mGetMarkerSets, mMarkerSetBuilder, mMarkerSetLabel, mMarkerSetBuild, mMarkerSetGetMarkers;
    private static Method mShapeMarkerBuilder, mShapeMarkerLabel, mShapeMarkerShape, mShapeMarkerDepthTest, mShapeMarkerFillColor, mShapeMarkerLineColor, mShapeMarkerLineWidth, mShapeMarkerDetail, mShapeMarkerBuild;
    private static Method mPOIMarkerBuilder, mPOIMarkerLabel, mPOIMarkerPosition, mPOIMarkerDetail, mPOIMarkerIcon, mPOIMarkerBuild;
    private static Constructor<?> cVector2d, cShape, cColor;

    public static void init() {
        if (!ModList.get().isLoaded("bluemap")) return;
        try { loadClasses(); checkApi(); enabled = true; } catch (Exception e) {}
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
        mMarkerSetLabel = mMarkerSetBuilder.getReturnType().getMethod("label", String.class);
        mMarkerSetBuild = mMarkerSetBuilder.getReturnType().getMethod("build");
        mMarkerSetGetMarkers = clsMarkerSet.getMethod("getMarkers");
        mShapeMarkerBuilder = clsShapeMarker.getMethod("builder");
        mShapeMarkerLabel = mShapeMarkerBuilder.getReturnType().getMethod("label", String.class);
        mShapeMarkerShape = mShapeMarkerBuilder.getReturnType().getMethod("shape", clsShape, float.class);
        mShapeMarkerDepthTest = mShapeMarkerBuilder.getReturnType().getMethod("depthTestEnabled", boolean.class);
        mShapeMarkerFillColor = mShapeMarkerBuilder.getReturnType().getMethod("fillColor", clsColor);
        mShapeMarkerLineColor = mShapeMarkerBuilder.getReturnType().getMethod("lineColor", clsColor);
        mShapeMarkerLineWidth = mShapeMarkerBuilder.getReturnType().getMethod("lineWidth", int.class);
        mShapeMarkerDetail = mShapeMarkerBuilder.getReturnType().getMethod("detail", String.class);
        mShapeMarkerBuild = mShapeMarkerBuilder.getReturnType().getMethod("build");
        mPOIMarkerBuilder = clsPOIMarker.getMethod("builder");
        mPOIMarkerLabel = mPOIMarkerBuilder.getReturnType().getMethod("label", String.class);
        mPOIMarkerPosition = mPOIMarkerBuilder.getReturnType().getMethod("position", double.class, double.class, double.class);
        mPOIMarkerDetail = mPOIMarkerBuilder.getReturnType().getMethod("detail", String.class);
        mPOIMarkerIcon = mPOIMarkerBuilder.getReturnType().getMethod("icon", String.class, int.class, int.class);
        mPOIMarkerBuild = mPOIMarkerBuilder.getReturnType().getMethod("build");
        cVector2d = clsVector2d.getConstructor(double.class, double.class);
        cShape = clsShape.getConstructor(clsVector2d.arrayType());
        cColor = clsColor.getConstructor(int.class, int.class, int.class, float.class);
    }

    private static void checkApi() {
        try { Optional<?> opt = (Optional<?>) mGetInstance.invoke(null); if (opt.isPresent()) blueMapAPI = opt.get(); } catch (Exception ignored) {}
    }

    public static void updateAllMarkers() {
        if (!enabled || blueMapAPI == null) return;
        try {
            Collection<?> maps = (Collection<?>) mGetMaps.invoke(blueMapAPI);
            for (Object map : maps) {
                String mapId = (String) mGetId.invoke(map);
                if (!mapId.toLowerCase().contains("overworld") && !mapId.equals("world")) continue;
                Map<String, Object> markerSets = (Map<String, Object>) mGetMarkerSets.invoke(map);
                Object markerSet = markerSets.get(MARKER_SET_ID);
                if (markerSet == null) {
                    Object b = mMarkerSetBuilder.invoke(null);
                    mMarkerSetLabel.invoke(b, "Города и Нации");
                    markerSet = mMarkerSetBuild.invoke(b);
                    markerSets.put(MARKER_SET_ID, markerSet);
                }
                Map<String, Object> markers = (Map<String, Object>) mMarkerSetGetMarkers.invoke(markerSet);
                markers.clear();
                for (Nation nation : NationsData.getAllNations()) {
                    drawNationBorder(nation, markers);
                    drawInnerTownBorders(nation, markers);
                }
                for (Town town : NationsData.getAllTowns()) {
                    if (town.getNationName() == null) drawStandaloneTown(town, markers);
                    drawTownPOI(town, markers);
                }
            }
        } catch (Exception e) { e.printStackTrace(); }
    }

    private static void drawNationBorder(Nation nation, Map<String, Object> markers) throws Exception {
        Set<ChunkPos> allChunks = new HashSet<>();
        for (String tn : nation.getTowns()) {
            Town t = NationsData.getTown(tn);
            if (t != null) allChunks.addAll(t.getClaimedChunks());
        }
        if (allChunks.isEmpty()) return;
        Set<String> edges = new HashSet<>();
        for (ChunkPos cp : allChunks) {
            double x1 = cp.x * 16.0, z1 = cp.z * 16.0, x2 = x1 + 16, z2 = z1 + 16;
            toggleEdge(edges, x1, z1, x2, z1); toggleEdge(edges, x2, z1, x2, z2);
            toggleEdge(edges, x2, z2, x1, z2); toggleEdge(edges, x1, z2, x1, z1);
        }
        List<List<Point>> polygons = tracePolygons(edges);
        int hex = nation.getColor().getHex();
        int r = (hex >> 16) & 0xFF, g = (hex >> 8) & 0xFF, b = hex & 0xFF;
        Object fillColor = cColor.newInstance(r, g, b, 0.3f);
        Object lineColor = cColor.newInstance(r, g, b, 1.0f);
        String popup = buildNationPopup(nation, r, g, b);
        int pi = 0;
        for (List<Point> pp : polygons) {
            Object va = java.lang.reflect.Array.newInstance(clsVector2d, pp.size());
            for (int i = 0; i < pp.size(); i++) java.lang.reflect.Array.set(va, i, cVector2d.newInstance(pp.get(i).x, pp.get(i).z));
            Object shape = cShape.newInstance(va), bd = mShapeMarkerBuilder.invoke(null);
            mShapeMarkerLabel.invoke(bd, nation.getName()); mShapeMarkerShape.invoke(bd, shape, 64f);
            mShapeMarkerDepthTest.invoke(bd, false); mShapeMarkerFillColor.invoke(bd, fillColor);
            mShapeMarkerLineColor.invoke(bd, lineColor); mShapeMarkerLineWidth.invoke(bd, 3);
            mShapeMarkerDetail.invoke(bd, popup);
            markers.put("n_" + nation.getName() + pi++, mShapeMarkerBuild.invoke(bd));
        }
    }

    private static void drawInnerTownBorders(Nation nation, Map<String, Object> markers) throws Exception {
        Map<ChunkPos, String> c2t = new HashMap<>();
        for (String tn : nation.getTowns()) {
            Town t = NationsData.getTown(tn);
            if (t != null) for (ChunkPos cp : t.getClaimedChunks()) c2t.put(cp, tn);
        }
        Set<String> innerEdges = new HashSet<>();
        for (Map.Entry<ChunkPos, String> e : c2t.entrySet()) {
            ChunkPos cp = e.getKey(); String my = e.getValue();
            double x1 = cp.x * 16.0, z1 = cp.z * 16.0, x2 = x1 + 16, z2 = z1 + 16;
            checkInner(innerEdges, c2t, my, new ChunkPos(cp.x, cp.z - 1), x1, z1, x2, z1);
            checkInner(innerEdges, c2t, my, new ChunkPos(cp.x, cp.z + 1), x1, z2, x2, z2);
            checkInner(innerEdges, c2t, my, new ChunkPos(cp.x + 1, cp.z), x2, z1, x2, z2);
            checkInner(innerEdges, c2t, my, new ChunkPos(cp.x - 1, cp.z), x1, z1, x1, z2);
        }
        int hex = nation.getColor().getHex(), r = (hex >> 16) & 0xFF, g = (hex >> 8) & 0xFF, b = hex & 0xFF;
        Object lc = cColor.newInstance(r, g, b, 0.8f);
        int ei = 0;
        for (String edge : innerEdges) {
            String[] pts = edge.split(">");
            String[] p1 = pts[0].split(","), p2 = pts[1].split(",");
            double ax = Double.parseDouble(p1[0]), az = Double.parseDouble(p1[1]);
            double bx = Double.parseDouble(p2[0]), bz = Double.parseDouble(p2[1]);
            double dx = 0, dz = 0;
            if (Math.abs(ax - bx) < 0.1) dx = 0.1; else dz = 0.1;
            Object va = java.lang.reflect.Array.newInstance(clsVector2d, 4);
            java.lang.reflect.Array.set(va, 0, cVector2d.newInstance(ax - dx, az - dz));
            java.lang.reflect.Array.set(va, 1, cVector2d.newInstance(bx + dx, az - dz));
            java.lang.reflect.Array.set(va, 2, cVector2d.newInstance(bx + dx, bz + dz));
            java.lang.reflect.Array.set(va, 3, cVector2d.newInstance(ax - dx, bz + dz));
            Object shape = cShape.newInstance(va), bd = mShapeMarkerBuilder.invoke(null);
            mShapeMarkerLabel.invoke(bd, ""); mShapeMarkerShape.invoke(bd, shape, 64f);
            mShapeMarkerDepthTest.invoke(bd, false); mShapeMarkerFillColor.invoke(bd, lc);
            mShapeMarkerLineColor.invoke(bd, lc); mShapeMarkerLineWidth.invoke(bd, 1);
            markers.put("in_" + nation.getName() + ei++, mShapeMarkerBuild.invoke(bd));
        }
    }

    private static void checkInner(Set<String> ie, Map<ChunkPos, String> c2t, String my, ChunkPos nb, double x1, double z1, double x2, double z2) {
        String other = c2t.get(nb);
        if (other != null && !other.equals(my)) {
            String k = (x1 < x2 || (x1 == x2 && z1 < z2)) ? x1 + "," + z1 + ">" + x2 + "," + z2 : x2 + "," + z2 + ">" + x1 + "," + z1;
            ie.add(k);
        }
    }

    private static void drawStandaloneTown(Town town, Map<String, Object> markers) throws Exception {
        Set<ChunkPos> chunks = town.getClaimedChunks(); if (chunks.isEmpty()) return;
        Set<String> edges = new HashSet<>();
        for (ChunkPos cp : chunks) {
            double x1 = cp.x * 16.0, z1 = cp.z * 16.0, x2 = x1 + 16, z2 = z1 + 16;
            toggleEdge(edges, x1, z1, x2, z1); toggleEdge(edges, x2, z1, x2, z2);
            toggleEdge(edges, x2, z2, x1, z2); toggleEdge(edges, x1, z2, x1, z1);
        }
        List<List<Point>> polygons = tracePolygons(edges);
        Object fc = cColor.newInstance(136, 136, 136, 0.4f), lc = cColor.newInstance(136, 136, 136, 0.9f);
        String popup = buildPopup(town, "Нет", 136, 136, 136);
        int pi = 0;
        for (List<Point> pp : polygons) {
            Object va = java.lang.reflect.Array.newInstance(clsVector2d, pp.size());
            for (int i = 0; i < pp.size(); i++) java.lang.reflect.Array.set(va, i, cVector2d.newInstance(pp.get(i).x, pp.get(i).z));
            Object shape = cShape.newInstance(va), bd = mShapeMarkerBuilder.invoke(null);
            mShapeMarkerLabel.invoke(bd, town.getName()); mShapeMarkerShape.invoke(bd, shape, 64f);
            mShapeMarkerDepthTest.invoke(bd, false); mShapeMarkerFillColor.invoke(bd, fc);
            mShapeMarkerLineColor.invoke(bd, lc); mShapeMarkerLineWidth.invoke(bd, 3);
            mShapeMarkerDetail.invoke(bd, popup);
            markers.put("t_" + town.getName() + pi++, mShapeMarkerBuild.invoke(bd));
        }
    }

    private static void drawTownPOI(Town town, Map<String, Object> markers) throws Exception {
        if (town.getSpawnPos() == null) return;
        boolean isCapital = false;
        String nationName = "Нет";
        int r = 136, g = 136, b = 136;
        if (town.getNationName() != null) {
            Nation n = NationsData.getNation(town.getNationName());
            if (n != null) {
                int hex = n.getColor().getHex(); r = (hex >> 16) & 0xFF; g = (hex >> 8) & 0xFF; b = hex & 0xFF;
                nationName = n.getName();
                isCapital = n.isCapital(town.getName());
            }
        }
        String popup = buildPopup(town, nationName, r, g, b);
        Object bd = mPOIMarkerBuilder.invoke(null);
        mPOIMarkerLabel.invoke(bd, town.getName());
        mPOIMarkerPosition.invoke(bd, (double)town.getSpawnPos().getX(), (double)town.getSpawnPos().getY() + 2, (double)town.getSpawnPos().getZ());
        mPOIMarkerDetail.invoke(bd, popup);
        mPOIMarkerIcon.invoke(bd, isCapital ? ICON_CROWN_BASE64 : ICON_TOWN_BASE64, 16, 16);
        markers.put("p_" + town.getName(), mPOIMarkerBuild.invoke(bd));
    }

    private static void toggleEdge(Set<String> edges, double x1, double z1, double x2, double z2) {
        String f = x1 + "," + z1 + ">" + x2 + "," + z2, b = x2 + "," + z2 + ">" + x1 + "," + z1;
        if (edges.contains(b)) edges.remove(b); else edges.add(f);
    }

    private static List<List<Point>> tracePolygons(Set<String> edges) {
        List<List<Point>> polygons = new ArrayList<>();
        Map<Point, Point> map = new HashMap<>();
        for (String e : edges) {
            String[] p = e.split(">"); String[] a = p[0].split(","), b = p[1].split(",");
            map.put(new Point(Double.parseDouble(a[0]), Double.parseDouble(a[1])), new Point(Double.parseDouble(b[0]), Double.parseDouble(b[1])));
        }
        while (!map.isEmpty()) {
            List<Point> poly = new ArrayList<>();
            Point start = map.keySet().iterator().next(), curr = start;
            do {
                poly.add(curr);
                curr = map.remove(curr);
            } while (curr != null && !curr.equals(start));
            if (poly.size() > 2) polygons.add(poly);
        }
        return polygons;
    }

    private static class Point {
        double x, z;
        Point(double x, double z) { this.x = x; this.z = z; }
        public boolean equals(Object o) { if (o instanceof Point p) return p.x == x && p.z == z; return false; }
        public int hashCode() { return Objects.hash(x, z); }
    }

    private static String buildNationPopup(Nation n, int r, int g, int b) {
        StringBuilder sb = new StringBuilder("<div style='font-family:Arial;padding:10px;color:white;background:rgba(0,0,0,0.8)'>");
        sb.append("<div style='color:rgb(").append(r).append(",").append(g).append(",").append(b).append(");font-weight:bold'>").append(n.getName()).append("</div>");
        sb.append("<div>Городов: ").append(n.getTowns().size()).append("</div>");
        sb.append("<div>Жителей: ").append(n.getTotalMembers()).append("</div>");
        sb.append("</div>");
        return sb.toString();
    }

    private static String buildPopup(Town t, String nName, int r, int g, int b) {
        StringBuilder sb = new StringBuilder("<div style='font-family:Arial;padding:10px;color:white;background:rgba(0,0,0,0.8)'>");
        sb.append("<div style='color:rgb(").append(r).append(",").append(g).append(",").append(b).append(");font-weight:bold'>").append(t.getName()).append("</div>");
        sb.append("<div>Нация: ").append(nName).append("</div>");
        sb.append("<div>Мэр: ").append(t.getMayor()).append("</div>"); // Упрощено для краткости
        sb.append("</div>");
        return sb.toString();
    }
}
