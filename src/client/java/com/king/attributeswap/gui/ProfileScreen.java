package com.king.attributeswap.gui;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.text.Text;

import java.util.function.Consumer;

/**
 * A dedicated screen for single-line text input (profile name, folder name, etc.).
 * Provides a prompt label, a text field, and OK / Cancel buttons.
 * The result is delivered via a {@code Consumer<String>} callback;
 * {@code null} is passed when the user cancels.
 */
public class ProfileScreen extends Screen {

    private static final int BOX_W = 260;
    private static final int BOX_H = 96;

    private final Screen          parent;
    private final String          prompt;
    private final Consumer<String> callback;

    private TextFieldWidget textField;

    /** Opens with the default "Enter name:" prompt. */
    public ProfileScreen(Screen parent, Consumer<String> callback) {
        this(parent, Text.translatable("gui.attributeswap.prompt.enter_name").getString(), callback);
    }

    /** Opens with a custom prompt (e.g. "Rename: PvP"). */
    public ProfileScreen(Screen parent, String prompt, Consumer<String> callback) {
        super(Text.translatable("gui.attributeswap.title"));
        this.parent   = parent;
        this.prompt   = prompt;
        this.callback = callback;
    }

    @Override
    protected void init() {
        int cx = (width  - BOX_W) / 2;
        int cy = (height - BOX_H) / 2;
        int bW = (BOX_W - 24) / 2;

        textField = new TextFieldWidget(textRenderer, cx + 8, cy + 34, BOX_W - 16, 20,
                Text.translatable("gui.attributeswap.name"));
        textField.setMaxLength(64);
        textField.setFocused(true);
        addDrawableChild(textField);

        addDrawableChild(ButtonWidget.builder(Text.translatable("gui.attributeswap.ok"), btn -> confirm())
                .dimensions(cx + 8, cy + BOX_H - 28, bW, 20).build());

        addDrawableChild(ButtonWidget.builder(Text.translatable("gui.attributeswap.cancel"), btn -> cancel())
                .dimensions(cx + 8 + bW + 8, cy + BOX_H - 28, bW, 20).build());
    }

    @Override
    public void render(DrawContext ctx, int mx, int my, float delta) {
        renderBackground(ctx, mx, my, delta);

        int cx = (width  - BOX_W) / 2;
        int cy = (height - BOX_H) / 2;

        // Dialog background + border
        ctx.fill(cx,             cy,             cx + BOX_W,     cy + BOX_H,     0xFF1A1A1A);
        ctx.fill(cx,             cy,             cx + BOX_W,     cy + 1,         0xFF666666);
        ctx.fill(cx,             cy + BOX_H - 1, cx + BOX_W,     cy + BOX_H,     0xFF666666);
        ctx.fill(cx,             cy,             cx + 1,         cy + BOX_H,     0xFF666666);
        ctx.fill(cx + BOX_W - 1, cy,             cx + BOX_W,     cy + BOX_H,     0xFF666666);

        // Title bar stripe
        ctx.fill(cx, cy, cx + BOX_W, cy + 20, 0xFF141414);
        ctx.drawCenteredTextWithShadow(textRenderer, prompt, cx + BOX_W / 2, cy + 6, 0xFFFFFFFF);

        super.render(ctx, mx, my, delta);
    }

    @Override
    public boolean keyPressed(net.minecraft.client.input.KeyInput keyInput) {
        int keyCode = keyInput.key();
        if (keyCode == 257 || keyCode == 335) { confirm(); return true; } // Enter / KP Enter
        if (keyCode == 256)                   { cancel();  return true; } // Escape
        return super.keyPressed(keyInput);
    }

    private void confirm() {
        String value = textField.getText().trim();
        callback.accept(value.isEmpty() ? null : value);
        client.setScreen(parent);
    }

    private void cancel() {
        callback.accept(null);
        client.setScreen(parent);
    }

    @Override
    public boolean shouldPause() { return false; }
}
