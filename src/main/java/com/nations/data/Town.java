package com.nations.data;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.core.BlockPos;

import java.util.*;

public class Town {
    private String name;
    private UUID mayor;
    private String nationName;
    private Set<UUID> members = new HashSet<>();
    private Map<UUID, TownRole> roles = new HashMap<>();
    private Set<ChunkPos> claimedChunks = new HashSet<>();
    private Map<String, UUID> plots = new HashMap<>(); // "x,z" -> owner UUID
    private boolean pvpEnabled = false;
    private boolean destructionEnabled = false;
    private boolean isAtWar = false;
    private double taxRate = 0.05;
    private boolean captured = false;
    private String capturedBy = null;
    private BlockPos spawnPos = null;
    private List<String> actionLog = new ArrayList<>();
    private long lastTaxCollection = 0;

    public Town(String name, UUID mayor) {
        this.name = name;
        this.mayor = mayor;
        this.members.add(mayor);
        this.roles.put(mayor, TownRole.RULER);
    }

    // === Getters/Setters ===
    public String getName() { return name; }
    public UUID getMayor() { return mayor; }
    public void setMayor(UUID mayor) { this.mayor = mayor; }
    public String getNationName() { return nationName; }
    public void setNationName(String nationName) { this.nationName = nationName; }
    public Set<UUID> getMembers() { return members; }
    public Set<ChunkPos> getClaimedChunks() { return claimedChunks; }
    public boolean isPvpEnabled() { return pvpEnabled; }
    public void setPvpEnabled(boolean pvp) { this.pvpEnabled = pvp; }
    public boolean isDestructionEnabled() { return destructionEnabled; }
    public void setDestructionEnabled(boolean d) { this.destructionEnabled = d; }
    public boolean isAtWar() { return isAtWar; }
    public void setAtWar(boolean war) { this.isAtWar = war; }
    public double getTaxRate() { return taxRate; }
    public void setTaxRate(double rate) { this.taxRate = Math.max(0, Math.min(0.5, rate)); }
    public boolean isCaptured() { return captured; }
    public void setCaptured(boolean captured) { this.captured = captured; }
    public String getCapturedBy() { return capturedBy; }
    public void setCapturedBy(String nation) { this.capturedBy = nation; }
    public BlockPos getSpawnPos() { return spawnPos; }
    public void setSpawnPos(BlockPos pos) { this.spawnPos = pos; }
    public long getLastTaxCollection() { return lastTaxCollection; }
    public void setLastTaxCollection(long time) { this.lastTaxCollection = time; }

    // === Лимит чанков: 10 + 5 за каждого жителя ===
    public int getMaxChunks() {
        return 10 + (members.size() * 5);
    }

    public boolean canClaimMore() {
        return claimedChunks.size() < getMaxChunks();
    }

    // === Roles ===
    public TownRole getRole(UUID player) {
        return roles.getOrDefault(player, TownRole.CITIZEN);
    }

    public void setRole(UUID player, TownRole role) {
        roles.put(player, role);
    }

    public boolean hasPermission(UUID player, TownRole minRole) {
        return getRole(player).getPower() >= minRole.getPower();
    }

    // === Members ===
    public void addMember(UUID player) {
        members.add(player);
        if (!roles.containsKey(player)) roles.put(player, TownRole.CITIZEN);
    }

    public void removeMember(UUID player) {
        members.remove(player);
        roles.remove(player);
        // Удалить участки этого игрока
        plots.values().removeIf(uuid -> uuid.equals(player));
    }

    public boolean isMember(UUID player) { return members.contains(player); }

    // === Chunks ===
    public void claimChunk(ChunkPos pos) { claimedChunks.add(pos); }
    public void unclaimChunk(ChunkPos pos) { claimedChunks.remove(pos); }
    public boolean ownsChunk(ChunkPos pos) { return claimedChunks.contains(pos); }

    // === Plots (участки) ===
    public void setPlotOwner(ChunkPos pos, UUID owner) {
        plots.put(pos.x + "," + pos.z, owner);
    }

    public UUID getPlotOwner(ChunkPos pos) {
        return plots.get(pos.x + "," + pos.z);
    }

    public void removePlot(ChunkPos pos) {
        plots.remove(pos.x + "," + pos.z);
    }

    public boolean isPlotOwner(ChunkPos pos, UUID player) {
        UUID owner = getPlotOwner(pos);
        return owner != null && owner.equals(player);
    }

    // === Action Log ===
    public void addLog(String action) {
        String timestamp = java.time.LocalDateTime.now()
            .format(java.time.format.DateTimeFormatter.ofPattern("MM-dd HH:mm"));
        actionLog.add("[" + timestamp + "] " + action);
        if (actionLog.size() > 100) actionLog.remove(0); // макс 100 записей
    }

