package com.ayeristheking.bindcraft.action;

/**
 * A single action entry: a type plus optional parameters.
 */
public final class Action {

    private ActionType type;
    private ActionParameters parameters;

    public Action(ActionType type) {
        this.type = type;
        this.parameters = new ActionParameters();
    }

    public Action(ActionType type, ActionParameters parameters) {
        this.type = type;
        this.parameters = parameters;
    }

    public ActionType getType() { return type; }

    public ActionParameters getParameters() { return parameters; }

    public void setType(ActionType type) { this.type = type; }

    public void setParameters(ActionParameters parameters) { this.parameters = parameters; }

    /** Convenience: get an int parameter. */
    public int getInt(String key, int defaultValue) {
        return parameters.getInt(key, defaultValue);
    }

    /** Convenience: get a String parameter. */
    public String getString(String key, String defaultValue) {
        return parameters.getString(key, defaultValue);
    }
}
