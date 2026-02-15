package com.nations.data;

public enum NationColor {
    RED("red", 0xFF0000, "Красный"),
    BLUE("blue", 0x0000FF, "Синий"),
    GREEN("green", 0x00FF00, "Зелёный"),
    YELLOW("yellow", 0xFFFF00, "Жёлтый"),
    PURPLE("purple", 0x800080, "Фиолетовый"),
    ORANGE("orange", 0xFFA500, "Оранжевый"),
    CYAN("cyan", 0x00FFFF, "Голубой"),
    PINK("pink", 0xFF69B4, "Розовый"),
    WHITE("white", 0xFFFFFF, "Белый"),
    DARK_GREEN("dark_green", 0x006400, "Тёмно-зелёный"),
    DARK_RED("dark_red", 0x8B0000, "Тёмно-красный"),
    GOLD("gold", 0xFFD700, "Золотой"),
    LIME("lime", 0x32CD32, "Лаймовый"),
    BROWN("brown", 0x8B4513, "Коричневый"),
    NAVY("navy", 0x000080, "Тёмно-синий"),
    TEAL("teal", 0x008080, "Бирюзовый");

    private final String id;
    private final int hex;
    private final String displayName;

    NationColor(String id, int hex, String displayName) {
        this.id = id;
        this.hex = hex;
        this.displayName = displayName;
    }

    public String getId() { return id; }
    public int getHex() { return hex; }
    public String getDisplayName() { return displayName; }

    public static NationColor fromId(String id) {
        for (NationColor c : values()) {
            if (c.id.equalsIgnoreCase(id)) return c;
        }
        return null;
    }
}