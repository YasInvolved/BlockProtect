package pl.yasinvolved.blockprotect.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import pl.yasinvolved.blockprotect.Blockprotect;

@EventBusSubscriber(modid = Blockprotect.MODID, value = Dist.CLIENT)
public class ClaimOverlayRenderer {
    @SubscribeEvent
    public static void onRenderLevelStage(RenderLevelStageEvent event) {
        if (!ClientInspectState.isInspecting()) return;
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_TRANSLUCENT_BLOCKS) {
            return;
        }

        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null || mc.player == null) {
            System.out.println("player or level is null");
        }

        Frustum frustum = event.getFrustum();
        Vec3 cameraPos = event.getCamera().getPosition();
        PoseStack poseStack = event.getPoseStack();

        poseStack.pushPose();
        poseStack.translate(-cameraPos.x, -cameraPos.y, -cameraPos.z);

        VertexConsumer buffer = mc.renderBuffers().bufferSource().getBuffer(RenderType.lines());

        for (ClientClaimData claim : ClientClaimCache.getActiveClaims()) {
            double minX = claim.min().getX();
            double minY = claim.min().getY();
            double minZ = claim.min().getZ();
            double maxX = claim.max().getX();
            double maxY = claim.max().getY();
            double maxZ = claim.max().getZ();

            AABB claimAABB = new AABB(minX, minY, minZ, maxX, maxY, maxZ);
            if (!frustum.isVisible(claimAABB)) {
                System.out.println("Frustum culled");
                continue;
            }

            System.out.println("Rendering box");
            LevelRenderer.renderLineBox(
                    poseStack,
                    buffer,
                    minX, minY, minZ,
                    maxX, maxY, maxZ,
                    0.0f, 1.0f, 0.0f, 1.0f
            );
        }

        poseStack.popPose();
    }
}
