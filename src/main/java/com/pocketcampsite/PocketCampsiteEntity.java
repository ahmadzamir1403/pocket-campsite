package com.pocketcampsite;

import java.util.UUID;
import net.fabricmc.fabric.api.menu.v1.ExtendedMenuProvider;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Unit;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.CraftingMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemContainerContents;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.attribute.EnvironmentAttributes;

public final class PocketCampsiteEntity extends Entity {
    private UUID owner;
    private boolean packing;
    private boolean bedActive;
    private final SimpleContainer inventory = new SimpleContainer(27) {
        @Override public boolean stillValid(Player player) { return canUse(player); }
        @Override public boolean canPlaceItem(int slot, ItemStack stack) { return !stack.is(PocketCampsite.CAMPSITE_KIT) && stack.getItem().canFitInsideContainerItems(); }
    };
    public PocketCampsiteEntity(EntityType<? extends PocketCampsiteEntity> type, Level level) { super(type, level); }
    public SimpleContainer inventory() { return inventory; }
    public void setOwner(Player player) { owner = player.getUUID(); }
    public boolean canUse(Player player) { return isAlive() && !packing && owner != null && owner.equals(player.getUUID()) && distanceToSqr(player) <= 64 && !player.isSpectator(); }
    @Override protected void defineSynchedData(SynchedEntityData.Builder builder) { }
    @Override public boolean isPickable() { return true; }
    @Override public boolean isPushable() { return false; }
    @Override public boolean isPushedByFluid() { return false; }
    @Override public boolean hurtServer(ServerLevel level, DamageSource source, float amount) {
        // Tent is reusable furniture. Owner packs it; attacks/explosions cannot destroy inventory.
        return false;
    }
    @Override public void onRemoval(RemovalReason reason) {
        if (!level().isClientSide() && reason.shouldDestroy()) {
            for (Player player : level().players()) if (player.isSleeping() && player.getSleepingPos().filter(blockPosition()::equals).isPresent()) player.stopSleeping();
            clearBed();
            if (!packing && level() instanceof ServerLevel server) {
                ItemStack kit = new ItemStack(PocketCampsite.CAMPSITE_KIT);
                kit.set(DataComponents.CONTAINER, ItemContainerContents.fromItems(inventory.getItems()));
                spawnAtLocation(server, kit);
                inventory.clearContent(); packing = true;
            }
        }
        super.onRemoval(reason);
    }
    public boolean hasSafeGround() {
        int y = blockPosition().getY();
        for (int x = blockPosition().getX()-1; x <= blockPosition().getX()+1; x++)
            for (int z = blockPosition().getZ()-1; z <= blockPosition().getZ()+1; z++) {
                BlockPos below = new BlockPos(x, y-1, z);
                if (!level().getBlockState(below).isSolidRender() || !level().getFluidState(below.above()).isEmpty()) return false;
            }
        return true;
    }
    @Override public InteractionResult interact(Player player, InteractionHand hand, net.minecraft.world.phys.Vec3 hit) {
        if (level().isClientSide()) return InteractionResult.SUCCESS;
        if (!canUse(player)) { player.sendSystemMessage(Component.translatable("message.pocketcampsite.owner")); return InteractionResult.SUCCESS_SERVER; }
        if (player.isShiftKeyDown()) pack(player);
        else player.openMenu(new ExtendedMenuProvider<Unit>() {
            @Override public Unit getScreenOpeningData(ServerPlayer player) { return Unit.INSTANCE; }
            @Override public Component getDisplayName() { return Component.translatable("container.pocketcampsite.campsite"); }
            @Override public AbstractContainerMenu createMenu(int id, Inventory inv, Player player) { return new PocketCampsiteMenu(id, inv, PocketCampsiteEntity.this); }
        });
        return InteractionResult.SUCCESS_SERVER;
    }
    public boolean performAction(Player player, int action) {
        if (!canUse(player)) return false;
        if (action == 0) {
            player.openMenu(new SimpleMenuProvider((id, inv, opener) -> new CraftingMenu(id, inv, net.minecraft.world.inventory.ContainerLevelAccess.create(level(), blockPosition())) {
                @Override public boolean stillValid(Player player) { return canUse(player); }
            }, Component.translatable("container.crafting")));
            return true;
        }
        if (action == 1) return rest(player);
        if (action == 2) return pack(player);
        return false;
    }
    private boolean rest(Player player) {
        if (!(player instanceof ServerPlayer server) || server.isSleeping()) return false;
        if (!level().dimension().equals(Level.OVERWORLD)) {
            player.sendSystemMessage(Component.translatable("message.pocketcampsite.rest_dimension")); return false;
        }
        if (!level().environmentAttributes().getValue(EnvironmentAttributes.BED_RULE, position()).canSleep(level())) {
            player.sendSystemMessage(Component.translatable("message.pocketcampsite.rest_time")); return false;
        }
        if (!level().getEntitiesOfClass(Monster.class, getBoundingBox().inflate(8, 5, 8), mob -> mob.isPreventingPlayerRest(server.level(), player)).isEmpty()) {
            player.sendSystemMessage(Component.translatable("block.minecraft.bed.not_safe")); return false;
        }
        if (!hasSafeGround() || !level().noCollision(player, player.getBoundingBox().move(getX()-player.getX(), getY()-player.getY(), getZ()-player.getZ()))) {
            player.sendSystemMessage(Component.translatable("message.pocketcampsite.space")); return false;
        }
        BlockPos head = blockPosition(), foot = head.south();
        if (!level().isEmptyBlock(head) || !level().isEmptyBlock(foot) || !level().mayInteract(player, head) || !level().mayInteract(player, foot)) return false;
        var base = PocketCampsite.CAMP_BED.defaultBlockState().setValue(net.minecraft.world.level.block.BedBlock.FACING, net.minecraft.core.Direction.NORTH);
        bedActive = true;
        level().setBlock(foot, base.setValue(net.minecraft.world.level.block.BedBlock.PART, net.minecraft.world.level.block.state.properties.BedPart.FOOT), 2);
        level().setBlock(head, base.setValue(net.minecraft.world.level.block.BedBlock.PART, net.minecraft.world.level.block.state.properties.BedPart.HEAD), 2);
        server.closeContainer();
        boolean slept = server.startSleeping(blockPosition());
        if (slept) server.level().updateSleepingPlayerList();
        else clearBed();
        return slept;
    }
    @Override public void tick() {
        super.tick();
        if (!level().isClientSide() && bedActive && level().players().stream().noneMatch(p -> p.isSleeping() && p.getSleepingPos().filter(blockPosition()::equals).isPresent())) clearBed();
    }
    private void clearBed() {
        if (!bedActive) return;
        for (BlockPos pos : new BlockPos[] { blockPosition(), blockPosition().south() })
            if (level().getBlockState(pos).is(PocketCampsite.CAMP_BED)) level().removeBlock(pos, false);
        bedActive = false;
    }
    private boolean pack(Player player) {
        if (!canUse(player)) return false;
        if (!level().getEntitiesOfClass(Player.class, getBoundingBox().inflate(2), p -> p.isSleeping() && p.getSleepingPos().filter(blockPosition()::equals).isPresent()).isEmpty()) {
            player.sendSystemMessage(Component.translatable("message.pocketcampsite.occupied")); return false;
        }
        packing = true;
        clearBed();
        // Close menus before snapshot so cursor/crafting stacks are returned normally.
        for (Player viewer : level().players()) if (viewer.containerMenu instanceof PocketCampsiteMenu menu && menu.getContainer() == inventory) ((ServerPlayer)viewer).closeContainer();
        ItemStack kit = new ItemStack(PocketCampsite.CAMPSITE_KIT);
        kit.set(DataComponents.CONTAINER, ItemContainerContents.fromItems(inventory.getItems()));
        int freeSlot = player.getInventory().getFreeSlot();
        if (freeSlot >= 0) player.getInventory().setItem(freeSlot, kit);
        else {
            ItemEntity drop = new ItemEntity(level(), getX(), getY() + 0.5, getZ(), kit);
            drop.setDefaultPickUpDelay();
            drop.setTarget(player.getUUID());
            if (!((ServerLevel) level()).addFreshEntity(drop)) { packing = false; return false; }
        }
        inventory.clearContent();
        discard();
        return true;
    }
    @Override protected void addAdditionalSaveData(ValueOutput output) {
        if (owner != null) output.putString("CampsiteOwner", owner.toString());
        output.putBoolean("CampsiteBedActive", bedActive);
        ContainerHelper.saveAllItems(output.child("CampsiteStorage"), inventory.getItems());
    }
    @Override protected void readAdditionalSaveData(ValueInput input) {
        try { owner = input.getString("CampsiteOwner").map(UUID::fromString).orElse(null); } catch (IllegalArgumentException ignored) { owner = null; }
        inventory.clearContent();
        bedActive = input.getBooleanOr("CampsiteBedActive", false);
        ContainerHelper.loadAllItems(input.childOrEmpty("CampsiteStorage"), inventory.getItems());
    }
}
