package com.ayeristheking.bindcraft.key;

import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.util.Identifier;
import org.lwjgl.glfw.GLFW;

public final class ModKeyBindings {

    /** The mod's keybinding category — uses Identifier as required by 1.21.11 API. */
    public static final KeyBinding.Category CATEGORY =
            new KeyBinding.Category(Identifier.of("bindcraft", "category"));

    public static final KeyBinding OPEN_MENU = KeyBindingHelper.registerKeyBinding(
            new KeyBinding(
                    "key.bindcraft.open_menu",
                    InputUtil.Type.KEYSYM,
                    GLFW.GLFW_KEY_L,
                    CATEGORY));

    private ModKeyBindings() {}

    /** Called during client init; static field initialisation registers the binding. */
    public static void register() {}
}