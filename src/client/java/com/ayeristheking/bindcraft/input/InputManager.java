package com.ayeristheking.bindcraft.input;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Manages two independent input states:
 * 1. A live set of currently held keys (used by BindingManager to match triggers).
 * 2. A recording session (used by the GUI trigger selector).
 */
public final class InputManager {

    /** Snapshot of all keys currently held down (keyboard + mouse). */
    private static final Set<InputKey> HELD_KEYS = new HashSet<>();

    /** Buffer built up during a recording session. */
    private static final KeyCombination RECORDING_BUFFER = new KeyCombination();

    private static boolean recording = false;

    private InputManager() {}

    // --- Live held-key tracking ---

    /** Called when a key is physically pressed. */
    public static void onKeyDown(InputKey key) {
        HELD_KEYS.add(key);
        if (recording) RECORDING_BUFFER.addKey(key);
    }

    /** Called when a key is physically released. */
    public static void onKeyUp(InputKey key) {
        HELD_KEYS.remove(key);
        if (recording) RECORDING_BUFFER.removeKey(key);
    }

    /** Returns an unmodifiable snapshot of currently held keys. */
    public static Set<InputKey> getHeldKeys() {
        return Collections.unmodifiableSet(HELD_KEYS);
    }

    /** Clears all held-key state (call when focus is lost or a screen opens). */
    public static void clearHeldKeys() {
        HELD_KEYS.clear();
    }

    // --- Recording session ---

    /** Starts a new recording session; clears any previous buffer. */
    public static void startRecording() {
        RECORDING_BUFFER.clear();
        recording = true;
    }

    /**
     * Stops the recording session and returns a snapshot of the recorded combination.
     * The returned combination contains the keys that were pressed during recording.
     */
    public static KeyCombination stopRecording() {
        recording = false;
        KeyCombination result = new KeyCombination();
        result.setKeys(RECORDING_BUFFER.getKeys());
        return result;
    }

    public static boolean isRecording() {
        return recording;
    }

    /** Returns the live recording buffer (unmodifiable view). */
    public static List<InputKey> getRecordingKeys() {
        return RECORDING_BUFFER.getKeys();
    }
}