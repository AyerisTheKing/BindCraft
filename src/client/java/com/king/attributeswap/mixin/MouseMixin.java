package com.king.attributeswap.mixin;

import com.king.attributeswap.listener.InputListener;
import net.minecraft.client.Mouse;
import net.minecraft.client.input.MouseInput;
import org.lwjgl.glfw.GLFW;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Hooks into Mouse.onMouseButton to intercept all mouse button press/release events.
 * Signature for 1.21.11: onMouseButton(long window, MouseInput input, int action)
 */
@Mixin(Mouse.class)
public class MouseMixin {

    @Inject(method = "onMouseButton", at = @At("HEAD"))
    private void attributeswap_onMouseButton(long window, MouseInput input, int action, CallbackInfo ci) {
        // Only process PRESS and RELEASE
        if (action == GLFW.GLFW_PRESS || action == GLFW.GLFW_RELEASE) {
            InputListener.onMouseEvent(input.button(), action);
        }
    }
}
