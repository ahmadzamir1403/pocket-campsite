package com.pocketcampsite;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.creativetab.v1.CreativeModeTabEvents;
import net.fabricmc.fabric.api.menu.v1.ExtendedMenuType;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.Unit;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.Item;

public final class PocketCampsite implements ModInitializer {
    public static final String MOD_ID = "pocketcampsite";
    public static Identifier id(String path) { return Identifier.fromNamespaceAndPath(MOD_ID, path); }
    public static final CampBedBlock CAMP_BED = Registry.register(BuiltInRegistries.BLOCK, id("camp_bed"),
        new CampBedBlock(net.minecraft.world.level.block.state.BlockBehaviour.Properties.of()
            .setId(ResourceKey.create(Registries.BLOCK, id("camp_bed"))).strength(-1F, 3600000F).noOcclusion().noLootTable()));
    public static final EntityType<PocketCampsiteEntity> CAMPSITE = Registry.register(BuiltInRegistries.ENTITY_TYPE, id("campsite"),
        EntityType.Builder.of(PocketCampsiteEntity::new, MobCategory.MISC).sized(2.5F, 2.0F).clientTrackingRange(10)
            .build(ResourceKey.create(Registries.ENTITY_TYPE, id("campsite"))));
    public static final Item CAMPSITE_KIT = Registry.register(BuiltInRegistries.ITEM, id("campsite_kit"),
        new CampsiteKitItem(new Item.Properties().setId(ResourceKey.create(Registries.ITEM, id("campsite_kit"))).stacksTo(1)));
    public static final MenuType<PocketCampsiteMenu> MENU = Registry.register(BuiltInRegistries.MENU, id("campsite"),
        new ExtendedMenuType<>((id, inv, unit) -> new PocketCampsiteMenu(id, inv), StreamCodec.unit(Unit.INSTANCE)));
    @Override public void onInitialize() {
        CreativeModeTabEvents.modifyOutputEvent(ResourceKey.create(Registries.CREATIVE_MODE_TAB,
            Identifier.fromNamespaceAndPath("minecraft", "tools_and_utilities"))).register(output -> output.accept(CAMPSITE_KIT));
    }
}
