package com.nyadom.nyadompublic.gui;

import cn.lunadeer.dominion.api.dtos.DominionDTO;
import com.nyadom.nyadompublic.MessageUtil;
import com.nyadom.nyadompublic.NyaDomPublicPlugin;
import com.nyadom.nyadompublic.config.Settings;
import com.nyadom.nyadompublic.data.ListingEntry;
import com.nyadom.nyadompublic.dominion.DominionService;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public final class ManageGui {

    private ManageGui() {
    }

    public static void open(Player player, int dominionId) {
        NyaDomPublicPlugin plugin = NyaDomPublicPlugin.getInstance();
        Settings settings = plugin.getSettings();
        DominionService dominionService = plugin.getDominionService();

        DominionDTO dto = dominionService.getDominion(dominionId);
        if (dto == null) {
            plugin.getListingManager().remove(dominionId);
            BoardGui.open(player, false);
            return;
        }

        ManageHolder holder = new ManageHolder(dominionId);
        Inventory inventory = Bukkit.createInventory(holder, 9, MessageUtil.color(settings.getMessage("manage-title")));
        holder.setInventory(inventory);

        ListingEntry entry = plugin.getListingManager().findByDominionId(dominionId).orElse(null);
        String description = entry == null || entry.getDescription().isEmpty()
                ? settings.colorMessage("no-description")
                : MessageUtil.color(entry.getDescription());
        String created = entry == null ? "?" : new SimpleDateFormat("yyyy-MM-dd HH:mm").format(new Date(entry.getCreatedAt()));
        String owner = dominionService.getOwnerName(dto);

        List<String> infoLore = new ArrayList<>();
        for (String line : settings.colorMessageList("manage-info-lore")) {
            infoLore.add(line.replace("{name}", dto.getName())
                    .replace("{owner}", owner)
                    .replace("{description}", description)
                    .replace("{created}", created));
        }
        inventory.setItem(0, GuiUtils.item(entry == null ? Material.OAK_SIGN : entry.resolveSignMaterial(),
                settings.colorMessage("manage-info-name").replace("{name}", dto.getName()), infoLore));

        inventory.setItem(3, GuiUtils.item(Material.WRITABLE_BOOK,
                settings.colorMessage("button-edit-desc"),
                settings.colorMessageList("button-edit-desc-lore")));
        inventory.setItem(5, GuiUtils.item(Material.RED_WOOL,
                settings.colorMessage("button-remove"),
                settings.colorMessageList("button-remove-lore")));
        inventory.setItem(8, GuiUtils.item(Material.BARRIER,
                settings.colorMessage("button-back"),
                settings.colorMessageList("button-back-lore")));

        player.openInventory(inventory);
    }
}
