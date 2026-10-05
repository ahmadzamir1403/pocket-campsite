package com.pocketcampsite;

import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public final class PocketCampsiteMenu extends ChestMenu {
    private final PocketCampsiteEntity campsite;
    public PocketCampsiteMenu(int id, Inventory inventory) { super(PocketCampsite.MENU, id, inventory, new SimpleContainer(27), 3); campsite = null; restrictSlots(); }
    public PocketCampsiteMenu(int id, Inventory inventory, PocketCampsiteEntity campsite) {
        super(PocketCampsite.MENU, id, inventory, campsite.inventory(), 3); this.campsite = campsite; restrictSlots();
    }
    private void restrictSlots() {
        for (int i = 0; i < 27; i++) {
            Slot original = slots.get(i);
            Slot restricted = new Slot(getContainer(), i, original.x, original.y) {
                @Override public boolean mayPlace(ItemStack stack) { return !stack.is(PocketCampsite.CAMPSITE_KIT) && stack.getItem().canFitInsideContainerItems(); }
            };
            restricted.index = i; slots.set(i, restricted);
        }
    }
    @Override public boolean clickMenuButton(Player player, int id) {
        if (campsite == null || !stillValid(player)) return false;
        return campsite.performAction(player, id);
    }
}
