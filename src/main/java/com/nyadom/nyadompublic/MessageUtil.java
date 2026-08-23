package com.nyadom.nyadompublic;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.util.List;

public final class MessageUtil {

    private MessageUtil() {
    }

    public static void send(Player player, String key) {
        NyaDomPublicPlugin plugin = NyaDomPublicPlugin.getInstance();
        String prefix = ChatColor.translateAlternateColorCodes('&', plugin.getSettings().getMessage("prefix"));
        String message = plugin.getSettings().colorMessage(key);
        player.sendMessage(prefix + message);
    }

    public static void send(Player player, String key, String... replacements) {
        NyaDomPublicPlugin plugin = NyaDomPublicPlugin.getInstance();
        String prefix = ChatColor.translateAlternateColorCodes('&', plugin.getSettings().getMessage("prefix"));
        String message = plugin.getSettings().colorMessage(key);
        for (int i = 0; i + 1 < replacements.length; i += 2) {
            message = message.replace(replacements[i], replacements[i + 1]);
        }
        player.sendMessage(prefix + message);
    }

    public static String formatMoney(double value) {
        if (value == Math.rint(value) && !Double.isInfinite(value)) {
            return String.valueOf((long) value);
        }
        return String.valueOf(value);
    }

    public static String color(String raw) {
        return ChatColor.translateAlternateColorCodes('&', raw);
    }

    public static List<String> colorList(List<String> raw) {
        return raw.stream().map(MessageUtil::color).toList();
    }
}

