package com.ayeristheking.bindcraft.gui;

import com.ayeristheking.bindcraft.binding.Binding;
import com.ayeristheking.bindcraft.profile.Profile;
import com.ayeristheking.bindcraft.profile.ProfileFolder;
import com.ayeristheking.bindcraft.profile.ProfileManager;
import com.ayeristheking.bindcraft.storage.JsonStorage;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;

import java.util.ArrayList;
import java.util.List;

/**
 * The main BindCraft screen, opened by the configured key (default: L).
 *
 * Layout:
 *   Left panel  [0 .. LEFT_W)   — profile list + profile action buttons
 *   Divider at LEFT_W
 *   Right panel [LEFT_W .. width) — selected profile's folder/binding list + toolbar
 */
public class BindCraftScreen extends Screen {

    // ─── Layout ───────────────────────────────────────────────────────────────
    private static final int LEFT_W      = 160;
    private static final int ROW_H       = 20;
    private static final int PAD         = 4;
    private static final int SCROLL_STEP = 12;

    // ─── Colors ───────────────────────────────────────────────────────────────
    private static final int C_LEFT_BG   = 0xFF1A1A1A;
    private static final int C_RIGHT_BG  = 0xFF1E1E1E;
    private static final int C_HEADER_BG = 0xFF141414;
    private static final int C_DIVIDER   = 0xFF444444;
    private static final int C_SEL       = 0xFF2A5080;
    private static final int C_HOVER     = 0xFF2C2C2C;
    private static final int C_ROW       = 0xFF222222;
    private static final int C_FOLDER    = 0xFF2A2A2A;
    private static final int C_TEXT      = 0xFFFFFFFF;
    private static final int C_DIM       = 0xFFA0A0A0;
    private static final int C_GREEN     = 0xFF55FF55;
    private static final int C_RED       = 0xFFFF5555;
    private static final int C_BLUE      = 0xFF5599FF;

    // ─── State ────────────────────────────────────────────────────────────────
    private Profile selectedProfile;
    private int     leftScroll  = 0;
    private int     rightScroll = 0;
    private int     mouseX, mouseY;

    // ─── Static widgets ───────────────────────────────────────────────────────
    private ButtonWidget btnAdd, btnRename, btnDelete;
    private ButtonWidget btnNewFolder, btnExport, btnImport;

    public BindCraftScreen() {
        super(Text.translatable("gui.BindCraft.title"));
        this.selectedProfile = ProfileManager.INSTANCE.getActiveProfile();
    }

    // ─── Init ─────────────────────────────────────────────────────────────────

    @Override
    protected void init() {
        int bY = height - ROW_H - PAD;

        // Left panel buttons
        btnAdd    = addDrawableChild(ButtonWidget.builder(Text.translatable("gui.BindCraft.add"),      btn -> onAddProfile())
                .dimensions(PAD, bY, 20, ROW_H).build());
        btnRename = addDrawableChild(ButtonWidget.builder(Text.translatable("gui.BindCraft.rename"), btn -> onRenameProfile())
                .dimensions(PAD + 22, bY, 56, ROW_H).build());
        btnDelete = addDrawableChild(ButtonWidget.builder(Text.translatable("gui.BindCraft.delete"), btn -> onDeleteProfile())
                .dimensions(PAD + 80, bY, 56, ROW_H).build());

        // Right panel toolbar
        int rX = LEFT_W + PAD;
        btnExport    = addDrawableChild(ButtonWidget.builder(Text.translatable("gui.BindCraft.export"),     btn -> onExport())
                .dimensions(rX, bY, 52, ROW_H).build());
        btnImport    = addDrawableChild(ButtonWidget.builder(Text.translatable("gui.BindCraft.import"),     btn -> onImport())
                .dimensions(rX + 54, bY, 52, ROW_H).build());
        btnNewFolder = addDrawableChild(ButtonWidget.builder(Text.translatable("gui.BindCraft.add_folder"),   btn -> onAddFolder())
                .dimensions(rX + 108, bY, 68, ROW_H).build());

        refreshButtons();
    }

