package com.ayeristheking.bindcraft.binding;

import com.ayeristheking.bindcraft.action.ActionExecutor;
import com.ayeristheking.bindcraft.input.InputKey;
import com.ayeristheking.bindcraft.input.InputManager;
import com.ayeristheking.bindcraft.input.KeyCombination;
import com.ayeristheking.bindcraft.profile.Profile;
import com.ayeristheking.bindcraft.profile.ProfileFolder;
import com.ayeristheking.bindcraft.profile.ProfileManager;

import java.util.HashSet;
import java.util.Set;

/**
 * Evaluates bindings against the live held-key state.
 * Called from InputListener on every key-down / key-up event.
 */
public final class BindingManager {

    /**
     * Tracks trigger combos that have already fired in the current press session.
     * Reset when any key in the combo is released.
     */
    private static final Set<KeyCombination> firedCombos = new HashSet<>();

    private BindingManager() {}

    /**
     * Called when a key is pressed.
     * Checks all enabled bindings; fires any whose trigger matches the current held keys.
     */
    public static void onKeyDown(InputKey key) {
        Set<InputKey> heldKeys = InputManager.getHeldKeys();

        Profile active = ProfileManager.INSTANCE.getActiveProfile();
        if (active == null) return;

        for (ProfileFolder folder : active.getFolders()) {
            if (!folder.isEnabled()) continue;

            for (Binding binding : folder.getBindings()) {
                if (!binding.isEnabled()) continue;

                KeyCombination trigger = binding.getTrigger();
                if (trigger.isEmpty()) continue;

                if (trigger.matches(heldKeys) && !firedCombos.contains(trigger)) {
                    firedCombos.add(trigger);
                    ActionExecutor.execute(binding.getActions());
                }
            }
        }
    }

    /**
     * Called when a key is released.
     * Removes combos that included the released key from the fired-set,
     * allowing them to fire again on the next press.
     */
    public static void onKeyUp(InputKey key) {
        firedCombos.removeIf(combo -> combo.getKeys().contains(key));
    }

    /** Resets all fired-combo state (e.g. when focus changes). */
    public static void reset() {
        firedCombos.clear();
    }
}
