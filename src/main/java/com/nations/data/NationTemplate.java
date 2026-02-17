package com.nations.data;

import java.util.*;

public class NationTemplate {

    private final String nationName;
    private final NationColor color;
    private final List<TownTemplate> towns;

    public NationTemplate(String nationName, NationColor color, List<TownTemplate> towns) {
        this.nationName = nationName;
        this.color = color;
        this.towns = removeDuplicateChunks(towns);
    }

    public String getNationName() { return nationName; }
    public NationColor getColor() { return color; }
    public List<TownTemplate> getTowns() { return towns; }

    public int getTotalChunks() {
        int total = 0;
        for (TownTemplate t : towns) total += t.getChunkCount();
        return total;
    }

    private static List<TownTemplate> removeDuplicateChunks(List<TownTemplate> input) {
        Set<String> used = new HashSet<>();
        List<TownTemplate> result = new ArrayList<>();
        for (TownTemplate tt : input) {
            List<int[]> clean = new ArrayList<>();
            for (int[] c : tt.chunks) {
                String key = c[0] + "," + c[1];
                if (used.add(key)) {
                    clean.add(c);
                }
            }
            if (!clean.isEmpty()) result.add(new TownTemplate(tt.name, clean));
        }
        return result;
    }

    public static class TownTemplate {
        public final String name;
        public final List<int[]> chunks;

        public TownTemplate(String name, List<int[]> chunks) {
            this.name = name;
            this.chunks = chunks;
        }

        public int getChunkCount() { return chunks.size(); }

        public int[] getCenter() {
            if (chunks.isEmpty()) return new int[]{0, 0};
            long sx = 0, sz = 0;
            for (int[] c : chunks) { sx += c[0]; sz += c[1]; }
            return new int[]{(int)(sx / chunks.size()), (int)(sz / chunks.size())};
        }
    }

    private static List<int[]> rect(int sx, int sz, int w, int h) {
        List<int[]> r = new ArrayList<>();
        for (int x = sx; x < sx + w; x++)
            for (int z = sz; z < sz + h; z++) r.add(new int[]{x, z});
        return r;
    }

    private static List<int[]> hline(int sx, int z, int len) {
        List<int[]> r = new ArrayList<>();
        for (int x = sx; x < sx + len; x++) r.add(new int[]{x, z});
        return r;
    }

    private static List<int[]> vline(int x, int sz, int len) {
        List<int[]> r = new ArrayList<>();
        for (int z = sz; z < sz + len; z++) r.add(new int[]{x, z});
        return r;
    }

    @SafeVarargs
    private static List<int[]> merge(List<int[]>... lists) {
        List<int[]> r = new ArrayList<>();
        for (List<int[]> l : lists) r.addAll(l);
        return r;
    }

    private static final Map<String, NationTemplate> TEMPLATES = new HashMap<>();

