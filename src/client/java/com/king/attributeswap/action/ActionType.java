package com.king.attributeswap.action;

/**
 * All supported action types.
 * Each entry declares a display name and which parameter keys it requires.
 */
public enum ActionType {

    SELECT_SLOT("Select Hotbar Slot"),
    SELECT_PREVIOUS_SLOT("Select Previous Slot"),
    LEFT_CLICK("Left Click"),
    RIGHT_CLICK("Right Click"),
    DELAY("Delay (ticks)"),
    SEND_CHAT("Send Chat Message"),
    RUN_COMMAND("Run Client Command");

    private final String displayName;

    ActionType(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    @Override
    public String toString() {
        return displayName;
    }
}
