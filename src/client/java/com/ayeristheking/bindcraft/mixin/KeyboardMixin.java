package com.ayeristheking.bindcraft.mixin;

import com.ayeristheking.bindcraft.listener.InputListener;
import net.minecraft.client.Keyboard;
import net.minecraft.client.input.KeyInput;
import org.lwjgl.glfw.GLFW;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Hooks into Keyboard.onKey to intercept all keyboard press/release events.
 * Signature for 1.21.11: onKey(long window, int action, KeyInput input)
 */
@Mixin(Keyboard.class)
public class KeyboardMixin {

    @Inject(method = "onKey", at = @At("HEAD"))
    private void BindCraft_onKey(long window, int action, KeyInput input, CallbackInfo ci) {
        // Only process PRESS and RELEASE; ignore REPEAT (action == 2)
        if (action == GLFW.GLFW_PRESS || action == GLFW.GLFW_RELEASE) {
            InputListener.onKeyEvent(input.key(), action);
        }
    }
}
