package com.ayeristheking.bindcraft.profile;

import java.util.ArrayList;
import java.util.List;

/**
 * A named collection of ProfileFolders; represents one independent keybinding configuration.
 */
public final class Profile {

    private String name;
    private final List<ProfileFolder> folders;

    public Profile(String name) {
        this.name = name;
        this.folders = new ArrayList<>();
    }

    public Profile(String name, List<ProfileFolder> folders) {
        this.name = name;
        this.folders = new ArrayList<>(folders);
    }

    public String getName()                { return name; }
    public List<ProfileFolder> getFolders(){ return folders; }

    public void setName(String name)       { this.name = name; }

    /** Returns the total count of enabled bindings across all enabled folders. */
    public int getTotalEnabledBindings() {
        int count = 0;
        for (ProfileFolder f : folders) {
            if (f.isEnabled()) count += f.getEnabledBindingCount();
        }
        return count;
    }
}
