package com.pocketcampsite.client;

import com.pocketcampsite.PocketCampsite;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.minecraft.client.gui.screens.MenuScreens;

public final class PocketCampsiteClient implements ClientModInitializer {
    @Override public void onInitializeClient() {
        EntityRendererRegistry.register(PocketCampsite.CAMPSITE, PocketCampsiteRenderer::new);
        MenuScreens.register(PocketCampsite.MENU, PocketCampsiteScreen::new);
    }
}
