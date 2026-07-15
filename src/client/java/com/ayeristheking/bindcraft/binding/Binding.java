package com.ayeristheking.bindcraft.binding;

import com.ayeristheking.bindcraft.action.Action;
import com.ayeristheking.bindcraft.input.KeyCombination;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * A single user-defined key binding: trigger → list of actions.
 */
public final class Binding {

    private final UUID id;
    private String name;
    private boolean enabled;
    private KeyCombination trigger;
    private final List<Action> actions;

    /** Creates a new binding with a generated UUID and default values. */
    public Binding(String name) {
        this.id = UUID.randomUUID();
        this.name = name;
        this.enabled = true;
        this.trigger = new KeyCombination();
        this.actions = new ArrayList<>();
    }

    /** Deserialization constructor. */
    public Binding(UUID id, String name, boolean enabled, KeyCombination trigger, List<Action> actions) {
        this.id = id;
        this.name = name;
        this.enabled = enabled;
        this.trigger = trigger;
        this.actions = new ArrayList<>(actions);
    }

    public UUID getId()            { return id; }
    public String getName()        { return name; }
    public boolean isEnabled()     { return enabled; }
    public KeyCombination getTrigger() { return trigger; }
    public List<Action> getActions()   { return actions; }

    public void setName(String name)           { this.name = name; }
    public void setEnabled(boolean enabled)    { this.enabled = enabled; }
    public void setTrigger(KeyCombination trigger) { this.trigger = trigger; }
}
