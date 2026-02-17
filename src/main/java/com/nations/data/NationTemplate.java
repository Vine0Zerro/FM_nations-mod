package com.nations.data;

import java.util.*;

public class NationTemplate {

    private final String nationName;
    private final NationColor color;
    private final List<TownTemplate> towns;

    public NationTemplate(String nationName, NationColor color, List<TownTemplate> towns) {
        this.nationName = nationName;
        this.color = color;
        this.towns = towns;
    }

    public String getNationName() { return nationName; }
    public NationColor getColor() { return color; }
    public List<TownTemplate> getTowns() { return towns; }

    public int getTotalChunks() {
        return towns.stream().mapToInt(t -> t.chunksX * t.chunksZ).sum();
    }

    public static class TownTemplate {
        public final String name;
        public final int offsetX;
        public final int offsetZ;
        public final int chunksX;
        public final int chunksZ;

        public TownTemplate(String name, int offsetX, int offsetZ, int chunksX, int chunksZ) {
            this.name = name;
            this.offsetX = offsetX;
            this.offsetZ = offsetZ;
            this.chunksX = chunksX;
            this.chunksZ = chunksZ;
        }
    }

    private static final Map<String, NationTemplate> TEMPLATES = new HashMap<>();

    static {
        // === РОССИЙСКАЯ ФЕДЕРАЦИЯ === (98 чанков — крупная)
        // Москва(30) + СПб(16) + Новосибирск(12) + Екатеринбург(10) +
        // Казань(8) + Нижний Новгород(8) + Владивосток(8) + Краснодар(6)
        TEMPLATES.put("russia", new NationTemplate(
            "Российская Федерация", NationColor.RED, Arrays.asList(
                new TownTemplate("Москва",            0,  0,  6, 5),   // 30
                new TownTemplate("Санкт-Петербург",  -6,  0,  4, 4),   // 16
                new TownTemplate("Казань",            6,  0,  4, 2),   // 8
                new TownTemplate("Нижний Новгород",   0, -5,  4, 2),   // 8
                new TownTemplate("Екатеринбург",      6, -3,  5, 2),   // 10
                new TownTemplate("Краснодар",        -6,  4,  3, 2),   // 6
                new TownTemplate("Новосибирск",       6,  2,  4, 3),   // 12
                new TownTemplate("Владивосток",      10,  2,  4, 2)    // 8
            )
        ));

        // === США === (95 чанков — крупная)
        // Вашингтон(24) + Нью-Йорк(18) + Лос-Анджелес(15) +
        // Чикаго(10) + Хьюстон(10) + Майами(8) + Сиэтл(6) + Денвер(4)
        TEMPLATES.put("usa", new NationTemplate(
            "Соединённые Штаты Америки", NationColor.BLUE, Arrays.asList(
                new TownTemplate("Вашингтон",       0,  0,  6, 4),   // 24
                new TownTemplate("Нью-Йорк",        6,  0,  6, 3),   // 18
                new TownTemplate("Лос-Анджелес",    -6,  0,  5, 3),   // 15
                new TownTemplate("Чикаго",           0, -4,  5, 2),   // 10
                new TownTemplate("Хьюстон",          0,  4,  5, 2),   // 10
                new TownTemplate("Майами",           6,  3,  4, 2),   // 8
                new TownTemplate("Сиэтл",           -6, -3,  3, 2),   // 6
                new TownTemplate("Денвер",           -6,  3,  2, 2)    // 4
            )
        ));

        // === КИТАЙ === (92 чанка — крупная)
        // Пекин(25) + Шанхай(20) + Гуанчжоу(15) +
        // Чэнду(12) + Ухань(10) + Шэньчжэнь(10)
        TEMPLATES.put("china", new NationTemplate(
            "Китайская Народная Республика", NationColor.DARK_RED, Arrays.asList(
                new TownTemplate("Пекин",            0,  0,  5, 5),   // 25
                new TownTemplate("Шанхай",           5,  0,  5, 4),   // 20
                new TownTemplate("Гуанчжоу",         0,  5,  5, 3),   // 15
                new TownTemplate("Чэнду",           -5,  0,  4, 3),   // 12
                new TownTemplate("Ухань",             0, -5,  5, 2),   // 10
                new TownTemplate("Шэньчжэнь",        5,  5,  5, 2)    // 10
            )
        ));

        // === БРАЗИЛИЯ === (88 чанков — крупная)
        // Бразилиа(20) + Сан-Паулу(18) + Рио(15) +
        // Сальвадор(12) + Форталеза(10) + Манаус(8) + Куритиба(5)
        TEMPLATES.put("brazil", new NationTemplate(
            "Федеративная Республика Бразилия", NationColor.GREEN, Arrays.asList(
                new TownTemplate("Бразилиа",         0,  0,  5, 4),   // 20
                new TownTemplate("Сан-Паулу",        0,  4,  6, 3),   // 18
                new TownTemplate("Рио-де-Жанейро",   6,  4,  5, 3),   // 15
                new TownTemplate("Сальвадор",        5,  0,  4, 3),   // 12
                new TownTemplate("Форталеза",        5, -3,  5, 2),   // 10
                new TownTemplate("Манаус",          -5,  0,  4, 2),   // 8
                new TownTemplate("Куритиба",        -5,  4,  5, 1)    // 5
            )
        ));

        // === ИНДИЯ === (86 чанков — крупная)
        // Дели(20) + Мумбаи(16) + Бангалор(12) +
        // Ченнаи(12) + Калькутта(10) + Хайдарабад(8) + Джайпур(8)
        TEMPLATES.put("india", new NationTemplate(
            "Республика Индия", NationColor.TEAL, Arrays.asList(
                new TownTemplate("Дели",             0,  0,  5, 4),   // 20
                new TownTemplate("Мумбаи",          -5,  0,  4, 4),   // 16
                new TownTemplate("Бангалор",         0,  4,  4, 3),   // 12
                new TownTemplate("Ченнаи",           4,  4,  4, 3),   // 12
                new TownTemplate("Калькутта",        5,  0,  5, 2),   // 10
                new TownTemplate("Хайдарабад",      -5,  4,  4, 2),   // 8
                new TownTemplate("Джайпур",          0, -4,  4, 2)    // 8
            )
        ));

        // === ТУРЦИЯ === (76 чанков — средняя)
        // Анкара(20) + Стамбул(18) + Измир(12) +
        // Анталья(10) + Бурса(8) + Трабзон(8)
        TEMPLATES.put("turkey", new NationTemplate(
            "Турецкая Республика", NationColor.ORANGE, Arrays.asList(
                new TownTemplate("Анкара",           0,  0,  5, 4),   // 20
                new TownTemplate("Стамбул",         -5,  0,  6, 3),   // 18
                new TownTemplate("Измир",           -5,  3,  4, 3),   // 12
                new TownTemplate("Анталья",          0,  4,  5, 2),   // 10
                new TownTemplate("Бурса",           -5, -3,  4, 2),   // 8
                new TownTemplate("Трабзон",          5,  0,  4, 2)    // 8
            )
        ));

        // === ФРАНЦИЯ === (74 чанка — средняя)
        // Париж(20) + Марсель(12) + Лион(12) +
        // Тулуза(10) + Ницца(8) + Страсбург(6) + Бордо(6)
        TEMPLATES.put("france", new NationTemplate(
            "Французская Республика", NationColor.NAVY, Arrays.asList(
                new TownTemplate("Париж",            0,  0,  5, 4),   // 20
                new TownTemplate("Марсель",          0,  4,  4, 3),   // 12
                new TownTemplate("Лион",            -5,  0,  4, 3),   // 12
                new TownTemplate("Тулуза",          -5,  3,  5, 2),   // 10
                new TownTemplate("Ницца",            5,  4,  4, 2),   // 8
                new TownTemplate("Страсбург",        5,  0,  3, 2),   // 6
                new TownTemplate("Бордо",           -5, -3,  3, 2)    // 6
            )
        ));

        // === ГЕРМАНИЯ === (72 чанка — средняя)
        // Берлин(20) + Мюнхен(12) + Гамбург(12) +
        // Франкфурт(10) + Кёльн(8) + Дрезден(6) + Штутгарт(4)
        TEMPLATES.put("germany", new NationTemplate(
            "Федеративная Республика Германия", NationColor.GOLD, Arrays.asList(
                new TownTemplate("Берлин",           0,  0,  5, 4),   // 20
                new TownTemplate("Мюнхен",           0,  4,  4, 3),   // 12
                new TownTemplate("Гамбург",          0, -4,  4, 3),   // 12
                new TownTemplate("Франкфурт",       -5,  0,  5, 2),   // 10
                new TownTemplate("Кёльн",           -5, -2,  4, 2),   // 8
                new TownTemplate("Дрезден",          5,  0,  3, 2),   // 6
                new TownTemplate("Штутгарт",         5,  2,  2, 2)    // 4
            )
        ));

        // === ЯПОНИЯ === (70 чанков — средняя)
        // Токио(20) + Осака(15) + Нагоя(12) +
        // Саппоро(10) + Хиросима(7) + Фукуока(6)
        TEMPLATES.put("japan", new NationTemplate(
            "Японская Империя", NationColor.WHITE, Arrays.asList(
                new TownTemplate("Токио",            0,  0,  5, 4),   // 20
                new TownTemplate("Осака",            0,  4,  5, 3),   // 15
                new TownTemplate("Нагоя",           -5,  0,  4, 3),   // 12
                new TownTemplate("Саппоро",          0, -4,  5, 2),   // 10
                new TownTemplate("Фукуока",          5,  4,  3, 2),   // 6
                new TownTemplate("Хиросима",         5,  0,  7, 1)    // 7
            )
        ));

        // === ВЕЛИКОБРИТАНИЯ === (68 чанков — средняя)
        // Лондон(20) + Манчестер(12) + Бирмингем(10) +
        // Ливерпуль(8) + Эдинбург(8) + Глазго(6) + Бристоль(4)
        TEMPLATES.put("uk", new NationTemplate(
            "Соединённое Королевство", NationColor.PURPLE, Arrays.asList(
                new TownTemplate("Лондон",           0,  0,  5, 4),   // 20
                new TownTemplate("Манчестер",        0, -4,  4, 3),   // 12
                new TownTemplate("Бирмингем",       -5,  0,  5, 2),   // 10
                new TownTemplate("Ливерпуль",       -5, -4,  4, 2),   // 8
                new TownTemplate("Эдинбург",         0, -7,  4, 2),   // 8
                new TownTemplate("Глазго",          -4, -7,  3, 2),   // 6
                new TownTemplate("Бристоль",         5,  0,  2, 2)    // 4
            )
        ));

        // === РУМЫНИЯ === (46 чанков — малая)
        // Бухарест(16) + Клуж-Напока(8) + Тимишоара(6) +
        // Яссы(6) + Констанца(4) + Брашов(4) + Крайова(2)
        TEMPLATES.put("romania", new NationTemplate(
            "Румыния", NationColor.YELLOW, Arrays.asList(
                new TownTemplate("Бухарест",         0,  0,  4, 4),   // 16
                new TownTemplate("Клуж-Напока",     -4,  0,  4, 2),   // 8
                new TownTemplate("Тимишоара",       -4, -2,  3, 2),   // 6
                new TownTemplate("Яссы",             4,  0,  3, 2),   // 6
                new TownTemplate("Констанца",        4,  2,  2, 2),   // 4
                new TownTemplate("Брашов",           0, -4,  2, 2),   // 4
                new TownTemplate("Крайова",          0,  4,  2, 1)    // 2
            )
        ));
    }

    public static NationTemplate getTemplate(String name) {
        return TEMPLATES.get(name.toLowerCase());
    }

    public static Set<String> getAvailableTemplates() {
        return TEMPLATES.keySet();
    }
}
