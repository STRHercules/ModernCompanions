package com.majorbonghits.moderncompanions.client.renderer;

import com.majorbonghits.moderncompanions.entity.AbstractHumanCompanionEntity;
import com.majorbonghits.moderncompanions.client.renderer.CompanionSkinManager;
import net.minecraft.resources.ResourceLocation;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.HumanoidMobRenderer;
import net.minecraft.client.renderer.entity.layers.HumanoidArmorLayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.item.CrossbowItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.UseAnim;

/**
 * Renderer that mimics the original Human Companions player-like visuals.
 */
public class CompanionRenderer extends HumanoidMobRenderer<AbstractHumanCompanionEntity, PlayerModel<AbstractHumanCompanionEntity>> {

    public CompanionRenderer(EntityRendererProvider.Context context) {
        super(context, new PlayerModel<>(context.bakeLayer(ModelLayers.PLAYER), false), 0.5f);
        this.addLayer(new HumanoidArmorLayer<>(this,
                new HumanoidModel<>(context.bakeLayer(ModelLayers.PLAYER_INNER_ARMOR)),
                new HumanoidModel<>(context.bakeLayer(ModelLayers.PLAYER_OUTER_ARMOR)),
                context.getModelManager()));
    }

    @Override
    public void render(AbstractHumanCompanionEntity entity, float entityYaw, float partialTicks, PoseStack poseStack,
                       MultiBufferSource buffer, int packedLight) {
        this.setModelProperties(entity);
        super.render(entity, entityYaw, partialTicks, poseStack, buffer, packedLight);
    }

    private void setModelProperties(AbstractHumanCompanionEntity companion) {
        PlayerModel<AbstractHumanCompanionEntity> model = this.getModel();
        HumanoidModel.ArmPose main = armPose(companion, InteractionHand.MAIN_HAND);
        HumanoidModel.ArmPose off = armPose(companion, InteractionHand.OFF_HAND);

        if (companion.getMainArm() == HumanoidArm.RIGHT) {
            model.rightArmPose = main;
            model.leftArmPose = off;
        } else {
            model.rightArmPose = off;
            model.leftArmPose = main;
        }
    }

    private static HumanoidModel.ArmPose armPose(AbstractHumanCompanionEntity companion, InteractionHand hand) {
        ItemStack stack = companion.getItemInHand(hand);
        if (stack.isEmpty()) {
            return HumanoidModel.ArmPose.EMPTY;
        }
        if (companion.getUsedItemHand() == hand && companion.getUseItemRemainingTicks() > 0) {
            UseAnim anim = stack.getUseAnimation();
            if (anim == UseAnim.BOW) {
                return HumanoidModel.ArmPose.BOW_AND_ARROW;
            }
            if (anim == UseAnim.CROSSBOW && hand == companion.getUsedItemHand()) {
                return HumanoidModel.ArmPose.CROSSBOW_CHARGE;
            }
        } else if (!companion.swinging && stack.is(Items.CROSSBOW) && CrossbowItem.isCharged(stack)) {
            return HumanoidModel.ArmPose.CROSSBOW_HOLD;
        }
        return HumanoidModel.ArmPose.ITEM;
    }

    @Override
    public ResourceLocation getTextureLocation(AbstractHumanCompanionEntity entity) {
        // If a custom URL is present, hand off to the dynamic downloader; otherwise use the bundled texture.
        return entity.getCustomSkinUrl()
                .map(CompanionSkinManager::getOrCreate)
                .orElse(entity.getDefaultSkinTexture());
    }

    @Override
    protected void scale(AbstractHumanCompanionEntity entity, PoseStack poseStack, float partialTickTime) {
        poseStack.scale(0.9375F, 0.9375F, 0.9375F);
    }
}