    private void refreshButtons() {
        boolean has   = selectedProfile != null;
        boolean multi = ProfileManager.INSTANCE.getProfiles().size() > 1;
        if (btnRename    != null) btnRename.active    = has;
        if (btnDelete    != null) btnDelete.active    = has && multi;
        if (btnExport    != null) btnExport.active    = has;
        if (btnNewFolder != null) btnNewFolder.active = has;
    }

    // ─── Render ───────────────────────────────────────────────────────────────

    @Override
    public void render(DrawContext ctx, int mx, int my, float delta) {
        this.mouseX = mx;
        this.mouseY = my;
        drawLeftPanel(ctx, mx, my);
        drawRightPanel(ctx, mx, my);
        super.render(ctx, mx, my, delta);
    }

    private void drawLeftPanel(DrawContext ctx, int mx, int my) {
        ctx.fill(0, 0, LEFT_W, height, C_LEFT_BG);
        ctx.fill(LEFT_W, 0, LEFT_W + 1, height, C_DIVIDER);

        // Header
        int headerH = ROW_H + PAD * 2;
        ctx.fill(0, 0, LEFT_W, headerH, C_HEADER_BG);
        ctx.drawCenteredTextWithShadow(textRenderer, Text.translatable("gui.BindCraft.profiles").getString(), LEFT_W / 2, PAD + 2, C_TEXT);

        int yTop = headerH;
        int yBot = height - ROW_H - PAD * 2;
        int itemY = yTop - leftScroll;

        for (Profile p : ProfileManager.INSTANCE.getProfiles()) {
            if (itemY + ROW_H > yTop && itemY < yBot) {
                boolean sel   = p == selectedProfile;
                boolean hover = mx >= 0 && mx < LEFT_W && my >= itemY && my < itemY + ROW_H;
                ctx.fill(0, itemY, LEFT_W, itemY + ROW_H, sel ? C_SEL : hover ? C_HOVER : C_ROW);

                // Green dot for active profile
                if (p == ProfileManager.INSTANCE.getActiveProfile()) {
                    ctx.fill(3, itemY + 6, 8, itemY + 14, C_GREEN);
                }
                ctx.drawTextWithShadow(textRenderer, p.getName(), 13, itemY + 6,
                        sel ? C_TEXT : C_DIM);
            }
            itemY += ROW_H;
        }
    }

    private void drawRightPanel(DrawContext ctx, int mx, int my) {
        int panelX = LEFT_W + 1;
        ctx.fill(panelX, 0, width, height, C_RIGHT_BG);

        if (selectedProfile == null) {
            ctx.drawCenteredTextWithShadow(textRenderer, Text.translatable("gui.BindCraft.select_profile").getString(),
                    (panelX + width) / 2, height / 2, C_DIM);
            return;
        }

        // Header
        int headerH = ROW_H + PAD * 2;
        ctx.fill(panelX, 0, width, headerH, C_HEADER_BG);
        String title = String.format(Text.translatable("gui.BindCraft.active_count").getString(),
                selectedProfile.getName(), selectedProfile.getTotalEnabledBindings());
        ctx.drawCenteredTextWithShadow(textRenderer, title, (panelX + width) / 2, PAD + 2, C_TEXT);

        int yTop  = headerH;
        int yBot  = height - ROW_H - PAD * 2;
        int lx    = panelX + PAD;
        int rowW  = width - panelX - PAD * 2;
        int itemY = yTop - rightScroll;

        for (ProfileFolder folder : selectedProfile.getFolders()) {
            itemY = drawFolderHeader(ctx, mx, my, lx, rowW, itemY, yTop, yBot, folder);
            if (!folder.isExpanded()) continue;
            for (Binding binding : folder.getBindings()) {
                itemY = drawBindingRow(ctx, mx, my, lx, rowW, itemY, yTop, yBot, binding);
            }
            // "Add Binding" row
            if (itemY + ROW_H > yTop && itemY < yBot) {
                boolean hover = mx >= lx && mx < lx + rowW && my >= itemY && my < itemY + ROW_H;
                ctx.fill(lx, itemY, lx + rowW, itemY + ROW_H, hover ? C_HOVER : 0xFF1E1E1E);
                ctx.drawTextWithShadow(textRenderer, Text.translatable("gui.BindCraft.add_binding").getString(), lx + 2, itemY + 6, C_BLUE);
            }
            itemY += ROW_H;
        }
    }

