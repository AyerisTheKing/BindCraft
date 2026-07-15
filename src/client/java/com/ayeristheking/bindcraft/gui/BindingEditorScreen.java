package com.ayeristheking.bindcraft.gui;

import com.ayeristheking.bindcraft.action.Action;
import com.ayeristheking.bindcraft.action.ActionType;
import com.ayeristheking.bindcraft.binding.Binding;
import com.ayeristheking.bindcraft.input.InputManager;
import com.ayeristheking.bindcraft.input.KeyCombination;
import com.ayeristheking.bindcraft.profile.ProfileFolder;
import com.ayeristheking.bindcraft.profile.ProfileManager;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.text.Text;

import java.util.ArrayList;
import java.util.List;

/**
 * Full-screen editor for a single Binding.
 *
 * Layout (top-down):
 *  - Title bar
 *  - Name text field (static widget)
 *  - Enabled toggle button (static widget)
 *  - Trigger record button (static widget)
 *  - Action list header + "Add Action" button (static widget)
 *  - Action list rows  ← drawn manually, clicks handled in mouseClicked
 *    Each row: [◄ TYPE ►]  [param label]  [param value (click to edit)]  [↑][↓][×]
 *  - Save / Cancel buttons (static widgets)
 *
 * No dynamic widget add/remove for the action list — avoids Screen.remove() issues.
 */
public class BindingEditorScreen extends Screen {

    // ─── Layout constants ──────────────────────────────────────────────────────
    private static final int ROW_H       = 22;
    private static final int HEADER_H    = 24;
    private static final int TOP_SECTION = HEADER_H + (ROW_H + 4) * 3 + 12; // title + 3 fixed rows + gap
    private static final int BOTTOM_H    = 28;
    private static final int SIDE_PAD    = 8;

    // ─── Colors ────────────────────────────────────────────────────────────────
    private static final int COL_TEXT  = 0xFFFFFFFF;
    private static final int COL_DIM   = 0xFFA0A0A0;
    private static final int COL_GREEN = 0xFF55FF55;
    private static final int COL_RED   = 0xFFFF5555;
    private static final int COL_BLUE  = 0xFF5599FF;
    private static final int COL_ROW   = 0xFF242424;
    private static final int COL_ROW_H = 0xFF2E2E2E;

    // ─── Recording ────────────────────────────────────────────────────────────
    private static final int IDLE_TIMEOUT = 40; // ticks before auto-stop

    // ─── State ────────────────────────────────────────────────────────────────
    private final Screen  parent;
    private final Binding binding;

    private final List<Action> workingActions;
    private KeyCombination     workingTrigger;
    private boolean            workingEnabled;

    private boolean recording     = false;
    private int     recordingIdle = 0;

    private int scrollOffset = 0;
    private int contentWidth;  // computed in init()
    private int listX;         // left edge of action list area
    private int listY;         // top of action list area
    private int listBottom;    // bottom of action list area

    // ─── Static widgets ────────────────────────────────────────────────────────
    private TextFieldWidget nameField;
    private ButtonWidget    btnEnabled;
    private ButtonWidget    btnTrigger;
    private ButtonWidget    btnSave;
    private ButtonWidget    btnCancel;
    private ButtonWidget    btnAddAction;

    // ─── Constructor ──────────────────────────────────────────────────────────

    /** @param folder The parent folder — retained for future use (e.g. folder-scoped exports). */
    public BindingEditorScreen(Screen parent, ProfileFolder folder, Binding binding) {
        super(Text.translatable("gui.BindCraft.edit_binding"));
        this.parent  = parent;
        this.binding = binding;

        this.workingActions = new ArrayList<>(binding.getActions());
        this.workingTrigger = cloneTrigger(binding.getTrigger());
        this.workingEnabled = binding.isEnabled();
    }

    // ─── Init ─────────────────────────────────────────────────────────────────

