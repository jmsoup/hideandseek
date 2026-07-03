package kr.jmsoup.client.util;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.player.PlayerModel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.entity.player.AvatarRenderer;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;
import org.joml.Vector3f;

public class SkinRaycaster {
    public static class HitResult {
        public float distance;
        public float u, v;
    }

    public static HitResult cast(AbstractClientPlayer player, double rawMouseX, double rawMouseY, float partialTick) {
        Minecraft client = Minecraft.getInstance();
        Camera camera = client.gameRenderer.getMainCamera();

        var rawRenderer = client.getEntityRenderDispatcher().getRenderer(player);
        if (!(rawRenderer instanceof AvatarRenderer renderer)) return null;
        AvatarRenderState state = (AvatarRenderState) renderer.createRenderState(player, partialTick);
        PlayerModel model = (PlayerModel) renderer.getModel();
        model.setupAnim(state);

        double guiScale = client.getWindow().getGuiScale();
        double physicalX = rawMouseX * guiScale;
        double physicalY = rawMouseY * guiScale;

        float nx = (float)(physicalX / (double)client.getWindow().getWidth()) * 2.0f - 1.0f;
        float ny = 1.0f - (float)(physicalY / (double)client.getWindow().getHeight()) * 2.0f;

        Camera.NearPlane nearPlane = camera.getNearPlane(camera.getFov());
        Vec3 planePoint = nearPlane.getPointOnPlane(nx, ny);

        Vector3f rayDir = new Vector3f((float)planePoint.x, (float)planePoint.y, (float)planePoint.z).normalize();
        Vector3f camOrigin = camera.position().toVector3f();

        double lerpX = player.xo + (player.getX() - player.xo) * partialTick;
        double lerpY = player.yo + (player.getY() - player.yo) * partialTick;
        double lerpZ = player.zo + (player.getZ() - player.zo) * partialTick;
        float lerpBodyYaw = lerpAngle(partialTick, player.yBodyRotO, player.yBodyRot);

        float scale = player.getScale() * 0.9375f;

        ModelPart root = model.root();
        ModelPart[] parts = { model.head, model.body, model.rightArm, model.leftArm, model.rightLeg, model.leftLeg };

        float[][] boxes = {
                {-4, -8, -4, 8, 8, 8, 0, 0},
                {-4, 0, -2, 8, 12, 4, 16, 16},
                {-3, -2, -2, 4, 12, 4, 40, 16},
                {-1, -2, -2, 4, 12, 4, 32, 48},
                {-2, 0, -2, 4, 12, 4, 0, 16},
                {-2, 0, -2, 4, 12, 4, 16, 48}
        };

        HitResult bestHit = null;

        for (int i = 0; i < parts.length; i++) {
            ModelPart part = parts[i];
            float[] b = boxes[i];

            PoseStack stack = new PoseStack();
            stack.translate(lerpX, lerpY, lerpZ);
            stack.mulPose(Axis.YP.rotationDegrees(180.0f - lerpBodyYaw));

            stack.scale(-scale, -scale, scale);
            stack.translate(0f, -1.501f, 0f);

            root.translateAndRotate(stack);
            part.translateAndRotate(stack);

            Matrix4f worldMatrix = new Matrix4f(stack.last().pose());
            Matrix4f invPart = worldMatrix.invert();

            Vector3f oLocal = new Vector3f(camOrigin);
            invPart.transformPosition(oLocal);

            Vector3f dLocal = new Vector3f(rayDir);
            invPart.transformDirection(dLocal).normalize();

            oLocal.mul(16.0f);

            HitResult hit = intersectLocalBox(oLocal, dLocal, b[0], b[1], b[2], b[3], b[4], b[5], (int)b[6], (int)b[7]);
            if (hit != null) {
                if (bestHit == null || hit.distance < bestHit.distance) {
                    bestHit = hit;
                }
            }
        }
        return bestHit;
    }

    private static float lerpAngle(float partial, float start, float end) {
        float delta = ((end - start) % 360.0f + 540.0f) % 360.0f - 180.0f;
        return start + delta * partial;
    }

    private static HitResult intersectLocalBox(Vector3f oLocal, Vector3f dLocal,
                                               float minX, float minY, float minZ,
                                               float w, float h, float dp,
                                               int texU, int texV) {
        double[] min = {minX, minY, minZ};
        double[] max = {minX + w, minY + h, minZ + dp};
        double[] origin = {oLocal.x, oLocal.y, oLocal.z};
        double[] dir = {dLocal.x, dLocal.y, dLocal.z};

        double tmin = -Double.MAX_VALUE;
        double tmax = Double.MAX_VALUE;
        int normalIn = -1;

        for (int i = 0; i < 3; i++) {
            if (Math.abs(dir[i]) < 1e-6) {
                if (origin[i] < min[i] || origin[i] > max[i]) return null;
            } else {
                double t1 = (min[i] - origin[i]) / dir[i];
                double t2 = (max[i] - origin[i]) / dir[i];
                int n1 = i * 2, n2 = i * 2 + 1;

                if (t1 > t2) {
                    double temp = t1; t1 = t2; t2 = temp;
                    int tempN = n1; n1 = n2; n2 = tempN;
                }
                if (t1 > tmin) { tmin = t1; normalIn = n1; }
                if (t2 < tmax) { tmax = t2; }
                if (tmin > tmax || tmax < 0) return null;
            }
        }
        if (tmin < 0) return null;

        double hx = oLocal.x + dLocal.x * tmin;
        double hy = oLocal.y + dLocal.y * tmin;
        double hz = oLocal.z + dLocal.z * tmin;

        double faceX = 0, faceY = 0;
        int uOff = 0, vOff = 0;

        int iW = (int) w, iH = (int) h, iDp = (int) dp;

        switch (normalIn) {
            case 0: faceX = max[2] - hz; faceY = hy - min[1]; uOff = 0; vOff = iDp; break;
            case 1: faceX = hz - min[2]; faceY = hy - min[1]; uOff = iDp + iW; vOff = iDp; break;
            case 2: faceX = hx - min[0]; faceY = max[2] - hz; uOff = iDp; vOff = 0; break;
            case 3: faceX = hx - min[0]; faceY = max[2] - hz; uOff = iDp + iW; vOff = 0; break;
            case 4: faceX = hx - min[0]; faceY = hy - min[1]; uOff = iDp; vOff = iDp; break;
            case 5: faceX = max[0] - hx; faceY = hy - min[1]; uOff = iDp + iW + iDp; vOff = iDp; break;
        }

        HitResult res = new HitResult();
        res.distance = (float) tmin;
        res.u = (float) (texU + uOff + faceX);
        res.v = (float) (texV + vOff + faceY);
        return res;
    }
}