    private int drawFolderHeader(DrawContext ctx, int mx, int my,
                                 int lx, int rowW, int itemY, int yTop, int yBot,
                                 ProfileFolder folder) {
        if (itemY + ROW_H > yTop && itemY < yBot) {
            ctx.fill(lx, itemY, lx + rowW, itemY + ROW_H, C_FOLDER);
            ctx.fill(lx, itemY + ROW_H - 1, lx + rowW, itemY + ROW_H, C_DIVIDER);

            // Enable dot
            ctx.fill(lx + 2, itemY + 6, lx + 8, itemY + 14,
                    folder.isEnabled() ? C_GREEN : C_RED);
            // Expand arrow
            ctx.drawTextWithShadow(textRenderer,
                    folder.isExpanded() ? "▼" : "►", lx + 11, itemY + 6, C_DIM);
            // Name + count
            ctx.drawTextWithShadow(textRenderer,
                    folder.getName() + "  (" + folder.getBindings().size() + ")",
                    lx + 24, itemY + 6, C_TEXT);
        }
        return itemY + ROW_H;
    }

    private int drawBindingRow(DrawContext ctx, int mx, int my,
                               int lx, int rowW, int itemY, int yTop, int yBot,
                               Binding binding) {
        if (itemY + ROW_H > yTop && itemY < yBot) {
            boolean hover = mx >= lx && mx < lx + rowW && my >= itemY && my < itemY + ROW_H;
            ctx.fill(lx, itemY, lx + rowW, itemY + ROW_H, hover ? C_HOVER : C_ROW);

            // Enable dot
            ctx.fill(lx + 8, itemY + 6, lx + 14, itemY + 14,
                    binding.isEnabled() ? C_GREEN : C_RED);
            // Name
            ctx.drawTextWithShadow(textRenderer, binding.getName(), lx + 20, itemY + 6, C_TEXT);
            // Trigger
            String trigger = binding.getTrigger().toString();
            int trigW = textRenderer.getWidth(trigger);
            ctx.drawTextWithShadow(textRenderer, trigger,
                    lx + rowW - trigW - 98, itemY + 6, C_DIM);
            // Action buttons (text links)
            ctx.drawTextWithShadow(textRenderer, "[" + Text.translatable("gui.BindCraft.edit").getString() + "]",   lx + rowW - 90, itemY + 6, C_BLUE);
            ctx.drawTextWithShadow(textRenderer, "[" + Text.translatable("gui.BindCraft.delete").getString() + "]", lx + rowW - 48, itemY + 6, C_RED);
        }
        return itemY + ROW_H;
    }

    // ─── Mouse ────────────────────────────────────────────────────────────────

    @Override
    public boolean mouseClicked(net.minecraft.client.gui.Click click, boolean doubled) {
        int button = click.buttonInfo().button();
        double mx = click.x();
        double my = click.y();
        if (button == 0) {
            if (clickLeftPanel((int) mx, (int) my)) return true;
            if (clickRightPanel((int) mx, (int) my)) return true;
        }
        return super.mouseClicked(click, doubled);
    }

    private boolean clickLeftPanel(int mx, int my) {
        if (mx >= LEFT_W) return false;
        int headerH = ROW_H + PAD * 2;
        int itemY   = headerH - leftScroll;
        for (Profile p : ProfileManager.INSTANCE.getProfiles()) {
            if (my >= itemY && my < itemY + ROW_H) {
                selectedProfile = p;
                rightScroll = 0;
                ProfileManager.INSTANCE.setActiveProfile(p);
                refreshButtons();
                return true;
            }
            itemY += ROW_H;
        }
        return false;
    }