    @Override
    protected void init() {
        int cx = width / 2;
        contentWidth = Math.min(420, width - SIDE_PAD * 4);
        listX = cx - contentWidth / 2;
        int fieldW = contentWidth;

        int y = HEADER_H + 4;

        // --- Name ---
        nameField = new TextFieldWidget(textRenderer, listX, y, fieldW, 20, Text.translatable("gui.BindCraft.name"));
        nameField.setText(binding.getName());
        nameField.setMaxLength(64);
        addDrawableChild(nameField);
        y += ROW_H + 4;

        // --- Enabled toggle ---
        final boolean[] en = { workingEnabled };
        btnEnabled = ButtonWidget.builder(Text.literal(enabledLabel(en[0])), btn -> {
            en[0] = !en[0];
            workingEnabled = en[0];
            btn.setMessage(Text.literal(enabledLabel(en[0])));
        }).dimensions(listX, y, fieldW, 20).build();
        addDrawableChild(btnEnabled);
        y += ROW_H + 4;

        // --- Trigger ---
        btnTrigger = ButtonWidget.builder(Text.literal(triggerLabel()), this::onTriggerClick)
                .dimensions(listX, y, fieldW, 20).build();
        addDrawableChild(btnTrigger);
        y += ROW_H + 12;

        // --- Add Action ---
        btnAddAction = ButtonWidget.builder(Text.translatable("gui.BindCraft.add_action"), btn -> addAction())
                .dimensions(listX, y, 120, 20).build();
        addDrawableChild(btnAddAction);
        y += ROW_H + 4;

        listY     = y;
        listBottom = height - BOTTOM_H - 4;

        // --- Save / Cancel ---
        int bY = height - BOTTOM_H;
        int bW = 80;
        addDrawableChild(ButtonWidget.builder(Text.translatable("gui.BindCraft.save"), btn -> save())
                .dimensions(cx - bW - 4, bY, bW, 20).build());
        addDrawableChild(ButtonWidget.builder(Text.translatable("gui.BindCraft.cancel"), btn -> cancel())
                .dimensions(cx + 4, bY, bW, 20).build());
    }

    // ─── Render ───────────────────────────────────────────────────────────────

    @Override
    public void render(DrawContext ctx, int mx, int my, float delta) {

        // Title bar
        ctx.fill(0, 0, width, HEADER_H, 0xFF181818);
        ctx.drawCenteredTextWithShadow(textRenderer, Text.translatable("gui.BindCraft.edit_binding").getString(), width / 2, 8, COL_TEXT);

        // Static section labels (drawn left of the widgets)
        int labelX = listX - 2;
        int ly = HEADER_H + 4 + 6;
        ctx.drawTextWithShadow(textRenderer, Text.translatable("gui.BindCraft.name").getString(),    labelX - textRenderer.getWidth(Text.translatable("gui.BindCraft.name").getString()),    ly, COL_DIM);
        ly += ROW_H + 4;
        ctx.drawTextWithShadow(textRenderer, Text.translatable("gui.BindCraft.enabled").getString(), labelX - textRenderer.getWidth(Text.translatable("gui.BindCraft.enabled").getString()), ly, COL_DIM);
        ly += ROW_H + 4;
        ctx.drawTextWithShadow(textRenderer, Text.translatable("gui.BindCraft.trigger").getString(), labelX - textRenderer.getWidth(Text.translatable("gui.BindCraft.trigger").getString()), ly, COL_DIM);

        // Recording overlay (drawn on top of trigger area)
        if (recording) {
            String rec = InputManager.getRecordingKeys().isEmpty()
                    ? Text.translatable("gui.BindCraft.recording").getString()
                    : String.format(Text.translatable("gui.BindCraft.release_to_confirm").getString(), joinKeys());
            ctx.drawCenteredTextWithShadow(textRenderer, rec, width / 2, listY - ROW_H - 8, COL_GREEN);
        }

        // "Actions" header label
        ctx.drawTextWithShadow(textRenderer, Text.translatable("gui.BindCraft.actions").getString(), listX, listY - ROW_H - 2, COL_DIM);

        // Action list rows
        drawActionList(ctx, mx, my);

        // Draw static widgets on top
        super.render(ctx, mx, my, delta);
    }

