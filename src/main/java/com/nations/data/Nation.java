package com.nations.data;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.util.*;

public class Nation {
    private String name;
    private UUID leader;
    private NationColor color;
    private Set<String> towns = new HashSet<>();
    private Set<UUID> pendingInvites = new HashSet<>();
    private Set<String> warTargets = new HashSet<>();
    private Map<String, String> diplomacy = new HashMap<>(); // nationName -> "neutral"/"hostile"/"friendly"
    private String allianceName = null;
    private double nationTaxRate = 0.03;
    private int warsWon = 0;
    private int warsLost = 0;
    private int townsCaptured = 0;

    public Nation(String name, UUID leader, NationColor color) {
        this.name = name;
        this.leader = leader;
        this.color = color;
    }

    // === Getters/Setters ===
    public String getName() { return name; }
    public UUID getLeader() { return leader; }
    public NationColor getColor() { return color; }
    public void setColor(NationColor color) { this.color = color; }
    public Set<String> getTowns() { return towns; }
    public Set<UUID> getPendingInvites() { return pendingInvites; }
    public Set<String> getWarTargets() { return warTargets; }
    public String getAllianceName() { return allianceName; }
    public void setAllianceName(String name) { this.allianceName = name; }
    public double getNationTaxRate() { return nationTaxRate; }
    public void setNationTaxRate(double rate) { this.nationTaxRate = Math.max(0, Math.min(0.3, rate)); }
    public int getWarsWon() { return warsWon; }
    public void addWarWon() { this.warsWon++; }
    public int getWarsLost() { return warsLost; }
    public void addWarLost() { this.warsLost++; }
    public int getTownsCaptured() { return townsCaptured; }
    public void addTownCaptured() { this.townsCaptured++; }

    // === Towns ===
    public void addTown(String townName) { towns.add(townName); }
    public void removeTown(String townName) { towns.remove(townName); }
    public boolean hasTown(String townName) { return towns.contains(townName); }

    // === War ===
    public void declareWar(String nationName) { warTargets.add(nationName); }
    public void endWar(String nationName) { warTargets.remove(nationName); }
    public boolean isAtWarWith(String nationName) { return warTargets.contains(nationName); }

    // === Diplomacy ===
    public void setDiplomacy(String nationName, String status) {
        diplomacy.put(nationName.toLowerCase(), status);
    }

    public String getDiplomacy(String nationName) {
        return diplomacy.getOrDefault(nationName.toLowerCase(), "neutral");
    }

    public Map<String, String> getAllDiplomacy() { return diplomacy; }

    // === Stats ===
    public int getRating() {
        int score = 0;
        score += towns.size() * 50;
        score += warsWon * 100;
        score -= warsLost * 50;
        score += townsCaptured * 75;
        return Math.max(0, score);
    }

    public int getTotalMembers() {
        int total = 0;
        for (String townName : towns) {
            Town t = NationsData.getTown(townName);
            if (t != null) total += t.getMembers().size();
        }
        return total;
    }

    public int getTotalChunks() {
        int total = 0;
        for (String townName : towns) {
            Town t = NationsData.getTown(townName);
            if (t != null) total += t.getClaimedChunks().size();
        }
        return total;
    }

    // === Serialization ===
    public JsonObject toJson() {
        JsonObject json = new JsonObject();
        json.addProperty("name", name);
        json.addProperty("leader", leader.toString());
        json.addProperty("color", color.getId());
        if (allianceName != null) json.addProperty("alliance", allianceName);
        json.addProperty("nationTaxRate", nationTaxRate);
        json.addProperty("warsWon", warsWon);
        json.addProperty("warsLost", warsLost);
        json.addProperty("townsCaptured", townsCaptured);

        JsonArray townsArr = new JsonArray();
        for (String t : towns) townsArr.add(t);
        json.add("towns", townsArr);

        JsonArray invArr = new JsonArray();
        for (UUID id : pendingInvites) invArr.add(id.toString());
        json.add("invites", invArr);

        JsonArray warArr = new JsonArray();
        for (String w : warTargets) warArr.add(w);
        json.add("wars", warArr);

        JsonObject dipObj = new JsonObject();
        for (var e : diplomacy.entrySet()) dipObj.addProperty(e.getKey(), e.getValue());
        json.add("diplomacy", dipObj);

        return json;
    }

    public static Nation fromJson(JsonObject json) {
        String name = json.get("name").getAsString();
        UUID leader = UUID.fromString(json.get("leader").getAsString());
        NationColor color = NationColor.fromId(json.get("color").getAsString());
        Nation nation = new Nation(name, leader, color);

        if (json.has("alliance")) nation.allianceName = json.get("alliance").getAsString();
        if (json.has("nationTaxRate")) nation.nationTaxRate = json.get("nationTaxRate").getAsDouble();
        if (json.has("warsWon")) nation.warsWon = json.get("warsWon").getAsInt();
        if (json.has("warsLost")) nation.warsLost = json.get("warsLost").getAsInt();
        if (json.has("townsCaptured")) nation.townsCaptured = json.get("townsCaptured").getAsInt();

        for (var el : json.getAsJsonArray("towns")) nation.towns.add(el.getAsString());
        for (var el : json.getAsJsonArray("invites")) nation.pendingInvites.add(UUID.fromString(el.getAsString()));
        if (json.has("wars")) {
            for (var el : json.getAsJsonArray("wars")) nation.warTargets.add(el.getAsString());
        }
        if (json.has("diplomacy")) {
            for (var e : json.getAsJsonObject("diplomacy").entrySet()) {
                nation.diplomacy.put(e.getKey(), e.getValue().getAsString());
            }
        }
        return nation;
    }
}
