package com.nations.gui;

import com.mojang.blaze3d.vertex.PoseStack;
import com.nations.network.ClaimChunksPacket;
import com.nations.network.ClaimMapPacket;
import com.nations.network.NetworkHandler;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.util.*;

public class ClaimMapScreen extends Screen {

    private final ClaimMapPacket data;
    private final int mapRadius = 16; // чанков от центра
    private final int cellSize = 8; // пикселей на чанк
    private final Set<long[]> selectedChunks = new HashSet<>();
    private final Set<String> selectedKeys = new HashSet<>();

    // Для хранения позиций
    private int mapStartX, mapStartY;

    public ClaimMapScreen(ClaimMapPacket data) {
        super(Component.literal("Карта территорий"));
        this.data = data;
    }

    @Override
    protected void init() {
        super.init();
        int mapWidth = (mapRadius * 2 + 1) * cellSize;
        int mapHeight = (mapRadius * 2 + 1) * cellSize;
        mapStartX = (this.width - mapWidth) / 2;
        mapStartY = (this.height - mapHeight) / 2 - 10;

        // Кнопка "Запривачить выбранные"
        this.addRenderableWidget(Button.builder(
            Component.literal("Запривачить выбранные"),
            button -> claimSelected()
        ).bounds(this.width / 2 - 75, mapStartY + mapHeight + 5, 150, 20).build());
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(graphics);

        int mapWidth = (mapRadius * 2 + 1) * cellSize;
        int mapHeight = (mapRadius * 2 + 1) * cellSize;

        // Фон карты
        graphics.fill(mapStartX - 1, mapStartY - 1,
            mapStartX + mapWidth + 1, mapStartY + mapHeight + 1, 0xFF333333);
        graphics.fill(mapStartX, mapStartY,
            mapStartX + mapWidth, mapStartY + mapHeight, 0xFF1a1a1a);

        int pcx = data.getPlayerChunkX();
        int pcz = data.getPlayerChunkZ();

        // Индекс занятых чанков
        Map<String, ClaimMapPacket.ChunkEntry> claimedMap = new HashMap<>();
        for (var e : data.getEntries()) {
            claimedMap.put(e.x + "," + e.z, e);
        }

        // Рисуем сетку
        for (int dx = -mapRadius; dx <= mapRadius; dx++) {
            for (int dz = -mapRadius; dz <= mapRadius; dz++) {
                int cx = pcx + dx;
                int cz = pcz + dz;
                int px = mapStartX + (dx + mapRadius) * cellSize;
                int py = mapStartY + (dz + mapRadius) * cellSize;

                String key = cx + "," + cz;
                ClaimMapPacket.ChunkEntry entry = claimedMap.get(key);

                int color;
                if (entry != null) {
                    // Занятый чанк — цвет нации или серый
                    color = (0xAA << 24) | (entry.color & 0xFFFFFF);
                } else if (selectedKeys.contains(key)) {
                    // Выбранный для привата
                    color = 0xAA00FF00;
                } else {
                    // Пустой
                    color = 0x44FFFFFF;
                }

                graphics.fill(px, py, px + cellSize - 1, py + cellSize - 1, color);

                // Позиция игрока
                if (dx == 0 && dz == 0) {
                    graphics.fill(px + 2, py + 2, px + cellSize - 3, py + cellSize - 3,
                        0xFFFF0000);
                }
            }
        }

        // Подсказка при наведении
        if (mouseX >= mapStartX && mouseX < mapStartX + mapWidth &&
            mouseY >= mapStartY && mouseY < mapStartY + mapHeight) {

            int dx = (mouseX - mapStartX) / cellSize - mapRadius;
            int dz = (mouseY - mapStartY) / cellSize - mapRadius;
            int cx = pcx + dx;
            int cz = pcz + dz;
            String key = cx + "," + cz;

            List<Component> tooltip = new ArrayList<>();
            tooltip.add(Component.literal("§7Чанк: [" + cx + ", " + cz + "]"));

            ClaimMapPacket.ChunkEntry entry = claimedMap.get(key);
            if (entry != null) {
                tooltip.add(Component.literal("§eГород: " + entry.townName));
                if (!entry.nationName.isEmpty()) {
                    tooltip.add(Component.literal("§9Нация: " + entry.nationName));
                }
            } else {
                tooltip.add(Component.literal("§aСвободен"));
                if (selectedKeys.contains(key)) {
                    tooltip.add(Component.literal("§2✔ Выбран для привата"));
                }
            }
            graphics.renderTooltip(this.font, tooltip, Optional.empty(), mouseX, mouseY);
        }

        // Заголовок
        graphics.drawCenteredString(this.font, "§6Карта территорий", this.width / 2,
            mapStartY - 15, 0xFFFFFF);
        graphics.drawCenteredString(this.font,
            "§7Выбрано: " + selectedKeys.size() + " чанков (ЛКМ — выбрать, ПКМ — убрать)",
            this.width / 2, mapStartY + mapHeight + 28, 0xAAAAAA);

        super.render(graphics, mouseX, mouseY, partialTick);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        int mapWidth = (mapRadius * 2 + 1) * cellSize;
        int mapHeight = (mapRadius * 2 + 1) * cellSize;

        if (mouseX >= mapStartX && mouseX < mapStartX + mapWidth &&
            mouseY >= mapStartY && mouseY < mapStartY + mapHeight) {

            int dx = (int)((mouseX - mapStartX) / cellSize) - mapRadius;
            int dz = (int)((mouseY - mapStartY) / cellSize) - mapRadius;
            int cx = data.getPlayerChunkX() + dx;
            int cz = data.getPlayerChunkZ() + dz;
            String key = cx + "," + cz;

            // Проверяем что чанк не занят
            boolean occupied = false;
            for (var e : data.getEntries()) {
                if (e.x == cx && e.z == cz) { occupied = true; break; }
            }

            if (!occupied) {
                if (button == 0) {
                    // ЛКМ — добавить
                    selectedKeys.add(key);
                } else if (button == 1) {
                    // ПКМ — убрать
                    selectedKeys.remove(key);
                }
            }
            return true;
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button,
                                 double dragX, double dragY) {
        // Позволяем выделять зажатием мыши
        int mapWidth = (mapRadius * 2 + 1) * cellSize;
        int mapHeight = (mapRadius * 2 + 1) * cellSize;

        if (mouseX >= mapStartX && mouseX < mapStartX + mapWidth &&
            mouseY >= mapStartY && mouseY < mapStartY + mapHeight) {

            int dx = (int)((mouseX - mapStartX) / cellSize) - mapRadius;
            int dz = (int)((mouseY - mapStartY) / cellSize) - mapRadius;
            int cx = data.getPlayerChunkX() + dx;
            int cz = data.getPlayerChunkZ() + dz;
            String key = cx + "," + cz;

            boolean occupied = false;
            for (var e : data.getEntries()) {
                if (e.x == cx && e.z == cz) { occupied = true; break; }
            }

            if (!occupied) {
                if (button == 0) selectedKeys.add(key);
                else if (button == 1) selectedKeys.remove(key);
            }
            return true;
        }
        return super.mouseDragged(mouseX, mouseY, button, dragX, dragY);
    }

    private void claimSelected() {
        if (selectedKeys.isEmpty()) return;

        List<int[]> chunks = new ArrayList<>();
        for (String key : selectedKeys) {
            String[] parts = key.split(",");
            chunks.add(new int[]{Integer.parseInt(parts[0]), Integer.parseInt(parts[1])});
        }

        NetworkHandler.sendToServer(new ClaimChunksPacket(chunks));
        selectedKeys.clear();
        this.onClose();
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}