    static {
        // РОССИЯ
        TEMPLATES.put("russia", new NationTemplate("Российская Федерация", NationColor.RED, Arrays.asList(
            new TownTemplate("Москва", merge(rect(0, -1, 7, 7), hline(-1, 0, 1), hline(-1, 1, 1), hline(-1, 2, 1), hline(-1, 3, 1), hline(7, 1, 1), hline(7, 2, 1), hline(7, 3, 1), hline(1, -2, 5), hline(2, 6, 4))),
            new TownTemplate("Санкт-Петербург", merge(rect(-6, -4, 5, 6), hline(-6, 2, 4), hline(-7, -2, 1), hline(-7, -1, 1), hline(-7, 0, 1), vline(-1, -4, 3), hline(-5, -5, 3), hline(-4, -6, 2))),
            new TownTemplate("Краснодар", merge(rect(-3, 6, 7, 3), hline(-4, 7, 1), hline(-4, 8, 1), hline(4, 6, 2), hline(4, 7, 2), hline(-2, 9, 5), hline(-1, 10, 3))),
            new TownTemplate("Нижний Новгород", merge(rect(8, -1, 5, 6), hline(8, -2, 4), hline(8, 5, 3), vline(13, 0, 4))),
            new TownTemplate("Казань", merge(rect(14, -1, 5, 5), hline(14, -2, 4), hline(14, 4, 3), vline(19, 0, 3))),
            new TownTemplate("Екатеринбург", merge(rect(20, -2, 5, 6), hline(20, -3, 4), hline(20, 4, 3), vline(25, -1, 4))),
            new TownTemplate("Новосибирск", merge(rect(26, -3, 6, 7), hline(26, -4, 5), vline(32, -1, 4))),
            new TownTemplate("Красноярск", merge(rect(33, -3, 5, 7), hline(33, -4, 4), hline(33, 4, 3), vline(38, -2, 5))),
            new TownTemplate("Якутск", merge(rect(33, -10, 6, 6), hline(33, -11, 4), vline(39, -9, 3))),
            new TownTemplate("Владивосток", merge(rect(39, -2, 5, 5), hline(39, -3, 4), hline(39, 3, 3), vline(44, -1, 3))),
            new TownTemplate("Самара", merge(rect(10, 5, 4, 4), hline(9, 6, 1), hline(9, 7, 1), hline(14, 5, 2), hline(14, 6, 2), hline(11, 9, 3))),
            new TownTemplate("Мурманск", merge(rect(-4, -8, 5, 5), rect(-5, -7, 1, 3), rect(-3, -3, 4, 1)))
        )));

        // США
        TEMPLATES.put("usa", new NationTemplate("Соединённые Штаты Америки", NationColor.BLUE, Arrays.asList(
            new TownTemplate("Вашингтон", merge(rect(0, 0, 6, 5), hline(0, -1, 5), hline(6, 1, 2), vline(-1, 1, 3))),
            new TownTemplate("Нью-Йорк", merge(rect(8, -2, 5, 6), hline(8, -3, 4), vline(13, -1, 4), vline(7, 0, 3))),
            new TownTemplate("Чикаго", merge(rect(-1, -8, 7, 5), hline(0, -3, 5), hline(-2, -7, 1), vline(6, -7, 3))),
            new TownTemplate("Лос-Анджелес", merge(rect(-15, -5, 6, 8), hline(-16, -3, 1), hline(-16, -2, 1), hline(-16, -1, 1), hline(-9, -4, 1), hline(-9, -3, 1), hline(-9, -2, 1), hline(-9, -1, 1), hline(-14, 3, 4))),
            new TownTemplate("Денвер", merge(rect(-9, -8, 6, 8), hline(-3, -8, 2), hline(-10, -6, 1), hline(-10, -5, 1))),
            new TownTemplate("Хьюстон", merge(rect(-6, 3, 7, 5), hline(-7, 4, 1), hline(-7, 5, 1), hline(1, 3, 2), hline(1, 4, 2), hline(-5, 8, 5))),
            new TownTemplate("Майами", merge(rect(3, 5, 5, 4), hline(3, 4, 5), hline(5, 9, 3), hline(6, 10, 2), hline(6, 11, 2), hline(7, 12, 1))),
            new TownTemplate("Сиэтл", merge(rect(-15, -13, 5, 8), hline(-16, -11, 1), hline(-16, -10, 1), hline(-10, -12, 1), hline(-10, -11, 1), hline(-10, -10, 1))),
            new TownTemplate("Миннеаполис", merge(rect(-3, -13, 7, 5), hline(-4, -12, 1), hline(-4, -11, 1), hline(4, -12, 2), hline(4, -11, 2)))
        )));

        // КИТАЙ
        TEMPLATES.put("china", new NationTemplate("Китайская Народная Республика", NationColor.DARK_RED, Arrays.asList(
            new TownTemplate("Пекин", merge(rect(0, 0, 6, 5), hline(0, -1, 5), vline(-1, 1, 3), vline(6, 1, 3))),
            new TownTemplate("Шанхай", merge(rect(7, 1, 5, 5), vline(12, 2, 3), hline(7, 0, 4))),
            new TownTemplate("Гуанчжоу", merge(rect(4, 6, 5, 5), hline(4, 5, 4), hline(9, 7, 1), hline(9, 8, 1), hline(5, 11, 3))),
            new TownTemplate("Шэньчжэнь", merge(rect(6, 11, 5, 4), hline(6, 15, 3), hline(11, 12, 1))),
            new TownTemplate("Чэнду", merge(rect(-7, 1, 6, 5), vline(-8, 2, 3), hline(-6, 0, 4), hline(-6, 6, 3))),
            new TownTemplate("Ухань", merge(rect(-1, 5, 5, 5), vline(-2, 6, 3), hline(0, 10, 3))),
            new TownTemplate("Харбин", merge(rect(1, -7, 6, 6), hline(0, -6, 1), hline(7, -6, 1), hline(2, -8, 4), vline(3, -1, 1))),
            new TownTemplate("Урумчи", merge(rect(-16, -3, 7, 7), hline(-17, -1, 1), hline(-17, 0, 1), hline(-17, 1, 1), hline(-9, -2, 1), hline(-9, -1, 1), hline(-9, 0, 1))),
            new TownTemplate("Лхаса", merge(rect(-14, 4, 6, 5), hline(-15, 5, 1), hline(-15, 6, 1), hline(-8, 5, 1), hline(-8, 6, 1))),
            new TownTemplate("Куньмин", merge(rect(-7, 6, 5, 5), vline(-8, 7, 3), hline(-6, 11, 3)))
        )));

        // ГЕРМАНИЯ
        TEMPLATES.put("germany", new NationTemplate("Федеративная Республика Германия", NationColor.GOLD, Arrays.asList(
            new TownTemplate("Берлин", merge(rect(0, 0, 6, 5), vline(-1, 1, 3), hline(1, -1, 4))),
            new TownTemplate("Гамбург", merge(rect(-1, -7, 8, 6), hline(-2, -6, 1), hline(-2, -5, 1), hline(7, -6, 1), hline(7, -5, 1))),
            new TownTemplate("Мюнхен", merge(rect(-1, 5, 8, 5), hline(-2, 6, 1), hline(-2, 7, 1), hline(7, 6, 1), hline(7, 7, 1), hline(0, 10, 6))),
            new TownTemplate("Франкфурт", merge(rect(-7, 0, 6, 5), vline(-8, 1, 3), hline(-6, -1, 4))),
            new TownTemplate("Кёльн", merge(rect(-7, -7, 6, 6), hline(-8, -5, 1), hline(-8, -4, 1))),
            new TownTemplate("Штутгарт", merge(rect(-7, 5, 6, 5), hline(-8, 6, 1), hline(-8, 7, 1))),
            new TownTemplate("Дрезден", merge(rect(6, -1, 4, 6), hline(6, -2, 3)))
        )));

        // ФРАНЦИЯ
        TEMPLATES.put("france", new NationTemplate("Французская Республика", NationColor.NAVY, Arrays.asList(
            new TownTemplate("Париж", merge(rect(0, 0, 6, 5), vline(-1, 1, 3), vline(6, 1, 3), hline(1, -1, 4))),
            new TownTemplate("Лион", merge(rect(4, 5, 5, 5), hline(3, 6, 1), hline(3, 7, 1), hline(9, 6, 1), hline(9, 7, 1))),
            new TownTemplate("Марсель", merge(rect(4, 10, 6, 5), hline(3, 11, 1), hline(3, 12, 1), hline(10, 11, 1), hline(5, 15, 4))),
            new TownTemplate("Тулуза", merge(rect(-4, 10, 6, 5), hline(-5, 11, 1), hline(-5, 12, 1), hline(2, 10, 2))),
            new TownTemplate("Бордо", merge(rect(-7, 5, 5, 5), hline(-8, 6, 1), hline(-8, 7, 1), hline(-2, 5, 2))),
            new TownTemplate("Страсбург", merge(rect(7, 0, 4, 5), hline(6, 1, 1), hline(6, 2, 1), hline(6, 3, 1))),
            new TownTemplate("Лилль", merge(rect(0, -6, 6, 5), hline(-1, -5, 1), hline(-1, -4, 1), hline(6, -5, 1), hline(6, -4, 1))),
            new TownTemplate("Нант", merge(rect(-7, 0, 5, 5), hline(-8, 1, 1), hline(-8, 2, 1), hline(-2, 1, 2)))
        )));

        // ЯПОНИЯ
        TEMPLATES.put("japan", new NationTemplate("Японская Империя", NationColor.WHITE, Arrays.asList(
            new TownTemplate("Токио", merge(rect(0, 0, 4, 6), vline(-1, 1, 4), vline(4, 1, 4))),
            new TownTemplate("Осака", merge(rect(-2, 6, 5, 5), hline(-3, 7, 1), hline(-3, 8, 1), hline(3, 7, 1), hline(3, 8, 1))),
            new TownTemplate("Нагоя", merge(rect(3, 3, 4, 5), hline(2, 4, 1), hline(2, 5, 1))),
            new TownTemplate("Саппоро", merge(rect(2, -8, 5, 6), hline(1, -7, 1), hline(1, -6, 1), hline(7, -7, 1), hline(7, -6, 1), hline(3, -2, 3))),
            new TownTemplate("Фукуока", merge(rect(-5, 11, 5, 5), hline(-6, 12, 1), hline(-6, 13, 1), hline(0, 11, 1), hline(0, 12, 1))),
            new TownTemplate("Хиросима", merge(rect(-4, 6, 2, 5), hline(-6, 7, 2), hline(-6, 8, 2), hline(-6, 9, 2))),
            new TownTemplate("Сэндай", merge(rect(0, -4, 4, 4), hline(-1, -3, 1), hline(-1, -2, 1), hline(4, -3, 1)))
        )));

        // ВЕЛИКОБРИТАНИЯ
        TEMPLATES.put("uk", new NationTemplate("Соединённое Королевство", NationColor.PURPLE, Arrays.asList(
            new TownTemplate("Лондон", merge(rect(0, 0, 5, 5), vline(-1, 1, 3), vline(5, 1, 3), hline(1, -1, 3))),
            new TownTemplate("Бирмингем", merge(rect(-1, -6, 6, 5), hline(-2, -5, 1), hline(-2, -4, 1), hline(5, -5, 1), hline(5, -4, 1), hline(0, -1, 4))),
            new TownTemplate("Манчестер", merge(rect(-1, -11, 5, 5), hline(-2, -10, 1), hline(-2, -9, 1), hline(4, -10, 1), hline(4, -9, 1))),
            new TownTemplate("Ливерпуль", merge(rect(-5, -11, 4, 5), hline(-6, -10, 1), hline(-6, -9, 1))),
            new TownTemplate("Эдинбург", merge(rect(-2, -17, 5, 5), hline(-3, -16, 1), hline(-3, -15, 1), hline(3, -16, 1), hline(3, -15, 1), hline(-1, -12, 4))),
            new TownTemplate("Глазго", merge(rect(-6, -17, 4, 5), hline(-7, -16, 1), hline(-7, -15, 1))),
            new TownTemplate("Бристоль", merge(rect(-4, -2, 4, 5), hline(-5, -1, 1), hline(-5, 0, 1))),
            new TownTemplate("Кардифф", merge(rect(-6, 0, 5, 4), hline(-7, 1, 1), hline(-7, 2, 1)))
        )));

        // РУМЫНИЯ
        TEMPLATES.put("romania", new NationTemplate("Румыния", NationColor.YELLOW, Arrays.asList(
            new TownTemplate("Бухарест", merge(rect(0, 0, 5, 5), vline(-1, 1, 3), vline(5, 1, 3))),
            new TownTemplate("Клуж-Напока", merge(rect(-6, -5, 5, 5), hline(-7, -4, 1), hline(-7, -3, 1), hline(-1, -4, 1), hline(-1, -3, 1))),
            new TownTemplate("Тимишоара", merge(rect(-8, 0, 5, 5), hline(-9, 1, 1), hline(-9, 2, 1), hline(-3, 1, 2))),
            new TownTemplate("Яссы", merge(rect(3, -6, 5, 5), hline(2, -5, 1), hline(2, -4, 1), hline(8, -5, 1), hline(8, -4, 1), hline(3, -1, 3))),
            new TownTemplate("Констанца", merge(rect(5, 1, 4, 5), hline(4, 2, 1), hline(4, 3, 1), hline(5, 0, 3))),
            new TownTemplate("Брашов", merge(rect(-3, -2, 3, 5), hline(-4, -1, 1), hline(-4, 0, 1))),
            new TownTemplate("Крайова", merge(rect(-3, 5, 5, 4), hline(-4, 6, 1), hline(-4, 7, 1), hline(2, 5, 1)))
        )));

        // ТУРЦИЯ
        TEMPLATES.put("turkey", new NationTemplate("Турецкая Республика", NationColor.ORANGE, Arrays.asList(
            new TownTemplate("Анкара", merge(rect(0, 0, 6, 5), vline(-1, 1, 3), hline(1, -1, 4))),
            new TownTemplate("Стамбул", merge(rect(-8, 0, 7, 5), hline(-9, 1, 1), hline(-9, 2, 1), hline(-9, 3, 1))),
            new TownTemplate("Измир", merge(rect(-9, 5, 5, 5), hline(-10, 6, 1), hline(-10, 7, 1))),
            new TownTemplate("Анталья", merge(rect(-4, 5, 6, 5), hline(-5, 6, 1), hline(-5, 7, 1), hline(2, 6, 1))),
            new TownTemplate("Адана", merge(rect(2, 5, 6, 5), hline(1, 6, 1), hline(1, 7, 1), hline(8, 6, 1))),
            new TownTemplate("Трабзон", merge(rect(6, 0, 6, 5), hline(12, 1, 1), hline(12, 2, 1), hline(12, 3, 1))),
            new TownTemplate("Газиантеп", merge(rect(8, 5, 5, 5), hline(7, 6, 1), hline(7, 7, 1), hline(13, 6, 1)))
        )));

        // БРАЗИЛИЯ
        TEMPLATES.put("brazil", new NationTemplate("Федеративная Республика Бразилия", NationColor.GREEN, Arrays.asList(
            new TownTemplate("Бразилиа", merge(rect(0, 0, 6, 5), vline(-1, 1, 3), vline(6, 1, 3))),
            new TownTemplate("Сан-Паулу", merge(rect(0, 5, 7, 6), vline(-1, 6, 4), vline(7, 6, 4), hline(1, 11, 5))),
            new TownTemplate("Рио-де-Жанейро", merge(rect(7, 2, 5, 6), hline(6, 3, 1), hline(6, 4, 1), hline(12, 3, 1), hline(12, 4, 1))),
            new TownTemplate("Манаус", merge(rect(-8, -7, 7, 7), hline(-9, -5, 1), hline(-9, -4, 1), hline(-1, -6, 1), hline(-1, -5, 1))),
            new TownTemplate("Форталеза", merge(rect(4, -7, 6, 5), hline(3, -6, 1), hline(3, -5, 1), hline(10, -6, 1), hline(10, -5, 1))),
            new TownTemplate("Сальвадор", merge(rect(8, -2, 5, 5), hline(7, -1, 1), hline(7, 0, 1), hline(13, -1, 1))),
            new TownTemplate("Белу-Оризонти", merge(rect(0, -3, 4, 3), hline(4, -2, 1))),
            new TownTemplate("Куритиба", merge(rect(-3, 5, 3, 5), hline(-4, 6, 1), hline(-4, 7, 1))),
            new TownTemplate("Порту-Алегри", merge(rect(-3, 10, 5, 5), hline(-4, 11, 1), hline(-4, 12, 1), hline(2, 11, 1)))
        )));

        // ИНДИЯ
        TEMPLATES.put("india", new NationTemplate("Республика Индия", NationColor.TEAL, Arrays.asList(
            new TownTemplate("Дели", merge(rect(0, 0, 6, 5), vline(-1, 1, 3), hline(1, -1, 4), vline(6, 1, 3))),
            new TownTemplate("Мумбаи", merge(rect(-6, 5, 5, 6), vline(-7, 6, 4), hline(-5, 4, 3), vline(-1, 6, 4))),
            new TownTemplate("Бангалор", merge(rect(-1, 11, 5, 6), vline(-2, 12, 4), vline(4, 12, 4), hline(0, 17, 3))),
            new TownTemplate("Ченнаи", merge(rect(4, 8, 5, 6), hline(3, 9, 1), hline(3, 10, 1), hline(9, 9, 1), hline(9, 10, 1))),
            new TownTemplate("Калькутта", merge(rect(7, 0, 5, 5), hline(6, 1, 1), hline(6, 2, 1), hline(12, 1, 1), hline(12, 2, 1))),
            new TownTemplate("Хайдарабад", merge(rect(0, 5, 5, 6), hline(-1, 6, 1), hline(-1, 7, 1), hline(5, 6, 2))),
            new TownTemplate("Джайпур", merge(rect(-6, -2, 5, 5), hline(-7, -1, 1), hline(-7, 0, 1), vline(-1, -1, 3))),
            new TownTemplate("Лакхнау", merge(rect(0, -5, 5, 4), hline(-1, -4, 1), hline(-1, -3, 1), hline(5, -4, 1), hline(5, -3, 1)))
        )));
    }

    public static NationTemplate getTemplate(String name) {
        return TEMPLATES.get(name.toLowerCase());
    }

    public static Set<String> getAvailableTemplates() {
        return TEMPLATES.keySet();
    }
}
