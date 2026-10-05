package com.pocketcampsite;

import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BedBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

/** A temporary invisible mattress used only while a camper is actually asleep. */
public final class CampBedBlock extends BedBlock {
    public CampBedBlock(Properties properties) { super(DyeColor.WHITE, properties); }
    @Override protected RenderShape getRenderShape(BlockState state) { return RenderShape.INVISIBLE; }
    @Override protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        return InteractionResult.SUCCESS; // Rest is authorized through the tent menu; never set spawn here.
    }
}
