package com.nyadom.nyadompublic.gui;

import cn.lunadeer.dominion.api.dtos.DominionDTO;
import com.nyadom.nyadompublic.MessageUtil;
import com.nyadom.nyadompublic.NyaDomPublicPlugin;
import com.nyadom.nyadompublic.config.Settings;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

import java.util.ArrayList;
import java.util.List;

public final class SelectDominionGui {

    public static final int PAGE_SIZE = 45;

    private SelectDominionGui() {
    }

    public static void open(Player player, List<DominionDTO> dominions, int page) {
        NyaDomPublicPlugin plugin = NyaDomPublicPlugin.getInstance();
        Settings settings = plugin.getSettings();

        int totalPages = Math.max(1, (int) Math.ceil(dominions.size() / (double) PAGE_SIZE));
        if (page < 0) {
            page = 0;
        }
        if (page >= totalPages) {
            page = totalPages - 1;
        }

        SelectDominionHolder holder = new SelectDominionHolder(page, dominions);
        Inventory inventory = Bukkit.createInventory(holder, 54, MessageUtil.color(settings.getMessage("select-title")));
        holder.setInventory(inventory);

        int start = page * PAGE_SIZE;
        int end = Math.min(dominions.size(), start + PAGE_SIZE);
        for (int slot = 0; slot < PAGE_SIZE; slot++) {
            int index = start + slot;
            if (index < end) {
                inventory.setItem(slot, dominionItem(dominions.get(index)));
            } else {
                inventory.setItem(slot, filler());
            }
        }

        inventory.setItem(45, page > 0 ? navButton(settings, Material.ARROW, "上一页") : filler());
        inventory.setItem(46, filler());
        inventory.setItem(47, navButton(settings, Material.BARRIER, "&7返回"));
        for (int slot = 48; slot <= 52; slot++) {
            inventory.setItem(slot, filler());
        }
        inventory.setItem(53, page + 1 < totalPages ? navButton(settings, Material.ARROW, "下一页") : filler());

        player.openInventory(inventory);
    }

    private static org.bukkit.inventory.ItemStack dominionItem(DominionDTO dto) {
        NyaDomPublicPlugin plugin = NyaDomPublicPlugin.getInstance();
        Settings settings = plugin.getSettings();
        String cost = MessageUtil.formatMoney(settings.getCost());
        String currency = settings.getCurrencyName();

        List<String> lore = new ArrayList<>();
        for (String line : settings.colorMessageList("select-lore")) {
            lore.add(line.replace("{cost}", cost).replace("{currency}", currency));
        }
        if (dto.getWorld() != null) {
            lore.add(MessageUtil.color("&7世界: &f" + dto.getWorld().getName()));
        }
        return GuiUtils.item(Material.GRASS_BLOCK, MessageUtil.color("&f" + dto.getName()), lore);
    }

    private static org.bukkit.inventory.ItemStack navButton(Settings settings, Material material, String text) {
        return GuiUtils.item(material, MessageUtil.color(text), new ArrayList<>());
    }

    private static org.bukkit.inventory.ItemStack filler() {
        return GuiUtils.item(Material.GRAY_STAINED_GLASS_PANE, " ", new ArrayList<>());
    }
}

