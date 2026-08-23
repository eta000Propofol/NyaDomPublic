package com.nyadom.nyadompublic.economy;

import com.nyadom.nyadompublic.NyaDomPublicPlugin;
import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.economy.EconomyResponse;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.RegisteredServiceProvider;

public final class EconomyService {

    private Economy economy;

    public EconomyService() {
        RegisteredServiceProvider<Economy> provider = Bukkit.getServicesManager().getRegistration(Economy.class);
        if (provider != null) {
            this.economy = provider.getProvider();
        }
    }

    public boolean isAvailable() {
        return economy != null;
    }

    public double getCost() {
        return NyaDomPublicPlugin.getInstance().getSettings().getCost();
    }

    public boolean withdraw(Player player, double amount) {
        if (!isAvailable()) {
            return false;
        }
        EconomyResponse response = economy.withdrawPlayer(player, amount);
        return response != null && response.transactionSuccess();
    }
}
