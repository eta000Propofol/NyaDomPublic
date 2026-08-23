package com.nyadom.nyadompublic.listener;

import cn.lunadeer.dominion.api.dtos.DominionDTO;
import com.nyadom.nyadompublic.MessageUtil;
import com.nyadom.nyadompublic.NyaDomPublicPlugin;
import com.nyadom.nyadompublic.config.Settings;
import com.nyadom.nyadompublic.data.ListingEntry;
import com.nyadom.nyadompublic.data.ListingManager;
import com.nyadom.nyadompublic.dominion.DominionService;
import com.nyadom.nyadompublic.economy.EconomyService;
import com.nyadom.nyadompublic.gui.BoardGui;
import com.nyadom.nyadompublic.gui.BoardHolder;
import com.nyadom.nyadompublic.gui.ConfirmGui;
import com.nyadom.nyadompublic.gui.ConfirmHolder;
import com.nyadom.nyadompublic.gui.ManageGui;
import com.nyadom.nyadompublic.gui.ManageHolder;
import com.nyadom.nyadompublic.gui.SelectDominionGui;
import com.nyadom.nyadompublic.gui.SelectDominionHolder;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.TimeUnit;

public final class GuiListener implements Listener {

    private final NyaDomPublicPlugin plugin;
    private final Random random = new Random();

    public GuiListener(NyaDomPublicPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) {
            return;
        }
        Inventory top = event.getView().getTopInventory();
        if (!(top.getHolder() instanceof com.nyadom.nyadompublic.gui.GuiHolder)) {
            return;
        }
        event.setCancelled(true);

        int rawSlot = event.getRawSlot();
        if (rawSlot < 0 || rawSlot >= top.getSize()) {
            return;
        }

