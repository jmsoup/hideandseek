package kr.jmsoup.client.util;

import java.nio.ByteBuffer;

public class ColorUtils {
    public static int blendColor(int abgr, int oldColor) {
        int srcA = (abgr >> 24) & 0xFF;

        if (srcA == 255) return abgr;

        int dstA = (oldColor >> 24) & 0xFF;
        int dstR = (oldColor >> 16) & 0xFF;
        int dstG = (oldColor >> 8) & 0xFF;
        int dstB = oldColor & 0xFF;

        if (srcA == 0) {
            return (dstA << 24) | (dstB << 16) | (dstG << 8) | dstR;
        }

        int srcB = (abgr >> 16) & 0xFF;
        int srcG = (abgr >> 8) & 0xFF;
        int srcR = abgr & 0xFF;

        float alphaSrc = srcA / 255.0f;
        float alphaDst = dstA / 255.0f;
        float outA = alphaSrc + alphaDst * (1.0f - alphaSrc);

        if (outA == 0.0f) return 0;

        int rOut = (int) (((srcR * alphaSrc) + (dstR * alphaDst * (1.0f - alphaSrc))) / outA);
        int gOut = (int) (((srcG * alphaSrc) + (dstG * alphaDst * (1.0f - alphaSrc))) / outA);
        int bOut = (int) (((srcB * alphaSrc) + (dstB * alphaDst * (1.0f - alphaSrc))) / outA);
        int aOutInt = (int) (outA * 255.0f);

        return (aOutInt << 24) | (bOut << 16) | (gOut << 8) | rOut;
    }

    // ABGR -> RGBA
    public static float[] getRGBA(int abgr) {
        return new float[] {
                ((abgr >> 16) & 0xFF) / 255.0f, // R
                ((abgr >> 8) & 0xFF) / 255.0f,  // G
                (abgr & 0xFF) / 255.0f,         // B
                ((abgr >> 24) & 0xFF) / 255.0f  // A
        };
    }

    // BGRA -> RGBA
    public static float[] getRGBA(ByteBuffer buffer) {
        return new float[] {
                (buffer.get(0) & 0xFF) / 255.0f, // R
                (buffer.get(1) & 0xFF) / 255.0f, // G
                (buffer.get(2) & 0xFF) / 255.0f, // B
                (buffer.get(3) & 0xFF) / 255.0f  // A
        };
    }

    // RGBA -> ABGR
    public static int getABGR(float r, float g, float b, float a) {
        return ((int)(a * 255) << 24) | ((int)(b * 255) << 16) | ((int)(g * 255) << 8) | ((int)(r * 255));
    }

    // RGBA -> ARGB
    public static int getARGB(float r, float g, float b, float a) {
        return ((int)(a * 255) << 24) | ((int)(r * 255) << 16) | ((int)(g * 255) << 8) | ((int)(b * 255));
    }
}