package com.king.attributeswap.profile;

import com.king.attributeswap.binding.BindingManager;
import com.king.attributeswap.input.InputManager;
import com.king.attributeswap.storage.JsonStorage;

import java.util.ArrayList;
import java.util.List;

/**
 * Manages the list of profiles and the currently active profile.
 * Handles all CRUD operations and delegates persistence to JsonStorage.
 */
public final class ProfileManager {

    public static final ProfileManager INSTANCE = new ProfileManager();

    private final List<Profile> profiles = new ArrayList<>();
    private Profile activeProfile = null;

    private ProfileManager() {}

    // --- Lifecycle ---

    /** Loads all profiles from disk. Creates a default profile if none exist. */
    public void loadAll() {
        profiles.clear();
        profiles.addAll(JsonStorage.loadAll());

        if (profiles.isEmpty()) {
            Profile defaultProfile = new Profile("Default");
            defaultProfile.getFolders().add(new ProfileFolder("Custom Bindings"));
            profiles.add(defaultProfile);
            JsonStorage.save(defaultProfile);
        }

        activeProfile = profiles.get(0);
    }

    /** Saves all profiles to disk. */
    public void saveAll() {
        for (Profile profile : profiles) {
            JsonStorage.save(profile);
        }
    }

    /** Saves a single profile to disk. */
    public void save(Profile profile) {
        JsonStorage.save(profile);
    }

    // --- Profile access ---

    public List<Profile> getProfiles() { return profiles; }

    public Profile getActiveProfile()  { return activeProfile; }

    public void setActiveProfile(Profile profile) {
        this.activeProfile = profile;
        // Clear input state so held keys from the previous context don't bleed over
        InputManager.clearHeldKeys();
        BindingManager.reset();
    }

    // --- CRUD ---

    /**
     * Adds a new profile with the given name.
     * Returns the created profile.
     */
    public Profile addProfile(String name) {
        Profile profile = new Profile(name);
        profile.getFolders().add(new ProfileFolder("Custom Bindings"));
        profiles.add(profile);
        JsonStorage.save(profile);
        return profile;
    }

    /**
     * Renames a profile (updates the in-memory name and moves the JSON file).
     */
    public void renameProfile(Profile profile, String newName) {
        String oldName = profile.getName();
        profile.setName(newName);
        JsonStorage.rename(oldName, newName, profile);
    }

    /**
     * Deletes a profile. If it was the active profile, switches to the first remaining one.
     */
    public void deleteProfile(Profile profile) {
        JsonStorage.delete(profile.getName());
        profiles.remove(profile);

        if (activeProfile == profile) {
            activeProfile = profiles.isEmpty() ? null : profiles.get(0);
        }
    }
}
