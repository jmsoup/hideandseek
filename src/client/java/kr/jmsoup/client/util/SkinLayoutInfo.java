package kr.jmsoup.client.util;

import kr.jmsoup.client.core.PaintCanvas;

public class SkinLayoutInfo {
    public static final float[] OFFSET_HEAD  = { 0.0f, 1.6f, 0.0f };
    public static final float[] OFFSET_BODY  = { 0.0f, 1.1f, 0.0f };
    public static final float[] OFFSET_R_ARM = { 0.0f, 0.75f, 0.0f };
    public static final float[] OFFSET_L_ARM = { 0.0f, 0.75f, 0.0f };
    public static final float[] OFFSET_R_LEG = { 0.0f, 0.27f, 0.0f };
    public static final float[] OFFSET_L_LEG = { 0.0f, 0.27f, 0.0f };

    public static boolean isOuterLayer(int u, int v) {
        int s = PaintCanvas.CANVAS_SIZE / 64;
        if (u >= 32 * s && u < 64 * s && v >= 0 && v < 16 * s) return true; // Hat
        if (u >= 16 * s && u < 40 * s && v >= 32 * s && v < 48 * s) return true; // Jacket
        if (u >= 40 * s && u < 56 * s && v >= 32 * s && v < 48 * s) return true; // Right Sleeve
        if (u >= 48 * s && u < 64 * s && v >= 48 * s && v < 64 * s) return true; // Left Sleeve
        if (u >= 0 && u < 16 * s && v >= 32 * s && v < 48 * s) return true; // Right Pants
        if (u >= 0 && u < 16 * s && v >= 48 * s && v < 64 * s) return true; // Left Pants
        return false;
    }
}