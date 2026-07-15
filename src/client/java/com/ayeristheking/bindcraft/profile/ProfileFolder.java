package com.ayeristheking.bindcraft.profile;

import com.ayeristheking.bindcraft.binding.Binding;

import java.util.ArrayList;
import java.util.List;

/**
 * A named, toggleable folder inside a profile that groups related bindings.
 */
public final class ProfileFolder {

    private String name;
    private boolean enabled;
    private boolean expanded;
    private final List<Binding> bindings;

    public ProfileFolder(String name) {
        this.name = name;
        this.enabled = true;
        this.expanded = true;
        this.bindings = new ArrayList<>();
    }

    public ProfileFolder(String name, boolean enabled, boolean expanded, List<Binding> bindings) {
        this.name = name;
        this.enabled = enabled;
        this.expanded = expanded;
        this.bindings = new ArrayList<>(bindings);
    }

    public String getName()          { return name; }
    public boolean isEnabled()       { return enabled; }
    public boolean isExpanded()      { return expanded; }
    public List<Binding> getBindings() { return bindings; }

    public void setName(String name)         { this.name = name; }
    public void setEnabled(boolean enabled)  { this.enabled = enabled; }
    public void setExpanded(boolean expanded){ this.expanded = expanded; }

    public int getEnabledBindingCount() {
        int count = 0;
        for (Binding b : bindings) { if (b.isEnabled()) count++; }
        return count;
    }
}
