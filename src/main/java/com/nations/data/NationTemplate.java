package com.nations.data;

import java.util.*;

public class NationTemplate {

    private final String nationName;
    private final NationColor color;
    private final List<TownTemplate> towns;

    public NationTemplate(String nationName, NationColor color, List<TownTemplate> towns) {
        this.nationName = nationName;
        this.color = color;
        this.towns = towns; // Дубликаты убираются внутри TownTemplate при создании
    }

    public String getNationName() { return nationName; }
    public NationColor getColor() { return color; }
    public List<TownTemplate> getTowns() { return towns; }

    public int getTotalChunks() {
        int total = 0;
        for (TownTemplate t : towns) total += t.getChunkCount();
        return total;
    }

    public static class TownTemplate {
        public final String name;
        public final List<int[]> chunks; // Список чанков [x, z]

        // Конструктор принимает "сырые" данные прямоугольников {x, z, w, h}
        // Это спасает компилятор от зависания
        public TownTemplate(String name, int[]... rects) {
            this.name = name;
            this.chunks = new ArrayList<>();
            Set<String> unique = new HashSet<>();

            for (int[] r : rects) {
                if (r.length == 4) { // Это прямоугольник x, z, w, h
                    int sx = r[0], sz = r[1], w = r[2], h = r[3];
                    for (int x = sx; x < sx + w; x++) {
                        for (int z = sz; z < sz + h; z++) {
                            String key = x + "," + z;
                            if (unique.add(key)) {
                                chunks.add(new int[]{x, z});
                            }
                        }
                    }
                } else if (r.length == 2) { // Это отдельный чанк x, z
                     String key = r[0] + "," + r[1];
                     if (unique.add(key)) {
                         chunks.add(r);
                     }
                }
            }
        }

        public int getChunkCount() { return chunks.size(); }

        public int[] getCenter() {
            if (chunks.isEmpty()) return new int[]{0, 0};
            long sx = 0, sz = 0;
            for (int[] c : chunks) { sx += c[0]; sz += c[1]; }
            return new int[]{(int)(sx / chunks.size()), (int)(sz / chunks.size())};
        }
    }

    // Легкий метод для записи данных
    private static int[] r(int x, int z, int w, int h) {
        return new int[]{x, z, w, h};
    }

    private static final Map<String, NationTemplate> TEMPLATES = new HashMap<>();

