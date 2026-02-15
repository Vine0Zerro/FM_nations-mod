package com.nations.data;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import net.minecraft.world.level.ChunkPos;

import java.util.*;

public class Town {
    private String name;
    private UUID mayor;
    private String nationName; // null если не в нации
    private Set<UUID> members = new HashSet<>();
    private Set<ChunkPos> claimedChunks = new HashSet<>();
    private boolean pvpEnabled = false;
    private boolean destructionEnabled = false;
    private boolean isAtWar = false;

    public Town(String name, UUID mayor) {
        this.name = name;
        this.mayor = mayor;
        this.members.add(mayor);
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

    public void addMember(UUID player) { members.add(player); }
    public void removeMember(UUID player) { members.remove(player); }
    public boolean isMember(UUID player) { return members.contains(player); }

    public void claimChunk(ChunkPos pos) { claimedChunks.add(pos); }
    public void unclaimChunk(ChunkPos pos) { claimedChunks.remove(pos); }
    public boolean ownsChunk(ChunkPos pos) { return claimedChunks.contains(pos); }

    public JsonObject toJson() {
        JsonObject json = new JsonObject();
        json.addProperty("name", name);
        json.addProperty("mayor", mayor.toString());
        if (nationName != null) json.addProperty("nation", nationName);
        json.addProperty("pvp", pvpEnabled);
        json.addProperty("destruction", destructionEnabled);
        json.addProperty("atWar", isAtWar);

        JsonArray membersArr = new JsonArray();
        for (UUID id : members) membersArr.add(id.toString());
        json.add("members", membersArr);

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

        town.members.clear();
        JsonArray membersArr = json.getAsJsonArray("members");
        for (var el : membersArr) town.members.add(UUID.fromString(el.getAsString()));

        JsonArray chunksArr = json.getAsJsonArray("chunks");
        for (var el : chunksArr) {
            JsonObject c = el.getAsJsonObject();
            town.claimedChunks.add(new ChunkPos(c.get("x").getAsInt(), c.get("z").getAsInt()));
        }
        return town;
    }
}