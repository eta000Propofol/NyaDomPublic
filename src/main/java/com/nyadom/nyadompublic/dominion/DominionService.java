package com.nyadom.nyadompublic.dominion;

import cn.lunadeer.dominion.api.DominionAPI;
import cn.lunadeer.dominion.api.dtos.DominionDTO;
import cn.lunadeer.dominion.api.dtos.flag.Flags;
import cn.lunadeer.dominion.providers.TeleportProvider;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public final class DominionService {

    private final DominionAPI api;

    public DominionService() {
        this.api = DominionAPI.getInstance();
    }

    public boolean isAvailable() {
        return api != null;
    }

    public DominionDTO getDominion(int id) {
        if (!isAvailable()) {
            return null;
        }
        try {
            return api.getDominion(id);
        } catch (RuntimeException ignored) {
            return null;
        }
    }

    public List<DominionDTO> getListableDominions(UUID playerUuid) {
        Set<Integer> ids = new HashSet<>();
        List<DominionDTO> result = new ArrayList<>();
        if (!isAvailable()) {
            return result;
        }
        addUnique(result, ids, api.getPlayerOwnDominionDTOs(playerUuid));
        addUnique(result, ids, api.getPlayerAdminDominionDTOs(playerUuid));
        return result;
    }

    public Set<Integer> getManagedDominionIds(UUID playerUuid) {
        Set<Integer> ids = new HashSet<>();
        if (!isAvailable()) {
            return ids;
        }
        for (DominionDTO dto : api.getPlayerOwnDominionDTOs(playerUuid)) {
            ids.add(dto.getId());
        }
        for (DominionDTO dto : api.getPlayerAdminDominionDTOs(playerUuid)) {
            ids.add(dto.getId());
        }
        return ids;
    }

    public boolean canManage(Player player, DominionDTO dto) {
        if (dto == null) {
            return false;
        }
        if (dto.getOwner() != null && dto.getOwner().equals(player.getUniqueId())) {
            return true;
        }
        return getManagedDominionIds(player.getUniqueId()).contains(dto.getId());
    }

    public boolean canTeleport(Player player, DominionDTO dto) {
        if (!isAvailable() || dto == null) {
            return false;
        }
        try {
            if (!Flags.TELEPORT.getEnable()) {
                return false;
            }
            return api.checkPrivilegeFlagSilence(dto, Flags.TELEPORT, player);
        } catch (RuntimeException ignored) {
            return false;
        }
    }

    public String getOwnerName(DominionDTO dto) {
        if (dto == null || dto.getOwner() == null) {
            return "?";
        }
        try {
            return api.getPlayerName(dto.getOwner());
        } catch (RuntimeException ignored) {
            return dto.getOwner().toString();
        }
    }

    public CompletableFuture<Boolean> teleport(Player player, DominionDTO dto) {
        return TeleportProvider.getInstance().teleport(player, dto);
    }

    private void addUnique(List<DominionDTO> target, Set<Integer> ids, List<DominionDTO> source) {
        if (source == null) {
            return;
        }
        for (DominionDTO dto : source) {
            if (dto != null && ids.add(dto.getId())) {
                target.add(dto);
            }
        }
    }
}

