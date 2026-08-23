package com.nyadom.nyadompublic.command;

import com.nyadom.nyadompublic.MessageUtil;
import com.nyadom.nyadompublic.NyaDomPublicPlugin;
import com.nyadom.nyadompublic.gui.BoardGui;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public final class BoardCommand implements CommandExecutor, TabCompleter {

    private final NyaDomPublicPlugin plugin;

    public BoardCommand(NyaDomPublicPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (args.length == 1 && args[0].equalsIgnoreCase("reload")) {
            if (!sender.hasPermission("nyadompublic.admin")) {
                if (sender instanceof Player player) {
                    MessageUtil.send(player, "no-permission");
                } else {
                    sender.sendMessage("No permission.");
                }
                return true;
            }
            plugin.reload();
            if (sender instanceof Player player) {
                MessageUtil.send(player, "reload-success");
            } else {
                sender.sendMessage("[NyaDomPublic] Reloaded.");
            }
            return true;
        }

        if (!(sender instanceof Player player)) {
            sender.sendMessage("[NyaDomPublic] This command is player-only.");
            return true;
        }
        if (!player.hasPermission("nyadompublic.use")) {
            MessageUtil.send(player, "no-permission");
            return true;
        }
        BoardGui.open(player, false);
        return true;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, @NotNull String[] args) {
        List<String> completions = new ArrayList<>();
        if (args.length == 1) {
            if (sender.hasPermission("nyadompublic.admin") && "reload".startsWith(args[0].toLowerCase())) {
                completions.add("reload");
            }
        }
        return completions;
    }
}
