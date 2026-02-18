package com.nations.integration;

import com.nations.NationsMod;
import com.nations.data.*;
import net.minecraft.world.level.ChunkPos;
import net.minecraftforge.fml.ModList;

import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.*;
import java.util.function.Consumer;

public class BlueMapIntegration {

    private static boolean enabled = false;
    private static Object blueMapAPI = null;
    private static final String MARKER_SET_ID = "nations_towns";

    private static String CAPITAL_ICON_BASE64 = "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";

    private static Class<?> clsBlueMapAPI, clsBlueMapMap, clsMarkerSet, clsShapeMarker, clsHtmlMarker;
    private static Class<?> clsShape, clsVector2d, clsVector3d, clsColor;
    private static Method mGetInstance, mGetMaps, mGetId, mGetMarkerSets, mMarkerSetBuilder, mMarkerSetLabel, mMarkerSetBuild, mMarkerSetGetMarkers;
    private static Method mShapeMarkerBuilder, mShapeMarkerLabel, mShapeMarkerShape, mShapeMarkerDepthTest, mShapeMarkerFillColor, mShapeMarkerLineColor, mShapeMarkerLineWidth, mShapeMarkerDetail, mShapeMarkerBuild;
    private static Method mHtmlMarkerBuilder, mHtmlMarkerLabel, mHtmlMarkerHtml, mHtmlMarkerBuild, mHtmlMarkerPosition, mOnEnable;
    private static boolean htmlUseVec3 = false;
    private static Constructor<?> cVector2d, cVector3d, cShape, cColor;

