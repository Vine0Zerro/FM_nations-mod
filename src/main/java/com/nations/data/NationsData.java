package com.nations.data;

import com.google.gson.*;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.ChunkPos;

import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class NationsData {
    private static final Map<String, Town> towns = new ConcurrentHashMap<>();
    private static final Map<String, Nation> nations = new ConcurrentHashMap<>();
    private static final Map<String, Alliance> alliances = new ConcurrentHashMap<>();
    private static final Map<UUID, Long> claimCooldowns = new ConcurrentHashMap<>();
    private static final Map<UUID, Integer> claimCountInMinute = new ConcurrentHashMap<>();
    private static Path saveDir;
    private static MinecraftServer serverInstance;
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    public static MinecraftServer getServer() { return serverInstance; }

    public static void load(MinecraftServer server) {
        serverInstance = server;
        saveDir = server.getServerDirectory().toPath().resolve("nations_data");
        try {
            Files.createDirectories(saveDir);
            loadTowns();
            loadNations();
            loadAlliances();
            loadEconomy();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void loadTowns() throws IOException {
        Path file = saveDir.resolve("towns.json");
        if (!Files.exists(file)) return;
        String content = Files.readString(file);
        JsonArray arr = JsonParser.parseString(content).getAsJsonArray();
        towns.clear();
        for (var el : arr) {
            Town t = Town.fromJson(el.getAsJsonObject());
            towns.put(t.getName().toLowerCase(), t);
        }
    }

    private static void loadNations() throws IOException {
        Path file = saveDir.resolve("nations.json");
        if (!Files.exists(file)) return;
        String content = Files.readString(file);
        JsonArray arr = JsonParser.parseString(content).getAsJsonArray();
        nations.clear();
        for (var el : arr) {
            Nation n = Nation.fromJson(el.getAsJsonObject());
            nations.put(n.getName().toLowerCase(), n);
        }
    }

    private static void loadAlliances() throws IOException {
        Path file = saveDir.resolve("alliances.json");
        if (!Files.exists(file)) return;
        String content = Files.readString(file);
        JsonArray arr = JsonParser.parseString(content).getAsJsonArray();
        alliances.clear();
        for (var el : arr) {
            Alliance a = Alliance.fromJson(el.getAsJsonObject());
            alliances.put(a.getName().toLowerCase(), a);
        }
    }

    private static void loadEconomy() throws IOException {
        Path file = saveDir.resolve("economy.json");
        if (!Files.exists(file)) return;
        String content = Files.readString(file);
        Economy.fromJson(JsonParser.parseString(content).getAsJsonObject());
    }

    public static void save() {
        if (saveDir == null) return;
        try {
            JsonArray townsArr = new JsonArray();
            for (Town t : towns.values()) townsArr.add(t.toJson());
            Files.writeString(saveDir.resolve("towns.json"), GSON.toJson(townsArr));

            JsonArray nationsArr = new JsonArray();
            for (Nation n : nations.values()) nationsArr.add(n.toJson());
            Files.writeString(saveDir.resolve("nations.json"), GSON.toJson(nationsArr));

            JsonArray alliancesArr = new JsonArray();
            for (Alliance a : alliances.values()) alliancesArr.add(a.toJson());
            Files.writeString(saveDir.resolve("alliances.json"), GSON.toJson(alliancesArr));

            Files.writeString(saveDir.resolve("economy.json"), GSON.toJson(Economy.toJson()));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // === Town ===
    public static Town getTown(String name) { return towns.get(name.toLowerCase()); }

    public static Town getTownByPlayer(UUID player) {
        for (Town t : towns.values()) {
            if (t.isMember(player)) return t;
        }
        return null;
    }

    public static Town getTownByChunk(ChunkPos pos) {
        for (Town t : towns.values()) {
            if (t.ownsChunk(pos)) return t;
        }
        return null;
    }

    public static boolean townExists(String name) { return towns.containsKey(name.toLowerCase()); }

    public static void addTown(Town town) {
        towns.put(town.getName().toLowerCase(), town);
        save();
    }

    public static void removeTown(String name) {
        Town town = towns.get(name.toLowerCase());
        if (town != null) {
            // Убрать из нации
            if (town.getNationName() != null) {
                Nation nation = getNation(town.getNationName());
                if (nation != null) {
                    nation.removeTown(town.getName());
                }
            }
            towns.remove(name.toLowerCase());
            save();
        }
    }

    public static Collection<Town> getAllTowns() { return towns.values(); }

    // === Nation ===
    public static Nation getNation(String name) { return nations.get(name.toLowerCase()); }

    public static Nation getNationByPlayer(UUID player) {
        Town t = getTownByPlayer(player);
        if (t != null && t.getNationName() != null) {
            return getNation(t.getNationName());
        }
        return null;
    }

    public static boolean nationExists(String name) { return nations.containsKey(name.toLowerCase()); }

    public static void addNation(Nation nation) {
        nations.put(nation.getName().toLowerCase(), nation);
        save();
    }

    public static void removeNation(String name) {
        Nation nation = nations.get(name.toLowerCase());
        if (nation != null) {
            // Убрать нацию у всех городов
            for (String townName : new HashSet<>(nation.getTowns())) {
                Town t = getTown(townName);
                if (t != null) {
                    t.setNationName(null);
                    t.setAtWar(false);
                    t.setPvpEnabled(false);
                    t.setDestructionEnabled(false);
                    t.setCaptured(false);
                    t.setCapturedBy(null);
                }
            }
            // Убрать из альянса
            if (nation.getAllianceName() != null) {
                Alliance a = getAlliance(nation.getAllianceName());
                if (a != null) a.removeMember(nation.getName());
            }
            // Убрать войны
            for (String warTarget : new HashSet<>(nation.getWarTargets())) {
                Nation target = getNation(warTarget);
                if (target != null) target.endWar(nation.getName());
            }
            nations.remove(name.toLowerCase());
            save();
        }
    }

    public static Collection<Nation> getAllNations() { return nations.values(); }

    public static boolean isColorTaken(NationColor color) {
        for (Nation n : nations.values()) {
            if (n.getColor() == color) return true;
        }
        return false;
    }

    // === Alliance ===
    public static Alliance getAlliance(String name) { return alliances.get(name.toLowerCase()); }

    public static Alliance getAllianceByNation(String nationName) {
        for (Alliance a : alliances.values()) {
            if (a.hasMember(nationName)) return a;
        }
        return null;
    }

    public static boolean allianceExists(String name) { return alliances.containsKey(name.toLowerCase()); }
    public static void addAlliance(Alliance alliance) { alliances.put(alliance.getName().toLowerCase(), alliance); save(); }
    public static void removeAlliance(String name) { alliances.remove(name.toLowerCase()); save(); }
    public static Collection<Alliance> getAllAlliances() { return alliances.values(); }

    public static boolean areAllied(String nation1, String nation2) {
        for (Alliance a : alliances.values()) {
            if (a.areAllied(nation1, nation2)) return true;
        }
        return false;
    }

    // === War zone check ===
    public static boolean isWarZone(ChunkPos pos) {
        Town town = getTownByChunk(pos);
        return town != null && town.isAtWar();
    }

    public static boolean areNationsAtWar(String nation1, String nation2) {
        if (nation1 == null || nation2 == null) return false;
        Nation n1 = getNation(nation1);
        return n1 != null && n1.isAtWarWith(nation2);
    }

    // === Ranking ===
    public static List<Nation> getNationRanking() {
        return nations.values().stream()
            .sorted((a, b) -> Integer.compare(b.getRating(), a.getRating()))
            .collect(Collectors.toList());
    }

    // === Claim rate ===
    public static boolean canClaim(UUID player) {
        long now = System.currentTimeMillis();
        Long lastReset = claimCooldowns.get(player);
        if (lastReset == null || now - lastReset > 60000) {
            claimCooldowns.put(player, now);
            claimCountInMinute.put(player, 0);
        }
        return claimCountInMinute.getOrDefault(player, 0) < 5;
    }

    public static void incrementClaim(UUID player) {
        claimCountInMinute.put(player, claimCountInMinute.getOrDefault(player, 0) + 1);
    }
}
