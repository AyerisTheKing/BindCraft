package com.king.attributeswap.action;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.util.Hand;

import com.ayeristheking.bindcraft.mixin.PlayerInventoryAccessor;

import java.util.List;

/**
 * Executes a list of Actions in sequence on a background daemon thread.
 * This allows DELAY actions to sleep without blocking the render thread.
 */
public final class ActionExecutor {

    private static final long TICK_MILLIS = 50L;

    private ActionExecutor() {
    }

    /**
     * Fires the action sequence asynchronously.
     * Captures the previous inventory slot before execution for
     * SELECT_PREVIOUS_SLOT.
     */
    public static void execute(List<Action> actions) {
        MinecraftClient client = MinecraftClient.getInstance();
        ClientPlayerEntity player = client.player;
        if (player == null || actions.isEmpty())
            return;

        // Capture previous slot before starting the sequence
        int previousSlot = ((PlayerInventoryAccessor) player.getInventory()).getSelectedSlot();

        Thread thread = new Thread(() -> runSequence(actions, previousSlot), "attributeswap-executor");
        thread.setDaemon(true);
        thread.start();
    }

    private static void runSequence(List<Action> actions, int previousSlot) {
        for (Action action : actions) {
            try {
                runAction(action, previousSlot);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return;
            }
        }
    }

    private static void runAction(Action action, int previousSlot) throws InterruptedException {
        MinecraftClient client = MinecraftClient.getInstance();

        switch (action.getType()) {

            case SELECT_SLOT -> {
                int slot = action.getInt("slot", 0);
                // Clamp to valid hotbar range [0, 8]
                int clamped = Math.max(0, Math.min(8, slot - 1)); // JSON is 1-indexed
                client.execute(() -> {
                    if (client.player != null) {
                        ((PlayerInventoryAccessor) client.player.getInventory()).setSelectedSlot(clamped);
                    }
                });
            }

            case SELECT_PREVIOUS_SLOT -> client.execute(() -> {
                if (client.player != null) {
                    ((PlayerInventoryAccessor) client.player.getInventory()).setSelectedSlot(previousSlot);
                }
            });

            case LEFT_CLICK -> client.execute(() -> {
                if (client.player != null) {
                    // Simulate use-item on main hand (left click = attack in world context)
                    client.options.attackKey.setPressed(true);
                }
                // Release on next execute to allow the game to register the press
                client.execute(() -> client.options.attackKey.setPressed(false));
            });

            case RIGHT_CLICK -> client.execute(() -> {
                if (client.player != null) {
                    client.interactionManager.interactItem(client.player, Hand.MAIN_HAND);
                }
            });

            case DELAY -> {
                int ticks = action.getInt("ticks", 1);
                Thread.sleep(ticks * TICK_MILLIS);
            }

            case SEND_CHAT -> {
                String message = action.getString("message", "");
                if (!message.isEmpty()) {
                    client.execute(() -> {
                        if (client.player != null) {
                            client.player.networkHandler.sendChatMessage(message);
                        }
                    });
                }
            }

            case RUN_COMMAND -> {
                String command = action.getString("command", "");
                if (!command.isEmpty()) {
                    // Strip leading slash if present
                    String cmd = command.startsWith("/") ? command.substring(1) : command;
                    client.execute(() -> {
                        if (client.player != null) {
                            client.player.networkHandler.sendChatCommand(cmd);
                        }
                    });
                }
            }
        }
    }
}
