package com.nyadom.nyadompublic.config;

import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public final class Settings {

    private final JavaPlugin plugin;
    private FileConfiguration config;
    private FileConfiguration messages;

    public Settings(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    public void load() {
        plugin.saveDefaultConfig();
        plugin.reloadConfig();
        config = plugin.getConfig();

        File messagesFile = new File(plugin.getDataFolder(), "messages.yml");
        if (!messagesFile.exists()) {
            plugin.saveResource("messages.yml", false);
        }
        messages = YamlConfiguration.loadConfiguration(messagesFile);

        InputStream defaults = plugin.getResource("messages.yml");
        if (defaults != null) {
            messages.setDefaults(YamlConfiguration.loadConfiguration(
                    new InputStreamReader(defaults, StandardCharsets.UTF_8)));
        }
    }

    public double getCost() {
        return config.getDouble("economy.cost", 1000.0);
    }

    public String getCurrencyName() {
        return config.getString("economy.currency-name", "龙门币");
    }

    public int getDurationDays() {
        return config.getInt("listing.duration-days", -1);
    }

    public int getMaxListings() {
        return Math.max(1, Math.min(45, config.getInt("listing.max", 45)));
    }

    public int getDescriptionMaxLength() {
        return Math.max(1, Math.min(256, config.getInt("listing.description-max-length", 100)));
    }

    public List<String> getSignMaterials() {
        List<String> values = config.getStringList("listing.sign-materials");
        if (values.isEmpty()) {
            values = new ArrayList<>();
            values.add("OAK_SIGN");
        }
        return values;
    }

    public String getMessage(String key) {
        return messages.getString(key, key);
    }

    public String colorMessage(String key) {
        return ChatColor.translateAlternateColorCodes('&', getMessage(key));
    }

    public List<String> colorMessageList(String key) {
        List<String> out = new ArrayList<>();
        for (String line : messages.getStringList(key)) {
            out.add(ChatColor.translateAlternateColorCodes('&', line));
        }
        return out;
    }
}