    public List<String> getActionLog() { return actionLog; }

    // === Transfer town ===
    public void transferTo(UUID newMayor) {
        roles.put(this.mayor, TownRole.VICE_RULER);
        this.mayor = newMayor;
        roles.put(newMayor, TownRole.RULER);
    }

    // === Power ===
    public int getPower() {
        return members.size() * 10 + claimedChunks.size() * 2;
    }

    // === Serialization ===
    public JsonObject toJson() {
        JsonObject json = new JsonObject();
        json.addProperty("name", name);
        json.addProperty("mayor", mayor.toString());
        if (nationName != null) json.addProperty("nation", nationName);
        json.addProperty("pvp", pvpEnabled);
        json.addProperty("destruction", destructionEnabled);
        json.addProperty("atWar", isAtWar);
        json.addProperty("taxRate", taxRate);
        json.addProperty("captured", captured);
        if (capturedBy != null) json.addProperty("capturedBy", capturedBy);
        json.addProperty("lastTaxCollection", lastTaxCollection);

        if (spawnPos != null) {
            JsonObject spawn = new JsonObject();
            spawn.addProperty("x", spawnPos.getX());
            spawn.addProperty("y", spawnPos.getY());
            spawn.addProperty("z", spawnPos.getZ());
            json.add("spawn", spawn);
        }

        JsonArray membersArr = new JsonArray();
        for (UUID id : members) membersArr.add(id.toString());
        json.add("members", membersArr);

        JsonObject rolesObj = new JsonObject();
        for (var e : roles.entrySet()) rolesObj.addProperty(e.getKey().toString(), e.getValue().getId());
        json.add("roles", rolesObj);

        JsonArray chunksArr = new JsonArray();
        for (ChunkPos cp : claimedChunks) {
            JsonObject c = new JsonObject();
            c.addProperty("x", cp.x);
            c.addProperty("z", cp.z);
            chunksArr.add(c);
        }
        json.add("chunks", chunksArr);

        JsonObject plotsObj = new JsonObject();
        for (var e : plots.entrySet()) plotsObj.addProperty(e.getKey(), e.getValue().toString());
        json.add("plots", plotsObj);

        JsonArray logArr = new JsonArray();
        for (String log : actionLog) logArr.add(log);
        json.add("log", logArr);

        return json;
    }

    public static Town fromJson(JsonObject json) {
        String name = json.get("name").getAsString();
        UUID mayor = UUID.fromString(json.get("mayor").getAsString());
        Town town = new Town(name, mayor);

        if (json.has("nation")) town.nationName = json.get("nation").getAsString();
        town.pvpEnabled = json.has("pvp") && json.get("pvp").getAsBoolean();
        town.destructionEnabled = json.has("destruction") && json.get("destruction").getAsBoolean();
        town.isAtWar = json.has("atWar") && json.get("atWar").getAsBoolean();
        if (json.has("taxRate")) town.taxRate = json.get("taxRate").getAsDouble();
        if (json.has("captured")) town.captured = json.get("captured").getAsBoolean();
        if (json.has("capturedBy")) town.capturedBy = json.get("capturedBy").getAsString();
        if (json.has("lastTaxCollection")) town.lastTaxCollection = json.get("lastTaxCollection").getAsLong();

        if (json.has("spawn")) {
            JsonObject sp = json.getAsJsonObject("spawn");
            town.spawnPos = new BlockPos(sp.get("x").getAsInt(), sp.get("y").getAsInt(), sp.get("z").getAsInt());
        }

        town.members.clear();
        for (var el : json.getAsJsonArray("members"))
            town.members.add(UUID.fromString(el.getAsString()));

        town.roles.clear();
        if (json.has("roles")) {
            for (var e : json.getAsJsonObject("roles").entrySet()) {
                TownRole role = TownRole.fromId(e.getValue().getAsString());
                if (role != null) town.roles.put(UUID.fromString(e.getKey()), role);
            }
        }

        for (var el : json.getAsJsonArray("chunks")) {
            JsonObject c = el.getAsJsonObject();
            town.claimedChunks.add(new ChunkPos(c.get("x").getAsInt(), c.get("z").getAsInt()));
        }

        if (json.has("plots")) {
            for (var e : json.getAsJsonObject("plots").entrySet()) {
                town.plots.put(e.getKey(), UUID.fromString(e.getValue().getAsString()));
            }
        }

        if (json.has("log")) {
            for (var el : json.getAsJsonArray("log")) {
                town.actionLog.add(el.getAsString());
            }
        }

        return town;
    }
}
