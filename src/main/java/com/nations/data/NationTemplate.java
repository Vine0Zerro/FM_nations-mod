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
        int t = 0; for (TownTemplate tt : towns) t += tt.getChunkCount(); return t;
    }

    public static class TownTemplate {
        public final String name;
        public final List<int[]> chunks;
        public TownTemplate(String name, List<int[]> chunks) { this.name = name; this.chunks = chunks; }
        public int getChunkCount() { return chunks.size(); }
        public int[] getCenter() {
            if (chunks.isEmpty()) return new int[]{0, 0};
            long sx = 0, sz = 0;
            for (int[] c : chunks) { sx += c[0]; sz += c[1]; }
            return new int[]{(int)(sx / chunks.size()), (int)(sz / chunks.size())};
        }
    }

    private static List<TownTemplate> parseMap(String[] map, Map<Character, String> key) {
        Map<String, List<int[]>> tmp = new HashMap<>();
        int h = map.length, w = 0;
        for (String s : map) if (s.length() > w) w = s.length();
        int cx = w / 2, cz = h / 2;
        for (int z = 0; z < h; z++) {
            String row = map[z];
            for (int x = 0; x < row.length(); x++) {
                char c = row.charAt(x);
                if (c == ' ' || c == '.') continue;
                String city = key.get(c);
                if (city != null) tmp.computeIfAbsent(city, k -> new ArrayList<>()).add(new int[]{x - cx, z - cz});
            }
        }
        List<TownTemplate> res = new ArrayList<>();
        for (var e : tmp.entrySet()) res.add(new TownTemplate(e.getKey(), e.getValue()));
        return res;
    }

    private static final Map<String, NationTemplate> TEMPLATES = new HashMap<>();

    static {

        // =============================================
        // РОССИЯ ~420 чанков
        // Широкая трапеция с выступами
        // =============================================
        Map<Character, String> ru = new HashMap<>();
        ru.put('U', "Мурманск");   ru.put('S', "Санкт-Петербург");
        ru.put('M', "Москва");     ru.put('N', "Нижний Новгород");
        ru.put('D', "Краснодар");  ru.put('K', "Казань");
        ru.put('E', "Екатеринбург"); ru.put('O', "Новосибирск");
        ru.put('R', "Красноярск"); ru.put('Y', "Якутск");
        ru.put('V', "Владивосток");

        // Верх шире (Сибирь), запад поуже (Европа), юго-восток выступ (Владивосток)
        // ~420 чанков, 70 символов шириной, 18 строк
        String[] ruMap = {
            "..........UU..........................................................",  // 0
            ".........UUUU........................................................",  // 1
            "......SSSSUUU........................................................",  // 2
            ".....SSSSSMM.........................................................",  // 3
            "....SSSMMMMMNNNKKKEEEEEOOOOOORRRRRRYYYYYYY...........................",  // 4
            "....SSMMMMMMNNNKKKEEEEEOOOOOORRRRRRRYYYYYYYY.........................",  // 5
            "...DDMMMMMMMNNNKKKEEEEEOOOOOORRRRRRRYYYYYYYYY........................",  // 6
            "...DDDMMMMMMNNNKKKEEEEEOOOOOORRRRRRRYYYYYYYYYYYY.....................",  // 7
            "...DDDMMMMMMNNNKKKEEEEEOOOOOORRRRRRRRYYYYYYYYYYY......VV.............",  // 8
            "...DDDDMMMMMNNNKKKEEEEEOOOOOORRRRRRRRYYYYYYYYY.......VVV.............",  // 9
            "....DDDMMMMMNNNKKKEEEEEOOOOORRRRRRRRRYYYYYYYYY......VVVV.............",  // 10
            ".....DDDMMMMNNNKKKEEEEEOOOOORRRRRRRRYYYYYYYYY.......VVV..............",  // 11
            "......DDDMMMNNNKKKEEEEEOOOOORRRRRRRRYYYYYYYY.........................",  // 12
            ".......DDDMNNNKKKEEEEEOOOOORRRRRRRYYYYYYY............................",  // 13
            "........DDDNNNKKKEEEEEOOOORRRRRRRYYYYYY..............................",  // 14
            "..........DDKKKEEEEEOOOORRRRRRRYYYYY.................................",  // 15
            "............KKKEEEEEOOORRRRRRYYYY....................................",  // 16
            ".............KKEEEEOOORRRRRYYY.......................................",  // 17
        };
        TEMPLATES.put("russia", new NationTemplate("Российская Федерация", NationColor.RED, parseMap(ruMap, ru)));

        // =============================================
        // КИТАЙ ~280 чанков
        // Запад широкий (Синьцзян+Тибет), восток уже, юг спускается
        // =============================================
        Map<Character, String> cn = new HashMap<>();
        cn.put('U', "Урумчи"); cn.put('H', "Харбин"); cn.put('P', "Пекин");
        cn.put('W', "Ухань"); cn.put('S', "Шанхай"); cn.put('C', "Чэнду");
        cn.put('G', "Гуанчжоу"); cn.put('Z', "Шэньчжэнь");

        String[] cnMap = {
            "...............HHHHH...................",  // 0
            "UUUUUU........HHHHHH..................",  // 1
            "UUUUUUUU....PPPPPHHHH.................",  // 2
            "UUUUUUUUU...PPPPPPHH..................",  // 3
            "UUUUUUUUUU.PPPPPPPP...................",  // 4
            "UUUUUUUUUUCCPPPPSSSS..................",  // 5
            ".UUUUUUUUCCCCWWWSSSS..................",  // 6
            "..UUUUUUCCCCCWWWWSSS..................",  // 7
            "...UUUUUCCCCWWWWWSS...................",  // 8
            "....UUUUCCCCWWGGGSSS..................",  // 9
            ".....UUUCCCCGGGGGZZ...................",  // 10
            "......UUCCCGGGGZZZ....................",  // 11
            ".......CCCCGGGZZZ.....................",  // 12
            "........CCGGGGZZ......................",  // 13
        };
        TEMPLATES.put("china", new NationTemplate("Китайская Народная Республика", NationColor.DARK_RED, parseMap(cnMap, cn)));

        // =============================================
        // США ~270 чанков
        // Прямоугольник + Флорида справа внизу
        // =============================================
        Map<Character, String> us = new HashMap<>();
        us.put('T', "Сиэтл"); us.put('L', "Лос-Анджелес"); us.put('D', "Денвер");
        us.put('C', "Чикаго"); us.put('N', "Нью-Йорк"); us.put('W', "Вашингтон");
        us.put('H', "Хьюстон"); us.put('A', "Майами");

        String[] usMap = {
            "TTTT.......CCCCCCNNNN.................",  // 0
            "TTTTT......CCCCCCCNNNNN...............",  // 1
            "TTTTTDD...CCCCCCCCNNNNNN..............",  // 2
            "TTLLDDDDDDCCCCCWWWWNNNN...............",  // 3
            "TLLLDDDDDDDCCWWWWWWNNN................",  // 4
            "LLLLDDDDDDDDDWWWWWWNN.................",  // 5
            "LLLLDDDDDDDDDWWWWWWN..................",  // 6
            "LLLLDDDDDDDHHHWWWWW...................",  // 7
            ".LLLDDDDDDHHHHHWWWW....................",  // 8
            "..LLDDDDDHHHHHWWW.....................",  // 9
            "...LDDDDDHHHHH........................",  // 10
            "....DDDDDHHHH.........................",  // 11
            ".....DDDDHHHH.........................",  // 12
            "..........HHAAA.......................",  // 13
            "...........HAAA.......................",  // 14
            "............AAA.......................",  // 15
            ".............AA.......................",  // 16
        };
        TEMPLATES.put("usa", new NationTemplate("Соединённые Штаты Америки", NationColor.BLUE, parseMap(usMap, us)));

        // =============================================
        // БРАЗИЛИЯ ~240 чанков
        // Выпуклая правая часть, сужается вниз
        // =============================================
        Map<Character, String> br = new HashMap<>();
        br.put('M', "Манаус"); br.put('F', "Форталеза"); br.put('V', "Сальвадор");
        br.put('B', "Бразилиа"); br.put('R', "Рио-де-Жанейро"); br.put('P', "Сан-Паулу");

        String[] brMap = {
            "....MMMMMMMFFFFFF.....................",  // 0
            "...MMMMMMMMFFFFFFF....................",  // 1
            "..MMMMMMMMMFFFFFFFF...................",  // 2
            "..MMMMMMMBBBVVVVVVV...................",  // 3
            "...MMMMMMBBBBVVVVVV...................",  // 4
            "....MMMMBBBBBVVVVV....................",  // 5
            ".....MMMBBBBBVVVV.....................",  // 6
            "......MBBBBBBRRRRR....................",  // 7
            ".......BBBBBBRRRRR....................",  // 8
            "........BBBBBRRRRR....................",  // 9
            ".........BBBPPRRR.....................",  // 10
            "..........PPPPPR......................",  // 11
            "...........PPPPP......................",  // 12
            "............PPP.......................",  // 13
        };
        TEMPLATES.put("brazil", new NationTemplate("Федеративная Республика Бразилия", NationColor.GREEN, parseMap(brMap, br)));

        // =============================================
        // ИНДИЯ ~160 чанков
        // Широкий север, треугольник на юг
        // =============================================
        Map<Character, String> in = new HashMap<>();
        in.put('J', "Джайпур"); in.put('D', "Дели"); in.put('K', "Калькутта");
        in.put('M', "Мумбаи"); in.put('B', "Бангалор"); in.put('C', "Ченнаи");

        String[] inMap = {
            "...JJJJDDDDDD........................",  // 0
            "..JJJJJDDDDDDDKK.....................",  // 1
            "..JJJJJDDDDDDDKKK....................",  // 2
            ".JJJJJDDDDDDDDKKKK...................",  // 3
            ".MMMMMMDDDDDDDKKKK...................",  // 4
            ".MMMMMMMDDDDDKKKKK...................",  // 5
            "..MMMMMMMBBBCCCCC.....................",  // 6
            "..MMMMMMMBBCCCCC......................",  // 7
            "...MMMMMBBBBCCCC......................",  // 8
            "....MMMBBBBBCCC.......................",  // 9
            ".....MMBBBBBCC........................",  // 10
            "......BBBBBCC.........................",  // 11
            ".......BBBBC..........................",  // 12
            "........BBB...........................",  // 13
        };
        TEMPLATES.put("india", new NationTemplate("Республика Индия", NationColor.TEAL, parseMap(inMap, in)));

        // =============================================
        // ТУРЦИЯ ~100 чанков
        // Анатолийский полуостров — длинный, Стамбул слева
        // =============================================
        Map<Character, String> tr = new HashMap<>();
        tr.put('S', "Стамбул"); tr.put('A', "Анкара"); tr.put('I', "Измир");
        tr.put('L', "Анталья"); tr.put('T', "Трабзон"); tr.put('G', "Газиантеп");

        String[] trMap = {
            "..SSAAAAAATTTTTTT......................",  // 0
            ".SSSAAAAAATTTTTTTT.....................",  // 1
            "SSIIAAAAAATTTTTTTTGG...................",  // 2
            "SIIIAAAAAAAATTTTTTGGG..................",  // 3
            ".IIIILLAAAAAATTTTGGG...................",  // 4
            "..IILLLLAAAAATTTGGG....................",  // 5
            "...LLLLLLAAA..........................",  // 6
            "....LLLLL..............................",  // 7
        };
        TEMPLATES.put("turkey", new NationTemplate("Турецкая Республика", NationColor.ORANGE, parseMap(trMap, tr)));

        // =============================================
        // ФРАНЦИЯ ~90 чанков
        // Шестиугольник (L'Hexagone)
        // =============================================
        Map<Character, String> fr = new HashMap<>();
        fr.put('N', "Нант"); fr.put('P', "Париж"); fr.put('S', "Страсбург");
        fr.put('B', "Бордо"); fr.put('L', "Лион"); fr.put('T', "Тулуза"); fr.put('A', "Марсель");

        String[] frMap = {
            "....NNNPPPPP...........................",  // 0
            "...NNNNPPPPPPSS........................",  // 1
            "..NNNNNPPPPPLLSS.......................",  // 2
            "..NNNBBPPPPPLLL........................",  // 3
            "...BBBBBPPLLLL.........................",  // 4
            "...BBBBTTTLLL..........................",  // 5
            "....BBTTTTAAAA.........................",  // 6
            ".....TTTTTAAAA.........................",  // 7
            "......TTTTAAA..........................",  // 8
        };
        TEMPLATES.put("france", new NationTemplate("Французская Республика", NationColor.NAVY, parseMap(frMap, fr)));

        // =============================================
        // ЯПОНИЯ ~70 чанков
        // Четыре острова дугой: Хоккайдо, Хонсю, Сикоку, Кюсю
        // Хонсю самый большой, изогнутый
        // =============================================
        Map<Character, String> jp = new HashMap<>();
        jp.put('R', "Саппоро"); jp.put('T', "Токио"); jp.put('N', "Нагоя");
        jp.put('O', "Осака"); jp.put('H', "Хиросима"); jp.put('F', "Фукуока");

        String[] jpMap = {
            "..........RRR..........................",  // 0
            ".........RRRR..........................",  // 1
            ".........RRRR..........................",  // 2
            "..........RR..........................",  // 3
            "..........TT..........................",  // 4
            ".........TTTT.........................",  // 5
            ".........TTTT.........................",  // 6
            "........NNTTT.........................",  // 7
            ".......NNNT...........................",  // 8
            "......OONN............................",  // 9
            ".....OOOO.............................",  // 10
            "....HOOO..............................",  // 11
            "...HHH................................",  // 12
            "..FHH.................................",  // 13
            ".FFF..................................",  // 14
        };
        TEMPLATES.put("japan", new NationTemplate("Японская Империя", NationColor.WHITE, parseMap(jpMap, jp)));

        // =============================================
        // ГЕРМАНИЯ ~65 чанков
        // Трапеция: широкий север (побережье), узкий юг (Бавария)
        // =============================================
        Map<Character, String> de = new HashMap<>();
        de.put('H', "Гамбург"); de.put('B', "Берлин"); de.put('K', "Кёльн");
        de.put('F', "Франкфурт"); de.put('D', "Дрезден"); de.put('S', "Штутгарт"); de.put('M', "Мюнхен");

        String[] deMap = {
            "...HHHHBBBB............................",  // 0
            "..HHHHHBBBBB...........................",  // 1
            "..HHHHBBBBBD...........................",  // 2
            ".KKKHHFFDDDDD..........................",  // 3
            ".KKKFFFFDDD............................",  // 4
            "..KKFFFSSS.............................",  // 5
            "...FFFSSSSS............................",  // 6
            "...SSSSSMMM............................",  // 7
            "....SSMMMM.............................",  // 8
            ".....MMMM..............................",  // 9
        };
        TEMPLATES.put("germany", new NationTemplate("Федеративная Республика Германия", NationColor.GOLD, parseMap(deMap, de)));

        // =============================================
        // ВЕЛИКОБРИТАНИЯ ~55 чанков
        // Шотландия узкая наверху, Англия шире внизу
        // =============================================
        Map<Character, String> uk = new HashMap<>();
        uk.put('E', "Эдинбург"); uk.put('G', "Глазго"); uk.put('M', "Манчестер");
        uk.put('V', "Ливерпуль"); uk.put('B', "Бирмингем"); uk.put('R', "Бристоль"); uk.put('L', "Лондон");

        String[] ukMap = {
            "....EE.................................",  // 0
            "...GEE.................................",  // 1
            "...GGE.................................",  // 2
            "...GGE.................................",  // 3
            "....MM.................................",  // 4
            "...VMM.................................",  // 5
            "...VVMM...............................",  // 6
            "...VVBB...............................",  // 7
            "...RRBB...............................",  // 8
            "...RRBBL..............................",  // 9
            "...RRLLL..............................",  // 10
            "....LLLL..............................",  // 11
            "....LLLL..............................",  // 12
            ".....LL................................",  // 13
        };
        TEMPLATES.put("uk", new NationTemplate("Соединённое Королевство", NationColor.PURPLE, parseMap(ukMap, uk)));

        // =============================================
        // РУМЫНИЯ ~50 чанков
        // Овал, Карпаты по центру
        // =============================================
        Map<Character, String> ro = new HashMap<>();
        ro.put('C', "Клуж-Напока"); ro.put('I', "Яссы"); ro.put('T', "Тимишоара");
        ro.put('B', "Бухарест"); ro.put('O', "Констанца");

        String[] roMap = {
            "...CCCCCIIII...........................",  // 0
            "..TCCCCCIIII...........................",  // 1
            "..TTCCCCIII............................",  // 2
            "..TTTCBBBBI............................",  // 3
            "..TTBBBBBOO............................",  // 4
            "...TBBBBBOOO...........................",  // 5
            "....BBBBOOO............................",  // 6
            ".....BBOOO.............................",  // 7
        };
        TEMPLATES.put("romania", new NationTemplate("Румыния", NationColor.YELLOW, parseMap(roMap, ro)));
    }

    public static NationTemplate getTemplate(String name) {
        return TEMPLATES.get(name.toLowerCase());
    }

    public static Set<String> getAvailableTemplates() {
        return TEMPLATES.keySet();
    }
}