    private void drawActionList(DrawContext ctx, int mx, int my) {
        int y = listY - scrollOffset;
        int rowW = contentWidth;

        for (int i = 0; i < workingActions.size(); i++) {
            if (y + ROW_H <= listY || y >= listBottom) { y += ROW_H + 2; continue; }

            Action action  = workingActions.get(i);
            boolean hovered = mx >= listX && mx < listX + rowW && my >= y && my < y + ROW_H;
            ctx.fill(listX, y, listX + rowW, y + ROW_H, hovered ? COL_ROW_H : COL_ROW);

            // ◄ type name ►
            ctx.drawTextWithShadow(textRenderer, "◄", listX + 4,  y + 7, COL_BLUE);
            ctx.drawTextWithShadow(textRenderer, action.getType().getDisplayName(), listX + 16, y + 7, COL_TEXT);
            ctx.drawTextWithShadow(textRenderer, "►", listX + 16 + 170, y + 7, COL_BLUE);

            // Parameter value (click-to-edit label in blue)
            String paramStr = buildParamString(action);
            if (!paramStr.isEmpty()) {
                int pX = listX + 16 + 182;
                ctx.drawTextWithShadow(textRenderer, paramStr, pX, y + 7, COL_BLUE);
            }

            // Move up / down / delete (right side)
            ctx.drawTextWithShadow(textRenderer, "↑", listX + rowW - 42, y + 7, COL_DIM);
            ctx.drawTextWithShadow(textRenderer, "↓", listX + rowW - 28, y + 7, COL_DIM);
            ctx.drawTextWithShadow(textRenderer, "×", listX + rowW - 14, y + 7, COL_RED);

            y += ROW_H + 2;
        }
    }

    // ─── Mouse ────────────────────────────────────────────────────────────────

    @Override
    public boolean mouseClicked(net.minecraft.client.gui.Click click, boolean doubled) {
        int button = click.buttonInfo().button();
        double mx = click.x();
        double my = click.y();
        if (button == 0 && handleActionListClick((int) mx, (int) my)) return true;
        return super.mouseClicked(click, doubled);
    }

    private boolean handleActionListClick(int mx, int my) {
        if (mx < listX || mx >= listX + contentWidth) return false;

        int y = listY - scrollOffset;
        for (int i = 0; i < workingActions.size(); i++) {
            if (my >= y && my < y + ROW_H) {
                Action action = workingActions.get(i);
                int rowW = contentWidth;

                // ◄ backward
                if (mx >= listX + 4 && mx < listX + 14) {
                    cycleType(i, -1); return true;
                }
                // ► forward
                if (mx >= listX + 16 + 170 && mx < listX + 16 + 185) {
                    cycleType(i, 1); return true;
                }
                // param value area → open dialog
                if (mx >= listX + 16 + 182 && mx < listX + rowW - 50) {
                    openParamDialog(i, action); return true;
                }
                // ↑ move up
                if (mx >= listX + rowW - 42 && mx < listX + rowW - 30 && i > 0) {
                    Action a = workingActions.remove(i);
                    workingActions.add(i - 1, a);
                    return true;
                }
                // ↓ move down
                if (mx >= listX + rowW - 28 && mx < listX + rowW - 16 && i < workingActions.size() - 1) {
                    Action a = workingActions.remove(i);
                    workingActions.add(i + 1, a);
                    return true;
                }
                // × delete
                if (mx >= listX + rowW - 14 && mx < listX + rowW) {
                    workingActions.remove(i); return true;
                }
            }
            y += ROW_H + 2;
        }
        return false;
    }

    @Override
    public boolean mouseScrolled(double mx, double my, double hAmount, double vAmount) {
        int maxScroll = Math.max(0, workingActions.size() * (ROW_H + 2) - (listBottom - listY));
        scrollOffset  = Math.max(0, Math.min(maxScroll, scrollOffset - (int)(vAmount * 10)));
        return true;
    }

    // ─── Keys ─────────────────────────────────────────────────────────────────

    @Override
    public boolean keyPressed(net.minecraft.client.input.KeyInput keyInput) {
        int keyCode = keyInput.key();
        if (keyCode == 256) { // ESC
            if (recording) { stopRecording(); return true; }
            cancel(); return true;
        }
        return super.keyPressed(keyInput);
    }

    // ─── Tick ─────────────────────────────────────────────────────────────────

    @Override
    public void tick() {
        super.tick();
        if (!recording) return;

        if (!InputManager.getRecordingKeys().isEmpty()) {
            recordingIdle = 0;
        } else {
            recordingIdle++;
            if (recordingIdle >= IDLE_TIMEOUT) stopRecording();
        }
    }

    // ─── Internal actions ─────────────────────────────────────────────────────

    private void onTriggerClick(ButtonWidget btn) {
        if (recording) { stopRecording(); }
        else           { startRecording(); }
    }

    private void startRecording() {
        recording     = true;
        recordingIdle = 0;
        InputManager.startRecording();
        btnTrigger.setMessage(Text.translatable("gui.BindCraft.recording_stop"));
    }

