package com.ayeristheking.bindcraft.action;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Typed wrapper around a flat parameter map.
 * Handles int, String, and boolean parameter access with defaults.
 */
public final class ActionParameters {

    private final Map<String, Object> params;

    public ActionParameters() {
        this.params = new HashMap<>();
    }

    public ActionParameters(Map<String, Object> params) {
        this.params = new HashMap<>(params);
    }

    // --- Typed getters ---

    public int getInt(String key, int defaultValue) {
        Object val = params.get(key);
        if (val instanceof Number n) return n.intValue();
        if (val instanceof String s) {
            try { return Integer.parseInt(s); } catch (NumberFormatException ignored) {}
        }
        return defaultValue;
    }

    public String getString(String key, String defaultValue) {
        Object val = params.get(key);
        return val != null ? val.toString() : defaultValue;
    }

    // --- Setter ---

    public void set(String key, Object value) {
        params.put(key, value);
    }

    public void remove(String key) {
        params.remove(key);
    }

    /** Returns an unmodifiable view of the underlying map. */
    public Map<String, Object> toMap() {
        return Collections.unmodifiableMap(params);
    }

    /** Produces a mutable copy of the underlying map (for serialization). */
    public Map<String, Object> toMutableMap() {
        return new HashMap<>(params);
    }

    public boolean isEmpty() {
        return params.isEmpty();
    }
}