    private boolean clickRightPanel(int mx, int my) {
        if (mx <= LEFT_W || selectedProfile == null) return false;
        int panelX = LEFT_W + 1;
        int lx     = panelX + PAD;
        int rowW   = width - panelX - PAD * 2;
        int headerH = ROW_H + PAD * 2;
        int itemY  = headerH - rightScroll;

        for (ProfileFolder folder : selectedProfile.getFolders()) {
            // Folder header row
            if (my >= itemY && my < itemY + ROW_H) {
                if (mx >= lx + 2 && mx < lx + 10) {
                    // Enable/disable dot
                    folder.setEnabled(!folder.isEnabled());
                } else {
                    // Expand/collapse
                    folder.setExpanded(!folder.isExpanded());
                }
                saveActive();
                return true;
            }
            itemY += ROW_H;
            if (!folder.isExpanded()) continue;

            for (Binding binding : new ArrayList<>(folder.getBindings())) {
                if (my >= itemY && my < itemY + ROW_H) {
                    if (mx >= lx + 8 && mx < lx + 16) {
                        // Enable dot
                        binding.setEnabled(!binding.isEnabled());
                        saveActive();
                    } else if (mx >= lx + rowW - 90 && mx < lx + rowW - 48) {
                        // Edit
                        client.setScreen(new BindingEditorScreen(this, folder, binding));
                    } else if (mx >= lx + rowW - 48) {
                        // Delete
                        folder.getBindings().remove(binding);
                        saveActive();
                    }
                    return true;
                }
                itemY += ROW_H;
            }

            // "Add Binding" row
            if (my >= itemY && my < itemY + ROW_H) {
                Binding nb = new Binding(Text.translatable("gui.BindCraft.new_binding").getString());
                folder.getBindings().add(nb);
                saveActive();
                client.setScreen(new BindingEditorScreen(this, folder, nb));
                return true;
            }
            itemY += ROW_H;
        }
        return false;
    }

    @Override
    public boolean mouseScrolled(double mx, double my, double hAmt, double vAmt) {
        int delta = (int) (-vAmt * SCROLL_STEP);
        if (mx < LEFT_W) leftScroll  = Math.max(0, leftScroll  + delta);
        else              rightScroll = Math.max(0, rightScroll + delta);
        return true;
    }

    // ─── Profile actions ──────────────────────────────────────────────────────

    private void onAddProfile() {
        client.setScreen(new ProfileScreen(this, name -> {
            if (name != null) {
                Profile p = ProfileManager.INSTANCE.addProfile(name);
                selectedProfile = p;
                ProfileManager.INSTANCE.setActiveProfile(p);
                refreshButtons();
            }
        }));
    }

    private void onRenameProfile() {
        if (selectedProfile == null) return;
        Profile target = selectedProfile;
        client.setScreen(new ProfileScreen(this, String.format(Text.translatable("gui.BindCraft.prompt.rename").getString(), target.getName()), name -> {
            if (name != null) ProfileManager.INSTANCE.renameProfile(target, name);
        }));
    }

    private void onDeleteProfile() {
        if (selectedProfile == null || ProfileManager.INSTANCE.getProfiles().size() <= 1) return;
        ProfileManager.INSTANCE.deleteProfile(selectedProfile);
        selectedProfile = ProfileManager.INSTANCE.getActiveProfile();
        rightScroll = 0;
        refreshButtons();
    }

    private void onAddFolder() {
        if (selectedProfile == null) return;
        client.setScreen(new ProfileScreen(this, Text.translatable("gui.BindCraft.prompt.folder_name").getString(), name -> {
            if (name != null) {
                selectedProfile.getFolders().add(new ProfileFolder(name));
                saveActive();
            }
        }));
    }

    private void onExport() {
        if (selectedProfile != null) ProfileManager.INSTANCE.save(selectedProfile);
    }

    private void onImport() {
        // Re-scan config directory for profiles not yet loaded
        for (Profile p : JsonStorage.loadAll()) {
            boolean exists = ProfileManager.INSTANCE.getProfiles().stream()
                    .anyMatch(e -> e.getName().equals(p.getName()));
            if (!exists) ProfileManager.INSTANCE.getProfiles().add(p);
        }
        if (selectedProfile == null && !ProfileManager.INSTANCE.getProfiles().isEmpty()) {
            selectedProfile = ProfileManager.INSTANCE.getProfiles().get(0);
            ProfileManager.INSTANCE.setActiveProfile(selectedProfile);
        }
        refreshButtons();
    }

    private void saveActive() {
        if (selectedProfile != null) ProfileManager.INSTANCE.save(selectedProfile);
    }

    @Override
    public boolean shouldPause() { return false; }
}