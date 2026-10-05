package com.pocketcampsite.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.pocketcampsite.PocketCampsiteEntity;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.Identifier;

public final class PocketCampsiteRenderer extends EntityRenderer<PocketCampsiteEntity, PocketCampsiteRenderState> {
    private static final Identifier TEXTURE = Identifier.fromNamespaceAndPath("pocketcampsite", "textures/entity/pocket_campsite.png");
    private final PocketCampsiteModel model = new PocketCampsiteModel();

    public PocketCampsiteRenderer(EntityRendererProvider.Context context) {
        super(context);
        shadowRadius = 1.2F;
    }

    @Override public PocketCampsiteRenderState createRenderState() {
        return new PocketCampsiteRenderState();
    }

    @Override public void extractRenderState(PocketCampsiteEntity entity, PocketCampsiteRenderState state, float partialTick) {
        super.extractRenderState(entity, state, partialTick);
        state.yaw = entity.getYRot(partialTick);
    }

    @Override public void submit(PocketCampsiteRenderState state, PoseStack poses, SubmitNodeCollector collector, CameraRenderState camera) {
        if (!state.isInvisible) {
            poses.pushPose();
            poses.rotateDegrees(Axis.YP, 180 - state.yaw);
            poses.scale(-1, -1, 1);
            collector.submitModel(model, state, poses, TEXTURE, state.lightCoords, OverlayTexture.NO_OVERLAY, state.outlineColor);
            poses.popPose();
        }
        super.submit(state, poses, collector, camera);
    }
}
