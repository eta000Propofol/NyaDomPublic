package com.nyadom.nyadompublic;

import com.nyadom.nyadompublic.command.BoardCommand;
import com.nyadom.nyadompublic.config.Settings;
import com.nyadom.nyadompublic.data.ListingManager;
import com.nyadom.nyadompublic.dominion.DominionService;
import com.nyadom.nyadompublic.economy.EconomyService;
import com.nyadom.nyadompublic.listener.ChatListener;
import com.nyadom.nyadompublic.listener.DominionListener;
import com.nyadom.nyadompublic.listener.GuiListener;
import org.bukkit.Bukkit;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.java.JavaPlugin;

public final class NyaDomPublicPlugin extends JavaPlugin {

    private static NyaDomPublicPlugin instance;

    private Settings settings;
    private ListingManager listingManager;
    private DominionService dominionService;
    private EconomyService economyService;
    private ChatListener chatListener;

    @Override
    public void onEnable() {
        instance = this;

        settings = new Settings(this);
        settings.load();

        dominionService = new DominionService();
        if (!dominionService.isAvailable()) {
            getLogger().severe("Dominion is not enabled or its API is incompatible. Disabling NyaDomPublic.");
            Bukkit.getPluginManager().disablePlugin(this);
            return;
        }

        economyService = new EconomyService();
        if (!economyService.isAvailable()) {
            getLogger().severe("Vault economy is not available. Disabling NyaDomPublic.");
            Bukkit.getPluginManager().disablePlugin(this);
            return;
        }

        listingManager = new ListingManager(this);
        listingManager.load();
        listingManager.purge(dominionService);

        chatListener = new ChatListener(this);

        BoardCommand boardCommand = new BoardCommand(this);
        PluginCommand command = getCommand("board");
        if (command != null) {
            command.setExecutor(boardCommand);
            command.setTabCompleter(boardCommand);
        }

        getServer().getPluginManager().registerEvents(new GuiListener(this), this);
        getServer().getPluginManager().registerEvents(chatListener, this);
        getServer().getPluginManager().registerEvents(new DominionListener(this), this);

        long purgeIntervalTicks = 20L * 60L;
        Bukkit.getScheduler().runTaskTimer(this, () -> listingManager.purge(dominionService), purgeIntervalTicks, purgeIntervalTicks);

        getLogger().info("NyaDomPublic enabled.");
    }

    @Override
    public void onDisable() {
        if (listingManager != null) {
            listingManager.save();
        }
        instance = null;
        getLogger().info("NyaDomPublic disabled.");
    }

    public void reload() {
        settings.load();
        listingManager.load();
        listingManager.purge(dominionService);
        if (chatListener != null) {
            chatListener.clearPendingEdits();
        }
    }

    public static NyaDomPublicPlugin getInstance() {
        return instance;
    }

    public Settings getSettings() {
        return settings;
    }

    public ListingManager getListingManager() {
        return listingManager;
    }

    public DominionService getDominionService() {
        return dominionService;
    }

    public EconomyService getEconomyService() {
        return economyService;
    }

    public ChatListener getChatListener() {
        return chatListener;
    }
}
