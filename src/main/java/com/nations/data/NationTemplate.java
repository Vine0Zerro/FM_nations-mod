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

    // Убирает дубликаты чанков: приоритет у городов, которые идут первыми в списке
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
            if (!clean.isEmpty()) {
                result.add(new TownTemplate(tt.name, clean));
            }
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

    // Вспомогательные методы для рисования
    private static List<int[]> rect(int x, int z, int w, int h) {
        List<int[]> r = new ArrayList<>();
        for (int i = 0; i < w; i++) 
            for (int j = 0; j < h; j++) 
                r.add(new int[]{x + i, z + j});
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
        // =====================================================================
        // 1. ВЕЛИКОБРИТАНИЯ (UK) — Островная форма
        // =====================================================================
        TEMPLATES.put("uk", new NationTemplate("Соединённое Королевство", NationColor.PURPLE, Arrays.asList(
            // Шотландия (север)
            new TownTemplate("Эдинбург", merge(
                rect(2, -18, 4, 3),   // Северное нагорье
                rect(3, -15, 3, 2),   // Грампиан
                rect(4, -13, 2, 2)    // Лоулендс
            )),
            new TownTemplate("Глазго", merge(
                rect(1, -14, 2, 3),   // Западное побережье
                rect(2, -12, 3, 2)    // Клайд
            )),
            // Северная Англия
            new TownTemplate("Манчестер", merge(
                rect(3, -10, 4, 3),   // Пеннины
                rect(4, -7, 3, 2)     // Пик-Дистрикт
            )),
            new TownTemplate("Ливерпуль", merge(
                rect(1, -9, 2, 3),    // Мерсисайд
                rect(2, -7, 2, 2)     // Чешир
            )),
            // Центральная Англия + Уэльс
            new TownTemplate("Бирмингем", merge(
                rect(3, -5, 4, 3),    // Мидлендс
                rect(2, -4, 2, 2)     // Шропшир
            )),
            new TownTemplate("Кардифф", merge(
                rect(0, -3, 3, 3),    // Уэльс
                rect(1, 0, 2, 1)      // Южный Уэльс
            )),
            // Южная Англия
            new TownTemplate("Лондон", merge(
                rect(5, -3, 4, 4),    // Восточная Англия
                rect(4, 1, 5, 2),     // Юго-Восток
                rect(7, -1, 3, 2)     // Эссекс
            )),
            new TownTemplate("Бристоль", merge(
                rect(2, 1, 2, 2),     // Сомерсет
                rect(0, 2, 4, 2)      // Корнуолл (выступ)
            ))
        )));

        // =====================================================================
        // 2. ФРАНЦИЯ — Шестиугольник (l'Hexagone)
        // =====================================================================
        TEMPLATES.put("france", new NationTemplate("Французская Республика", NationColor.NAVY, Arrays.asList(
            // Север
            new TownTemplate("Лилль", merge(
                rect(4, -8, 3, 2),    // Нор-Па-де-Кале
                rect(3, -6, 4, 2)     // Пикардия
            )),
            new TownTemplate("Париж", merge(
                rect(2, -4, 6, 4),    // Иль-де-Франс (центр)
                rect(3, -5, 4, 1)     // Пригород севера
            )),
            // Запад
            new TownTemplate("Брест", merge(
                rect(-6, -3, 4, 2),   // Бретань (выступ)
                rect(-4, -1, 2, 2)    // Нижняя Нормандия
            )),
            new TownTemplate("Нант", merge(
                rect(-2, -2, 4, 3),   // Земли Луары
                rect(-1, 1, 3, 2)     // Пуату
            )),
            // Юго-Запад
            new TownTemplate("Бордо", merge(
                rect(-1, 3, 4, 4),    // Аквитания
                rect(0, 7, 3, 2)      // Гасконь
            )),
            new TownTemplate("Тулуза", merge(
                rect(3, 7, 4, 3),     // Юг-Пиренеи
                rect(2, 10, 5, 2)     // Пиренеи (граница)
            )),
            // Юго-Восток
            new TownTemplate("Марсель", merge(
                rect(7, 8, 4, 3),     // Прованс
                rect(8, 11, 3, 1),    // Лазурный берег
                rect(10, 7, 2, 2)     // Альпы
            )),
            new TownTemplate("Лион", merge(
                rect(6, 4, 4, 4),     // Рона-Альпы
                rect(7, 2, 2, 2)      // Бургундия
            )),
            // Восток
            new TownTemplate("Страсбург", merge(
                rect(8, -1, 2, 4),    // Эльзас
                rect(7, -2, 2, 2)     // Лотарингия
            ))
        )));

        // =====================================================================
        // 3. ГЕРМАНИЯ — Вертикальная, широкая на севере
        // =====================================================================
        TEMPLATES.put("germany", new NationTemplate("Федеративная Республика Германия", NationColor.GOLD, Arrays.asList(
            // Север
            new TownTemplate("Гамбург", merge(
                rect(1, -7, 4, 2),    // Шлезвиг-Гольштейн
                rect(0, -5, 6, 3),    // Нижняя Саксония
                rect(6, -6, 3, 3)     // Мекленбург
            )),
            new TownTemplate("Берлин", merge(
                rect(6, -3, 4, 3),    // Бранденбург
                rect(7, 0, 3, 2)      // Саксония-Анхальт
            )),
            // Запад
            new TownTemplate("Кёльн", merge(
                rect(-2, -2, 3, 3),   // Сев. Рейн-Вестфалия
                rect(-3, -1, 2, 2)    // Саар
            )),
            new TownTemplate("Франкфурт", merge(
                rect(1, 0, 4, 3),     // Гессен
                rect(0, 2, 3, 2)      // Рейнланд-Пфальц
            )),
            // Восток
            new TownTemplate("Дрезден", merge(
                rect(8, 2, 3, 2),     // Саксония
                rect(7, 3, 2, 1)      // Рудные горы
            )),
            // Юг
            new TownTemplate("Штутгарт", merge(
                rect(1, 4, 4, 3),     // Баден-Вюртемберг
                rect(2, 7, 2, 1)      // Шварцвальд
            )),
            new TownTemplate("Мюнхен", merge(
                rect(5, 4, 5, 4),     // Бавария
                rect(6, 8, 4, 1)      // Альпы
            ))
        )));

        // =====================================================================
        // 4. РУМЫНИЯ — Круглая ("Рыбка")
        // =====================================================================
        TEMPLATES.put("romania", new NationTemplate("Румыния", NationColor.YELLOW, Arrays.asList(
            new TownTemplate("Клуж-Напока", merge(
                rect(-2, -3, 4, 3),   // Трансильвания (север)
                rect(-3, -2, 2, 2)    // Кришана
            )),
            new TownTemplate("Яссы", merge(
                rect(2, -4, 3, 4),    // Молдова
                rect(3, -1, 2, 2)     // Бессарабия (часть)
            )),
            new TownTemplate("Тимишоара", merge(
                rect(-4, 0, 3, 3),    // Банат
                rect(-3, 3, 2, 1)     // Юго-Запад
            )),
            new TownTemplate("Брашов", merge(
                rect(0, -1, 3, 3),    // Центр (Карпаты)
                rect(1, 2, 2, 1)      // Южные Карпаты
            )),
            new TownTemplate("Бухарест", merge(
                rect(0, 3, 4, 2),     // Валахия (центр)
                rect(-1, 4, 2, 1)     // Олтения
            )),
            new TownTemplate("Крайова", merge(
                rect(-3, 4, 2, 2),    // Олтения (запад)
                rect(-2, 6, 3, 1)     // Граница с Дунаем
            )),
            new TownTemplate("Констанца", merge(
                rect(4, 2, 2, 3),     // Добруджа
                rect(5, 1, 1, 2)      // Дельта Дуная
            ))
        )));

        // =====================================================================
        // 5. РОССИЯ — Гигантская вытянутая
        // =====================================================================
        TEMPLATES.put("russia", new NationTemplate("Российская Федерация", NationColor.RED, Arrays.asList(
            // Европейская часть
            new TownTemplate("Санкт-Петербург", merge(
                rect(-5, -6, 4, 3),   // Карелия/Ленобласть
                rect(-4, -3, 3, 2)    // Новгород
            )),
            new TownTemplate("Мурманск", merge(
                rect(-2, -9, 4, 3),   // Кольский п-ов
                rect(0, -10, 2, 1)    // Арктика
            )),
            new TownTemplate("Москва", merge(
                rect(-2, -2, 4, 4),   // Центр
                rect(-3, -1, 2, 2)    // Запад
            )),
            new TownTemplate("Краснодар", merge(
                rect(-4, 3, 3, 3),    // Юг/Кавказ
                rect(-5, 4, 2, 2)     // Крым/Кубань
            )),
            new TownTemplate("Нижний Новгород", merge(
                rect(2, -3, 3, 4),    // Поволжье (верх)
                rect(3, 1, 2, 2)      // Мордовия
            )),
            new TownTemplate("Казань", merge(
                rect(5, -2, 3, 3),    // Татарстан
                rect(6, 1, 2, 2)      // Башкирия
            )),
            new TownTemplate("Самара", merge(
                rect(4, 2, 3, 3),     // Самарская обл.
                rect(3, 4, 2, 1)      // Оренбург
            )),
            // Урал и Сибирь
            new TownTemplate("Екатеринбург", merge(
                rect(8, -4, 3, 5),    // Урал (хребет)
                rect(9, 1, 2, 2)      // Южный Урал
            )),
            new TownTemplate("Новосибирск", merge(
                rect(11, -2, 4, 4),   // Западная Сибирь
                rect(12, 2, 3, 2)     // Алтай
            )),
            new TownTemplate("Красноярск", merge(
                rect(15, -3, 4, 5),   // Центральная Сибирь
                rect(16, 2, 3, 2)     // Саяны
            )),
            new TownTemplate("Якутск", merge(
                rect(19, -6, 5, 5),   // Саха (Якутия)
                rect(20, -8, 3, 2)    // Северная Якутия
            )),
            // Дальний Восток
            new TownTemplate("Владивосток", merge(
                rect(24, -1, 3, 4),   // Приморье
                rect(25, 3, 2, 2)     // Крайний юг
            ))
        )));
    }
}