        if (top.getHolder() instanceof BoardHolder boardHolder) {
            handleBoardClick(player, rawSlot, boardHolder.isMineOnly());
        } else if (top.getHolder() instanceof SelectDominionHolder selectHolder) {
            handleSelectClick(player, rawSlot, selectHolder);
        } else if (top.getHolder() instanceof ManageHolder manageHolder) {
            handleManageClick(player, rawSlot, manageHolder.getDominionId());
        } else if (top.getHolder() instanceof ConfirmHolder confirmHolder) {
            handleConfirmClick(player, rawSlot, confirmHolder.getDominionId());
        }
    }

    private void handleBoardClick(Player player, int rawSlot, boolean mineOnly) {
        Settings settings = plugin.getSettings();
        DominionService dominionService = plugin.getDominionService();

        switch (rawSlot) {
            case 45 -> openLater(player, () -> {
                List<DominionDTO> dominions = dominionService.getListableDominions(player.getUniqueId());
                if (dominions.isEmpty()) {
                    MessageUtil.send(player, "no-dominion");
                    return;
                }
                SelectDominionGui.open(player, dominions, 0);
            });
            case 46 -> openLater(player, () -> BoardGui.open(player, true));
            case 47 -> openLater(player, () -> BoardGui.open(player, mineOnly));
            case 53 -> player.closeInventory();
            default -> {
                if (rawSlot >= 0 && rawSlot < settings.getMaxListings()) {
                    handleEntryClick(player, rawSlot, mineOnly);
                }
            }
        }
    }

    private void handleEntryClick(Player player, int index, boolean mineOnly) {
        ListingManager manager = plugin.getListingManager();
        DominionService dominionService = plugin.getDominionService();
        List<ListingEntry> shown = getShownEntries(player, mineOnly);
        if (index >= shown.size()) {
            return;
        }
        ListingEntry entry = shown.get(index);
        DominionDTO dto = dominionService.getDominion(entry.getDominionId());
        if (dto == null) {
            manager.purge(dominionService);
            BoardGui.open(player, mineOnly);
            return;
        }
        if (dominionService.canManage(player, dto)) {
            openLater(player, () -> ManageGui.open(player, entry.getDominionId()));
        } else {
            if (!dominionService.canTeleport(player, dto)) {
                MessageUtil.send(player, "teleport-denied");
                return;
            }
            player.closeInventory();
            dominionService.teleport(player, dto).thenAccept(success -> {
                if (!Boolean.TRUE.equals(success)) {
                    Bukkit.getScheduler().runTask(plugin, () -> {
                        if (player.isOnline()) {
                            MessageUtil.send(player, "teleport-failed");
                        }
                    });
                }
            });
        }
    }

    private List<ListingEntry> getShownEntries(Player player, boolean mineOnly) {
        ListingManager manager = plugin.getListingManager();
        DominionService dominionService = plugin.getDominionService();
        Set<Integer> managedIds = dominionService.getManagedDominionIds(player.getUniqueId());
        List<ListingEntry> shown = new ArrayList<>();
        for (ListingEntry entry : manager.getAll()) {
            if (shown.size() >= plugin.getSettings().getMaxListings()) {
                break;
            }
            if (mineOnly && !managedIds.contains(entry.getDominionId())) {
                continue;
            }
            if (dominionService.getDominion(entry.getDominionId()) != null) {
                shown.add(entry);
            }
        }
        return shown;
    }

    private void handleSelectClick(Player player, int rawSlot, SelectDominionHolder holder) {
        int page = holder.getPage();
        List<DominionDTO> dominions = holder.getDominions();

        if (rawSlot < SelectDominionGui.PAGE_SIZE) {
            int index = page * SelectDominionGui.PAGE_SIZE + rawSlot;
            if (index < dominions.size()) {
                tryList(player, dominions.get(index));
            }
            return;
        }

        int totalPages = Math.max(1, (int) Math.ceil(dominions.size() / (double) SelectDominionGui.PAGE_SIZE));
        switch (rawSlot) {
            case 45 -> {
                if (page > 0) {
                    openLater(player, () -> SelectDominionGui.open(player, dominions, page - 1));
                }
            }
            case 47 -> openLater(player, () -> BoardGui.open(player, false));
            case 53 -> {
                if (page + 1 < totalPages) {
                    openLater(player, () -> SelectDominionGui.open(player, dominions, page + 1));
                }
            }
            default -> {
            }
        }
    }

    private void tryList(Player player, DominionDTO dto) {
        NyaDomPublicPlugin plugin = NyaDomPublicPlugin.getInstance();
        Settings settings = plugin.getSettings();
        ListingManager manager = plugin.getListingManager();
        EconomyService economyService = plugin.getEconomyService();

        int dominionId = dto.getId();
        if (manager.isListed(dominionId)) {
            MessageUtil.send(player, "already-listed");
            return;
        }
        if (manager.size() >= settings.getMaxListings()) {
            MessageUtil.send(player, "board-full");
            return;
        }
        double cost = settings.getCost();
        if (!economyService.withdraw(player, cost)) {
            MessageUtil.send(player, "not-enough-money",
                    "{cost}", MessageUtil.formatMoney(cost),
                    "{currency}", settings.getCurrencyName());
            return;
        }

        String signType = randomSignType(settings);
        long createdAt = System.currentTimeMillis();
        int days = settings.getDurationDays();
        long expireAt = days < 0 ? -1L : createdAt + TimeUnit.DAYS.toMillis(days);
        ListingEntry entry = new ListingEntry(dominionId, signType, "", createdAt, expireAt);
        manager.add(entry);

        MessageUtil.send(player, "listed-success",
                "{cost}", MessageUtil.formatMoney(cost),
                "{currency}", settings.getCurrencyName());
        openLater(player, () -> BoardGui.open(player, false));
    }

    private String randomSignType(Settings settings) {
        List<String> configured = settings.getSignMaterials();
        List<String> valid = new ArrayList<>();
        for (String name : configured) {
            Material material = Material.matchMaterial(name);
            if (material != null && material.isItem()) {
                valid.add(name);
            }
        }
        if (valid.isEmpty()) {
            return "OAK_SIGN";
        }
        return valid.get(random.nextInt(valid.size()));
    }

    private void handleManageClick(Player player, int rawSlot, int dominionId) {
        switch (rawSlot) {
            case 3 -> {
                player.closeInventory();
                plugin.getChatListener().beginDescriptionEdit(player, dominionId);
            }
            case 5 -> openLater(player, () -> ConfirmGui.open(player, dominionId));
            case 8 -> openLater(player, () -> BoardGui.open(player, false));
            default -> {
            }
        }
    }

    private void handleConfirmClick(Player player, int rawSlot, int dominionId) {
        switch (rawSlot) {
            case 3 -> {
                plugin.getListingManager().remove(dominionId);
                MessageUtil.send(player, "removed-success");
                openLater(player, () -> BoardGui.open(player, false));
            }
            case 5 -> openLater(player, () -> ManageGui.open(player, dominionId));
            default -> {
            }
        }
    }

    private void openLater(Player player, Runnable action) {
        Bukkit.getScheduler().runTask(plugin, action);
    }
}



