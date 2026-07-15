package com.king.attributeswap.listener;

import com.king.attributeswap.binding.BindingManager;
import com.king.attributeswap.input.InputKey;
import com.king.attributeswap.input.InputManager;
import net.minecraft.client.MinecraftClient;

/**
 * Central bridge between mixin hooks (KeyboardMixin, MouseMixin) and the mod logic.
 * The mixins call these static methods; this class routes to InputManager and BindingManager.
 */
public final class InputListener {

    private InputListener() {}

    /**
     * Called by KeyboardMixin when a keyboard key is pressed or released.
     *
     * @param glfwKey    GLFW key code
     * @param action     GLFW_PRESS (1) or GLFW_RELEASE (0)
     */
    public static void onKeyEvent(int glfwKey, int action) {
        if (glfwKey <= 0) return;

        InputKey key = InputKey.ofKey(glfwKey);

        if (action == 1) { // GLFW_PRESS
            InputManager.onKeyDown(key);
            if (shouldProcessBindings()) {
                BindingManager.onKeyDown(key);
            }
        } else if (action == 0) { // GLFW_RELEASE
            InputManager.onKeyUp(key);
            BindingManager.onKeyUp(key);
        }
    }

    /**
     * Called by MouseMixin when a mouse button is pressed or released.
     *
     * @param button GLFW mouse button index
     * @param action GLFW_PRESS (1) or GLFW_RELEASE (0)
     */
    public static void onMouseEvent(int button, int action) {
        if (button < 0) return;

        InputKey key = InputKey.ofMouse(button);

        if (action == 1) { // GLFW_PRESS
            InputManager.onKeyDown(key);
            if (shouldProcessBindings()) {
                BindingManager.onKeyDown(key);
            }
        } else if (action == 0) { // GLFW_RELEASE
            InputManager.onKeyUp(key);
            BindingManager.onKeyUp(key);
        }
    }

    /**
     * Bindings should only fire when no screen is open (or only the AttributeSwap screen is open
     * during recording — but recording is handled by the GUI itself, not via BindingManager).
     */
    private static boolean shouldProcessBindings() {
        MinecraftClient client = MinecraftClient.getInstance();
        return client.currentScreen == null && !InputManager.isRecording();
    }
}
