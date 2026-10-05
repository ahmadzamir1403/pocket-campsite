import java.lang.reflect.Field;
import net.minecraft.SharedConstants;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.registries.VanillaRegistries;
import net.minecraft.server.Bootstrap;
import net.minecraft.world.entity.EntityEquipment;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.ItemContainerContents;
import net.minecraft.world.level.GameType;
import sun.misc.Unsafe;

/** Reproduce vanilla creative-full delivery loss without launching a world. */
public final class CampsiteDeliveryCheck {
    // Test-only allocation skips Player's world-dependent constructor. The
    // exercised Inventory APIs need only hasInfiniteMaterials() from this stub.
    private static final class PlayerStub extends Player {
        private boolean creative;
        private PlayerStub() { super(null, null); throw new UnsupportedOperationException("Test allocation only"); }
        @Override public boolean hasInfiniteMaterials() { return creative; }
        @Override public GameType gameMode() { return creative ? GameType.CREATIVE : GameType.SURVIVAL; }
    }
    public static void main(String[] args) throws Exception {
        SharedConstants.tryDetectVersion();
        Bootstrap.bootStrap();
        var lookup = VanillaRegistries.createWorldLookup();
        BuiltInRegistries.DATA_COMPONENT_INITIALIZERS.build(lookup).forEach(initializer -> initializer.apply());
        Field field = Unsafe.class.getDeclaredField("theUnsafe");
        field.setAccessible(true);
        Unsafe allocator = (Unsafe) field.get(null);
        PlayerStub creative = (PlayerStub) allocator.allocateInstance(PlayerStub.class);
        creative.creative = true;
        Inventory fullCreative = fullInventory(creative);
        ItemStack unsafeKit = packedKit();
        if (!fullCreative.add(unsafeKit) || !unsafeKit.isEmpty()) throw new AssertionError("Expected vanilla creative-full deletion behavior changed; re-evaluate pack delivery");
        Inventory safeCreative = fullInventory(creative);
        ItemStack safeKit = packedKit();
        if (safeCreative.getFreeSlot() != -1) throw new AssertionError("Fixture must have no free slot");
        // Production must take its drop/failure path here without calling add().
        assertContents(safeKit);
        safeCreative.setItem(11, ItemStack.EMPTY);
        int slot = safeCreative.getFreeSlot();
        safeCreative.setItem(slot, safeKit);
        assertContents(safeCreative.getItem(slot));
        PlayerStub survival = (PlayerStub) allocator.allocateInstance(PlayerStub.class);
        ItemStack survivalKit = packedKit();
        if (fullInventory(survival).add(survivalKit)) throw new AssertionError("Full survival inventory should refuse kit");
        assertContents(survivalKit);
        System.out.println("PASS: actual vanilla Inventory.add deletes creative-full kit; explicit free-slot delivery preserves packed contents; survival-full remainder preserved");
        System.out.println("NOTE: world-free Player stub tests delivery API behavior; entity spawn/removal integration still requires review/in-game testing.");
    }
    private static Inventory fullInventory(PlayerStub player) {
        Inventory inventory = new Inventory(player, new EntityEquipment());
        for (int i = 0; i < 36; i++) inventory.setItem(i, new ItemStack(Items.STONE, 64));
        return inventory;
    }
    private static ItemStack packedKit() {
        ItemStack kit = new ItemStack(Items.CHEST);
        kit.set(DataComponents.MAX_STACK_SIZE, 1);
        kit.set(DataComponents.CONTAINER, ItemContainerContents.fromItems(java.util.List.of(new ItemStack(Items.DIAMOND, 17))));
        return kit;
    }
    private static void assertContents(ItemStack kit) {
        if (kit.isEmpty() || kit.getCount() != 1 || kit.get(DataComponents.CONTAINER).itemCopies().mapToInt(ItemStack::getCount).sum() != 17) throw new AssertionError("Delivery lost kit or stored diamonds");
    }
}
