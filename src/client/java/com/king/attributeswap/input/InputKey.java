package com.king.attributeswap.input;

import org.lwjgl.glfw.GLFW;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * A single physical keyboard key or mouse button.
 * Immutable value object identified by (code, mouse).
 *
 * Stores two name representations:
 *  - displayName  : human-readable label shown in the GUI ("Left Ctrl", "Mouse5")
 *  - glfwName     : canonical identifier stored in JSON ("LEFT_CONTROL", "MOUSE_BUTTON_5")
 */
public final class InputKey {

    // Registry: glfw key code → InputKey (keyboard)
    private static final Map<Integer, InputKey> KEY_REGISTRY   = new HashMap<>();
    // Registry: button index → InputKey (mouse, 0-based)
    private static final Map<Integer, InputKey> MOUSE_REGISTRY = new HashMap<>();
    // Registry: glfwName → InputKey (for JSON deserialization)
    private static final Map<String,  InputKey> BY_GLFW_NAME   = new HashMap<>();

    static {
        // Keyboard punctuation / special keys
        regKey(GLFW.GLFW_KEY_SPACE,         "SPACE",         "Space");
        regKey(GLFW.GLFW_KEY_APOSTROPHE,    "APOSTROPHE",    "'");
        regKey(GLFW.GLFW_KEY_COMMA,         "COMMA",         ",");
        regKey(GLFW.GLFW_KEY_MINUS,         "MINUS",         "-");
        regKey(GLFW.GLFW_KEY_PERIOD,        "PERIOD",        ".");
        regKey(GLFW.GLFW_KEY_SLASH,         "SLASH",         "/");
        regKey(GLFW.GLFW_KEY_SEMICOLON,     "SEMICOLON",     ";");
        regKey(GLFW.GLFW_KEY_EQUAL,         "EQUAL",         "=");
        regKey(GLFW.GLFW_KEY_LEFT_BRACKET,  "LEFT_BRACKET",  "[");
        regKey(GLFW.GLFW_KEY_BACKSLASH,     "BACKSLASH",     "\\");
        regKey(GLFW.GLFW_KEY_RIGHT_BRACKET, "RIGHT_BRACKET", "]");
        regKey(GLFW.GLFW_KEY_GRAVE_ACCENT,  "GRAVE_ACCENT",  "`");
        // Control keys
        regKey(GLFW.GLFW_KEY_ESCAPE,        "ESCAPE",        "Escape");
        regKey(GLFW.GLFW_KEY_ENTER,         "ENTER",         "Enter");
        regKey(GLFW.GLFW_KEY_TAB,           "TAB",           "Tab");
        regKey(GLFW.GLFW_KEY_BACKSPACE,     "BACKSPACE",     "Backspace");
        regKey(GLFW.GLFW_KEY_INSERT,        "INSERT",        "Insert");
        regKey(GLFW.GLFW_KEY_DELETE,        "DELETE",        "Delete");
        // Arrow keys
        regKey(GLFW.GLFW_KEY_RIGHT,         "RIGHT",         "Right");
        regKey(GLFW.GLFW_KEY_LEFT,          "LEFT",          "Left");
        regKey(GLFW.GLFW_KEY_DOWN,          "DOWN",          "Down");
        regKey(GLFW.GLFW_KEY_UP,            "UP",            "Up");
        // Navigation
        regKey(GLFW.GLFW_KEY_PAGE_UP,       "PAGE_UP",       "Page Up");
        regKey(GLFW.GLFW_KEY_PAGE_DOWN,     "PAGE_DOWN",     "Page Down");
        regKey(GLFW.GLFW_KEY_HOME,          "HOME",          "Home");
        regKey(GLFW.GLFW_KEY_END,           "END",           "End");
        regKey(GLFW.GLFW_KEY_CAPS_LOCK,     "CAPS_LOCK",     "Caps Lock");
        // Modifier keys
        regKey(GLFW.GLFW_KEY_LEFT_SHIFT,    "LEFT_SHIFT",    "Left Shift");
        regKey(GLFW.GLFW_KEY_LEFT_CONTROL,  "LEFT_CONTROL",  "Left Ctrl");
        regKey(GLFW.GLFW_KEY_LEFT_ALT,      "LEFT_ALT",      "Left Alt");
        regKey(GLFW.GLFW_KEY_LEFT_SUPER,    "LEFT_SUPER",    "Left Super");
        regKey(GLFW.GLFW_KEY_RIGHT_SHIFT,   "RIGHT_SHIFT",   "Right Shift");
        regKey(GLFW.GLFW_KEY_RIGHT_CONTROL, "RIGHT_CONTROL", "Right Ctrl");
        regKey(GLFW.GLFW_KEY_RIGHT_ALT,     "RIGHT_ALT",     "Right Alt");
        regKey(GLFW.GLFW_KEY_RIGHT_SUPER,   "RIGHT_SUPER",   "Right Super");
        // Letters A–Z
        for (int c = GLFW.GLFW_KEY_A; c <= GLFW.GLFW_KEY_Z; c++) {
            String letter = String.valueOf((char) c);
            regKey(c, letter, letter);
        }
        // Digits 0–9
        for (int c = GLFW.GLFW_KEY_0; c <= GLFW.GLFW_KEY_9; c++) {
            String digit = String.valueOf((char) c);
            regKey(c, digit, digit);
        }
        // F1–F12
        for (int i = 0; i < 12; i++) {
            String fn = "F" + (i + 1);
            regKey(GLFW.GLFW_KEY_F1 + i, fn, fn);
        }
        // Numpad 0–9
        for (int i = 0; i <= 9; i++) {
            regKey(GLFW.GLFW_KEY_KP_0 + i, "KP_" + i, "Numpad " + i);
        }
        // Mouse buttons 1–8 (GLFW button index is 0-based; JSON name is 1-based)
        for (int b = 0; b < 8; b++) {
            regMouse(b, "MOUSE_BUTTON_" + (b + 1), "Mouse" + (b + 1));
        }
    }

