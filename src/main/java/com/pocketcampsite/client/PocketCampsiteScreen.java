package com.pocketcampsite.client;

import com.pocketcampsite.PocketCampsiteMenu;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

/** Camp actions use vanilla inventory-button packets and server-side checks. */
public final class PocketCampsiteScreen extends AbstractContainerScreen<PocketCampsiteMenu> {
    public PocketCampsiteScreen(PocketCampsiteMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title, 176, 168);
        inventoryLabelY = 74;
    }

    @Override protected void init() {
        super.init();
        addAction("Craft", "Open the camp crafting table", 0, 0);
        addAction("Rest", "Sleep through the night. Your spawn point stays unchanged.", 1, 59);
        addAction("Pack", "Pack the tent and all stored items", 2, 118);
    }

    private void addAction(String label, String description, int action, int offset) {
        addRenderableWidget(Button.builder(Component.literal(label), button -> {
            if (minecraft != null && minecraft.gameMode != null) {
                minecraft.gameMode.handleInventoryButtonClick(menu.containerId, action);
            }
        }).bounds(leftPos + offset, topPos - 25, 58, 20)
            .tooltip(Tooltip.create(Component.literal(description))).build());
    }

    @Override public void extractBackground(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        graphics.fill(leftPos, topPos, leftPos + imageWidth, topPos + imageHeight, 0xFFF0DFC0);
        graphics.outline(leftPos, topPos, imageWidth, imageHeight, 0xFF845B3E);
        graphics.fill(leftPos + 2, topPos + 2, leftPos + imageWidth - 2, topPos + 16, 0xFFD9B987);
        for (int row = 0; row < 3; row++) {
            for (int column = 0; column < 9; column++) slotBackground(graphics, 7 + column * 18, 17 + row * 18);
        }
        for (int row = 0; row < 3; row++) {
            for (int column = 0; column < 9; column++) slotBackground(graphics, 7 + column * 18, 84 + row * 18);
        }
        for (int column = 0; column < 9; column++) slotBackground(graphics, 7 + column * 18, 142);
    }

    private void slotBackground(GuiGraphicsExtractor graphics, int x, int y) {
        graphics.fill(leftPos + x, topPos + y, leftPos + x + 18, topPos + y + 18, 0xFFAB8A63);
        graphics.fill(leftPos + x + 1, topPos + y + 1, leftPos + x + 17, topPos + y + 17, 0xFFDFCBA9);
    }
}