    public static void init() {
        if (!ModList.get().isLoaded("bluemap")) return;
        try {
            loadClasses();
            registerOnEnable();
        } catch (Exception e) { e.printStackTrace(); }
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
            try { updateAllMarkers(); } catch (Exception e) {}
        };
        mOnEnable.invoke(null, callback);
        try {
            Optional<?> opt = (Optional<?>) mGetInstance.invoke(null);
            if (opt.isPresent()) { blueMapAPI = opt.get(); enabled = true; }
        } catch (Exception ignored) {}
    }

    @SuppressWarnings("unchecked")
    public static void updateAllMarkers() {
        if (!enabled || blueMapAPI == null) return;
        try {
            Collection<?> maps = (Collection<?>) mGetMaps.invoke(blueMapAPI);
            if (maps.isEmpty()) return;
            for (Object map : maps) {
                String mapId = (String) mGetId.invoke(map);
                if (!mapId.toLowerCase().contains("overworld") && !mapId.equals("world")) continue;

                Map<String, Object> markerSets = (Map<String, Object>) mGetMarkerSets.invoke(map);
                Object territorySet = getOrCreateMarkerSet(markerSets, MARKER_SET_ID, "Нации и Города");
                Object iconSet = getOrCreateMarkerSet(markerSets, MARKER_SET_ID + "_icons", "Иконки городов");

                Map<String, Object> tMarkers = (Map<String, Object>) mMarkerSetGetMarkers.invoke(territorySet);
                Map<String, Object> iMarkers = (Map<String, Object>) mMarkerSetGetMarkers.invoke(iconSet);
                tMarkers.clear();
                iMarkers.clear();

                for (Nation nation : NationsData.getAllNations()) {
                    try { drawNationTowns(nation, tMarkers); } catch (Exception e) { e.printStackTrace(); }
                }

                for (Town town : NationsData.getAllTowns()) {
                    if (town.getNationName() == null)
                        try { drawStandaloneTown(town, tMarkers); } catch (Exception e) { e.printStackTrace(); }
                    try { drawTownIcon(town, iMarkers); } catch (Exception e) { e.printStackTrace(); }
                }
            }
        } catch (Exception e) { e.printStackTrace(); }
    }

    private static Object getOrCreateMarkerSet(Map<String, Object> sets, String id, String label) throws Exception {
        Object set = sets.get(id);
        if (set == null) {
            Object builder = mMarkerSetBuilder.invoke(null);
            mMarkerSetLabel.invoke(builder, label);
            set = mMarkerSetBuild.invoke(builder);
            sets.put(id, set);
        }
        return set;
    }

    private static void drawNationTowns(Nation nation, Map<String, Object> markers) throws Exception {
        int hex = nation.getColor().getHex();
        int cr = (hex >> 16) & 0xFF, cg = (hex >> 8) & 0xFF, cb = hex & 0xFF;

        Object fill = cColor.newInstance(cr, cg, cb, 0.22f);
        Object line = cColor.newInstance(cr, cg, cb, 1.0f);

        for (String townName : nation.getTowns()) {
            Town town = NationsData.getTown(townName);
            if (town == null || town.getClaimedChunks().isEmpty()) continue;

            Set<String> edges = calcEdges(town.getClaimedChunks());
            List<List<Point>> polygons = tracePolygons(edges);
            String popup = buildTownPopup(town, nation);

            int j = 0;
            for (List<Point> poly : polygons) {
                if (poly.size() < 3) continue;
                markers.put("town_" + townName + "_" + (j++),
                        createShapeMarker(townName, createShape(poly), fill, line, 3, popup));
            }
        }
    }

    private static void drawStandaloneTown(Town town, Map<String, Object> markers) throws Exception {
        if (town.getClaimedChunks().isEmpty()) return;
        Set<String> edges = calcEdges(town.getClaimedChunks());
        List<List<Point>> polygons = tracePolygons(edges);

        int cr = 150, cg = 150, cb = 150;
        Object fill = cColor.newInstance(cr, cg, cb, 0.25f);
        Object line = cColor.newInstance(cr, cg, cb, 1.0f);
        String popup = buildTownPopup(town, null);

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
        if (town.getNationName() != null) {
            Nation nation = NationsData.getNation(town.getNationName());
            if (nation != null) isCapital = nation.isCapital(town.getName());
        }

        // Только столицы получают иконку
        if (!isCapital) return;

        double px = town.getSpawnPos().getX() + 0.5;
        double py = town.getSpawnPos().getY() + 2.0;
        double pz = town.getSpawnPos().getZ() + 0.5;

        int iconSize = 26;

        String html = "<div style=\"transform:translate(-50%,-50%);width:" + iconSize
                + "px;height:" + iconSize
                + "px;filter:drop-shadow(0 1px 3px rgba(0,0,0,0.7));cursor:pointer;z-index:1;position:relative;\">";
        html += "<img src=\"data:image/png;base64," + CAPITAL_ICON_BASE64
                + "\" width=\"" + iconSize + "\" height=\"" + iconSize
                + "\" style=\"display:block;\" />";
        html += "</div>";

        Object builder = mHtmlMarkerBuilder.invoke(null);
        mHtmlMarkerLabel.invoke(builder, town.getName());
        mHtmlMarkerHtml.invoke(builder, html);
        if (htmlUseVec3)
            mHtmlMarkerPosition.invoke(builder, cVector3d.newInstance(px, py, pz));
        else
            mHtmlMarkerPosition.invoke(builder, px, py, pz);
        markers.put("icon_" + town.getName(), mHtmlMarkerBuild.invoke(builder));
    }

    private static Object createShapeMarker(String label, Object shape,
                                            Object fill, Object line,
                                            int width, String detail) throws Exception {
        Object bd = mShapeMarkerBuilder.invoke(null);
        mShapeMarkerLabel.invoke(bd, label);
        mShapeMarkerShape.invoke(bd, shape, 64f);
        mShapeMarkerDepthTest.invoke(bd, false);
        mShapeMarkerFillColor.invoke(bd, fill);
        mShapeMarkerLineColor.invoke(bd, line);
        mShapeMarkerLineWidth.invoke(bd, width);
        if (detail != null && !detail.isEmpty())
            mShapeMarkerDetail.invoke(bd, detail);
        return mShapeMarkerBuild.invoke(bd);
    }

    private static String buildTownPopup(Town town, Nation nation) {
        String nationName = (nation != null) ? nation.getName() : "\u0411\u0435\u0437 \u043D\u0430\u0446\u0438\u0438";
        String townName = town.getName();
        String mayorName = getPlayerName(town.getMayor());

        List<String> memberNames = new ArrayList<>();
        for (UUID id : town.getMembers()) {
            memberNames.add(getPlayerName(id));
        }
        String residentsStr = memberNames.isEmpty()
                ? "\u2014"
                : String.join(", ", memberNames);

        StringBuilder sb = new StringBuilder();
        sb.append("<div style=\"")
          .append("font-family:'Segoe UI',Arial,sans-serif;")
          .append("font-size:13px;")
          .append("line-height:1.7;")
          .append("min-width:200px;")
          .append("\">");

        sb.append("<div style=\"margin-bottom:1px;\">")
          .append("<span style=\"color:#b6b8bf;font-weight:bold;\">\u041D\u0430\u0446\u0438\u044F: </span>")
          .append("<span style=\"color:#ffffff;font-weight:bold;\">")
          .append(escapeHtml(nationName))
          .append("</span></div>");

        sb.append("<div>")
          .append("<span style=\"color:#b6b8bf;font-weight:bold;\">\u0413\u043E\u0440\u043E\u0434: </span>")
          .append("<span style=\"color:#ffffff;font-weight:bold;\">")
          .append(escapeHtml(townName))
          .append("</span></div>");

        sb.append("<hr style=\"border:none;border-top:1px solid rgba(255,255,255,0.15);margin:8px 0;\">");

        sb.append("<div style=\"margin-bottom:2px;\">")
          .append("<span style=\"color:#b6b8bf;font-weight:bold;\">\u041C\u044D\u0440: </span>")
          .append("<span style=\"color:#ffd700;font-weight:bold;\">")
          .append(escapeHtml(mayorName))
          .append("</span></div>");

        sb.append("<div style=\"word-wrap:break-word;overflow-wrap:break-word;\">")
          .append("<span style=\"color:#b6b8bf;font-weight:bold;\">\u0416\u0438\u0442\u0435\u043B\u0438: </span>")
          .append("<span style=\"color:#ffffff;font-weight:bold;\">")
          .append(escapeHtml(residentsStr))
          .append("</span></div>");

        sb.append("</div>");
        return sb.toString();
    }

    private static Set<String> calcEdges(Set<ChunkPos> chunks) {
        Set<String> e = new HashSet<>();
        for (ChunkPos c : chunks) {
            double x1 = c.x * 16, z1 = c.z * 16, x2 = x1 + 16, z2 = z1 + 16;
            toggleEdge(e, x1, z1, x2, z1);
            toggleEdge(e, x2, z1, x2, z2);
            toggleEdge(e, x2, z2, x1, z2);
            toggleEdge(e, x1, z2, x1, z1);
        }
        return e;
    }

    private static void toggleEdge(Set<String> s, double x1, double z1, double x2, double z2) {
        String f = x1 + "," + z1 + ">" + x2 + "," + z2;
        String r = x2 + "," + z2 + ">" + x1 + "," + z1;
        if (s.contains(r)) s.remove(r); else s.add(f);
    }

    private static List<List<Point>> tracePolygons(Set<String> edges) {
        List<List<Point>> polys = new ArrayList<>();
        Map<Point, Point> m = new HashMap<>();
        for (String e : edges) {
            String[] p = e.split(">");
            String[] a = p[0].split(","), b = p[1].split(",");
            m.put(new Point(Double.parseDouble(a[0]), Double.parseDouble(a[1])),
                  new Point(Double.parseDouble(b[0]), Double.parseDouble(b[1])));
        }
        while (!m.isEmpty()) {
            List<Point> poly = new ArrayList<>();
            Point start = m.keySet().iterator().next(), curr = start;
            while (curr != null) {
                poly.add(curr);
                Point next = m.remove(curr);
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

    private static String getPlayerName(UUID id) {
        if (id == null) return "\u2014";
        if (NationsData.getServer() != null) {
            var p = NationsData.getServer().getPlayerList().getPlayer(id);
            if (p != null) return p.getName().getString();
            var pr = NationsData.getServer().getProfileCache();
            if (pr != null && pr.get(id).isPresent()) return pr.get(id).get().getName();
        }
        return id.toString().substring(0, 8) + "...";
    }

    private static String escapeHtml(String t) {
        return t == null ? ""
                : t.replace("&", "&amp;").replace("<", "&lt;")
                   .replace(">", "&gt;").replace("\"", "&quot;");
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
}
