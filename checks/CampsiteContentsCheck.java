import net.minecraft.SharedConstants;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.registries.VanillaRegistries;
import net.minecraft.nbt.NbtOps;
import net.minecraft.network.chat.Component;
import net.minecraft.server.Bootstrap;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.ItemContainerContents;
import net.minecraft.world.level.storage.TagValueInput;
import net.minecraft.world.level.storage.TagValueOutput;
import net.minecraft.util.ProblemReporter;

/** Actual 26.3 serialization contracts used by a campsite's packed item. */
public final class CampsiteContentsCheck {
    private static final int SIZE = 27;

    public static void main(String[] args) {
        SharedConstants.tryDetectVersion();
        Bootstrap.bootStrap();
        HolderLookup.Provider lookup = VanillaRegistries.createWorldLookup();
        BuiltInRegistries.DATA_COMPONENT_INITIALIZERS.build(lookup).forEach(initializer -> initializer.apply());
        SimpleContainer inventory = new SimpleContainer(SIZE);
        inventory.setItem(0, new ItemStack(Items.DIAMOND, 17));
        inventory.setItem(13, new ItemStack(Items.COOKED_BEEF, 8));
        inventory.setItem(26, new ItemStack(Items.IRON_PICKAXE));
        inventory.getItem(26).set(DataComponents.CUSTOM_NAME, Component.literal("Campsite pick"));
        inventory.getItem(26).set(DataComponents.DAMAGE, 37);
        NonNullList<ItemStack> original = copy(inventory);

        // Save/reload a deployed inventory, preserving holes and the final slot.
        TagValueOutput worldSave = TagValueOutput.createWithContext(ProblemReporter.DISCARDING, lookup);
        ContainerHelper.saveAllItems(worldSave.child("CampInventory"), inventory.getItems());
        SimpleContainer worldReload = new SimpleContainer(SIZE);
        ContainerHelper.loadAllItems(TagValueInput.create(ProblemReporter.DISCARDING, lookup,
            worldSave.buildResult()).childOrEmpty("CampInventory"), worldReload.getItems());
        assertContents(original, worldReload.getItems(), "deployed save/reload");

        ItemStack packed = new ItemStack(Items.CHEST);
        packed.set(DataComponents.CONTAINER, ItemContainerContents.fromItems(inventory.getItems()));
        inventory.getItem(0).setCount(1);
        inventory.clearContent();
        NonNullList<ItemStack> unpacked = unpack(packed);
        assertContents(original, unpacked, "packed snapshot survives source mutation");

        var ops = lookup.createSerializationContext(NbtOps.INSTANCE);
        var nbt = ItemStack.CODEC.encodeStart(ops, packed).getOrThrow();
        ItemStack reloaded = ItemStack.CODEC.parse(ops, nbt).getOrThrow();
        assertContents(original, unpack(reloaded), "packed item save/reload");

        // Mutating a placed copy must never mutate the packed data component.
        unpacked.get(0).setCount(3);
        assertContents(original, unpack(packed), "unpacked inventory has no mutable aliases");
        ItemStack copiedItem = packed.copy();
        assertContents(original, unpack(copiedItem), "copied packed item keeps components");

        ItemStack empty = new ItemStack(Items.CHEST);
        empty.set(DataComponents.CONTAINER, ItemContainerContents.EMPTY);
        if (unpack(empty).stream().anyMatch(stack -> !stack.isEmpty())) throw new AssertionError("Empty campsite manufactured items");
        for (int cycle = 0; cycle < 20; cycle++) {
            ItemStack next = new ItemStack(Items.CHEST);
            next.set(DataComponents.CONTAINER, ItemContainerContents.fromItems(unpack(reloaded)));
            reloaded = ItemStack.CODEC.parse(ops, ItemStack.CODEC.encodeStart(ops, next).getOrThrow()).getOrThrow();
            assertContents(original, unpack(reloaded), "pack/unpack cycle " + cycle);
        }
        SimpleContainer restricted = new SimpleContainer(SIZE) {
            @Override public boolean canPlaceItem(int slot, ItemStack stack) {
                return stack.getItem().canFitInsideContainerItems();
            }
        };
        ItemStack shulker = new ItemStack(Items.SHULKER_BOX);
        if (restricted.canPlaceItem(0, shulker)) throw new AssertionError("Shulker should be excluded from packed container items");
        if (!new Slot(restricted, 0, 0, 0).mayPlace(shulker)) throw new AssertionError("Vanilla slot policy changed; re-evaluate need for custom menu slots");
        System.out.println("CONFIRMED: vanilla Slot bypasses Container.canPlaceItem; campsite menu must enforce restrictions in Slot.mayPlace.");
        System.out.println("PASS: 27-slot sparse deployed persistence, packed NBT round-trip, item counts/name/damage, snapshot/copy isolation, empty contents, 20 pack/unpack cycles");
        System.out.println("NOTE: these verify vanilla container serialization; live entity removal/drop/ownership/menu races still require core review and in-game checks.");
    }

    private static NonNullList<ItemStack> copy(SimpleContainer inventory) {
        NonNullList<ItemStack> result = NonNullList.withSize(SIZE, ItemStack.EMPTY);
        for (int slot = 0; slot < SIZE; slot++) result.set(slot, inventory.getItem(slot).copy());
        return result;
    }
    private static NonNullList<ItemStack> unpack(ItemStack packed) {
        NonNullList<ItemStack> items = NonNullList.withSize(SIZE, ItemStack.EMPTY);
        packed.getOrDefault(DataComponents.CONTAINER, ItemContainerContents.EMPTY).copyInto(items);
        return items;
    }
    private static void assertContents(java.util.List<ItemStack> expected, java.util.List<ItemStack> actual, String stage) {
        for (int slot = 0; slot < SIZE; slot++) {
            if (!ItemStack.matches(expected.get(slot), actual.get(slot))) throw new AssertionError(stage + ": slot " + slot + " lost or changed items");
        }
    }
}