    // ─── Registry helpers ─────────────────────────────────────────────────────

    private static void regKey(int code, String glfwName, String displayName) {
        InputKey key = new InputKey(code, false, displayName, glfwName);
        KEY_REGISTRY.put(code, key);
        BY_GLFW_NAME.put(glfwName, key);
    }

    private static void regMouse(int button, String glfwName, String displayName) {
        InputKey key = new InputKey(button, true, displayName, glfwName);
        MOUSE_REGISTRY.put(button, key);
        BY_GLFW_NAME.put(glfwName, key);
    }

    // ─── Instance fields ──────────────────────────────────────────────────────

    private final int     code;
    private final boolean mouse;
    private final String  displayName;
    private final String  glfwName;

    private InputKey(int code, boolean mouse, String displayName, String glfwName) {
        this.code        = code;
        this.mouse       = mouse;
        this.displayName = Objects.requireNonNull(displayName);
        this.glfwName    = Objects.requireNonNull(glfwName);
    }

    // ─── Factory methods ──────────────────────────────────────────────────────

    /** Returns the InputKey for a keyboard GLFW keycode. Creates a generic entry if unknown. */
    public static InputKey ofKey(int glfwCode) {
        return KEY_REGISTRY.computeIfAbsent(glfwCode,
                c -> new InputKey(c, false, "Key " + c, "KEY_" + c));
    }

    /** Returns the InputKey for a 0-based GLFW mouse button. Creates a generic entry if unknown. */
    public static InputKey ofMouse(int button) {
        return MOUSE_REGISTRY.computeIfAbsent(button,
                b -> new InputKey(b, true, "Mouse" + (b + 1), "MOUSE_BUTTON_" + (b + 1)));
    }

    /**
     * Looks up an InputKey by its GLFW name (e.g. "LEFT_CONTROL", "MOUSE_BUTTON_5").
     * Used for JSON deserialization. Returns {@code null} if the name is unrecognised.
     */
    public static InputKey fromGlfwName(String name) {
        if (name == null) return null;
        InputKey k = BY_GLFW_NAME.get(name);
        if (k != null) return k;
        // Dynamic fallback for MOUSE_BUTTON_N
        if (name.startsWith("MOUSE_BUTTON_")) {
            try { return ofMouse(Integer.parseInt(name.substring(13)) - 1); }
            catch (NumberFormatException ignored) {}
        }
        // Dynamic fallback for KEY_N (raw code)
        if (name.startsWith("KEY_")) {
            try { return ofKey(Integer.parseInt(name.substring(4))); }
            catch (NumberFormatException ignored) {}
        }
        return null;
    }

    // ─── Accessors ────────────────────────────────────────────────────────────

    public int     getCode()     { return code; }
    public boolean isMouse()     { return mouse; }
    public boolean isKeyboard()  { return !mouse; }
    /** Human-readable label for GUI display (e.g. "Left Ctrl", "Mouse5"). */
    public String  getName()     { return displayName; }
    /** Canonical GLFW-style name used in JSON storage (e.g. "LEFT_CONTROL", "MOUSE_BUTTON_5"). */
    public String  getGlfwName() { return glfwName; }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof InputKey other)) return false;
        return code == other.code && mouse == other.mouse;
    }

    @Override
    public int hashCode() { return Objects.hash(code, mouse); }

    @Override
    public String toString() { return displayName; }
}