package com.nations.data;

import com.google.gson.*;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.ChunkPos;

import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class NationsData {
    private static final Map<String, Town> towns = new ConcurrentHashMap<>();
    private static final Map<String, Nation> nations = new ConcurrentHashMap<>();
    // Ограничение: UUID -> timestamp последнего claim batch
    private static final Map<UUID, Long> claimCooldowns = new ConcurrentHashMap<>();
    private static final Map<UUID, Integer> claimCountInMinute = new ConcurrentHashMap<>();
    private static Path saveDir;
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    public static void load(MinecraftServer server) {
        saveDir = server.getServerDirectory().toPath().resolve("nations_data");
        try {
            Files.createDirectories(saveDir);
            loadTowns();
            loadNations();
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

    public static void save() {
        if (saveDir == null) return;
        try {
            JsonArray townsArr = new JsonArray();
            for (Town t : towns.values()) townsArr.add(t.toJson());
            Files.writeString(saveDir.resolve("towns.json"), GSON.toJson(townsArr));

            JsonArray nationsArr = new JsonArray();
            for (Nation n : nations.values()) nationsArr.add(n.toJson());
            Files.writeString(saveDir.resolve("nations.json"), GSON.toJson(nationsArr));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // === Town methods ===
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

    public static boolean townExists(String name) {
        return towns.containsKey(name.toLowerCase());
    }

    public static void addTown(Town town) {
        towns.put(town.getName().toLowerCase(), town);
        save();
    }

    public static void removeTown(String name) {
        towns.remove(name.toLowerCase());
        save();
    }

    public static Collection<Town> getAllTowns() { return towns.values(); }

    // === Nation methods ===
    public static Nation getNation(String name) {
        return nations.get(name.toLowerCase());
    }

    public static Nation getNationByPlayer(UUID player) {
        Town t = getTownByPlayer(player);
        if (t != null && t.getNationName() != null) {
            return getNation(t.getNationName());
        }
        return null;
    }

    public static boolean nationExists(String name) {
        return nations.containsKey(name.toLowerCase());
    }

    public static void addNation(Nation nation) {
        nations.put(nation.getName().toLowerCase(), nation);
        save();
    }

    public static void removeNation(String name) {
        nations.remove(name.toLowerCase());
        save();
    }

    public static Collection<Nation> getAllNations() { return nations.values(); }

    public static boolean isColorTaken(NationColor color) {
        for (Nation n : nations.values()) {
            if (n.getColor() == color) return true;
        }
        return false;
    }

    // === Claim rate limiting: 5 chunks per minute ===
    public static boolean canClaim(UUID player) {
        long now = System.currentTimeMillis();
        Long lastReset = claimCooldowns.get(player);
        if (lastReset == null || now - lastReset > 60000) {
            claimCooldowns.put(player, now);
            claimCountInMinute.put(player, 0);
        }
        int count = claimCountInMinute.getOrDefault(player, 0);
        return count < 5;
    }

    public static void incrementClaim(UUID player) {
        int count = claimCountInMinute.getOrDefault(player, 0);
        claimCountInMinute.put(player, count + 1);
    }
}