package com.nyadom.nyadompublic.gui;

import cn.lunadeer.dominion.api.dtos.DominionDTO;
import com.nyadom.nyadompublic.MessageUtil;
import com.nyadom.nyadompublic.NyaDomPublicPlugin;
import com.nyadom.nyadompublic.config.Settings;
import com.nyadom.nyadompublic.data.ListingEntry;
import com.nyadom.nyadompublic.data.ListingManager;
import com.nyadom.nyadompublic.dominion.DominionService;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public final class BoardGui {

    private BoardGui() {
    }

    public static void open(Player player, boolean mineOnly) {
        NyaDomPublicPlugin plugin = NyaDomPublicPlugin.getInstance();
        Settings settings = plugin.getSettings();
        ListingManager manager = plugin.getListingManager();
        DominionService dominionService = plugin.getDominionService();

        manager.purge(dominionService);

        BoardHolder holder = new BoardHolder(mineOnly);
        Inventory inventory = Bukkit.createInventory(holder, 54, MessageUtil.color(settings.getMessage("board-title")));
        holder.setInventory(inventory);

        List<ListingEntry> all = manager.getAll();
        List<ListingEntry> shown = new ArrayList<>();
        Set<Integer> managedIds = dominionService.getManagedDominionIds(player.getUniqueId());

        for (ListingEntry entry : all) {
            if (shown.size() >= settings.getMaxListings()) {
                break;
            }
            if (mineOnly && !managedIds.contains(entry.getDominionId())) {
                continue;
            }
            DominionDTO dto = dominionService.getDominion(entry.getDominionId());
            if (dto == null) {
                continue;
            }
            shown.add(entry);
        }

        for (int slot = 0; slot < settings.getMaxListings(); slot++) {
            if (slot < shown.size()) {
                ListingEntry entry = shown.get(slot);
                DominionDTO dto = dominionService.getDominion(entry.getDominionId());
                inventory.setItem(slot, buildEntryItem(player, entry, dto, managedIds.contains(entry.getDominionId())));
            } else {
                inventory.setItem(slot, emptySlotItem(settings));
            }
        }

        inventory.setItem(45, button(settings, Material.EMERALD, "button-list", "button-list-lore"));
        inventory.setItem(46, button(settings, Material.CHEST, "button-mine", "button-mine-lore"));
        inventory.setItem(47, button(settings, Material.COMPASS, "button-refresh", "button-refresh-lore"));
        for (int slot = 48; slot <= 52; slot++) {
            inventory.setItem(slot, filler(settings));
        }
        inventory.setItem(53, button(settings, Material.BARRIER, "button-close", "button-close-lore"));

        player.openInventory(inventory);
    }

    private static ItemStack buildEntryItem(Player player, ListingEntry entry, DominionDTO dto, boolean canManage) {
        NyaDomPublicPlugin plugin = NyaDomPublicPlugin.getInstance();
        Settings settings = plugin.getSettings();
        String ownerName = plugin.getDominionService().getOwnerName(dto);
        String description = entry.getDescription().isEmpty()
                ? settings.colorMessage("no-description")
                : MessageUtil.color(entry.getDescription());
        String hint = canManage
                ? settings.colorMessage("manage-hint")
                : settings.colorMessage("teleport-hint");

        String itemName = MessageUtil.color("&f" + dto.getName());
        List<String> lore = new ArrayList<>();
        for (String line : settings.colorMessageList("board-lore")) {
            lore.add(line
                    .replace("{owner}", ownerName)
                    .replace("{description}", description)
                    .replace("{hint}", hint));
        }
        return GuiUtils.item(entry.resolveSignMaterial(), itemName, lore);
    }

    private static ItemStack emptySlotItem(Settings settings) {
        return GuiUtils.item(Material.GRAY_STAINED_GLASS_PANE,
                settings.colorMessage("empty-slot-name"),
                settings.colorMessageList("empty-slot-lore"));
    }

    private static ItemStack filler(Settings settings) {
        return GuiUtils.item(Material.GRAY_STAINED_GLASS_PANE, " ", new ArrayList<>());
    }

    private static ItemStack button(Settings settings, Material material, String nameKey, String loreKey) {
        return GuiUtils.item(material,
                settings.colorMessage(nameKey),
                settings.colorMessageList(loreKey));
    }
}
