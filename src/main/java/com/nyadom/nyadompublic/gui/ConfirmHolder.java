package com.nyadom.nyadompublic.gui;

public final class ConfirmHolder extends GuiHolder {

    private final int dominionId;

    public ConfirmHolder(int dominionId) {
        this.dominionId = dominionId;
    }

    public int getDominionId() {
        return dominionId;
    }
}
