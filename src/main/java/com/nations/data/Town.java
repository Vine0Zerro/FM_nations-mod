package com.nations.data;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import net.minecraft.world.level.ChunkPos;

import java.util.*;

public class Town {
    private String name;
    private UUID mayor;
    private String nationName;
    private Set<UUID> members = new HashSet<>();
    private Map<UUID, TownRole> roles = new HashMap<>();
    private Set<ChunkPos> claimedChunks = new HashSet<>();
    private boolean pvpEnabled = false;
    private boolean destructionEnabled = false;
    private boolean isAtWar = false;
    private double taxRate = 0.05; // 5% по умолчанию
    private boolean captured = false; // захвачен ли
    private String capturedBy = null; // кем захвачен

    public Town(String name, UUID mayor) {
        this.name = name;
        this.mayor = mayor;
        this.members.add(mayor);
        this.roles.put(mayor, TownRole.RULER);
    }

    public String getName() { return name; }
    public UUID getMayor() { return mayor; }
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

    // Roles
    public TownRole getRole(UUID player) {
        return roles.getOrDefault(player, TownRole.CITIZEN);
    }

    public void setRole(UUID player, TownRole role) {
        roles.put(player, role);
    }

    public boolean hasPermission(UUID player, TownRole minRole) {
        return getRole(player).getPower() >= minRole.getPower();
    }

    public void addMember(UUID player) {
        members.add(player);
        if (!roles.containsKey(player)) roles.put(player, TownRole.CITIZEN);
    }

    public void removeMember(UUID player) {
        members.remove(player);
        roles.remove(player);
    }

    public boolean isMember(UUID player) { return members.contains(player); }
    public void claimChunk(ChunkPos pos) { claimedChunks.add(pos); }
    public void unclaimChunk(ChunkPos pos) { claimedChunks.remove(pos); }
    public boolean ownsChunk(ChunkPos pos) { return claimedChunks.contains(pos); }

    public int getPower() {
        return members.size() * 10 + claimedChunks.size() * 2;
    }

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
        return town;
    }
}
