package com.nyadom.nyadompublic.gui;

import cn.lunadeer.dominion.api.dtos.DominionDTO;

import java.util.ArrayList;
import java.util.List;

public final class SelectDominionHolder extends GuiHolder {

    private final int page;
    private final List<DominionDTO> dominions;

    public SelectDominionHolder(int page, List<DominionDTO> dominions) {
        this.page = page;
        this.dominions = new ArrayList<>(dominions);
    }

    public int getPage() {
        return page;
    }

    public List<DominionDTO> getDominions() {
        return dominions;
    }
}
