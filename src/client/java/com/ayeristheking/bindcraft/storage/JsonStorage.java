package com.ayeristheking.bindcraft.storage;

import com.google.gson.*;
import com.ayeristheking.bindcraft.action.Action;
import com.ayeristheking.bindcraft.action.ActionParameters;
import com.ayeristheking.bindcraft.action.ActionType;
import com.ayeristheking.bindcraft.binding.Binding;
import com.ayeristheking.bindcraft.input.InputKey;
import com.ayeristheking.bindcraft.input.KeyCombination;
import com.ayeristheking.bindcraft.profile.Profile;
import com.ayeristheking.bindcraft.profile.ProfileFolder;
import net.fabricmc.loader.api.FabricLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Reads and writes Profile JSON files.
 * Storage path: config/BindCraft/profile-{name}.json
 *
 * JSON format matches the spec exactly:
 *   trigger.keys → array of GLFW name strings, e.g. ["LEFT_CONTROL", "R"]
 */
public final class JsonStorage {

    private static final Logger LOGGER = LoggerFactory.getLogger("BindCraft");
    private static final Gson   GSON   = new GsonBuilder().setPrettyPrinting().create();

    private static final int FORMAT_VERSION  = 1;
    private static final int PROFILE_VERSION = 1;

    private static final Path CONFIG_DIR =
            FabricLoader.getInstance().getConfigDir().resolve("BindCraft");

    private JsonStorage() {}

    // ─── Public API ───────────────────────────────────────────────────────────

    public static void save(Profile profile) {
        ensureDir();
        Path file = CONFIG_DIR.resolve("profile-" + sanitize(profile.getName()) + ".json");
        try (Writer w = Files.newBufferedWriter(file)) {
            GSON.toJson(serializeProfile(profile), w);
        } catch (IOException e) {
            LOGGER.error("Failed to save profile '{}': {}", profile.getName(), e.getMessage());
        }
    }

    public static List<Profile> loadAll() {
        ensureDir();
        List<Profile> result = new ArrayList<>();
        try (var stream = Files.list(CONFIG_DIR)) {
            stream.filter(p -> {
                        String n = p.getFileName().toString();
                        return n.startsWith("profile-") && n.endsWith(".json");
                    })
                  .forEach(p -> {
                      Profile profile = load(p);
                      if (profile != null) result.add(profile);
                  });
        } catch (IOException e) {
            LOGGER.error("Failed to list profiles: {}", e.getMessage());
        }
        return result;
    }

    public static void delete(String name) {
        Path file = CONFIG_DIR.resolve("profile-" + sanitize(name) + ".json");
        try { Files.deleteIfExists(file); }
        catch (IOException e) { LOGGER.error("Failed to delete profile '{}': {}", name, e.getMessage()); }
    }

    /** Deletes the old file and saves under the new name. */
    public static void rename(String oldName, String newName, Profile profile) {
        delete(oldName);
        save(profile);
    }

    // ─── Serialization ────────────────────────────────────────────────────────

    private static JsonObject serializeProfile(Profile profile) {
        JsonObject root = new JsonObject();
        root.addProperty("formatVersion",  FORMAT_VERSION);
        root.addProperty("profileVersion", PROFILE_VERSION);
        root.addProperty("name",           profile.getName());

        JsonArray folders = new JsonArray();
        for (ProfileFolder folder : profile.getFolders()) folders.add(serializeFolder(folder));
        root.add("folders", folders);
        return root;
    }

    private static JsonObject serializeFolder(ProfileFolder folder) {
        JsonObject obj = new JsonObject();
        obj.addProperty("name",     folder.getName());
        obj.addProperty("enabled",  folder.isEnabled());
        obj.addProperty("expanded", folder.isExpanded());

        JsonArray bindings = new JsonArray();
        for (Binding b : folder.getBindings()) bindings.add(serializeBinding(b));
        obj.add("bindings", bindings);
        return obj;
    }

    private static JsonObject serializeBinding(Binding binding) {
        JsonObject obj = new JsonObject();
        obj.addProperty("id",      binding.getId().toString());
        obj.addProperty("name",    binding.getName());
        obj.addProperty("enabled", binding.isEnabled());
        obj.add("trigger", serializeTrigger(binding.getTrigger()));

        JsonArray actions = new JsonArray();
        for (Action a : binding.getActions()) actions.add(serializeAction(a));
        obj.add("actions", actions);
        return obj;
    }

    /**
     * Serializes the trigger as per spec:
     * <pre>
     * { "type": "KEY_COMBINATION", "keys": ["LEFT_CONTROL", "R"] }
     * </pre>
     */
    private static JsonObject serializeTrigger(KeyCombination trigger) {
        JsonObject obj  = new JsonObject();
        obj.addProperty("type", "KEY_COMBINATION");

        JsonArray keys = new JsonArray();
        for (InputKey key : trigger.getKeys()) {
            keys.add(key.getGlfwName()); // e.g. "LEFT_CONTROL", "MOUSE_BUTTON_5"
        }
        obj.add("keys", keys);
        return obj;
    }

