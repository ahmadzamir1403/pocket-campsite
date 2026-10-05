package com.pocketcampsite.client;

import net.minecraft.client.model.Model;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.client.renderer.rendertype.RenderTypes;

/** Stationary A-frame tent. Model coordinates have the floor at y=0. */
public final class PocketCampsiteModel extends Model<PocketCampsiteRenderState> {
    public PocketCampsiteModel() {
        super(createLayer().bakeRoot(), RenderTypes::entityCutoutCull);
    }

    public static LayerDefinition createLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        // A ridge 30 pixels above ground, with two 36-pixel slopes.
        root.addOrReplaceChild("left_canvas", CubeListBuilder.create()
            .texOffs(0, 0).addBox(-1, 0, -18, 1, 36, 36),
            PartPose.offsetAndRotation(0, -30, 0, 0, 0, 0.58F));
        root.addOrReplaceChild("right_canvas", CubeListBuilder.create()
            .texOffs(0, 0).addBox(0, 0, -18, 1, 36, 36),
            PartPose.offsetAndRotation(0, -30, 0, 0, 0, -0.58F));
        root.addOrReplaceChild("floor_left", CubeListBuilder.create()
            .texOffs(128, 32).addBox(-20, -1, -18, 20, 1, 36), PartPose.ZERO);
        root.addOrReplaceChild("floor_right", CubeListBuilder.create()
            .texOffs(128, 32).addBox(0, -1, -18, 20, 1, 36), PartPose.ZERO);
        root.addOrReplaceChild("ridge", CubeListBuilder.create()
            .texOffs(0, 168).addBox(-1, -31, -19, 2, 2, 38), PartPose.ZERO);
        root.addOrReplaceChild("front_pole", CubeListBuilder.create()
            .texOffs(0, 128).addBox(-1, -30, -19, 2, 30, 2), PartPose.ZERO);
        root.addOrReplaceChild("back_pole", CubeListBuilder.create()
            .texOffs(0, 128).addBox(-1, -30, 17, 2, 30, 2), PartPose.ZERO);
        root.addOrReplaceChild("bedroll", CubeListBuilder.create()
            .texOffs(128, 128).addBox(-17, -4, -14, 12, 3, 28), PartPose.ZERO);
        root.addOrReplaceChild("pillow", CubeListBuilder.create()
            .texOffs(128, 164).addBox(-17, -6, 9, 12, 2, 5), PartPose.ZERO);
        root.addOrReplaceChild("chest", CubeListBuilder.create()
            .texOffs(128, 0).addBox(5, -10, 4, 12, 9, 9), PartPose.ZERO);
        root.addOrReplaceChild("chest_latch", CubeListBuilder.create()
            .texOffs(20, 128).addBox(10, -7, 3.5F, 2, 3, 1), PartPose.ZERO);
        root.addOrReplaceChild("workbench", CubeListBuilder.create()
            .texOffs(128, 80).addBox(5, -8, -13, 12, 7, 10), PartPose.ZERO);
        return LayerDefinition.create(mesh, 256, 256);
    }
}
