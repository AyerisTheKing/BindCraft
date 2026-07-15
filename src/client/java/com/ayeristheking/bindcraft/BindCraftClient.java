package com.ayeristheking.bindcraft;

import com.ayeristheking.bindcraft.gui.BindCraftScreen;
import com.ayeristheking.bindcraft.input.InputManager;
import com.ayeristheking.bindcraft.key.ModKeyBindings;
import com.ayeristheking.bindcraft.profile.ProfileManager;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.MinecraftClient;

public class BindCraftClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {

        // Load all profiles from disk (creates a default profile if none exist)
        ProfileManager.INSTANCE.loadAll();

        // Register the menu keybinding
        ModKeyBindings.register();

        // End-of-tick handler: open menu when OPEN_MENU is pressed
        ClientTickEvents.END_CLIENT_TICK.register(client -> {

            // Clear held-key state when a screen opens (not BindCraft screen itself)
            if (client.currentScreen != null && !(client.currentScreen instanceof BindCraftScreen)) {
                InputManager.clearHeldKeys();
            }

            // Open the main screen
            while (ModKeyBindings.OPEN_MENU.wasPressed()) {
                if (client.currentScreen == null) {
                    MinecraftClient.getInstance().setScreen(new BindCraftScreen());
                }
            }
        });
    }
}