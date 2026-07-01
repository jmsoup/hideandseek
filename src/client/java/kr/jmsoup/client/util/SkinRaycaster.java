package kr.jmsoup.client.util;

import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.AbstractClientPlayer;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector4f;

public class SkinRaycaster {
    public static class HitResult {
        public float distance;
        public float u, v;
    }

    public static HitResult cast(AbstractClientPlayer player, double rawMouseX, double rawMouseY, float partialTick) {
        Minecraft client = Minecraft.getInstance();
        Camera camera = client.gameRenderer.getMainCamera();

        double guiScale = client.getWindow().getGuiScale();
        float nx = (float)((rawMouseX * guiScale) / client.getWindow().getWidth()) * 2.0f - 1.0f;
        float ny = 1.0f - (float)((rawMouseY * guiScale) / client.getWindow().getHeight()) * 2.0f;

        Matrix4f viewProj = new Matrix4f();
        camera.getViewRotationProjectionMatrix(viewProj);
        Matrix4f invViewProj = viewProj.invert();

        Vector4f near = new Vector4f(nx, ny, 0.01f, 1.0f);
        Vector4f far = new Vector4f(nx, ny, 0.99f, 1.0f);

        invViewProj.transform(near);
        invViewProj.transform(far);

        if (near.w != 0.0f) near.div(near.w);
        if (far.w != 0.0f) far.div(far.w);

        Vector3f rayDir = new Vector3f(far.x - near.x, far.y - near.y, far.z - near.z).normalize();

        double lerpX = player.xo + (player.getX() - player.xo) * partialTick;
        double lerpY = player.yo + (player.getY() - player.yo) * partialTick;
        double lerpZ = player.zo + (player.getZ() - player.zo) * partialTick;

        float lerpBodyYaw = lerpAngle(partialTick, player.yBodyRotO, player.yBodyRot);
        float lerpHeadYaw = lerpAngle(partialTick, player.yHeadRotO, player.yHeadRot);
        float lerpPitch = lerpAngle(partialTick, player.xRotO, player.getXRot());

        float scale = player.getScale();

        Matrix4f modelMatrix = new Matrix4f();
        modelMatrix.translate((float)lerpX, (float)lerpY, (float)lerpZ);
        modelMatrix.rotateY((float)Math.toRadians(180.0f - lerpBodyYaw));

        modelMatrix.scale(-scale, -scale, scale);
        modelMatrix.translate(0f, -1.501f, 0f);

        Matrix4f invModel = modelMatrix.invert();

        Vector3f camOrigin = new Vector3f((float)camera.position().x, (float)camera.position().y, (float)camera.position().z);
        Vector3f oRoot = new Vector3f(camOrigin);
        invModel.transformPosition(oRoot);
        oRoot.mul(16.0f);

        Vector3f dRoot = new Vector3f(rayDir);
        invModel.transformDirection(dRoot).normalize();

        float[][] parts = {
                { 0 + SkinLayoutInfo.OFFSET_HEAD[0], 0 + SkinLayoutInfo.OFFSET_HEAD[1], 0 + SkinLayoutInfo.OFFSET_HEAD[2],
                        -4, -8, -4,  8, 8, 8,  0, 0,  lerpPitch, (lerpHeadYaw - lerpBodyYaw), 0 },
                { 0 + SkinLayoutInfo.OFFSET_BODY[0], 0 + SkinLayoutInfo.OFFSET_BODY[1], 0 + SkinLayoutInfo.OFFSET_BODY[2],
                        -4,  0, -2,  8, 12, 4,  16, 16,  0, 0, 0 },
                { -5 + SkinLayoutInfo.OFFSET_R_ARM[0], 2f + SkinLayoutInfo.OFFSET_R_ARM[1], 0 + SkinLayoutInfo.OFFSET_R_ARM[2],
                        -3, -2, -2,  4, 12, 4,  40, 16,  0, 0, 0 },
                { 5 + SkinLayoutInfo.OFFSET_L_ARM[0], 2f + SkinLayoutInfo.OFFSET_L_ARM[1], 0 + SkinLayoutInfo.OFFSET_L_ARM[2],
                        -1, -2, -2,  4, 12, 4,  32, 48,  0, 0, 0 },
                { -1.9f + SkinLayoutInfo.OFFSET_R_LEG[0], 12 + SkinLayoutInfo.OFFSET_R_LEG[1], 0 + SkinLayoutInfo.OFFSET_R_LEG[2],
                        -2, 0, -2,  4, 12, 4,  0, 16,  0, 0, 0 },
                { 1.9f + SkinLayoutInfo.OFFSET_L_LEG[0], 12 + SkinLayoutInfo.OFFSET_L_LEG[1], 0 + SkinLayoutInfo.OFFSET_L_LEG[2],
                        -2, 0, -2,  4, 12, 4,  16, 48,  0, 0, 0 }
        };

        HitResult bestHit = null;

        for (float[] p : parts) {
            Matrix4f partTransform = new Matrix4f();
            partTransform.translate(p[0], p[1], p[2]);

            if (p[13] != 0) partTransform.rotateZ((float)Math.toRadians(p[13]));
            if (p[12] != 0) partTransform.rotateY((float)Math.toRadians(p[12]));
            if (p[11] != 0) partTransform.rotateX((float)Math.toRadians(p[11]));

            Matrix4f invPart = partTransform.invert();

            Vector3f oLocal = new Vector3f(oRoot);
            invPart.transformPosition(oLocal);
            Vector3f dLocal = new Vector3f(dRoot);
            invPart.transformDirection(dLocal).normalize();

            HitResult hit = intersectLocalBox(oLocal, dLocal, p);
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

    private static HitResult intersectLocalBox(Vector3f oLocal, Vector3f dLocal, float[] p) {
        double[] min = {p[3], p[4], p[5]};
        double[] max = {p[3] + p[6], p[4] + p[7], p[5] + p[8]};
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

        int texU = (int) p[9], texV = (int) p[10];
        int w = (int) p[6], h = (int) p[7], dp = (int) p[8];

        double faceX = 0, faceY = 0;
        int uOff = 0, vOff = 0;

        switch (normalIn) {
            case 0: faceX = max[2] - hz; faceY = hy - min[1]; uOff = 0; vOff = dp; break;
            case 1: faceX = hz - min[2]; faceY = hy - min[1]; uOff = dp + w; vOff = dp; break;
            case 2: faceX = hx - min[0]; faceY = max[2] - hz; uOff = dp; vOff = 0; break;
            case 3: faceX = hx - min[0]; faceY = max[2] - hz; uOff = dp + w; vOff = 0; break;
            case 4: faceX = hx - min[0]; faceY = hy - min[1]; uOff = dp; vOff = dp; break;
            case 5: faceX = max[0] - hx; faceY = hy - min[1]; uOff = dp + w + dp; vOff = dp; break;
        }

        HitResult res = new HitResult();
        res.distance = (float) tmin;
        res.u = (float) (texU + uOff + faceX);
        res.v = (float) (texV + vOff + faceY);
        return res;
    }
}