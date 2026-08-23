package com.nyadom.nyadompublic.gui;

public final class BoardHolder extends GuiHolder {

    private final boolean mineOnly;

    public BoardHolder(boolean mineOnly) {
        this.mineOnly = mineOnly;
    }

    public boolean isMineOnly() {
        return mineOnly;
    }
}
