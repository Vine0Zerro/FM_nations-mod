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
    private static Method mGetInstance, mGetMaps, mGetId, mGetMarkerSets;
    private static Method mMarkerSetBuilder, mMarkerSetLabel, mMarkerSetBuild, mMarkerSetGetMarkers;
    private static Method mShapeMarkerBuilder, mShapeMarkerLabel, mShapeMarkerShape, mShapeMarkerDepthTest, mShapeMarkerFillColor, mShapeMarkerLineColor, mShapeMarkerLineWidth, mShapeMarkerDetail, mShapeMarkerBuild;
    private static Method mPOIMarkerBuilder, mPOIMarkerLabel, mPOIMarkerPosition, mPOIMarkerDetail, mPOIMarkerIcon, mPOIMarkerBuild;
    private static Constructor<?> cVector2d, cShape, cColor;

    public static void init() {
        if (!ModList.get().isLoaded("bluemap")) return;
        try {
            loadClasses();
            checkApi();
            enabled = true;
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
        mPOIMarkerBuilder = clsPOIMarker.getMethod("builder");
        Class<?> pmb = mPOIMarkerBuilder.getReturnType();
        mPOIMarkerLabel = pmb.getMethod("label", String.class);
        mPOIMarkerPosition = pmb.getMethod("position", double.class, double.class, double.class);
        mPOIMarkerDetail = pmb.getMethod("detail", String.class);
        mPOIMarkerIcon = pmb.getMethod("icon", String.class, int.class, int.class);
        mPOIMarkerBuild = pmb.getMethod("build");
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
                    Object b = mMarkerSetBuilder.invoke(null);
                    mMarkerSetLabel.invoke(b, "Города и Нации");
                    markerSet = mMarkerSetBuild.invoke(b);
                    markerSets.put(MARKER_SET_ID, markerSet);
                }
                Map<String, Object> markers = (Map<String, Object>) mMarkerSetGetMarkers.invoke(markerSet);
                markers.clear();
                for (Nation nation : NationsData.getAllNations()) drawNationBorder(nation, markers);
                for (Nation nation : NationsData.getAllNations()) drawInnerTownBorders(nation, markers);
                for (Town town : NationsData.getAllTowns()) {
                    if (town.getNationName() == null) drawStandaloneTown(town, markers);
                }
                for (Town town : NationsData.getAllTowns()) drawTownPOI(town, markers);
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
            double x1=cp.x*16.0, z1=cp.z*16.0, x2=x1+16, z2=z1+16;
            toggleEdge(edges,x1,z1,x2,z1); toggleEdge(edges,x2,z1,x2,z2);
            toggleEdge(edges,x2,z2,x1,z2); toggleEdge(edges,x1,z2,x1,z1);
        }
        List<List<Point>> polygons = tracePolygons(edges);
        int hex=nation.getColor().getHex(), r=(hex>>16)&0xFF, g=(hex>>8)&0xFF, b=hex&0xFF;
        Object fillColor=cColor.newInstance(r,g,b,0.25f), lineColor=cColor.newInstance(r,g,b,1.0f);
        boolean atWar=false;
        for (String tn : nation.getTowns()) { Town t=NationsData.getTown(tn); if(t!=null&&t.isAtWar()){atWar=true;break;} }
        if (atWar) { fillColor=cColor.newInstance(255,0,0,0.25f); lineColor=cColor.newInstance(255,0,0,1.0f); }
        String popup = buildNationPopup(nation, r, g, b);
        int pi=0;
        for (List<Point> pp : polygons) {
            if(pp.size()<3) continue;
            Object va=java.lang.reflect.Array.newInstance(clsVector2d,pp.size());
            for(int i=0;i<pp.size();i++) java.lang.reflect.Array.set(va,i,cVector2d.newInstance(pp.get(i).x,pp.get(i).z));
            Object shape=cShape.newInstance(va), bd=mShapeMarkerBuilder.invoke(null);
            mShapeMarkerLabel.invoke(bd,nation.getName()); mShapeMarkerShape.invoke(bd,shape,64f);
            mShapeMarkerDepthTest.invoke(bd,false); mShapeMarkerFillColor.invoke(bd,fillColor);
            mShapeMarkerLineColor.invoke(bd,lineColor); mShapeMarkerLineWidth.invoke(bd,3); // 3px border
            mShapeMarkerDetail.invoke(bd,popup);
            markers.put("nation_"+nation.getName()+"_"+(pi++), mShapeMarkerBuild.invoke(bd));
        }
    }

    private static void drawInnerTownBorders(Nation nation, Map<String, Object> markers) throws Exception {
        Map<ChunkPos,String> chunkToTown = new HashMap<>();
        for (String tn : nation.getTowns()) {
            Town t=NationsData.getTown(tn); if(t==null) continue;
            for (ChunkPos cp : t.getClaimedChunks()) chunkToTown.put(cp, tn);
        }
        Set<String> innerEdges = new HashSet<>();
        for (Map.Entry<ChunkPos,String> e : chunkToTown.entrySet()) {
            ChunkPos cp=e.getKey(); String my=e.getValue();
            double x1=cp.x*16.0, z1=cp.z*16.0, x2=x1+16, z2=z1+16;
            addInner(innerEdges,chunkToTown,my,new ChunkPos(cp.x,cp.z-1),x1,z1,x2,z1);
            addInner(innerEdges,chunkToTown,my,new ChunkPos(cp.x,cp.z+1),x1,z2,x2,z2);
            addInner(innerEdges,chunkToTown,my,new ChunkPos(cp.x+1,cp.z),x2,z1,x2,z2);
            addInner(innerEdges,chunkToTown,my,new ChunkPos(cp.x-1,cp.z),x1,z1,x1,z2);
        }
        if (innerEdges.isEmpty()) return;
        int hex=nation.getColor().getHex(), r=(hex>>16)&0xFF, g=(hex>>8)&0xFF, b=hex&0xFF;
        Object lineColor = cColor.newInstance(r, g, b, 0.7f); // Transparent
        int ei=0;
        for (String edge : innerEdges) {
            String[] pts=edge.split(">"); String[] p1=pts[0].split(","), p2=pts[1].split(",");
            double ex1=Double.parseDouble(p1[0]),ez1=Double.parseDouble(p1[1]);
            double ex2=Double.parseDouble(p2[0]),ez2=Double.parseDouble(p2[1]);
            double dx=0,dz=0; if(Math.abs(ex1-ex2)<0.1) dx=0.1; else dz=0.1;
            Object va=java.lang.reflect.Array.newInstance(clsVector2d,4);
            java.lang.reflect.Array.set(va,0,cVector2d.newInstance(ex1-dx,ez1-dz));
            java.lang.reflect.Array.set(va,1,cVector2d.newInstance(ex2+dx,ez1-dz));
            java.lang.reflect.Array.set(va,2,cVector2d.newInstance(ex2+dx,ez2+dz));
            java.lang.reflect.Array.set(va,3,cVector2d.newInstance(ex1-dx,ez2+dz));
            Object shape=cShape.newInstance(va), bd=mShapeMarkerBuilder.invoke(null);
            mShapeMarkerLabel.invoke(bd,"Граница городов"); mShapeMarkerShape.invoke(bd,shape,64f);
            mShapeMarkerDepthTest.invoke(bd,false); mShapeMarkerFillColor.invoke(bd,lineColor);
            mShapeMarkerLineColor.invoke(bd,lineColor); mShapeMarkerLineWidth.invoke(bd,1); // 1px border
            mShapeMarkerDetail.invoke(bd,"");
            markers.put("inner_"+nation.getName()+"_"+(ei++), mShapeMarkerBuild.invoke(bd));
        }
    }

    private static void addInner(Set<String> ie, Map<ChunkPos,String> ctm, String my, ChunkPos nb, double x1, double z1, double x2, double z2) {
        String nt=ctm.get(nb);
        if (nt!=null && !nt.equals(my)) {
            String k1;
            if (x1 < x2 || (x1 == x2 && z1 < z2)) k1 = x1+","+z1+">"+x2+","+z2;
            else k1 = x2+","+z2+">"+x1+","+z1;
            ie.add(k1);
        }
    }

    private static void drawStandaloneTown(Town town, Map<String, Object> markers) throws Exception {
        Set<ChunkPos> chunks=town.getClaimedChunks(); if(chunks.isEmpty()) return;
        Set<String> edges=new HashSet<>();
        for (ChunkPos cp : chunks) {
            double x1=cp.x*16.0,z1=cp.z*16.0,x2=x1+16,z2=z1+16;
            toggleEdge(edges,x1,z1,x2,z1); toggleEdge(edges,x2,z1,x2,z2);
            toggleEdge(edges,x2,z2,x1,z2); toggleEdge(edges,x1,z2,x1,z1);
        }
        List<List<Point>> polygons=tracePolygons(edges);
        Object fillColor=cColor.newInstance(136,136,136,0.4f), lineColor=cColor.newInstance(136,136,136,0.9f);
        if(town.isAtWar()){fillColor=cColor.newInstance(255,0,0,0.4f);lineColor=cColor.newInstance(255,0,0,1f);}
        else if(town.isCaptured()){fillColor=cColor.newInstance(255,140,0,0.4f);lineColor=cColor.newInstance(255,140,0,1f);}
        String popup=buildPopup(town,"Без нации",136,136,136);
        int pi=0;
        for (List<Point> pp : polygons) {
            if(pp.size()<3) continue;
            Object va=java.lang.reflect.Array.newInstance(clsVector2d,pp.size());
            for(int i=0;i<pp.size();i++) java.lang.reflect.Array.set(va,i,cVector2d.newInstance(pp.get(i).x,pp.get(i).z));
            Object shape=cShape.newInstance(va),bd=mShapeMarkerBuilder.invoke(null);
            mShapeMarkerLabel.invoke(bd,town.getName()); mShapeMarkerShape.invoke(bd,shape,64f);
            mShapeMarkerDepthTest.invoke(bd,false); mShapeMarkerFillColor.invoke(bd,fillColor);
            mShapeMarkerLineColor.invoke(bd,lineColor); mShapeMarkerLineWidth.invoke(bd,3);
            mShapeMarkerDetail.invoke(bd,popup);
            markers.put("town_"+town.getName()+"_"+(pi++), mShapeMarkerBuild.invoke(bd));
        }
    }

    private static void drawTownPOI(Town town, Map<String, Object> markers) throws Exception {
        if (town.getSpawnPos() == null) return;
        boolean isCapital = false;
        String nationName = "Без нации";
        int r=136, g=136, b=136;
        if (town.getNationName() != null) {
            Nation nation = NationsData.getNation(town.getNationName());
            if (nation != null) {
                int hex = nation.getColor().getHex();
                r=(hex>>16)&0xFF; g=(hex>>8)&0xFF; b=hex&0xFF;
                nationName = nation.getName();
                isCapital = nation.isCapital(town.getName());
            }
        }
        String popup = buildPopup(town, nationName, r, g, b);
        Object bd = mPOIMarkerBuilder.invoke(null);
        mPOIMarkerLabel.invoke(bd, town.getName());
        mPOIMarkerPosition.invoke(bd, (double)town.getSpawnPos().getX(), (double)town.getSpawnPos().getY()+2, (double)town.getSpawnPos().getZ());
        mPOIMarkerDetail.invoke(bd, popup);
        if (isCapital) mPOIMarkerIcon.invoke(bd, ICON_CROWN_BASE64, 16, 16);
        else mPOIMarkerIcon.invoke(bd, ICON_TOWN_BASE64, 8, 8);
        markers.put("poi_"+town.getName(), mPOIMarkerBuild.invoke(bd));
    }

    private static void toggleEdge(Set<String> edges, double x1, double z1, double x2, double z2) {
        String f=x1+","+z1+">"+x2+","+z2, bk=x2+","+z2+">"+x1+","+z1;
        if(edges.contains(bk)) edges.remove(bk); else edges.add(f);
    }

    private static List<List<Point>> tracePolygons(Set<String> edges) {
        List<List<Point>> polygons=new ArrayList<>();
        Map<Point,Point> pm=new HashMap<>();
        for (String e : edges) {
            String[] p=e.split(">"); String[] a=p[0].split(","), b=p[1].split(",");
            pm.put(new Point(Double.parseDouble(a[0]),Double.parseDouble(a[1])),
                   new Point(Double.parseDouble(b[0]),Double.parseDouble(b[1])));
        }
        while(!pm.isEmpty()) {
            List<Point> poly=new ArrayList<>();
            Point start=pm.keySet().iterator().next(), cur=start;
            int s=0;
            while(cur!=null && s++<10000) {
                poly.add(cur); Point next=pm.remove(cur);
                if(next==null||next.equals(start)) break; cur=next;
            }
            if(poly.size()>=3) polygons.add(poly);
        }
        return polygons;
    }

    private static class Point {
        double x,z;
        Point(double x,double z){this.x=x;this.z=z;}
        @Override public boolean equals(Object o){if(this==o)return true;if(o==null||getClass()!=o.getClass())return false;Point p=(Point)o;return Double.compare(p.x,x)==0&&Double.compare(p.z,z)==0;}
        @Override public int hashCode(){return Objects.hash(x,z);}
    }

    private static String buildNationPopup(Nation nation, int r, int g, int b) {
        StringBuilder sb=new StringBuilder();
        String cs="font-family:'Segoe UI',sans-serif;background:rgba(10,10,15,0.95);padding:12px;border-radius:8px;color:#fff;min-width:280px;margin:-10px;border:1px solid rgba(255,255,255,0.15);box-shadow:0 4px 15px rgba(0,0,0,0.8);position:relative;pointer-events:auto;";
        String cb="position:absolute;top:2px;right:8px;color:#aaa;font-size:20px;cursor:pointer;font-weight:bold;line-height:1;padding:2px 4px;user-select:none;";
        String tc=String.format("rgb(%d,%d,%d)",r,g,b);
        String ls="color:#999;font-weight:500;white-space:nowrap;", vs="font-weight:bold;text-align:left;";
        String gs="display:grid;grid-template-columns:min-content 1fr;column-gap:10px;row-gap:4px;font-size:14px;";
        sb.append("<div style=\"").append(cs).append("\">");
        sb.append("<div style=\"").append(cb).append("\" onmouseover=\"this.style.color='#fff'\" onmouseout=\"this.style.color='#aaa'\" onclick=\"var lp=this.closest('.bm-marker-labelpopup');if(lp){lp.style.display='none';}event.stopPropagation();\">×</div>");
        sb.append("<div style=\"font-size:18px;font-weight:900;color:").append(tc).append(";text-align:center;margin-bottom:8px;\">🏛 ").append(nation.getName()).append("</div>");
        sb.append("<hr style=\"border:0;border-top:2px solid rgba(255,255,255,0.3);margin:8px 0;\">");
        sb.append("<div style=\"").append(gs).append("\">");
        sb.append("<div style=\"").append(ls).append("\">Городов:</div><div style=\"").append(vs).append("color:#FFD700;\">").append(nation.getTowns().size()).append("</div>");
        sb.append("<div style=\"").append(ls).append("\">Жителей:</div><div style=\"").append(vs).append("color:#DDD;\">").append(nation.getTotalMembers()).append("</div>");
        sb.append("<div style=\"").append(ls).append("\">Территория:</div><div style=\"").append(vs).append("color:#DDD;\">").append(nation.getTotalChunks()).append(" чанков</div>");
        sb.append("<div style=\"").append(ls).append("\">Рейтинг:</div><div style=\"").append(vs).append("color:#FFD700;\">⭐ ").append(nation.getRating()).append("</div>");
        if(nation.getCapitalTown()!=null){sb.append("<div style=\"").append(ls).append("\">Столица:</div><div style=\"").append(vs).append("color:#FFD700;\">👑 ").append(nation.getCapitalTown()).append("</div>");}
        sb.append("</div>");
        sb.append("<hr style=\"border:0;border-top:1px solid rgba(255,255,255,0.2);margin:8px 0;\">");
        sb.append("<div style=\"font-size:12px;color:#aaa;\">Города:</div><div style=\"font-size:13px;color:#ddd;margin-top:4px;\">");
        for (String tn : nation.getTowns()) {
            Town t=NationsData.getTown(tn); if(t==null) continue;
            boolean cap=nation.isCapital(tn);
            sb.append(cap?"👑":"🏠").append(" ").append(tn).append(" <span style=\"color:#888;\">(").append(t.getClaimedChunks().size()).append(")</span><br>");
        }
        sb.append("</div></div>");
        return sb.toString();
    }

    private static String buildPopup(Town town, String nationName, int r, int g, int b) {
        StringBuilder sb=new StringBuilder();
        String cs="font-family:'Segoe UI',sans-serif;background:rgba(10,10,15,0.95);padding:12px;border-radius:8px;color:#fff;min-width:260px;margin:-10px;border:1px solid rgba(255,255,255,0.15);box-shadow:0 4px 15px rgba(0,0,0,0.8);position:relative;pointer-events:auto;";
        String cb="position:absolute;top:2px;right:8px;color:#aaa;font-size:20px;cursor:pointer;font-weight:bold;line-height:1;padding:2px 4px;user-select:none;";
        String gs="display:grid;grid-template-columns:min-content 1fr;align-items:baseline;column-gap:10px;row-gap:4px;font-size:14px;";
        String ls="color:#999;font-weight:500;white-space:nowrap;", vs="font-weight:bold;text-align:left;";
        String tc=String.format("rgb(%d,%d,%d)",r,g,b);
        if(town.isAtWar()) tc="#FF4444";
        sb.append("<div class=\"nations-popup\" style=\"").append(cs).append("\">");
        sb.append("<div class=\"nations-close-btn\" style=\"").append(cb).append("\" onmouseover=\"this.style.color='#fff'\" onmouseout=\"this.style.color='#aaa'\" onclick=\"var lp=this.closest('.bm-marker-labelpopup');if(lp){lp.style.display='none';}var mp=this.closest('.bm-marker-popup');if(mp){mp.style.display='none';}event.stopPropagation();\">×</div>");
        sb.append("<div style=\"").append(gs).append("\">");
        String nc=town.getNationName()!=null?tc:"#999";
        sb.append("<div style=\"").append(ls).append("\">Нация:</div><div style=\"").append(vs).append("color:").append(nc).append(";\">").append(nationName).append("</div>");
        sb.append("<div style=\"").append(ls).append("\">Город:</div><div style=\"").append(vs).append("color:#DDD;\">").append(town.getName()).append("</div>");
        sb.append("</div>");
        sb.append("<hr style=\"border:0;border-top:2px solid rgba(255,255,255,0.3);margin:8px 0;\">");
        sb.append("<div style=\"").append(gs).append("\">");
        String mn="Неизвестно";
        if(NationsData.getServer()!=null){var p=NationsData.getServer().getPlayerList().getPlayer(town.getMayor());if(p!=null)mn=p.getName().getString();}
        sb.append("<div style=\"").append(ls).append("\">МЭР:</div><div style=\"").append(vs).append("color:#FFD700;font-size:13px;\">").append(mn).append("</div>");
        sb.append("<div style=\"").append(ls).append("align-self:start;\">Жители:</div><div style=\"").append(vs).append("color:#DDD;font-size:13px;line-height:1.3;\">");
        List<String> names=new ArrayList<>(); int lim=0;
        for(UUID id:town.getMembers()){if(lim>=15){names.add("...");break;}if(NationsData.getServer()!=null){var p=NationsData.getServer().getPlayerList().getPlayer(id);names.add(p!=null?p.getName().getString():"оффлайн");}else names.add("?");lim++;}
        sb.append(String.join(", ",names)).append("</div></div>");
        if(town.isAtWar()) sb.append("<div style=\"margin-top:10px;color:#ff5555;font-weight:900;text-align:center;text-transform:uppercase;\">⚠ ИДЕТ ВОЙНА</div>");
        else if(town.isCaptured()) sb.append("<div style=\"margin-top:10px;color:#ffaa00;font-weight:900;text-align:center;text-transform:uppercase;\">🏴 ЗАХВАЧЕН</div>");
        sb.append("</div>");
        return sb.toString();
    }
}
