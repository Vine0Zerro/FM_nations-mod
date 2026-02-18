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

        // ======================= РОССИЯ =======================
        // 18 городов. Калининград=анклав, Новая Земля=остров,
        // Крым=полуостров, Камчатка=полуостров, Сахалин=остров
        Map<Character, String> ru = new HashMap<>();
        ru.put('I', "Калининград"); ru.put('U', "Мурманск"); ru.put('S', "Санкт-Петербург");
        ru.put('A', "Архангельск"); ru.put('M', "Москва"); ru.put('W', "Воронеж");
        ru.put('D', "Краснодар"); ru.put('N', "Нижний Новгород"); ru.put('K', "Казань");
        ru.put('E', "Екатеринбург"); ru.put('P', "Тюмень"); ru.put('B', "Норильск");
        ru.put('O', "Новосибирск"); ru.put('R', "Красноярск"); ru.put('Y', "Якутск");
        ru.put('G', "Магадан"); ru.put('V', "Владивосток"); ru.put('Z', "Новая Земля");
        String[] ruMap = {
            ".............................ZZZ.Z..............................................................................................",
            "............................ZZZZZZ..............................................................................................",
            "............................ZZZZZZZ.............................................................................................",
            ".............................ZZZZZZ.............................................................................................",
            "..............................ZZZZZ.............................................................................................",
            "...............................ZZZ..............................................................................................",
            ".................................................................................................",
            "..............UU.U.............................................................................................................",
            ".............UUUUU.............................................................................................................",
            "............UUUUUUU............................................................................................................",
            "...........UUUUUUUU............................................................................................................",
            "..........UUUUUUUUU...AAAA.....................................................................................................",
            "..........UUUUUUUU...AAAAAA....................................................................................................",
            ".........SSSUUUU....AAAAAAA....................................................................................................",
            "........SSSSSUU....AAAAAAAA................BBBBB...............................................................................",
            ".......SSSSSSS....AAAAAAAAA...............BBBBBBB..............................................................................",
            "......SSSSSSSS...AAAAAAAAAA...............BBBBBBBB.............................................................................",
            "......SSSSSSMM...AAAAAAAAA................BBBBBBBBB............................................................................",
            ".....SSSSSMMMM...AAAAAAAA.................BBBBBBBBBB...........................................................................",
            ".....SSSSMMMMMM..AAAAAAA..................BBBBBBBBB............................................................................",
            "....SSSSMMMMMMM.AAAAAAA....................BBBBBBBB............................................................................",
            "....SSSMMMMMMMMNNNNKKKKEEEEPPPPP.OOOOOOO.RRRRRRRRRR...........YYYYYYYYYY......................................................",
            "....SSSMMMMMMMMNNNNKKKKKEEEEPPPPPOOOOOOOORRRRRRRRRRR..........YYYYYYYYYYY......................................................",
            "...SSSMMMMMMMMMNNNKKKKKEEEEPPPPPOOOOOOOORRRRRRRRRRRRR.........YYYYYYYYYYYY.....................................................",
            "...SSSMMMMMMMMMNNNKKKKKEEEEEPPPPOOOOOOOORRRRRRRRRRRRR.........YYYYYYYYYYYYY....................................................",
            "...SSMMMMMMMMMMNNNNKKKKKEEEEPPPPOOOOOOOORRRRRRRRRRRRRRR........YYYYYYYYYYYY....................................................",
            "..DWWMMMMMMMMMMNNNNKKKKKEEEEPPPPOOOOOOOORRRRRRRRRRRRRRR........YYYYYYYYYYYY.GGG................................................",
            "..DWWWMMMMMMMMMNNNNKKKKKEEEEEPPPOOOOOOOORRRRRRRRRRRRRRRR......YYYYYYYYYYYY.GGGGG...............................................",
            "..DDWWWMMMMMMMMNNNKKKKKKEEEEPPPPOOOOOOOORRRRRRRRRRRRRRRRR.....YYYYYYYYYYYYGGGGGG...............................................",
            "..DDDWWMMMMMMMMNNNKKKKKKEEEEEPPPOOOOOOOORRRRRRRRRRRRRRRRRR....YYYYYYYYYYYYGGGGG.VVVVV..........................................",
            "..DDDWWWMMMMMMMNNNKKKKKKEEEEPPPPOOOOOOORRRRRRRRRRRRRRRRRR.....YYYYYYYYYYYYGGGGG.VVVVVV.........................................",
            "...DDDWWMMMMMMMNNNNKKKKKEEEEEPPPOOOOOOORRRRRRRRRRRRRRRRR......YYYYYYYYYYYGGGG...VVVVVVV........................................",
            "...DDDDWMMMMMMMNNNKKKKKKEEEEEPPPOOOOOORRRRRRRRRRRRRRRRRR......YYYYYYYYYYYGGGG...VVVVVVVV.......................................",
            "...DDDDDMMMMMMMNNNNKKKKKEEEEEPPPOOOOOORRRRRRRRRRRRRRRRR.......YYYYYYYYYYYGGG....VVVVVVVV......................................",
            "....DDDDMMMMMMMNNNNKKKKKEEEEEPPPOOOOOORRRRRRRRRRRRRRRR........YYYYYYYYYYYYGG.....VVVVVVV.......................................",
            "....DDDDDMMMMMMNNNKKKKKKEEEEPPPOOOOOORRRRRRRRRRRRRRR..........YYYYYYYYYYYYY......VVVVVV........................................",
            ".....DDDDMMMMMMNNNKKKKKKEEEEEPPOOOOOORRRRRRRRRRRRRR...........YYYYYYYYYYYY........VVVV.........................................",
            ".....DDDDDMMMMMNNNKKKKKKEEEEPPPOOOOORRRRRRRRRRRRR.............YYYYYYYYYYYY........VVV..........................................",
            "......DDDDMMMMMNNNNKKKKKEEEEEPPOOOOORRRRRRRRRRRRR..............YYYYYYYYYYY.........VV...........................................",
            "......DDDDDMMMMNNNNKKKKKEEEEE.PPOOOORRRRRRRRRRR................YYYYYYYYY..........V............................................",
            ".......DDDDMMMMNNNKKKKKEEEE...PPOOOORRRRRRRRRR.................YYYYYYYY.......VVVV.............................................",
            ".......DDDDDMMMNNNKKKKKEEE....PPOOORRRRRRRRRR..................YYYYYYY.......VVVVV.............................................",
            "........DDDDMMMNNNKKKKKEE.....PPOORRRRRRRRR....................YYYYYY.......VVVVV..............................................",
            ".........DDDMMMNNNKKKKEEE.....PPOORRRRRRRR.....................YYYYY........VVVVV..............................................",
            "..........DDDMMNNNKKKKEE......PPORRRRRRR......................YYYY.........VVVV...............................................",
            "...........DDMMNNNKKKKE.......PORRRRRRR.......................YYY.........VVVV................................................",
            "............DDMNNKKKKEE........ORRRRR.........................YY..........VVV.................................................",
            ".............DDNNKKKKE.........ORRRR..........................Y..........VVV..................................................",
            "..............DNKKKKE..........ORRR.......................................VV...................................................",
            "...............DKKKE...........ORR.......................................VV....................................................",
            "................DKE............OR........................................V.....................................................",
            ".................................................................................................",
            "II...............................................................................................",
            "III..............................................................................................",
            "IIII.............................................................................................",
            "III..............................................................................................",
            "II...............................................................................................",
        };
        TEMPLATES.put("russia", new NationTemplate("Российская Федерация", NationColor.RED, parseMap(ruMap, ru)));

        // ======================= КИТАЙ =======================
        // 11 городов. Маньчжурия=выступ СВ, Синьцзян=широкий запад,
        // Тибет=массивный ЮЗ, побережье=заливы Бохай/Ханчжоу, Хайнань=остров
        Map<Character, String> cn = new HashMap<>();
        cn.put('U', "Урумчи"); cn.put('L', "Лхаса"); cn.put('H', "Харбин");
        cn.put('P', "Пекин"); cn.put('X', "Сиань"); cn.put('W', "Ухань");
        cn.put('S', "Шанхай"); cn.put('C', "Чэнду"); cn.put('G', "Гуанчжоу");
        cn.put('F', "Фучжоу"); cn.put('N', "Нанкин");
        String[] cnMap = {
            "......................................HHHH..............",
            ".....................................HHHHHH.............",
            "....................................HHHHHHH.............",
            "....................................HHHHHHHH............",
            "...................................HHHHHHHHH............",
            "UUUUUUUU.........................PPPPHHHHHH............",
            "UUUUUUUUUU......................PPPPPPPHHHH............",
            ".UUUUUUUUUUU..................PPPPPPPPPHH..............",
            ".UUUUUUUUUUUUU...............PPPPPPPPPPP...............",
            "..UUUUUUUUUUUUUU............PPPPPPPPPPPP...............",
            "..UUUUUUUUUUUUUUUXX.......XPPPPPPPPPPPP...............",
            "...UUUUUUUUUUUUUXXXX.....XXXXPPPPPPPPPP...............",
            "...UUUUUUUUUUUUUXXXXX...XXXXXNNNPPPSSS................",
            "....UUUUUUUUUUUUXXXXXXXXXXXXXXNNNNSSSSS...............",
            ".....UUUUUUUUULLXXXXXXXXXXXXXXNNNNSSSSSS..............",
            "......UUUUUULLLLLXXXXXXXXXXXXXWWWNNSSSSS..............",
            ".......UUUULLLLLLLXXXXXXXXXXXXWWWWNNSSS...............",
            "........UULLLLLLLLXXXXXXXXXXXWWWWWNNSSS...............",
            ".........LLLLLLLLLLXXXXXXXXXXWWWWWWNNSS................",
            "..........LLLLLLLLLXXXXXXXXXCWWWWWWNNSS................",
            "...........LLLLLLLLXXXXXXXXCCWWWWWNNSFF...............",
            "............LLLLLLLXXXXXXXCCCCWWWWNNFFF...............",
            ".............LLLLLLXXXXXXCCCCCWWWGGGFFFF..............",
            "..............LLLLLLXXXXXCCCCCCWGGGGGFFF..............",
            "...............LLLLLLXXXXCCCCCCCGGGGGGFF..............",
            "................LLLLLXXXCCCCCCCGGGGGGGF...............",
            ".................LLLLLXXCCCCCCCCGGGGGGF...............",
            "..................LLLLXCCCCCCCCCGGGGGGG...............",
            "...................LLLCCCCCCCCCCGGGGGG................",
            "....................LLCCCCCCCCCGGGGGGG................",
            ".....................LCCCCCCCCGGGGGG..................",
            "......................CCCCCCCGGGGGGG..................",
            ".......................CCCCCCGGGGGG...................",
            "........................CCCCCGGGGG...................",
            ".........................CCCCGGGG....................",
            "..........................CCCGGG.....................",
            "...........................CCGG......................",
            "............................CGG......................",
            ".............................G.......................",
        };
        TEMPLATES.put("china", new NationTemplate("Китайская Народная Республика", NationColor.DARK_RED, parseMap(cnMap, cn)));

        // ======================= США =======================
        // 11 городов. Аляска=отделена, Флорида=полуостров,
        // Великие озёра=выемка сверху, граница с Мексикой=Рио-Гранде зигзаги
        Map<Character, String> us = new HashMap<>();
        us.put('T', "Сиэтл"); us.put('L', "Лос-Анджелес"); us.put('X', "Феникс");
        us.put('D', "Денвер"); us.put('C', "Чикаго"); us.put('H', "Хьюстон");
        us.put('K', "Канзас-Сити"); us.put('N', "Нью-Йорк"); us.put('W', "Вашингтон");
        us.put('A', "Майами"); us.put('Q', "Аляска");
        String[] usMap = {
            "QQQQQQQQ.......................................................",
            "QQQQQQQQQQ.....................................................",
            "QQQQQQQQQQQ....................................................",
            "QQQQQQQQQQQQ...................................................",
            "QQQQQQQQQQQ....................................................",
            "QQQQQQQQQQ.....................................................",
            "QQQQQQQQQ......................................................",
            "QQQQQQQQ.......................................................",
            "QQQQQQQ........................................................",
            "...............................................................",
            "...............................................................",
            "TTTT..............CCCC.CCC..NNNN.N.............................",
            "TTTTT............CCCCCCCCC.NNNNN.N.............................",
            ".TTTTT..........CCCCCCCCCCNNNNNNN..............................",
            ".TTTTT.........CCCCCCCCCCCNNNNNNN..............................",
            "TTTTTDDDD.....CCCCCCCCCCCCNNNNNNNN.............................",
            "TTLLLDDDDDDDKKCCCCCCCCCCCWWWWWNNNNN...........................",
            "TLLLLDDDDDDDDKKKCCCCCCCCWWWWWWWWNNNN..........................",
            "LLLLLDDDDDDDDDDDDKKCCCWWWWWWWWWNNN...........................",
            "LLLLLDDDDDDDDDDDDDKKWWWWWWWWWWNN..............................",
            ".LLLLDDDDDDDDDDDDDDKWWWWWWWWWNN...............................",
            ".LLLLDDDDDDDDDDDDDDDKWWWWWWWWN................................",
            "..LLLDDDDDDDDDDDDDDDKWWWWWWWWW................................",
            "..LLXDDDDDDDDDDDDDDKKWWWWWWWWW................................",
            "..LXXDDDDDDDDDDDDKKKHHWWWWWWWW................................",
            "...XXDDDDDDDDDDDDKKKHHHHHWWWWW................................",
            "...XXXDDDDDDDDDDDDKKKHHHHHHWWW................................",
            "....XXXDDDDDDDDDDDKKKHHHHHHWW.................................",
            "....XXXXDDDDDDDDDDDKKHHHHHHW..................................",
            ".....XXXXDDDDDDDDDKKKHHHHHHH..................................",
            "......XXXXDDDDDDDDKKKHHHHHH...................................",
            ".......XXXDDDDDDDDDKKHHHHHH...................................",
            "........XXDDDDDDDDDKHHHHHH....................................",
            ".........XDDDDDDDDDHHHHH.....................................",
            "..........XDDDDDDDDHHHHH.....................................",
            "...........XDDDDDDHHHHHH......................................",
            "....................HHHHHAAAA..................................",
            "....................HHHHAAAAAA.................................",
            ".....................HHHAAAAA..................................",
            "......................HHAAAA...................................",
            ".......................HAAA....................................",
            "........................AAA....................................",
            ".........................AA....................................",
            "..........................A....................................",
        };
        TEMPLATES.put("usa", new NationTemplate("Соединённые Штаты Америки", NationColor.BLUE, parseMap(usMap, us)));

        // ======================= БРАЗИЛИЯ =======================
        // 9 городов. Северо-восточный мыс=выступ, дельта Амазонки=зазубрины,
        // юг=сужается, побережье с лагунами
        Map<Character, String> br = new HashMap<>();
        br.put('M', "Манаус"); br.put('B', "Белен"); br.put('F', "Форталеза");
        br.put('V', "Сальвадор"); br.put('R', "Ресифи"); br.put('Z', "Бразилиа");
        br.put('J', "Рио-де-Жанейро"); br.put('P', "Сан-Паулу"); br.put('T', "Порту-Алегри");
        String[] brMap = {
            ".......MMMMMM.BBBBBFFFF.............................",
            "......MMMMMMM.BBBBBBFFFFF...........................",
            ".....MMMMMMMMMBBBBBBFFFFFF..........................",
            "....MMMMMMMMMMBBBBBBFFFFFF..........................",
            "...MMMMMMMMMMMBBBBBBFFFFFFRRR.......................",
            "..MMMMMMMMMMMMBBBBBBFFFFFRRRRR......................",
            "..MMMMMMMMMMMMBBBBBBBFFFFRRRRRR.....................",
            "...MMMMMMMMMMMBBBBBBBFFFRRRRRRR.....................",
            "....MMMMMMMMMMBBBBBBBFFRRRRRRRR.....................",
            ".....MMMMMMMMMBBBBBZZZZRRRRRRR......................",
            "......MMMMMMMMBBBZZZZZZRRRRRR.......................",
            ".......MMMMMMMBBZZZZZZZZVVVVVV......................",
            "........MMMMMMBBZZZZZZZVVVVVVVV.....................",
            ".........MMMMMBBZZZZZZVVVVVVVV......................",
            "..........MMMMBZZZZZZZVVVVVVV.......................",
            "...........MMMZZZZZZZZVVVVVV........................",
            "............MMZZZZZZZZVVVVV.........................",
            ".............MZZZZZZZZJJJVV.........................",
            "..............ZZZZZZZJJJJV..........................",
            "...............ZZZZZZJJJJJ..........................",
            "................ZZZZZJJJJJ..........................",
            ".................ZZZZJJJJ...........................",
            "..................ZZPJJJJ...........................",
            "...................ZPPPJJJ..........................",
            "....................PPPPPJJ.........................",
            ".....................PPPPPP.........................",
            "......................PPPPP.........................",
            ".......................PPPP.........................",
            "........................PPP.........................",
            ".........................PPP........................",
            "..........................TTT.......................",
            "..........................TTT.......................",
            "...........................TT.......................",
            "............................T.......................",
        };
        TEMPLATES.put("brazil", new NationTemplate("Федеративная Республика Бразилия", NationColor.GREEN, parseMap(brMap, br)));

        // ======================= ИНДИЯ =======================
        // 7 городов. Гималаи=зазубрины север, полуостров Индостан=треугольник,
        // Кач/Катхиявар=выступы запад, побережье с заливами
        Map<Character, String> in = new HashMap<>();
        in.put('D', "Дели"); in.put('J', "Джайпур"); in.put('K', "Калькутта");
        in.put('M', "Мумбаи"); in.put('H', "Хайдерабад"); in.put('B', "Бангалор");
        in.put('C', "Ченнаи");
        String[] inMap = {
            "......JJJDDDDDDDD..............................",
            ".....JJJJDDDDDDDDD.............................",
            "....JJJJJJDDDDDDDDD...........................",
            "...JJJJJJJDDDDDDDDDKK.........................",
            "...JJJJJJJDDDDDDDDDDKKK.......................",
            "..JJJJJJJJDDDDDDDDDDDKKKK.....................",
            "..JJJJJJJJDDDDDDDDDDDDKKKKK...................",
            ".JJJJJJJJJDDDDDDDDDDDDKKKKKK..................",
            ".JJJJJJJJDDDDDDDDDDDDKKKKKKK..................",
            ".MMMMMMJJDDDDDDDDDDDDKKKKKK....................",
            "MMMMMMMMMDDDDDDDDDDDKKKKKKK.....................",
            "MMMMMMMMMMDDDDDDDDDKKKKKK.......................",
            "MMMMMMMMMMMMDDDDDDKKKKKK........................",
            ".MMMMMMMMMMMMHHHHHHHCCCCC........................",
            ".MMMMMMMMMMMHHHHHHHHHCCCCC.......................",
            "..MMMMMMMMMMHHHHHHHHHCCCCCC......................",
            "..MMMMMMMMMHHHHHHHHHCCCCCC.......................",
            "...MMMMMMMMHHHHHHHHHCCCCC........................",
            "....MMMMMMMHHHHHHHHCCCCCC........................",
            ".....MMMMMMHHHHHHHCCCCC..........................",
            "......MMMMMHHHHHHHCCCC...........................",
            ".......MMMMBBBBHHCCCC...........................",
            "........MMMBBBBBHCCC............................",
            ".........MMBBBBBHCC.............................",
            "..........MBBBBBBCC.............................",
            "...........BBBBBBC..............................",
            "............BBBBBC..............................",
            ".............BBBBC..............................",
            "..............BBBC..............................",
            "...............BBB..............................",
            "................BB..............................",
            ".................B..............................",
        };
        TEMPLATES.put("india", new NationTemplate("Республика Индия", NationColor.TEAL, parseMap(inMap, in)));

        // ======================= ТУРЦИЯ =======================
        // 6 городов. Фракия=отделена проливами, Эгейское побережье=зазубрины,
        // Анатолия=массив, Хатай=выступ юг
        Map<Character, String> tr = new HashMap<>();
        tr.put('S', "Стамбул"); tr.put('A', "Анкара"); tr.put('I', "Измир");
        tr.put('L', "Анталья"); tr.put('T', "Трабзон"); tr.put('G', "Газиантеп");
        String[] trMap = {
            "..SS..AAAAAAAAAAATTTTTTTTTTT............................",
            "..SSSAAAAAAAAAAATTTTTTTTTTTT............................",
            ".SSSAAAAAAAAAAAATTTTTTTTTTTTT...........................",
            "SSSIIAAAAAAAAAAAATTTTTTTTTTTTTGG........................",
            "SSIIIIAAAAAAAAAAAATTTTTTTTTTTTGGGG......................",
            ".IIIIIIAAAAAAAAAAATTTTTTTTTTTTGGGGG.....................",
            ".IIIIIIILLAAAAAAATTTTTTTTTTTTTTGGGG.....................",
            "..IIIIILLLLAAAAATTTTTTTTTTTTTGGG........................",
            "..III.LLLLLLAAAA.TTTTTTTTTTTGGG.........................",
            "...I..LLLLLLLAA...TTTTTTTTGGG..........................",
            "......LLLLLLLLA....TTTTTTTGG...........................",
            ".......LLLLLLL......TTTTGGG............................",
            "........LLLLLL.......TTGG..............................",
            ".........LLLLL........GG...............................",
            "..........LLLL........G................................",
            "...........LLL.........................................",
        };
        TEMPLATES.put("turkey", new NationTemplate("Турецкая Республика", NationColor.ORANGE, parseMap(trMap, tr)));

        // ======================= ФРАНЦИЯ =======================
        // 7 городов. Бретань=полуостров запад, Альпы=зазубрины восток,
        // Пиренеи=зазубрины юг, Лазурный берег=заливы
        Map<Character, String> fr = new HashMap<>();
        fr.put('N', "Нант"); fr.put('P', "Париж"); fr.put('S', "Страсбург");
        fr.put('B', "Бордо"); fr.put('L', "Лион"); fr.put('T', "Тулуза"); fr.put('A', "Марсель");
        String[] frMap = {
            "......NNNPPPPPP.................................",
            ".....NNNNPPPPPPP................................",
            "....NNNNNPPPPPPPP.SS............................",
            "...NNNNNNPPPPPPPPPSSSS..........................",
            "..NNNNNNNPPPPPPPPPLLSSS.........................",
            ".NNNNNNNBPPPPPPPPLLLLSS.........................",
            "..NNNNBBBBPPPPPPLLLLL...........................",
            "...NNNBBBBBPPPPPLLLLLL..........................",
            "....NBBBBBBBPPPLLLLLLL..........................",
            ".....BBBBBBBPPLLLLLLLL..........................",
            "......BBBBBBTTLLLLLLL...........................",
            ".......BBBTTTTTTLLLLLL..........................",
            "........BBTTTTTTTAAAA...........................",
            ".........TTTTTTTTAAAA...........................",
            "..........TTTTTTTAAAA...........................",
            "...........TTTTTTAAA............................",
            "............TTTTTAAA............................",
            ".............TTTTAA.............................",
            "..............TTTAA.............................",
            "...............TTAA.............................",
        };
        TEMPLATES.put("france", new NationTemplate("Французская Республика", NationColor.NAVY, parseMap(frMap, fr)));

        // ======================= ЯПОНИЯ =======================
        // 6 городов. Хоккайдо=остров отделён, Хонсю=длинный изогнутый,
        // Кюсю=остров отделён, полуострова Ното/Кии выступают
        Map<Character, String> jp = new HashMap<>();
        jp.put('R', "Саппоро"); jp.put('T', "Токио"); jp.put('N', "Нагоя");
        jp.put('O', "Осака"); jp.put('H', "Хиросима"); jp.put('F', "Фукуока");
        String[] jpMap = {
            "...........RRRR.........................",
            "..........RRRR.R........................",
            "..........RRRRRRR.......................",
            "..........RRRRRR........................",
            "...........RRRRR........................",
            "...........RRRR.........................",
            "............RRR.........................",
            ".........................................",
            "............TT..........................",
            "...........TTT..........................",
            "..........TTTT..........................",
            ".........TTTTT..........................",
            ".........TTTTTT.........................",
            "........TTTTTT..........................",
            "........TTTTT...........................",
            ".......NNTTT............................",
            "......NNNNT.............................",
            "......NNNNN.............................",
            ".....NNNNN..............................",
            "....OOONN...............................",
            "....OOOOO...............................",
            "...OOOOOO...............................",
            "...OOOOO................................",
            "..HHOOO.................................",
            "..HHHH..................................",
            ".HHHHH..................................",
            ".HHHH...................................",
            ".........................................",
            ".FFFF...................................",
            "FFFFF...................................",
            "FFFFFF..................................",
            "FFFFF...................................",
            ".FFFF...................................",
            "..FFF...................................",
            "...FF...................................",
        };
        TEMPLATES.put("japan", new NationTemplate("Японская Империя", NationColor.WHITE, parseMap(jpMap, jp)));

        // ======================= ГЕРМАНИЯ =======================
        // 7 городов. Побережье=фьорды Шлезвиг, Рюген=выступ,
        // Рейнланд=выемки, Альпы=зазубрины юг
        Map<Character, String> de = new HashMap<>();
        de.put('H', "Гамбург"); de.put('B', "Берлин"); de.put('K', "Кёльн");
        de.put('F', "Франкфурт"); de.put('D', "Дрезден"); de.put('S', "Штутгарт"); de.put('Q', "Мюнхен");
        String[] deMap = {
            "....HHHH.BBBBB..............................",
            "...HHHHHH.BBBBBB............................",
            "...HHHHHHH.BBBBB............................",
            "..HHHHHHHH.BBBBBD...........................",
            "..HHHHHHHHFFDDDDDD..........................",
            ".KKKHHHHHFFFFDDDDD..........................",
            ".KKKKKHHFFFFFFDDD...........................",
            "..KKKKKKFFFFFFFF............................",
            "...KKKFFFFFFFS.............................",
            "...KKFFFFFFFSSS............................",
            "....KFFFFFFSSSS............................",
            ".....FFFFFSSSSSS...........................",
            "......FFSSSSSSS............................",
            "......SSSSSQQQQQ...........................",
            ".......SSQQQQQQQ...........................",
            "........SQQQQQQQQ..........................",
            ".........QQQQQQQ...........................",
            "..........QQQQQQ...........................",
            "...........QQQQQ...........................",
        };
        TEMPLATES.put("germany", new NationTemplate("Федеративная Республика Германия", NationColor.GOLD, parseMap(deMap, de)));

        // ======================= ВЕЛИКОБРИТАНИЯ =======================
        // 7 городов. Шотландия=фьорды/заливы, Уэльс=выступ,
        // Корнуолл=полуостров, Восточная Англия=выпуклая
        Map<Character, String> uk = new HashMap<>();
        uk.put('E', "Эдинбург"); uk.put('G', "Глазго"); uk.put('M', "Манчестер");
        uk.put('W', "Ливерпуль"); uk.put('B', "Бирмингем"); uk.put('R', "Бристоль"); uk.put('L', "Лондон");
        String[] ukMap = {
            "......EE................................",
            "....GEEEE...............................",
            "....GGEEE...............................",
            "...GGGEE................................",
            "....GGEE................................",
            "....GGMM................................",
            "...WGMMM................................",
            "...WWMMMM...............................",
            "...WWMMM................................",
            "..WWWBB.................................",
            "..WWWBBB................................",
            "...WWBBBB...............................",
            "...RRBBB................................",
            "...RRRBBL...............................",
            "....RRBLL...............................",
            "....RRLLL...............................",
            "....RLLLL...............................",
            ".....LLLLL..............................",
            ".....LLLLLL.............................",
            "......LLLLL.............................",
            "......LLLL..............................",
            ".......LLL..............................",
            ".......LL...............................",
            "........L...............................",
        };
        TEMPLATES.put("uk", new NationTemplate("Соединённое Королевство", NationColor.PURPLE, parseMap(ukMap, uk)));

        // ======================= РУМЫНИЯ =======================
        // 5 городов. Карпаты=дуга внутри, дельта Дуная=зазубрины,
        // Дунай=извилистая южная граница, Прут=восточная граница
        Map<Character, String> ro = new HashMap<>();
        ro.put('C', "Клуж-Напока"); ro.put('I', "Яссы"); ro.put('T', "Тимишоара");
        ro.put('B', "Бухарест"); ro.put('O', "Констанца");
        String[] roMap = {
            "....CCCCCC.IIIIIII..........................",
            "...CCCCCCCCIIIIIIII.........................",
            "..CCCCCCCCCIIIIIII..........................",
            ".TTCCCCCCCCIIIIII...........................",
            ".TTTCCCCCCCIIIIII...........................",
            ".TTTTCCCCCBBBIIII...........................",
            ".TTTTTCCBBBBBBBII...........................",
            "..TTTTTBBBBBBBBBOOO.........................",
            "..TTTBBBBBBBBBBOOOO.........................",
            "...TTBBBBBBBBBBOOOOO........................",
            "....TBBBBBBBBB.OOOO.........................",
            ".....BBBBBBBBBOOOO..........................",
            "......BBBBBBBOOOO...........................",
            ".......BBBBBBOOO............................",
            "........BBBBBOO.............................",
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
