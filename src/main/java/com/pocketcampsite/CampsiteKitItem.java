package com.pocketcampsite;

import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.component.ItemContainerContents;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;

public final class CampsiteKitItem extends Item {
    public CampsiteKitItem(Properties properties) { super(properties); }
    @Override public boolean canFitInsideContainerItems() { return false; }
    @Override public InteractionResult useOn(UseOnContext context) {
        Player player = context.getPlayer(); Level level = context.getLevel();
        if (player == null) return InteractionResult.PASS;
        if (level.isClientSide()) return InteractionResult.SUCCESS;
        BlockPos pos = context.getClickedPos().relative(context.getClickedFace());
        if (!level.mayInteract(player, pos) || !player.mayUseItemAt(pos, context.getClickedFace(), context.getItemInHand())) return InteractionResult.FAIL;
        PocketCampsiteEntity camp = new PocketCampsiteEntity(PocketCampsite.CAMPSITE, level);
        camp.snapTo(pos.getX() + .5, pos.getY(), pos.getZ() + .5, 0, 0);
        camp.setOwner(player);
        if (!camp.hasSafeGround() || !level.noCollision(camp) || !level.getEntities(camp, camp.getBoundingBox(), e -> e instanceof PocketCampsiteEntity).isEmpty()) {
            player.sendSystemMessage(Component.translatable("message.pocketcampsite.space")); return InteractionResult.FAIL;
        }
        context.getItemInHand().getOrDefault(DataComponents.CONTAINER, ItemContainerContents.EMPTY).copyInto(camp.inventory().getItems());
        if (!level.addFreshEntity(camp)) return InteractionResult.FAIL;
        // Even Creative consumes a filled kit: keeping its contents would duplicate storage.
        context.getItemInHand().shrink(1);
        return InteractionResult.SUCCESS_SERVER;
    }
}
