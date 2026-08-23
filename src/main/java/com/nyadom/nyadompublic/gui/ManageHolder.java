package com.nyadom.nyadompublic.gui;

public final class ManageHolder extends GuiHolder {

    private final int dominionId;

    public ManageHolder(int dominionId) {
        this.dominionId = dominionId;
    }

    public int getDominionId() {
        return dominionId;
    }
}
