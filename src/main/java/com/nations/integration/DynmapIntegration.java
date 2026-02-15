package com.nations.integration;

import com.nations.NationsMod;
import com.nations.data.*;
import net.minecraftforge.fml.ModList;

import java.lang.reflect.Method;
import java.util.*;

public class DynmapIntegration {

    private static Object markerAPI = null;
    private static Object townMarkerSet = null;
    private static boolean enabled = false;

    public static void init() {
        if (!ModList.get().isLoaded("dynmap")) {
            NationsMod.LOGGER.info("DynMap не установлен — интеграция отключена");
            return;
        }

        try {
            Class<?> apiListenerClass = Class.forName("org.dynmap.DynmapCommonAPIListener");
            Class<?> apiClass = Class.forName("org.dynmap.DynmapCommonAPI");

            Object listener = java.lang.reflect.Proxy.newProxyInstance(
                apiListenerClass.getClassLoader(),
                new Class[]{apiListenerClass},
                (proxy, method, args) -> {
                    if (method.getName().equals("apiEnabled")) {
                        Object api = args[0];
                        Method getMarkerAPI = api.getClass().getMethod("getMarkerAPI");
                        markerAPI = getMarkerAPI.invoke(api);
                        if (markerAPI != null) {
                            enabled = true;
                            setupMarkerSets();
                            updateAllMarkers();
                            NationsMod.LOGGER.info("DynMap интеграция активирована!");
                        }
                    }
                    return null;
                }
            );

            Method registerMethod = apiListenerClass.getMethod("register", apiListenerClass);
            registerMethod.invoke(null, listener);

        } catch (Exception e) {
            NationsMod.LOGGER.warn("Ошибка инициализации DynMap: " + e.getMessage());
        }
    }

    public static boolean isEnabled() { return enabled; }

    private static void setupMarkerSets() {
        try {
            // Удалить старый набор если есть
            Method getSet = markerAPI.getClass().getMethod("getMarkerSet", String.class);
            Object existing = getSet.invoke(markerAPI, "nations.towns");
            if (existing != null) {
                Method deleteSet = existing.getClass().getMethod("deleteMarkerSet");
                deleteSet.invoke(existing);
            }

            // Создать новый
            Method createSet = markerAPI.getClass().getMethod("createMarkerSet",
                String.class, String.class, Set.class, boolean.class);
            townMarkerSet = createSet.invoke(markerAPI, "nations.towns", "Города и Нации", null, false);

            // Настройки
            Method setPriority = townMarkerSet.getClass().getMethod("setLayerPriority", int.class);
            setPriority.invoke(townMarkerSet, 10);

            Method setHide = townMarkerSet.getClass().getMethod("setHideByDefault", boolean.class);
            setHide.invoke(townMarkerSet, false);

        } catch (Exception e) {
            NationsMod.LOGGER.warn("Ошибка создания маркеров: " + e.getMessage());
        }
    }

    public static void updateAllMarkers() {
        if (!enabled || townMarkerSet == null) return;

        try {
            // Очистить
            clearMarkers();

            // Рисуем города
            for (Town town : NationsData.getAllTowns()) {
                drawTown(town);
            }
        } catch (Exception e) {
            NationsMod.LOGGER.warn("Ошибка обновления маркеров: " + e.getMessage());
        }
    }

    private static void clearMarkers() throws Exception {
        Method getAreas = townMarkerSet.getClass().getMethod("getAreaMarkers");
        Set<?> areas = (Set<?>) getAreas.invoke(townMarkerSet);
        for (Object area : new HashSet<>(areas)) {
            Method delete = area.getClass().getMethod("deleteMarker");
            delete.invoke(area);
        }

        Method getMarkers = townMarkerSet.getClass().getMethod("getMarkers");
        Set<?> markers = (Set<?>) getMarkers.invoke(townMarkerSet);
        for (Object marker : new HashSet<>(markers)) {
            Method delete = marker.getClass().getMethod("deleteMarker");
            delete.invoke(marker);
        }
    }

    private static void drawTown(Town town) throws Exception {
        int color = 0x888888;
        int fillColor = 0x888888;
        String nationName = "";
        int strokeWeight = 2;

        if (town.getNationName() != null) {
            Nation nation = NationsData.getNation(town.getNationName());
            if (nation != null) {
                color = nation.getColor().getHex();
                fillColor = color;
                nationName = nation.getName();
            }
        }

        if (town.isAtWar()) { color = 0xFF0000; strokeWeight = 3; }
        if (town.isCaptured()) { color = 0xFF6600; strokeWeight = 3; }

        for (net.minecraft.world.level.ChunkPos cp : town.getClaimedChunks()) {
            double x1 = cp.x * 16;
            double z1 = cp.z * 16;
            double x2 = x1 + 16;
            double z2 = z1 + 16;

            String markerId = "town_" + town.getName().toLowerCase() + "_" + cp.x + "_" + cp.z;

            Method createArea = townMarkerSet.getClass().getMethod("createAreaMarker",
                String.class, String.class, boolean.class, String.class,
                double[].class, double[].class, boolean.class);

            Object area = createArea.invoke(townMarkerSet,
                markerId,
                buildLabel(town, nationName),
                true, // isHTML
                "world",
                new double[]{x1, x2, x2, x1},
                new double[]{z1, z1, z2, z2},
                false
            );

            if (area != null) {
                Method setFill = area.getClass().getMethod("setFillStyle", double.class, int.class);
                setFill.invoke(area, 0.25, fillColor);

                Method setLine = area.getClass().getMethod("setLineStyle", int.class, double.class, int.class);
                setLine.invoke(area, strokeWeight, 0.8, color);
            }
        }
    }

    private static String buildLabel(Town town, String nationName) {
        StringBuilder sb = new StringBuilder();
        sb.append("<div style='padding:6px;background:rgba(0,0,0,0.8);border-radius:6px;text-align:center;'>");
        sb.append("<b style='color:#FFD700;font-size:13px;'>&#127984; ").append(town.getName()).append("</b>");

        if (!nationName.isEmpty()) {
            sb.append("<br><span style='color:#6688FF;'>&#127963; ").append(nationName).append("</span>");
        }

        if (town.isAtWar()) {
            sb.append("<br><b style='color:#FF4444;'>&#9876; ВОЙНА</b>");
        }
        if (town.isCaptured()) {
            sb.append("<br><span style='color:#FF6600;'>Захвачен: ").append(town.getCapturedBy()).append("</span>");
        }

        sb.append("<br><span style='color:#AAA;'>Жителей: ").append(town.getMembers().size());
        sb.append(" | Чанков: ").append(town.getClaimedChunks().size()).append("</span>");

        sb.append("<br><span style='color:").append(town.isPvpEnabled() ? "#F44" : "#4F4").append(";'>");
        sb.append(town.isPvpEnabled() ? "PvP: ВКЛ" : "PvP: ВЫКЛ").append("</span>");

        // Правитель
        if (NationsData.getServer() != null) {
            var p = NationsData.getServer().getPlayerList().getPlayer(town.getMayor());
            String mayorName = p != null ? p.getName().getString() : "оффлайн";
            sb.append("<br><span style='color:#FFD700;'>&#128081; ").append(mayorName).append("</span>");
        }

        sb.append("</div>");
        return sb.toString();
    }
}
