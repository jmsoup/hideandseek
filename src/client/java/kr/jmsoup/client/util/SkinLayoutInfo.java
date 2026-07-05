package kr.jmsoup.client.util;

import kr.jmsoup.client.core.PaintCanvas;

public class SkinLayoutInfo {
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