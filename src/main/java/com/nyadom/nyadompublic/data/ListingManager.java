package com.nyadom.nyadompublic.data;

import com.nyadom.nyadompublic.NyaDomPublicPlugin;
import com.nyadom.nyadompublic.dominion.DominionService;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public final class ListingManager {

    private final NyaDomPublicPlugin plugin;
    private final List<ListingEntry> entries = new ArrayList<>();

    public ListingManager(NyaDomPublicPlugin plugin) {
        this.plugin = plugin;
    }

    private File dataFile() {
        return new File(plugin.getDataFolder(), "data.yml");
    }

    public synchronized void load() {
        entries.clear();
        File file = dataFile();
        if (!file.exists()) {
            return;
        }
        YamlConfiguration yaml = YamlConfiguration.loadConfiguration(file);
        for (Object obj : yaml.getList("entries", new ArrayList<>())) {
            ListingEntry entry = null;
            if (obj instanceof ConfigurationSection section) {
                entry = ListingEntry.fromSection(section);
            } else if (obj instanceof Map<?, ?> map) {
                ConfigurationSection section = yaml.createSection("tmp");
                for (Map.Entry<?, ?> kv : map.entrySet()) {
                    section.set(String.valueOf(kv.getKey()), kv.getValue());
                }
                entry = ListingEntry.fromSection(section);
            }
            if (entry != null) {
                entries.add(entry);
            }
        }
        sort();
    }

    public synchronized void save() {
        File file = dataFile();
        YamlConfiguration yaml = new YamlConfiguration();
        List<Map<String, Object>> list = new ArrayList<>();
        for (ListingEntry entry : entries) {
            Map<String, Object> map = new LinkedHashMap<>();
            map.put("dominionId", entry.getDominionId());
            map.put("signType", entry.getSignType());
            map.put("description", entry.getDescription());
            map.put("createdAt", entry.getCreatedAt());
            map.put("expireAt", entry.getExpireAt());
            list.add(map);
        }
        yaml.set("entries", list);
        try {
            yaml.save(file);
        } catch (IOException e) {
            plugin.getLogger().severe("Failed to save data.yml: " + e.getMessage());
        }
    }

    public synchronized List<ListingEntry> getAll() {
        return new ArrayList<>(entries);
    }

    public synchronized Optional<ListingEntry> findByDominionId(int dominionId) {
        return entries.stream().filter(e -> e.getDominionId() == dominionId).findFirst();
    }

    public synchronized boolean isListed(int dominionId) {
        return findByDominionId(dominionId).isPresent();
    }

    public synchronized int size() {
        return entries.size();
    }

    public synchronized void add(ListingEntry entry) {
        entries.add(entry);
        sort();
        save();
    }

    public synchronized void remove(int dominionId) {
        entries.removeIf(e -> e.getDominionId() == dominionId);
        save();
    }

    public synchronized void purge(DominionService dominionService) {
        boolean changed = entries.removeIf(e -> e.isExpired() || dominionService.getDominion(e.getDominionId()) == null);
        List<Integer> seen = new ArrayList<>();
        changed |= entries.removeIf(e -> {
            int id = e.getDominionId();
            if (seen.contains(id)) {
                return true;
            }
            seen.add(id);
            return false;
        });
        if (changed) {
            sort();
            save();
        }
    }

    private void sort() {
        entries.sort(Comparator.comparingLong(ListingEntry::getCreatedAt).reversed());
    }
}
