package com.nyadom.nyadompublic.data;

import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;

public final class ListingEntry {

    private final int dominionId;
    private final String signType;
    private String description;
    private final long createdAt;
    private final long expireAt;

    public ListingEntry(int dominionId, String signType, String description, long createdAt, long expireAt) {
        this.dominionId = dominionId;
        this.signType = signType;
        this.description = description == null ? "" : description;
        this.createdAt = createdAt;
        this.expireAt = expireAt;
    }

    public int getDominionId() {
        return dominionId;
    }

    public String getSignType() {
        return signType;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description == null ? "" : description;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public long getExpireAt() {
        return expireAt;
    }

    public boolean isPermanent() {
        return expireAt < 0;
    }

    public boolean isExpired() {
        return expireAt >= 0 && expireAt <= System.currentTimeMillis();
    }

    public Material resolveSignMaterial() {
        Material material = Material.matchMaterial(signType);
        return material != null && material.isItem() ? material : Material.OAK_SIGN;
    }

    public static ListingEntry fromSection(ConfigurationSection section) {
        int dominionId = section.getInt("dominionId", -1);
        if (dominionId < 0) {
            return null;
        }
        String signType = section.getString("signType", "OAK_SIGN");
        String description = section.getString("description", "");
        long createdAt = section.getLong("createdAt", System.currentTimeMillis());
        long expireAt = section.getLong("expireAt", -1L);
        return new ListingEntry(dominionId, signType, description, createdAt, expireAt);
    }

    public void writeTo(ConfigurationSection section) {
        section.set("dominionId", dominionId);
        section.set("signType", signType);
        section.set("description", description);
        section.set("createdAt", createdAt);
        section.set("expireAt", expireAt);
    }
}
