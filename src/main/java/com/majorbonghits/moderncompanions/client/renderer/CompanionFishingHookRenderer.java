package com.majorbonghits.moderncompanions.client.renderer;

import com.majorbonghits.moderncompanions.entity.AbstractHumanCompanionEntity;
import com.majorbonghits.moderncompanions.entity.projectile.CompanionFishingHook;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.Vec3;

/**
 * Client renderer that draws a bobber and line from a companion's hand.
 */
public class CompanionFishingHookRenderer extends EntityRenderer<CompanionFishingHook> {
    private static final ResourceLocation TEXTURE_LOCATION =
            ResourceLocation.withDefaultNamespace("textures/entity/fishing_hook.png");
    private static final RenderType RENDER_TYPE = RenderType.entityCutout(TEXTURE_LOCATION);

    public CompanionFishingHookRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public void render(CompanionFishingHook hook, float entityYaw, float partialTicks, PoseStack poseStack,
                       MultiBufferSource buffer, int packedLight) {
        AbstractHumanCompanionEntity owner = hook.getOwnerCompanion();
        if (owner == null) {
            return;
        }
        poseStack.pushPose();
        poseStack.pushPose();
        poseStack.scale(0.5F, 0.5F, 0.5F);
        poseStack.mulPose(this.entityRenderDispatcher.cameraOrientation());
        PoseStack.Pose pose = poseStack.last();
        VertexConsumer quad = buffer.getBuffer(RENDER_TYPE);
        vertex(quad, pose, packedLight, 0.0F, 0, 0, 1);
        vertex(quad, pose, packedLight, 1.0F, 0, 1, 1);
        vertex(quad, pose, packedLight, 1.0F, 1, 1, 0);
        vertex(quad, pose, packedLight, 0.0F, 1, 0, 0);
        poseStack.popPose();

        Vec3 handPos = getCompanionHandPos(owner, partialTicks);
        Vec3 hookPos = hook.getPosition(partialTicks).add(0.0D, 0.25D, 0.0D);
        float dx = (float) (handPos.x - hookPos.x);
        float dy = (float) (handPos.y - hookPos.y);
        float dz = (float) (handPos.z - hookPos.z);
        VertexConsumer line = buffer.getBuffer(RenderType.lineStrip());
        PoseStack.Pose linePose = poseStack.last();

        for (int i = 0; i <= 16; ++i) {
            stringVertex(dx, dy, dz, line, linePose, fraction(i, 16), fraction(i + 1, 16));
        }

        poseStack.popPose();
        super.render(hook, entityYaw, partialTicks, poseStack, buffer, packedLight);
    }

    private Vec3 getCompanionHandPos(AbstractHumanCompanionEntity owner, float partialTicks) {
        int arm = owner.getMainArm() == HumanoidArm.RIGHT ? 1 : -1;
        ItemStack main = owner.getMainHandItem();
        if (!main.is(Items.FISHING_ROD)) {
            arm = -arm;
        }
        float bodyRot = Mth.lerp(partialTicks, owner.yBodyRotO, owner.yBodyRot) * 0.017453292F;
        double sin = Mth.sin(bodyRot);
        double cos = Mth.cos(bodyRot);
        float scale = owner.getScale();
        double handOffset = (double) arm * 0.35D * (double) scale;
        double forwardOffset = 0.8D * (double) scale;
        float crouchOffset = owner.isCrouching() ? -0.1875F : 0.0F;
        return owner.getEyePosition(partialTicks).add(-cos * handOffset - sin * forwardOffset,
                (double) crouchOffset - 0.45D * (double) scale,
                -sin * handOffset + cos * forwardOffset);
    }

    private static float fraction(int value, int max) {
        return (float) value / (float) max;
    }

    private static void vertex(VertexConsumer consumer, PoseStack.Pose pose, int light, float x, int y, int u, int v) {
        consumer.addVertex(pose, x - 0.5F, (float) y - 0.5F, 0.0F)
                .setColor(-1)
                .setUv((float) u, (float) v)
                .setOverlay(OverlayTexture.NO_OVERLAY)
                .setLight(light)
                .setNormal(pose, 0.0F, 1.0F, 0.0F);
    }

    private static void stringVertex(float dx, float dy, float dz, VertexConsumer consumer, PoseStack.Pose pose,
                                     float start, float end) {
        float x0 = dx * start;
        float y0 = dy * (start * start + start) * 0.5F + 0.25F;
        float z0 = dz * start;
        float x1 = dx * end - x0;
        float y1 = dy * (end * end + end) * 0.5F + 0.25F - y0;
        float z1 = dz * end - z0;
        float len = Mth.sqrt(x1 * x1 + y1 * y1 + z1 * z1);
        x1 /= len;
        y1 /= len;
        z1 /= len;
        consumer.addVertex(pose, x0, y0, z0).setColor(-16777216).setNormal(pose, x1, y1, z1);
    }

    @Override
    public ResourceLocation getTextureLocation(CompanionFishingHook entity) {
        return TEXTURE_LOCATION;
    }
}
