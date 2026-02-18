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
        // =================================================================
        // РОССИЯ ~950 чанков, 11 городов
        // Кольский полуостров, европейская часть широкая,
        // Сибирь массивная, Дальний Восток с выступом вниз,
        // Неровные зазубренные края на севере и юге
        // =================================================================
        Map<Character, String> ru = new HashMap<>();
        ru.put('U', "Мурманск"); ru.put('S', "Санкт-Петербург"); ru.put('M', "Москва");
        ru.put('N', "Нижний Новгород"); ru.put('D', "Краснодар"); ru.put('K', "Казань");
        ru.put('E', "Екатеринбург"); ru.put('O', "Новосибирск"); ru.put('R', "Красноярск");
        ru.put('Y', "Якутск"); ru.put('V', "Владивосток");

        String[] ruMap = {
            "..............UU................................................................................",
            ".............UUUU...............................................................................",
            "............UUUUUU..............................................................................",
            "...........UUUUUU...............................................................................",
            ".........SSSUUUUU...............................................................................",
            "........SSSSSU..................................................................................",
            ".......SSSSSSM..................................................................................",
            "......SSSSMMMM....KKKKEEEEE.OOOOOO.RRRRRRR..YYYYYYYY.........................................",
            ".....SSSSMMMMMNNNNKKKKEEEEEEOOOOOOORRRRRRRRRRYYYYYYYYY........................................",
            ".....SSSMMMMMMNNNKKKKEEEEEEOOOOOOOORRRRRRRRRRRYYYYYYYYYV......................................",
            "....SSSMMMMMMMNNNKKKKEEEEEEOOOOOOOORRRRRRRRRRRYYYYYYYYYVV.....................................",
            "...DDDMMMMMMMNNNNKKKKEEEEEEOOOOOOOORRRRRRRRRRRRYYYYYYYVVV....................................",
            "...DDDDMMMMMMNNNKKKKEEEEEEOOOOOOOORRRRRRRRRRRRRYYYYYYVVVVV...................................",
            "...DDDDDMMMMMNNNKKKKEEEEEEOOOOOOOORRRRRRRRRRRRRYYYYYVVVVV...................................",
            "....DDDDMMMMMNNNNKKKKEEEEEEOOOOOOORRRRRRRRRRRRRRYYYYVVVV....................................",
            "....DDDDDMMMMNNNNKKKKEEEEEEOOOOOOORRRRRRRRRRRRRRYYYYYY......................................",
            ".....DDDDMMMMNNNKKKKEEEEEEOOOOOOORRRRRRRRRRRRRRYYYYY........................................",
            "......DDDMMMMNNNKKKKEEEEEEOOOOOORRRRRRRRRRRRRRYYYY...........................................",
            ".......DDDMMMNNNKKKKEEEEEEOOOOOORRRRRRRRRRRRRYYY............................................",
            "........DDMMMNNKKKKEEEEEE.OOOOORRRRRRRRRRRRYY...............................................",
            ".........DMMNNKKKKEEEEEE.OOOOORRRRRRRRRRRYYY................................................",
            "..........DMNKKKKEEEEEE.OOOORRRRRRRRRRYY....................................................",
            "...........DKKKKEEEEEE.OOORRRRRRRRRYYY......................................................",
            "............KKKKEEEE..OORRRRRRRRYYY.........................................................",
            ".............KKKEEEE.OORRRRRRRYYY...........................................................",
            "..............KKEEE..ORRRRRRYYY.............................................................",
            "...............KEEE.ORRRRRYY................................................................",
        };
        TEMPLATES.put("russia", new NationTemplate("Российская Федерация", NationColor.RED, parseMap(ruMap, ru)));

        // =================================================================
        // КИТАЙ ~560 чанков, 8 городов
        // Синьцзян/Тибет массивные слева с неровным краем,
        // Маньчжурия выступает вверх, побережье зазубренное
        // =================================================================
        Map<Character, String> cn = new HashMap<>();
        cn.put('U', "Урумчи"); cn.put('H', "Харбин"); cn.put('P', "Пекин");
        cn.put('W', "Ухань"); cn.put('S', "Шанхай"); cn.put('C', "Чэнду");
        cn.put('G', "Гуанчжоу"); cn.put('Z', "Шэньчжэнь");

        String[] cnMap = {
            ".........................HHHH.........",
            "........................HHHHHH........",
            ".......................HHHHHHH.........",
            "UUUUUUU...............PPPPHHHH........",
            "UUUUUUUUU............PPPPPPHHH........",
            ".UUUUUUUUUU........PPPPPPPPHH.........",
            ".UUUUUUUUUUUU.....PPPPPPPPPP..........",
            "..UUUUUUUUUUUUU..PPPPPPPPPP...........",
            "..UUUUUUUUUUUUUCCPPPPPPSSSS...........",
            "...UUUUUUUUUUUCCCCPPPPSSSSSS..........",
            "...UUUUUUUUUUCCCCCCWWWSSSSS...........",
            "....UUUUUUUUCCCCCCWWWWWSSSS...........",
            ".....UUUUUUUCCCCCCWWWWWWSSS...........",
            "......UUUUUCCCCCCWWWWWWSS..............",
            ".......UUUUCCCCCCWWWGGGSS..............",
            "........UUUCCCCWWWGGGGGZZ..............",
            ".........UUCCCCCCGGGGZZZ...............",
            "..........UCCCCGGGGGZZZZ...............",
            "...........CCCCGGGGGZZZ................",
            "............CCGGGGZZZ..................",
            ".............CGGGGGZZ..................",
            "..............GGGGZZ...................",
            "...............GGGZ....................",
        };
        TEMPLATES.put("china", new NationTemplate("Китайская Народная Республика", NationColor.DARK_RED, parseMap(cnMap, cn)));

        // =================================================================
        // США ~550 чанков, 8 городов
        // Западное побережье зазубренное (горы),
        // Восток ровнее, Флорида длинная, Великие озёра вырез сверху
        // =================================================================
        Map<Character, String> us = new HashMap<>();
        us.put('T', "Сиэтл"); us.put('L', "Лос-Анджелес"); us.put('D', "Денвер");
        us.put('C', "Чикаго"); us.put('N', "Нью-Йорк"); us.put('W', "Вашингтон");
        us.put('H', "Хьюстон"); us.put('A', "Майами");

        String[] usMap = {
            "TTTT...........CCCCCCCC.NNNNN.........",
            "TTTTT.........CCCCCCCCCNNNNNN.........",
            ".TTTTT.......CCCCCCCCCCNNNNNNN........",
            ".TTTTDDD....CCCCCCCCCCCNNNNNNNN.......",
            "TTLLLDDDDDDCCCCCCCCCCWWWWWNNNNN......",
            "TLLLLDDDDDDDDCCCCCWWWWWWWWNNNN.......",
            "LLLLLDDDDDDDDDDDCWWWWWWWWWNNN........",
            "LLLLLDDDDDDDDDDDDDWWWWWWWWWNN........",
            ".LLLLDDDDDDDDDDDDDWWWWWWWWWN.........",
            ".LLLLDDDDDDDDDDDDDDWWWWWWWWN.........",
            "..LLLDDDDDDDDDDDDDDWWWWWWWW..........",
            "..LLLDDDDDDDDDDDDDDDWWWWWWW..........",
            "...LLDDDDDDDDDDDDDDHHHWWWWW..........",
            "...LLDDDDDDDDDDDDDHHHHHWWWW..........",
            "....LDDDDDDDDDDDDHHHHHHWWW...........",
            ".....DDDDDDDDDDDDHHHHHHWW.............",
            "......DDDDDDDDDDHHHHHH................",
            "..............HHHHHAAAA................",
            "..............HHHHAAAAA................",
            "...............HHHAAAA.................",
            "................HHAAAA.................",
            ".................HAAA..................",
            "..................AAA..................",
            "...................AA..................",
            "....................A..................",
        };
        TEMPLATES.put("usa", new NationTemplate("Соединённые Штаты Америки", NationColor.BLUE, parseMap(usMap, us)));

        // =================================================================
        // БРАЗИЛИЯ ~500 чанков, 6 городов
        // Северо-восток выпуклый с зазубринами (дельта Амазонки),
        // сужается к югу, западный край неровный
        // =================================================================
        Map<Character, String> br = new HashMap<>();
        br.put('M', "Манаус"); br.put('F', "Форталеза"); br.put('V', "Сальвадор");
        br.put('B', "Бразилиа"); br.put('R', "Рио-де-Жанейро"); br.put('P', "Сан-Паулу");

        String[] brMap = {
            ".....MMMMMMM.FFFFFF...................",
            "....MMMMMMMMMFFFFFFF..................",
            "...MMMMMMMMMMFFFFFFFF.................",
            "..MMMMMMMMMMMBBBVVVVVV................",
            "..MMMMMMMMMMBBBVVVVVVVV...............",
            "...MMMMMMMMBBBBVVVVVVVV...............",
            "....MMMMMMMBBBBVVVVVVV................",
            ".....MMMMMMBBBBBVVVVVV................",
            "......MMMMMBBBBBBVVVVV................",
            ".......MMMMBBBBBBVVVV.................",
            "........MMMBBBBBBVVVV.................",
            ".........MMBBBBBBRRRRR................",
            "..........MBBBBBBRRRRR................",
            "...........BBBBBBRRRR.................",
            "............BBBBBRRRRR................",
            ".............BBBBRRRRR................",
            "..............BBBPRRR.................",
            "...............BPPPPRR................",
            "................PPPPPR................",
            ".................PPPPP................",
            "..................PPPP................",
            "...................PPP................",
            "....................PP................",
        };
        TEMPLATES.put("brazil", new NationTemplate("Федеративная Республика Бразилия", NationColor.GREEN, parseMap(brMap, br)));

        // =================================================================
        // ИНДИЯ ~300 чанков, 6 городов
        // Гималаи зазубренные сверху, западное побережье неровное,
        // треугольник сужается к югу с выемками
        // =================================================================
        Map<Character, String> in = new HashMap<>();
        in.put('J', "Джайпур"); in.put('D', "Дели"); in.put('K', "Калькутта");
        in.put('M', "Мумбаи"); in.put('B', "Бангалор"); in.put('C', "Ченнаи");

        String[] inMap = {
            "....JJJJ.DDDDDDD....................",
            "...JJJJJJDDDDDDDDKK................",
            "..JJJJJJJDDDDDDDDDKKK..............",
            "..JJJJJJJDDDDDDDDDDKKKK............",
            ".JJJJJJJJDDDDDDDDDDKKKKK...........",
            ".JJJJJJJDDDDDDDDDDKKKKK............",
            ".MMMMMMMDDDDDDDDDDDKKKKKK...........",
            "MMMMMMMMMMDDDDDDDKKKKKKK............",
            "MMMMMMMMMMMDDDDDKKKKKK..............",
            ".MMMMMMMMMMMMDDDKKKKKKK..............",
            ".MMMMMMMMMMMBBBBBCCCCC...............",
            "..MMMMMMMMMBBBBBBCCCCC...............",
            "..MMMMMMMMMBBBBBCCCCC................",
            "...MMMMMMMBBBBBCCCC..................",
            "....MMMMMMBBBBBCCC...................",
            ".....MMMMBBBBBBCC....................",
            "......MMMBBBBBCC.....................",
            ".......MMBBBBCC......................",
            "........MBBBBC.......................",
            ".........BBBBC.......................",
            "..........BBBC.......................",
            "...........BBB.......................",
            "............BB.......................",
            ".............B.......................",
        };
        TEMPLATES.put("india", new NationTemplate("Республика Индия", NationColor.TEAL, parseMap(inMap, in)));

        // =================================================================
        // ТУРЦИЯ ~200 чанков, 6 городов
        // Стамбул/проливы неровные слева, Анатолийское плато,
        // восточная часть гористая с зазубринами
        // =================================================================
        Map<Character, String> tr = new HashMap<>();
        tr.put('S', "Стамбул"); tr.put('A', "Анкара"); tr.put('I', "Измир");
        tr.put('L', "Анталья"); tr.put('T', "Трабзон"); tr.put('G', "Газиантеп");

        String[] trMap = {
            "..SS.AAAAAAAAATTTTTTTTT.............",
            "..SSSAAAAAAAAATTTTTTTTTT............",
            ".SSSSAAAAAAAAATTTTTTTTTTTGG.........",
            "SSIIIAAAAAAAAATTTTTTTTTTTTGGG.......",
            "SSIIIIAAAAAAAAATTTTTTTTTTTGGGG......",
            ".IIIIIAAAAAAAAATTTTTTTTTTTGGGGG.....",
            ".IIIIILLLAAAAATTTTTTTTTTTGGGG.......",
            "..IILLLLLLAAAATTTTTTTTTTGGG.........",
            "...LLLLLLLLAAA.TTTTTTTTGG...........",
            "....LLLLLLLAA...TTTTTGG.............",
            ".....LLLLLL................................",
            "......LLLLL...............................",
        };
        TEMPLATES.put("turkey", new NationTemplate("Турецкая Республика", NationColor.ORANGE, parseMap(trMap, tr)));

        // =================================================================
        // ФРАНЦИЯ ~180 чанков, 7 городов
        // Шестиугольник с неровными краями:
        // Бретань выступает слева, Ривьера внизу справа,
        // Альпы зазубренные справа
        // =================================================================
        Map<Character, String> fr = new HashMap<>();
        fr.put('N', "Нант"); fr.put('P', "Париж"); fr.put('S', "Страсбург");
        fr.put('B', "Бордо"); fr.put('L', "Лион"); fr.put('T', "Тулуза"); fr.put('A', "Марсель");

        String[] frMap = {
            "....NNNPPPPPP........................",
            "...NNNNNPPPPPPP......................",
            "..NNNNNNPPPPPPPP.SS..................",
            "..NNNNNNPPPPPPPPSSS..................",
            "..NNNNNBPPPPPPPLLLSS.................",
            "...NNNBBPPPPPPPLLLS..................",
            "....NBBBBPPPPPLLLL...................",
            "....BBBBBPPPPLLLLLL..................",
            ".....BBBBBBPPLLLLLL..................",
            ".....BBBBBTTLLLLLL...................",
            "......BBBTTTTLLLLL...................",
            "......BBTTTTTAAAA....................",
            ".......TTTTTTAAAA....................",
            "........TTTTTAAAA....................",
            ".........TTTAAAA.....................",
            "..........TTAAA......................",
        };
        TEMPLATES.put("france", new NationTemplate("Французская Республика", NationColor.NAVY, parseMap(frMap, fr)));

        // =================================================================
        // ЯПОНИЯ ~150 чанков, 6 городов
        // Дуга: Хоккайдо ромбовидный вверху,
        // Хонсю длинный изогнутый с расширениями (Канто, Кансай),
        // Кюсю внизу, все соединены
        // =================================================================
        Map<Character, String> jp = new HashMap<>();
        jp.put('R', "Саппоро"); jp.put('T', "Токио"); jp.put('N', "Нагоя");
        jp.put('O', "Осака"); jp.put('H', "Хиросима"); jp.put('F', "Фукуока");

        String[] jpMap = {
            "..........RRR........................",
            ".........RRRRR.......................",
            ".........RRRRRR......................",
            ".........RRRRR.......................",
            "..........RRRR......................",
            "..........RRR.......................",
            "..........TT........................",
            ".........TTT........................",
            "........TTTTT.......................",
            "........TTTTT.......................",
            ".......TTTTTT.......................",
            ".......TTTTT........................",
            "......NNTTT.........................",
            ".....NNNNT..........................",
            ".....NNNN...........................",
            "....NNNN............................",
            "...OOONN............................",
            "...OOOON............................",
            "..OOOOO.............................",
            "..OOOO..............................",
            ".HHOO...............................",
            ".HHH................................",
            "FHHH................................",
            "FFHH................................",
            "FFF.................................",
            "FFF.................................",
            "FF..................................",
        };
        TEMPLATES.put("japan", new NationTemplate("Японская Империя", NationColor.WHITE, parseMap(jpMap, jp)));

        // =================================================================
        // ГЕРМАНИЯ ~140 чанков, 7 городов
        // Побережье зазубренное сверху, Рейнланд слева,
        // Бавария расширяется внизу, восточная граница неровная
        // =================================================================
        Map<Character, String> de = new HashMap<>();
        de.put('H', "Гамбург"); de.put('B', "Берлин"); de.put('K', "Кёльн");
        de.put('F', "Франкфурт"); de.put('D', "Дрезден"); de.put('S', "Штутгарт"); de.put('Q', "Мюнхен");

        String[] deMap = {
            "...HHHH.BBBBB........................",
            "...HHHHHBBBBBB.......................",
            "..HHHHHHBBBBBB.......................",
            "..HHHHHHHBBBBBD......................",
            ".KKKHHHHFFDDDDDD.....................",
            ".KKKKHHFFFFDDDDD.....................",
            "..KKKKKFFFFFFFF......................",
            "..KKKFFFFFFFS.......................",
            "...KKFFFFFFSS.......................",
            "....KFFFFFSSSS......................",
            ".....FFFFSSSSS......................",
            "......FFSSSSSS......................",
            ".....SSSSSQQQQQ.....................",
            "......SSSSQQQQQ.....................",
            ".......SQQQQQQ......................",
            "........QQQQQ.......................",
        };
        TEMPLATES.put("germany", new NationTemplate("Федеративная Республика Германия", NationColor.GOLD, parseMap(deMap, de)));

        // =================================================================
        // ВЕЛИКОБРИТАНИЯ ~110 чанков, 7 городов
        // Шотландия с заливами (зазубрины), Уэльс выступает влево,
        // Восточная Англия выпуклая, Корнуолл внизу
        // =================================================================
        Map<Character, String> uk = new HashMap<>();
        uk.put('E', "Эдинбург"); uk.put('G', "Глазго"); uk.put('M', "Манчестер");
        uk.put('W', "Ливерпуль"); uk.put('B', "Бирмингем"); uk.put('R', "Бристоль"); uk.put('L', "Лондон");

        String[] ukMap = {
            ".....EE..............................",
            "....GEEE.............................",
            "....GGEE.............................",
            "...GGGE..............................",
            "....GGE..............................",
            "....GMM..............................",
            "...WMMM.............................",
            "...WWMMM............................",
            "...WWMM.............................",
            "...WWBB.............................",
            "...WWBBB............................",
            "...RRBBB............................",
            "...RRBBB............................",
            "....RRBBL...........................",
            "....RRLLL...........................",
            "....RLLLL...........................",
            ".....LLLLL..........................",
            ".....LLLLLL.........................",
            "......LLLLL.........................",
            "......LLLL..........................",
            ".......LLL..........................",
            ".......LL...........................",
        };
        TEMPLATES.put("uk", new NationTemplate("Соединённое Королевство", NationColor.PURPLE, parseMap(ukMap, uk)));

        // =================================================================
        // РУМЫНИЯ ~100 чанков, 5 городов
        // Карпаты дугой создают неровность внутри,
        // побережье Чёрного моря справа, Дунай внизу зазубренный
        // =================================================================
        Map<Character, String> ro = new HashMap<>();
        ro.put('C', "Клуж-Напока"); ro.put('I', "Яссы"); ro.put('T', "Тимишоара");
        ro.put('B', "Бухарест"); ro.put('O', "Констанца");

        String[] roMap = {
            "...CCCCC.IIIIII......................",
            "..CCCCCCCIIIIIII.....................",
            ".TTCCCCCCIIIIIII.....................",
            ".TTTCCCCCIIIII.......................",
            ".TTTTCCCCBBBIII......................",
            ".TTTTTCBBBBBBII.....................",
            "..TTTTBBBBBBBBOO.....................",
            "..TTTBBBBBBBBOOOO....................",
            "...TTBBBBBBBOOOO.....................",
            "....TBBBBBBOOOO......................",
            ".....BBBBBOOO.......................",
            "......BBBBOOO.......................",
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
