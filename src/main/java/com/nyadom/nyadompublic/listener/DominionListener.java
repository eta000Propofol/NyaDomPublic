package com.nyadom.nyadompublic.listener;

import cn.lunadeer.dominion.events.dominion.DominionDeleteEvent;
import com.nyadom.nyadompublic.NyaDomPublicPlugin;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public final class DominionListener implements Listener {

    private final NyaDomPublicPlugin plugin;

    public DominionListener(NyaDomPublicPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onDominionDelete(DominionDeleteEvent event) {
        if (event.getDominion() != null) {
            plugin.getListingManager().remove(event.getDominion().getId());
        }
    }
}