    private static JsonObject serializeAction(Action action) {
        JsonObject obj = new JsonObject();
        obj.addProperty("type", action.getType().name());

        Map<String, Object> params = action.getParameters().toMap();
        if (!params.isEmpty()) {
            JsonObject paramsObj = new JsonObject();
            for (Map.Entry<String, Object> entry : params.entrySet()) {
                Object val = entry.getValue();
                if (val instanceof Number n) paramsObj.addProperty(entry.getKey(), n);
                else                         paramsObj.addProperty(entry.getKey(), val.toString());
            }
            obj.add("parameters", paramsObj);
        }
        return obj;
    }

    // ─── Deserialization ──────────────────────────────────────────────────────

    private static Profile load(Path file) {
        try (Reader r = Files.newBufferedReader(file)) {
            return deserializeProfile(GSON.fromJson(r, JsonObject.class));
        } catch (Exception e) {
            LOGGER.error("Failed to load profile from '{}': {}", file, e.getMessage());
            return null;
        }
    }

    private static Profile deserializeProfile(JsonObject root) {
        String name = root.get("name").getAsString();
        List<ProfileFolder> folders = new ArrayList<>();
        if (root.has("folders")) {
            for (JsonElement el : root.getAsJsonArray("folders")) {
                folders.add(deserializeFolder(el.getAsJsonObject()));
            }
        }
        return new Profile(name, folders);
    }

    private static ProfileFolder deserializeFolder(JsonObject obj) {
        String  name     = obj.get("name").getAsString();
        // Default enabled=true and expanded=true when keys are absent
        boolean enabled  = !obj.has("enabled")  || obj.get("enabled").getAsBoolean();
        boolean expanded = !obj.has("expanded")  || obj.get("expanded").getAsBoolean();

        List<Binding> bindings = new ArrayList<>();
        if (obj.has("bindings")) {
            for (JsonElement el : obj.getAsJsonArray("bindings")) {
                bindings.add(deserializeBinding(el.getAsJsonObject()));
            }
        }
        return new ProfileFolder(name, enabled, expanded, bindings);
    }

    private static Binding deserializeBinding(JsonObject obj) {
        UUID    id      = obj.has("id") ? UUID.fromString(obj.get("id").getAsString()) : UUID.randomUUID();
        String  name    = obj.get("name").getAsString();
        boolean enabled = !obj.has("enabled") || obj.get("enabled").getAsBoolean();

        KeyCombination trigger = new KeyCombination();
        if (obj.has("trigger")) {
            JsonObject triggerObj = obj.getAsJsonObject("trigger");
            if (triggerObj.has("keys")) {
                for (JsonElement keyEl : triggerObj.getAsJsonArray("keys")) {
                    InputKey key = null;
                    if (keyEl.isJsonPrimitive()) {
                        // Current format: string GLFW name, e.g. "LEFT_CONTROL"
                        key = InputKey.fromGlfwName(keyEl.getAsString());
                    } else if (keyEl.isJsonObject()) {
                        // Legacy format: {code, mouse, name} object
                        JsonObject keyObj = keyEl.getAsJsonObject();
                        int     code  = keyObj.get("code").getAsInt();
                        boolean mouse = keyObj.has("mouse") && keyObj.get("mouse").getAsBoolean();
                        key = mouse ? InputKey.ofMouse(code) : InputKey.ofKey(code);
                    }
                    if (key != null) trigger.addKey(key);
                }
            }
        }

        List<Action> actions = new ArrayList<>();
        if (obj.has("actions")) {
            for (JsonElement el : obj.getAsJsonArray("actions")) {
                Action action = deserializeAction(el.getAsJsonObject());
                if (action != null) actions.add(action);
            }
        }
        return new Binding(id, name, enabled, trigger, actions);
    }

    private static Action deserializeAction(JsonObject obj) {
        try {
            ActionType type = ActionType.valueOf(obj.get("type").getAsString());
            ActionParameters params = new ActionParameters();
            if (obj.has("parameters")) {
                for (Map.Entry<String, JsonElement> entry : obj.getAsJsonObject("parameters").entrySet()) {
                    JsonElement val = entry.getValue();
                    if (val.isJsonPrimitive()) {
                        JsonPrimitive prim = val.getAsJsonPrimitive();
                        if (prim.isNumber()) params.set(entry.getKey(), prim.getAsNumber());
                        else                 params.set(entry.getKey(), prim.getAsString());
                    }
                }
            }
            return new Action(type, params);
        } catch (IllegalArgumentException e) {
            LOGGER.warn("Skipping unknown action type: {}", obj.get("type").getAsString());
            return null;
        }
    }

    // ─── Helpers ──────────────────────────────────────────────────────────────

    private static void ensureDir() {
        try { Files.createDirectories(CONFIG_DIR); }
        catch (IOException e) { LOGGER.error("Cannot create config dir: {}", e.getMessage()); }
    }

    /** Strips characters that are invalid in file names. */
    private static String sanitize(String name) {
        return name.replaceAll("[^a-zA-Z0-9_\\-]", "_");
    }
}