    static {
        // РОССИЯ
        TEMPLATES.put("russia", new NationTemplate("Российская Федерация", NationColor.RED, Arrays.asList(
            new TownTemplate("Москва", 
                r(0, -1, 7, 7), r(-1, 0, 1, 1), r(-1, 1, 1, 1), r(-1, 2, 1, 1), r(-1, 3, 1, 1),
                r(7, 1, 1, 1), r(7, 2, 1, 1), r(7, 3, 1, 1), r(1, -2, 5, 1), r(2, 6, 4, 1)),
            new TownTemplate("Санкт-Петербург", 
                r(-6, -4, 5, 6), r(-6, 2, 4, 1), r(-7, -2, 1, 1), r(-7, -1, 1, 1), r(-7, 0, 1, 1),
                r(-1, -4, 1, 3), r(-5, -5, 3, 1), r(-4, -6, 2, 1)),
            new TownTemplate("Краснодар", 
                r(-3, 6, 7, 3), r(-4, 7, 1, 1), r(-4, 8, 1, 1), r(4, 6, 2, 1), r(4, 7, 2, 1),
                r(-2, 9, 5, 1), r(-1, 10, 3, 1)),
            new TownTemplate("Нижний Новгород", 
                r(8, -1, 5, 6), r(8, -2, 4, 1), r(8, 5, 3, 1), r(13, 0, 1, 4)),
            new TownTemplate("Казань", 
                r(14, -1, 5, 5), r(14, -2, 4, 1), r(14, 4, 3, 1), r(19, 0, 1, 3)),
            new TownTemplate("Екатеринбург", 
                r(20, -2, 5, 6), r(20, -3, 4, 1), r(20, 4, 3, 1), r(25, -1, 1, 4)),
            new TownTemplate("Новосибирск", 
                r(26, -3, 6, 7), r(26, -4, 5, 1), r(32, -1, 1, 4)),
            new TownTemplate("Красноярск", 
                r(33, -3, 5, 7), r(33, -4, 4, 1), r(33, 4, 3, 1), r(38, -2, 1, 5)),
            new TownTemplate("Якутск", 
                r(33, -10, 6, 6), r(33, -11, 4, 1), r(39, -9, 1, 3)),
            new TownTemplate("Владивосток", 
                r(39, -2, 5, 5), r(39, -3, 4, 1), r(39, 3, 3, 1), r(44, -1, 1, 3)),
            new TownTemplate("Самара", 
                r(10, 5, 4, 4), r(9, 6, 1, 1), r(9, 7, 1, 1), r(14, 5, 2, 1), r(14, 6, 2, 1), r(11, 9, 3, 1)),
            new TownTemplate("Мурманск",
                r(-4, -8, 5, 5), r(-5, -7, 1, 3), r(-3, -3, 4, 1))
        )));

        // США
        TEMPLATES.put("usa", new NationTemplate("Соединённые Штаты Америки", NationColor.BLUE, Arrays.asList(
            new TownTemplate("Вашингтон", 
                r(0, 0, 6, 5), r(0, -1, 5, 1), r(6, 1, 2, 1), r(-1, 1, 1, 3)),
            new TownTemplate("Нью-Йорк", 
                r(8, -2, 5, 6), r(8, -3, 4, 1), r(13, -1, 1, 4), r(7, 0, 1, 3)),
            new TownTemplate("Чикаго", 
                r(-1, -8, 7, 5), r(0, -3, 5, 1), r(-2, -7, 1, 1), r(6, -7, 1, 3)),
            new TownTemplate("Лос-Анджелес", 
                r(-15, -5, 6, 8), r(-16, -3, 1, 1), r(-16, -2, 1, 1), r(-16, -1, 1, 1),
                r(-9, -4, 1, 1), r(-9, -3, 1, 1), r(-9, -2, 1, 1), r(-9, -1, 1, 1), r(-14, 3, 4, 1)),
            new TownTemplate("Денвер", 
                r(-9, -8, 6, 8), r(-3, -8, 2, 1), r(-10, -6, 1, 1), r(-10, -5, 1, 1)),
            new TownTemplate("Хьюстон", 
                r(-6, 3, 7, 5), r(-7, 4, 1, 1), r(-7, 5, 1, 1), r(1, 3, 2, 1), r(1, 4, 2, 1), r(-5, 8, 5, 1)),
            new TownTemplate("Майами", 
                r(3, 5, 5, 4), r(3, 4, 5, 1), r(5, 9, 3, 1), r(6, 10, 2, 1), r(6, 11, 2, 1), r(7, 12, 1, 1)),
            new TownTemplate("Сиэтл", 
                r(-15, -13, 5, 8), r(-16, -11, 1, 1), r(-16, -10, 1, 1), r(-10, -12, 1, 1), r(-10, -11, 1, 1), r(-10, -10, 1, 1)),
            new TownTemplate("Миннеаполис", 
                r(-3, -13, 7, 5), r(-4, -12, 1, 1), r(-4, -11, 1, 1), r(4, -12, 2, 1), r(4, -11, 2, 1))
        )));

        // КИТАЙ
        TEMPLATES.put("china", new NationTemplate("Китайская Народная Республика", NationColor.DARK_RED, Arrays.asList(
            new TownTemplate("Пекин", 
                r(0, 0, 6, 5), r(0, -1, 5, 1), r(-1, 1, 1, 3), r(6, 1, 1, 3)),
            new TownTemplate("Шанхай", 
                r(7, 1, 5, 5), r(12, 2, 1, 3), r(7, 0, 4, 1)),
            new TownTemplate("Гуанчжоу", 
                r(4, 6, 5, 5), r(4, 5, 4, 1), r(9, 7, 1, 1), r(9, 8, 1, 1), r(5, 11, 3, 1)),
            new TownTemplate("Шэньчжэнь", 
                r(6, 11, 5, 4), r(6, 15, 3, 1), r(11, 12, 1, 1)),
            new TownTemplate("Чэнду", 
                r(-7, 1, 6, 5), r(-8, 2, 1, 3), r(-6, 0, 4, 1), r(-6, 6, 3, 1)),
            new TownTemplate("Ухань", 
                r(-1, 5, 5, 5), r(-2, 6, 1, 3), r(0, 10, 3, 1)),
            new TownTemplate("Харбин", 
                r(1, -7, 6, 6), r(0, -6, 1, 1), r(7, -6, 1, 1), r(2, -8, 4, 1), r(3, -1, 1, 1)),
            new TownTemplate("Урумчи", 
                r(-16, -3, 7, 7), r(-17, -1, 1, 1), r(-17, 0, 1, 1), r(-17, 1, 1, 1), 
                r(-9, -2, 1, 1), r(-9, -1, 1, 1), r(-9, 0, 1, 1)),
            new TownTemplate("Лхаса", 
                r(-14, 4, 6, 5), r(-15, 5, 1, 1), r(-15, 6, 1, 1), r(-8, 5, 1, 1), r(-8, 6, 1, 1)),
            new TownTemplate("Куньмин", 
                r(-7, 6, 5, 5), r(-8, 7, 1, 3), r(-6, 11, 3, 1))
        )));

        // ГЕРМАНИЯ
        TEMPLATES.put("germany", new NationTemplate("Федеративная Республика Германия", NationColor.GOLD, Arrays.asList(
            new TownTemplate("Берлин", r(0, 0, 6, 5), r(-1, 1, 1, 3), r(1, -1, 4, 1)),
            new TownTemplate("Гамбург", r(-1, -7, 8, 6), r(-2, -6, 1, 1), r(-2, -5, 1, 1), r(7, -6, 1, 1), r(7, -5, 1, 1)),
            new TownTemplate("Мюнхен", r(-1, 5, 8, 5), r(-2, 6, 1, 1), r(-2, 7, 1, 1), r(7, 6, 1, 1), r(7, 7, 1, 1), r(0, 10, 6, 1)),
            new TownTemplate("Франкфурт", r(-7, 0, 6, 5), r(-8, 1, 1, 3), r(-6, -1, 4, 1)),
            new TownTemplate("Кёльн", r(-7, -7, 6, 6), r(-8, -5, 1, 1), r(-8, -4, 1, 1)),
            new TownTemplate("Штутгарт", r(-7, 5, 6, 5), r(-8, 6, 1, 1), r(-8, 7, 1, 1)),
            new TownTemplate("Дрезден", r(6, -1, 4, 6), r(6, -2, 3, 1))
        )));

        // ФРАНЦИЯ
        TEMPLATES.put("france", new NationTemplate("Французская Республика", NationColor.NAVY, Arrays.asList(
            new TownTemplate("Париж", r(0, 0, 6, 5), r(-1, 1, 1, 3), r(6, 1, 1, 3), r(1, -1, 4, 1)),
            new TownTemplate("Лион", r(4, 5, 5, 5), r(3, 6, 1, 1), r(3, 7, 1, 1), r(9, 6, 1, 1), r(9, 7, 1, 1)),
            new TownTemplate("Марсель", r(4, 10, 6, 5), r(3, 11, 1, 1), r(3, 12, 1, 1), r(10, 11, 1, 1), r(5, 15, 4, 1)),
            new TownTemplate("Тулуза", r(-4, 10, 6, 5), r(-5, 11, 1, 1), r(-5, 12, 1, 1), r(2, 10, 2, 1)),
            new TownTemplate("Бордо", r(-7, 5, 5, 5), r(-8, 6, 1, 1), r(-8, 7, 1, 1), r(-2, 5, 2, 1)),
            new TownTemplate("Страсбург", r(7, 0, 4, 5), r(6, 1, 1, 1), r(6, 2, 1, 1), r(6, 3, 1, 1)),
            new TownTemplate("Лилль", r(0, -6, 6, 5), r(-1, -5, 1, 1), r(-1, -4, 1, 1), r(6, -5, 1, 1), r(6, -4, 1, 1)),
            new TownTemplate("Нант", r(-7, 0, 5, 5), r(-8, 1, 1, 1), r(-8, 2, 1, 1), r(-2, 1, 2, 1))
        )));

        // ЯПОНИЯ
        TEMPLATES.put("japan", new NationTemplate("Японская Империя", NationColor.WHITE, Arrays.asList(
            new TownTemplate("Токио", r(0, 0, 4, 6), r(-1, 1, 1, 4), r(4, 1, 1, 4)),
            new TownTemplate("Осака", r(-2, 6, 5, 5), r(-3, 7, 1, 1), r(-3, 8, 1, 1), r(3, 7, 1, 1), r(3, 8, 1, 1)),
            new TownTemplate("Нагоя", r(3, 3, 4, 5), r(2, 4, 1, 1), r(2, 5, 1, 1)),
            new TownTemplate("Саппоро", r(2, -8, 5, 6), r(1, -7, 1, 1), r(1, -6, 1, 1), r(7, -7, 1, 1), r(7, -6, 1, 1), r(3, -2, 3, 1)),
            new TownTemplate("Фукуока", r(-5, 11, 5, 5), r(-6, 12, 1, 1), r(-6, 13, 1, 1), r(0, 11, 1, 1), r(0, 12, 1, 1)),
            new TownTemplate("Хиросима", r(-4, 6, 2, 5), r(-6, 7, 2, 1), r(-6, 8, 2, 1), r(-6, 9, 2, 1)),
            new TownTemplate("Сэндай", r(0, -4, 4, 4), r(-1, -3, 1, 1), r(-1, -2, 1, 1), r(4, -3, 1, 1))
        )));

        // ВЕЛИКОБРИТАНИЯ
        TEMPLATES.put("uk", new NationTemplate("Соединённое Королевство", NationColor.PURPLE, Arrays.asList(
            new TownTemplate("Лондон", r(0, 0, 5, 5), r(-1, 1, 1, 3), r(5, 1, 1, 3), r(1, -1, 3, 1)),
            new TownTemplate("Бирмингем", r(-1, -6, 6, 5), r(-2, -5, 1, 1), r(-2, -4, 1, 1), r(5, -5, 1, 1), r(5, -4, 1, 1), r(0, -1, 4, 1)),
            new TownTemplate("Манчестер", r(-1, -11, 5, 5), r(-2, -10, 1, 1), r(-2, -9, 1, 1), r(4, -10, 1, 1), r(4, -9, 1, 1)),
            new TownTemplate("Ливерпуль", r(-5, -11, 4, 5), r(-6, -10, 1, 1), r(-6, -9, 1, 1)),
            new TownTemplate("Эдинбург", r(-2, -17, 5, 5), r(-3, -16, 1, 1), r(-3, -15, 1, 1), r(3, -16, 1, 1), r(3, -15, 1, 1), r(-1, -12, 4, 1)),
            new TownTemplate("Глазго", r(-6, -17, 4, 5), r(-7, -16, 1, 1), r(-7, -15, 1, 1)),
            new TownTemplate("Бристоль", r(-4, -2, 4, 5), r(-5, -1, 1, 1), r(-5, 0, 1, 1)),
            new TownTemplate("Кардифф", r(-6, 0, 5, 4), r(-7, 1, 1, 1), r(-7, 2, 1, 1))
        )));

        // РУМЫНИЯ
        TEMPLATES.put("romania", new NationTemplate("Румыния", NationColor.YELLOW, Arrays.asList(
            new TownTemplate("Бухарест", r(0, 0, 5, 5), r(-1, 1, 1, 3), r(5, 1, 1, 3)),
            new TownTemplate("Клуж-Напока", r(-6, -5, 5, 5), r(-7, -4, 1, 1), r(-7, -3, 1, 1), r(-1, -4, 1, 1), r(-1, -3, 1, 1)),
            new TownTemplate("Тимишоара", r(-8, 0, 5, 5), r(-9, 1, 1, 1), r(-9, 2, 1, 1), r(-3, 1, 2, 1)),
            new TownTemplate("Яссы", r(3, -6, 5, 5), r(2, -5, 1, 1), r(2, -4, 1, 1), r(8, -5, 1, 1), r(8, -4, 1, 1), r(3, -1, 3, 1)),
            new TownTemplate("Констанца", r(5, 1, 4, 5), r(4, 2, 1, 1), r(4, 3, 1, 1), r(5, 0, 3, 1)),
            new TownTemplate("Брашов", r(-3, -2, 3, 5), r(-4, -1, 1, 1), r(-4, 0, 1, 1)),
            new TownTemplate("Крайова", r(-3, 5, 5, 4), r(-4, 6, 1, 1), r(-4, 7, 1, 1), r(2, 5, 1, 1))
        )));

        // ТУРЦИЯ
        TEMPLATES.put("turkey", new NationTemplate("Турецкая Республика", NationColor.ORANGE, Arrays.asList(
            new TownTemplate("Анкара", r(0, 0, 6, 5), r(-1, 1, 1, 3), r(1, -1, 4, 1)),
            new TownTemplate("Стамбул", r(-8, 0, 7, 5), r(-9, 1, 1, 1), r(-9, 2, 1, 1), r(-9, 3, 1, 1)),
            new TownTemplate("Измир", r(-9, 5, 5, 5), r(-10, 6, 1, 1), r(-10, 7, 1, 1)),
            new TownTemplate("Анталья", r(-4, 5, 6, 5), r(-5, 6, 1, 1), r(-5, 7, 1, 1), r(2, 6, 1, 1)),
            new TownTemplate("Адана", r(2, 5, 6, 5), r(1, 6, 1, 1), r(1, 7, 1, 1), r(8, 6, 1, 1)),
            new TownTemplate("Трабзон", r(6, 0, 6, 5), r(12, 1, 1, 1), r(12, 2, 1, 1), r(12, 3, 1, 1)),
            new TownTemplate("Газиантеп", r(8, 5, 5, 5), r(7, 6, 1, 1), r(7, 7, 1, 1), r(13, 6, 1, 1))
        )));

        // БРАЗИЛИЯ
        TEMPLATES.put("brazil", new NationTemplate("Федеративная Республика Бразилия", NationColor.GREEN, Arrays.asList(
            new TownTemplate("Бразилиа", r(0, 0, 6, 5), r(-1, 1, 1, 3), r(6, 1, 1, 3)),
            new TownTemplate("Сан-Паулу", r(0, 5, 7, 6), r(-1, 6, 1, 4), r(7, 6, 1, 4), r(1, 11, 5, 1)),
            new TownTemplate("Рио-де-Жанейро", r(7, 2, 5, 6), r(6, 3, 1, 1), r(6, 4, 1, 1), r(12, 3, 1, 1), r(12, 4, 1, 1)),
            new TownTemplate("Манаус", r(-8, -7, 7, 7), r(-9, -5, 1, 1), r(-9, -4, 1, 1), r(-1, -6, 1, 1), r(-1, -5, 1, 1)),
            new TownTemplate("Форталеза", r(4, -7, 6, 5), r(3, -6, 1, 1), r(3, -5, 1, 1), r(10, -6, 1, 1), r(10, -5, 1, 1)),
            new TownTemplate("Сальвадор", r(8, -2, 5, 5), r(7, -1, 1, 1), r(7, 0, 1, 1), r(13, -1, 1, 1)),
            new TownTemplate("Белу-Оризонти", r(0, -3, 4, 3), r(4, -2, 1, 1)),
            new TownTemplate("Куритиба", r(-3, 5, 3, 5), r(-4, 6, 1, 1), r(-4, 7, 1, 1)),
            new TownTemplate("Порту-Алегри", r(-3, 10, 5, 5), r(-4, 11, 1, 1), r(-4, 12, 1, 1), r(2, 11, 1, 1))
        )));

        // ИНДИЯ
        TEMPLATES.put("india", new NationTemplate("Республика Индия", NationColor.TEAL, Arrays.asList(
            new TownTemplate("Дели", r(0, 0, 6, 5), r(-1, 1, 1, 3), r(1, -1, 4, 1), r(6, 1, 1, 3)),
            new TownTemplate("Мумбаи", r(-6, 5, 5, 6), r(-7, 6, 1, 4), r(-5, 4, 3, 1), r(-1, 6, 1, 4)),
            new TownTemplate("Бангалор", r(-1, 11, 5, 6), r(-2, 12, 1, 4), r(4, 12, 1, 4), r(0, 17, 3, 1)),
            new TownTemplate("Ченнаи", r(4, 8, 5, 6), r(3, 9, 1, 1), r(3, 10, 1, 1), r(9, 9, 1, 1), r(9, 10, 1, 1)),
            new TownTemplate("Калькутта", r(7, 0, 5, 5), r(6, 1, 1, 1), r(6, 2, 1, 1), r(12, 1, 1, 1), r(12, 2, 1, 1)),
            new TownTemplate("Хайдарабад", r(0, 5, 5, 6), r(-1, 6, 1, 1), r(-1, 7, 1, 1), r(5, 6, 2, 1)),
            new TownTemplate("Джайпур", r(-6, -2, 5, 5), r(-7, -1, 1, 1), r(-7, 0, 1, 1), r(-1, -1, 1, 3)),
            new TownTemplate("Лакхнау", r(0, -5, 5, 4), r(-1, -4, 1, 1), r(-1, -3, 1, 1), r(5, -4, 1, 1), r(5, -3, 1, 1))
        )));
    }
}
