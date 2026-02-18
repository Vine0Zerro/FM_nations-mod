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
    private static String TOWN_ICON_BASE64 = "iVBORw0KGgoAAAANSUhEUgAAABAAAAAQCAMAAAAoLQ9TAAAAIGNIUk0AAHomAACAhAAA+gAAAIDoAAB1MAAA6mAAADqYAAAXcJy6UTwAAAEpUExURQAAAM/P0dDQ0svLzczMzvfz9vv7+/z7++Tm5tzd2/r5+fj49/n19/79/fX19P/+/v38/P39/f///+rq6cfHxt7e3qenpt/f3qiop9XU1KalpcC+vqKhocG/v8/PzquqqqGfn6OiocG7v8bFxMLAwNTU07q6uaWjpKqpqaaipa6qrLOusamlp6yoqv///9/h4dja2f////j49u3t7NLU1MbHxevr6vr6+erq6e3t7N3d3MbGxt7e3uDf37e3tqempt/f3t/f37a2tainp9TU09ra2bKxsaWlpL68vMvKydTT09XV1Lu7u7Kysainp5+enry6usHAv7Cwr5yam+np6NjY2NLS0ubm5eXl5d3d3MnJyN/f39vb27+/v7q6ud7e3tva2r+/vv///+VauVgAAABUdFJOUwAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAKU1MITbP29rNNOe3tOUD29kBA9vZAOe3tOAhNs/f2s0wIClxcCsucjc0AAAABYktHRBJ7vGwAAAAAB3RJTUUH6gIQFR8CaGuYkgAAAIxJREFUGNNjYCISMDIxsyBxWdnYOTi5uHlgfHZePj19Az0+fgGogKChkbGJqZm5oRBUQNjCMiQ0LNzKWgQqIGpjGxESGWVnLwYVEHdwjI6JjXNyloAKSLq4xickxrm5S0EFpD08vbx9fP38ZaACstJyAYFBwfIKijCHKCmrqKqpa2giuVVLW0eXWG8CAKATE9MHZILXAAAAJXRFWHRkYXRlOmNyZWF0ZQAyMDI2LTAyLTE2VDIxOjMwOjU0KzAwOjAwx1vJegAAACV0RVh0ZGF0ZTptb2RpZnkAMjAyNi0wMi0xNlQyMTozMDo1NCswMDowMLYGccYAAAAodEVYdGRhdGU6dGltZXN0YW1wADIwMjYtMDItMTZUMjE6MzE6MDIrMDA6MDAl4QB5AAAAAElFTkSuQmCC";

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
                    try { drawNationTerritory(nation, tMarkers); } catch (Exception e) {}
                    try { drawNationTownBorders(nation, tMarkers); } catch (Exception e) {}
                }
                for (Town town : NationsData.getAllTowns()) {
                    if (town.getNationName() == null)
                        try { drawStandaloneTown(town, tMarkers); } catch (Exception e) {}
                    try { drawTownIcon(town, iMarkers); } catch (Exception e) {}
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

    // ── Территория нации (общий полигон) ──────────────────────────
    private static void drawNationTerritory(Nation nation, Map<String, Object> markers) throws Exception {
        Set<ChunkPos> allChunks = new HashSet<>();
        for (String tn : nation.getTowns()) {
            Town t = NationsData.getTown(tn);
            if (t != null) allChunks.addAll(t.getClaimedChunks());
        }
        if (allChunks.isEmpty()) return;

        Set<String> outerEdges = calcEdges(allChunks);
        List<List<Point>> polygons = tracePolygons(outerEdges);

        int hex = nation.getColor().getHex();
        int cr = (hex >> 16) & 0xFF, cg = (hex >> 8) & 0xFF, cb = hex & 0xFF;
        Object fill = cColor.newInstance(cr, cg, cb, 0.22f);
        Object line = cColor.newInstance(cr, cg, cb, 1.0f);

        int i = 0;
        for (List<Point> poly : polygons)
            markers.put("nation_" + nation.getName() + "_" + (i++),
                    createShapeMarker(nation.getName(), createShape(poly), fill, line, 3, ""));
    }

    // ── Границы городов внутри нации ──────────────────────────────
    private static void drawNationTownBorders(Nation nation, Map<String, Object> markers) throws Exception {
        List<String> townNames = new ArrayList<>(nation.getTowns());

        int hex = nation.getColor().getHex();
        int cr = (hex >> 16) & 0xFF, cg = (hex >> 8) & 0xFF, cb = hex & 0xFF;
        int lr = Math.min(255, cr + 60), lg = Math.min(255, cg + 60), lb = Math.min(255, cb + 60);
        Object townLine = cColor.newInstance(lr, lg, lb, 0.5f);
        Object noFill   = cColor.newInstance(0, 0, 0, 0.0f);

        for (String townName : townNames) {
            Town town = NationsData.getTown(townName);
            if (town == null || town.getClaimedChunks().isEmpty()) continue;

            Set<String> townEdges = calcEdges(town.getClaimedChunks());
            List<List<Point>> townPolygons = tracePolygons(townEdges);
            String popup = buildTownPopup(town, nation);

            int j = 0;
            for (List<Point> poly : townPolygons) {
                if (poly.size() < 3) continue;
                if (townNames.size() == 1) {
                    Object fill  = cColor.newInstance(cr, cg, cb, 0.22f);
                    Object line2 = cColor.newInstance(cr, cg, cb, 1.0f);
                    markers.put("townborder_" + townName + "_" + (j++),
                            createShapeMarker(townName, createShape(poly), fill, line2, 3, popup));
                } else {
                    markers.put("townborder_" + townName + "_" + (j++),
                            createShapeMarker(townName, createShape(poly), noFill, townLine, 1, popup));
                }
            }
        }
    }

    // ── Город без нации ───────────────────────────────────────────
    private static void drawStandaloneTown(Town town, Map<String, Object> markers) throws Exception {
        if (town.getClaimedChunks().isEmpty()) return;
        Set<String> edges = calcEdges(town.getClaimedChunks());
        List<List<Point>> polygons = tracePolygons(edges);
        int cr = 150, cg = 150, cb = 150;
        Object fill = cColor.newInstance(cr, cg, cb, 0.25f);
        Object line = cColor.newInstance(cr, cg, cb, 1.0f);
        String popup = buildTownPopup(town, null);
        int i = 0;
        for (List<Point> poly : polygons)
            markers.put("standalone_" + town.getName() + "_" + (i++),
                    createShapeMarker(town.getName(), createShape(poly), fill, line, 2, popup));
    }

    // ── Иконка города ─────────────────────────────────────────────
    private static void drawTownIcon(Town town, Map<String, Object> markers) throws Exception {
        if (town.getSpawnPos() == null) return;
        boolean isCapital = false;
        if (town.getNationName() != null) {
            Nation nation = NationsData.getNation(town.getNationName());
            if (nation != null) isCapital = nation.isCapital(town.getName());
        }

        double px = town.getSpawnPos().getX() + 0.5;
        double py = town.getSpawnPos().getY() + 2.0;
        double pz = town.getSpawnPos().getZ() + 0.5;

        String base64   = isCapital ? CAPITAL_ICON_BASE64 : TOWN_ICON_BASE64;
        int    iconSize  = isCapital ? 26 : 14;

        // z-index:1 — внутри контейнера; глобальный z-index задаётся CSS в index.html
        String html = "<div style=\"transform:translate(-50%,-50%);width:" + iconSize
                + "px;height:" + iconSize
                + "px;filter:drop-shadow(0 1px 3px rgba(0,0,0,0.7));cursor:pointer;z-index:1;position:relative;\">";
        if (base64 != null) {
            html += "<img src=\"data:image/png;base64," + base64
                    + "\" width=\"" + iconSize + "\" height=\"" + iconSize
                    + "\" style=\"display:block;\" />";
        } else {
            html += "<span style=\"font-size:" + (isCapital ? 18 : 8) + "px;\">"
                    + (isCapital ? "★" : "•") + "</span>";
        }
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

    // ── ShapeMarker builder ───────────────────────────────────────
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

    // ══════════════════════════════════════════════════════════════
    //  ПОПАП  —  информация о городе (клик по территории)
    // ══════════════════════════════════════════════════════════════
    private static String buildTownPopup(Town town, Nation nation) {

        String nationName = (nation != null) ? nation.getName() : "Без нации";
        String townName   = town.getName();
        String mayorName  = getPlayerName(town.getMayor());

        // Собираем имена жителей
        List<String> memberNames = new ArrayList<>();
        for (UUID id : town.getMembers()) {
            memberNames.add(getPlayerName(id));
        }
        String residentsStr = memberNames.isEmpty()
                ? "—"
                : String.join(", ", memberNames);

        // ── HTML с inline-стилями (не зависит от внешнего CSS) ────
        StringBuilder sb = new StringBuilder();
        sb.append("<div style=\"")
          .append("font-family:'Segoe UI',Arial,sans-serif;")
          .append("font-size:13px;")
          .append("line-height:1.7;")
          .append("min-width:200px;")
          .append("\">");

        // Строка 1 — Нация
        sb.append("<div style=\"margin-bottom:1px;\">")
          .append("<span style=\"color:#b6b8bf;font-weight:bold;\">Нация: </span>")
          .append("<span style=\"color:#ffffff;font-weight:bold;\">")
          .append(escapeHtml(nationName))
          .append("</span></div>");

        // Строка 2 — Город
        sb.append("<div>")
          .append("<span style=\"color:#b6b8bf;font-weight:bold;\">Город: </span>")
          .append("<span style=\"color:#ffffff;font-weight:bold;\">")
          .append(escapeHtml(townName))
          .append("</span></div>");

        // Разделитель
        sb.append("<hr style=\"border:none;border-top:1px solid rgba(255,255,255,0.15);margin:8px 0;\">");

        // Строка 3 — Мэр (жёлтый ник)
        sb.append("<div style=\"margin-bottom:2px;\">")
          .append("<span style=\"color:#b6b8bf;font-weight:bold;\">Мэр: </span>")
          .append("<span style=\"color:#ffd700;font-weight:bold;\">")
          .append(escapeHtml(mayorName))
          .append("</span></div>");

        // Строка 4 — Жители (перенос если не вмещаются)
        sb.append("<div style=\"word-wrap:break-word;overflow-wrap:break-word;\">")
          .append("<span style=\"color:#b6b8bf;font-weight:bold;\">Жители: </span>")
          .append("<span style=\"color:#ffffff;font-weight:bold;\">")
          .append(escapeHtml(residentsStr))
          .append("</span></div>");

        sb.append("</div>");
        return sb.toString();
    }

    // ── Geometry helpers ──────────────────────────────────────────
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
        if (id == null) return "—";
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
