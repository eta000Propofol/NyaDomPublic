package com.nyadom.nyadompublic.listener;

import cn.lunadeer.dominion.api.dtos.DominionDTO;
import com.nyadom.nyadompublic.MessageUtil;
import com.nyadom.nyadompublic.NyaDomPublicPlugin;
import com.nyadom.nyadompublic.data.ListingEntry;
import com.nyadom.nyadompublic.data.ListingManager;
import com.nyadom.nyadompublic.gui.ManageGui;
import io.papermc.paper.event.player.AsyncChatEvent;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class ChatListener implements Listener {

    private static final long TIMEOUT_TICKS = 20L * 60L;

    private final NyaDomPublicPlugin plugin;
    private final Map<UUID, PendingEdit> pendingEdits = new ConcurrentHashMap<>();

    public ChatListener(NyaDomPublicPlugin plugin) {
        this.plugin = plugin;
    }

    public void beginDescriptionEdit(Player player, int dominionId) {
        UUID uuid = player.getUniqueId();
        cancelPending(uuid);

        PendingEdit pending = new PendingEdit(dominionId);
        pending.task = new BukkitRunnable() {
            @Override
            public void run() {
                PendingEdit current = pendingEdits.get(uuid);
                if (current == pending) {
                    pendingEdits.remove(uuid);
                    MessageUtil.send(player, "edit-desc-timeout");
                }
            }
        };
        pending.task.runTaskLater(plugin, TIMEOUT_TICKS);
        pendingEdits.put(uuid, pending);

        MessageUtil.send(player, "edit-desc-prompt",
                "{max}", String.valueOf(plugin.getSettings().getDescriptionMaxLength()));
    }

    @EventHandler
    public void onChat(AsyncChatEvent event) {
        Player player = event.getPlayer();
        PendingEdit pending = pendingEdits.get(player.getUniqueId());
        if (pending == null) {
            return;
        }

        event.setCancelled(true);
        pendingEdits.remove(player.getUniqueId());
        if (pending.task != null) {
            pending.task.cancel();
        }

        String text = PlainTextComponentSerializer.plainText().serialize(event.message()).trim();
        Bukkit.getScheduler().runTask(plugin, () -> applyDescription(player, pending.dominionId, text));
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        cancelPending(event.getPlayer().getUniqueId());
    }

    public void clearPendingEdits() {
        for (UUID uuid : pendingEdits.keySet()) {
            cancelPending(uuid);
        }
    }

    private void applyDescription(Player player, int dominionId, String text) {
        ListingManager manager = plugin.getListingManager();
        ListingEntry entry = manager.findByDominionId(dominionId).orElse(null);
        DominionDTO dto = plugin.getDominionService().getDominion(dominionId);
        if (entry == null || dto == null) {
            MessageUtil.send(player, "error-generic");
            return;
        }

        int maxLength = plugin.getSettings().getDescriptionMaxLength();
        String sanitized = text
                .replace('\n', ' ')
                .replace('\r', ' ')
                .trim();
        if (sanitized.length() > maxLength) {
            sanitized = sanitized.substring(0, maxLength);
        }

        entry.setDescription(sanitized);
        manager.save();
        MessageUtil.send(player, "edit-desc-saved");
        Bukkit.getScheduler().runTask(plugin, () -> ManageGui.open(player, dominionId));
    }

    private void cancelPending(UUID uuid) {
        PendingEdit pending = pendingEdits.remove(uuid);
        if (pending != null && pending.task != null) {
            pending.task.cancel();
        }
    }

    private static final class PendingEdit {
        private final int dominionId;
        private BukkitRunnable task;

        private PendingEdit(int dominionId) {
            this.dominionId = dominionId;
        }
    }
}