    private void stopRecording() {
        recording      = false;
        workingTrigger = InputManager.stopRecording();
        btnTrigger.setMessage(Text.literal(triggerLabel()));
    }

    private void addAction() {
        workingActions.add(new Action(ActionType.SELECT_SLOT));
        // Default slot = 1
        workingActions.get(workingActions.size() - 1).getParameters().set("slot", 1);
    }

    private void cycleType(int index, int dir) {
        ActionType[] types = ActionType.values();
        int next = (workingActions.get(index).getType().ordinal() + dir + types.length) % types.length;
        Action replacement = new Action(types[next]);
        // Set sensible defaults for the new type
        switch (types[next]) {
            case SELECT_SLOT  -> replacement.getParameters().set("slot",  1);
            case DELAY        -> replacement.getParameters().set("ticks", 1);
            default           -> {}
        }
        workingActions.set(index, replacement);
    }

    /**
     * Opens a text-input dialog for editing the parameter of the action at {@code index}.
     * For action types with no parameters (SELECT_PREVIOUS_SLOT, LEFT_CLICK, RIGHT_CLICK),
     * this does nothing.
     */
    private void openParamDialog(int index, Action action) {
        String key = paramKey(action.getType());
        if (key == null) return;

        String prompt = String.format(Text.translatable("gui.BindCraft.set_param").getString(), paramLabel(action.getType()));

        client.setScreen(new ProfileScreen(this, prompt, value -> {
            if (value != null && !value.isBlank()) {
                try {
                    // Numeric types (slot, ticks) → parse int; text types → store as string
                    if (action.getType() == ActionType.SELECT_SLOT
                            || action.getType() == ActionType.DELAY) {
                        workingActions.get(index).getParameters().set(key, Integer.parseInt(value.trim()));
                    } else {
                        workingActions.get(index).getParameters().set(key, value.trim());
                    }
                } catch (NumberFormatException ignored) {}
            }
        }));
    }

    private void save() {
        String name = nameField.getText().trim();
        if (name.isEmpty()) name = binding.getName();

        binding.setName(name);
        binding.setEnabled(workingEnabled);
        binding.setTrigger(workingTrigger);
        binding.getActions().clear();
        binding.getActions().addAll(workingActions);

        ProfileManager.INSTANCE.save(ProfileManager.INSTANCE.getActiveProfile());
        client.setScreen(parent);
    }

    private void cancel() {
        if (recording) InputManager.stopRecording();
        client.setScreen(parent);
    }

    // ─── Helpers ──────────────────────────────────────────────────────────────

    private String triggerLabel() {
        return workingTrigger.isEmpty() ? Text.translatable("gui.BindCraft.click_to_record").getString() : workingTrigger.toString();
    }

    private static String enabledLabel(boolean enabled) {
        return Text.translatable(enabled ? "gui.BindCraft.enabled_on" : "gui.BindCraft.enabled_off").getString();
    }

    private String joinKeys() {
        return InputManager.getRecordingKeys().stream()
                .map(k -> k.getName())
                .reduce((a, b) -> a + " + " + b)
                .orElse("...");
    }

    private String buildParamString(Action action) {
        String key = paramKey(action.getType());
        if (key == null) return "";
        String label = paramLabel(action.getType());
        String val   = action.getParameters().getString(key, "?");
        return "[" + label + ": " + val + "]";
    }

    private static String paramKey(ActionType type) {
        return switch (type) {
            case SELECT_SLOT  -> "slot";
            case DELAY        -> "ticks";
            case SEND_CHAT    -> "message";
            case RUN_COMMAND  -> "command";
            default           -> null;
        };
    }

    private static String paramLabel(ActionType type) {
        return switch (type) {
            case SELECT_SLOT  -> Text.translatable("gui.BindCraft.param.slot").getString();
            case DELAY        -> Text.translatable("gui.BindCraft.param.ticks").getString();
            case SEND_CHAT    -> Text.translatable("gui.BindCraft.param.message").getString();
            case RUN_COMMAND  -> Text.translatable("gui.BindCraft.param.command").getString();
            default           -> "";
        };
    }

    private static KeyCombination cloneTrigger(KeyCombination source) {
        KeyCombination copy = new KeyCombination();
        copy.setKeys(source.getKeys());
        return copy;
    }

    @Override
    public boolean shouldPause() { return false; }
}
