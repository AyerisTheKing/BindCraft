package com.ayeristheking.bindcraft.input;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

/**
 * An ordered, immutable-snapshot set of keys that form a trigger combination.
 * Order matters for display purposes but matching is order-independent.
 */
public final class KeyCombination {

    private final List<InputKey> keys = new ArrayList<>();

    // --- Mutation (used during recording only) ---

    public void addKey(InputKey key) {
        if (!keys.contains(key)) {
            keys.add(key);
        }
    }

    public void removeKey(InputKey key) {
        keys.remove(key);
    }

    public void clear() {
        keys.clear();
    }

    /** Replaces the contents of this combination with the given list. */
    public void setKeys(List<InputKey> newKeys) {
        keys.clear();
        keys.addAll(newKeys);
    }

    // --- Query ---

    public List<InputKey> getKeys() {
        return Collections.unmodifiableList(keys);
    }

    public boolean isEmpty() {
        return keys.isEmpty();
    }

    /**
     * Returns true when the held-key set contains exactly all keys in this combination
     * (and the combination is non-empty).
     */
    public boolean matches(Set<InputKey> heldKeys) {
        if (keys.isEmpty()) return false;
        return heldKeys.containsAll(keys);
    }

    // --- Object ---

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof KeyCombination other)) return false;
        return keys.equals(other.keys);
    }

    @Override
    public int hashCode() {
        return keys.hashCode();
    }

    @Override
    public String toString() {
        if (keys.isEmpty()) return "Not set";
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < keys.size(); i++) {
            sb.append(keys.get(i).getName());
            if (i < keys.size() - 1) sb.append(" + ");
        }
        return sb.toString();
    }
}