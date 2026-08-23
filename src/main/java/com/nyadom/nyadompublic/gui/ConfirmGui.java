package com.nyadom.nyadompublic.gui;

import com.nyadom.nyadompublic.MessageUtil;
import com.nyadom.nyadompublic.NyaDomPublicPlugin;
import com.nyadom.nyadompublic.config.Settings;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

import java.util.ArrayList;

public final class ConfirmGui {

    private ConfirmGui() {
    }

    public static void open(Player player, int dominionId) {
        NyaDomPublicPlugin plugin = NyaDomPublicPlugin.getInstance();
        Settings settings = plugin.getSettings();

        ConfirmHolder holder = new ConfirmHolder(dominionId);
        Inventory inventory = Bukkit.createInventory(holder, 9, MessageUtil.color(settings.getMessage("confirm-title")));
        holder.setInventory(inventory);

        inventory.setItem(3, GuiUtils.item(Material.RED_WOOL,
                settings.colorMessage("button-confirm"),
                new ArrayList<>()));
        inventory.setItem(5, GuiUtils.item(Material.GRAY_WOOL,
                settings.colorMessage("button-cancel"),
                new ArrayList<>()));

        player.openInventory(inventory);
    }
}
