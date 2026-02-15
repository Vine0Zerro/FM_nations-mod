package com.nations.data;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.util.*;

public class Nation {
    private String name;
    private UUID leader;
    private NationColor color;
    private Set<String> towns = new HashSet<>();
    private Set<UUID> pendingInvites = new HashSet<>(); // UUID мэров
    private Set<String> warTargets = new HashSet<>(); // имена наций

    public Nation(String name, UUID leader, NationColor color) {
        this.name = name;
        this.leader = leader;
        this.color = color;
    }

    public String getName() { return name; }
    public UUID getLeader() { return leader; }
    public NationColor getColor() { return color; }
    public void setColor(NationColor color) { this.color = color; }
    public Set<String> getTowns() { return towns; }
    public Set<UUID> getPendingInvites() { return pendingInvites; }
    public Set<String> getWarTargets() { return warTargets; }

    public void addTown(String townName) { towns.add(townName); }
    public void removeTown(String townName) { towns.remove(townName); }
    public boolean hasTown(String townName) { return towns.contains(townName); }

    public void declareWar(String nationName) { warTargets.add(nationName); }
    public void endWar(String nationName) { warTargets.remove(nationName); }
    public boolean isAtWarWith(String nationName) { return warTargets.contains(nationName); }

    public JsonObject toJson() {
        JsonObject json = new JsonObject();
        json.addProperty("name", name);
        json.addProperty("leader", leader.toString());
        json.addProperty("color", color.getId());

        JsonArray townsArr = new JsonArray();
        for (String t : towns) townsArr.add(t);
        json.add("towns", townsArr);

        JsonArray invArr = new JsonArray();
        for (UUID id : pendingInvites) invArr.add(id.toString());
        json.add("invites", invArr);

        JsonArray warArr = new JsonArray();
        for (String w : warTargets) warArr.add(w);
        json.add("wars", warArr);

        return json;
    }

    public static Nation fromJson(JsonObject json) {
        String name = json.get("name").getAsString();
        UUID leader = UUID.fromString(json.get("leader").getAsString());
        NationColor color = NationColor.fromId(json.get("color").getAsString());
        Nation nation = new Nation(name, leader, color);

        for (var el : json.getAsJsonArray("towns")) nation.towns.add(el.getAsString());
        for (var el : json.getAsJsonArray("invites")) nation.pendingInvites.add(UUID.fromString(el.getAsString()));
        if (json.has("wars")) {
            for (var el : json.getAsJsonArray("wars")) nation.warTargets.add(el.getAsString());
        }
        return nation;
    }
}