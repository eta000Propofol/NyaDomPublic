package com.nyadom.nyadompublic.gui;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

public final class GuiUtils {

    private GuiUtils() {
    }

    public static ItemStack item(Material material, String name, List<String> lore) {
        ItemStack stack = new ItemStack(material);
        ItemMeta meta = stack.getItemMeta();
        if (meta != null) {
            if (name != null) {
                meta.setDisplayName(name);
            }
            if (lore != null) {
                meta.setLore(new ArrayList<>(lore));
            }
            stack.setItemMeta(meta);
        }
        return stack;
    }
}
