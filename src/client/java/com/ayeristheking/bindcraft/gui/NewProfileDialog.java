package com.ayeristheking.bindcraft.gui;

import net.minecraft.client.gui.screen.Screen;

import java.util.function.Consumer;

/**
 * @deprecated Replaced by {@link ProfileScreen}.
 */
@Deprecated
final class NewProfileDialog extends ProfileScreen {
    NewProfileDialog(Screen parent, Consumer<String> callback) {
        super(parent, callback);
    }
    NewProfileDialog(Screen parent, String prompt, Consumer<String> callback) {
        super(parent, prompt, callback);
    }
}
