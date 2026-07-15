package com.king.attributeswap;

import com.king.attributeswap.gui.AttributeSwapScreen;
import com.king.attributeswap.input.InputManager;
import com.king.attributeswap.key.ModKeyBindings;
import com.king.attributeswap.profile.ProfileManager;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.MinecraftClient;

public class AttributeSwapClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {

        // Load all profiles from disk (creates a default profile if none exist)
        ProfileManager.INSTANCE.loadAll();

        // Register the menu keybinding
        ModKeyBindings.register();

        // End-of-tick handler: open menu when OPEN_MENU is pressed
        ClientTickEvents.END_CLIENT_TICK.register(client -> {

            // Clear held-key state when a screen opens (not attributeswap screen itself)
            if (client.currentScreen != null && !(client.currentScreen instanceof AttributeSwapScreen)) {
                InputManager.clearHeldKeys();
            }

            // Open the main screen
            while (ModKeyBindings.OPEN_MENU.wasPressed()) {
                if (client.currentScreen == null) {
                    MinecraftClient.getInstance().setScreen(new AttributeSwapScreen());
                }
            }
        });
    }
}